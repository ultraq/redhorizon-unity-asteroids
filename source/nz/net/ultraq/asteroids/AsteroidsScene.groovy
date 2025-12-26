/*
 * Copyright 2025, Emanuel Rabina (http://www.ultraq.net.nz/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nz.net.ultraq.asteroids

import nz.net.ultraq.asteroids.engine.BoxCollisionComponent
import nz.net.ultraq.asteroids.objects.AsteroidSpawner
import nz.net.ultraq.asteroids.objects.Player
import nz.net.ultraq.redhorizon.engine.Entity
import nz.net.ultraq.redhorizon.engine.graphics.CameraEntity
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsComponent
import nz.net.ultraq.redhorizon.engine.graphics.GridLinesEntity
import nz.net.ultraq.redhorizon.engine.graphics.MeshComponent
import nz.net.ultraq.redhorizon.engine.scripts.GameLogicComponent
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.Mesh.Type
import nz.net.ultraq.redhorizon.graphics.Vertex
import nz.net.ultraq.redhorizon.graphics.opengl.BasicShader
import nz.net.ultraq.redhorizon.input.InputEventHandler
import nz.net.ultraq.redhorizon.scenegraph.Scene
import static nz.net.ultraq.asteroids.ScopedValues.*

import org.joml.Vector3f
import org.joml.primitives.Rectanglef
import static org.lwjgl.glfw.GLFW.GLFW_KEY_C

/**
 * Scene setup for the Asteroids game.
 *
 * @author Emanuel Rabina
 */
class AsteroidsScene extends Scene implements AutoCloseable {

	static final int WIDTH = 1920
	static final int HEIGHT = 1440
	private static final String COLLISION_OUTLINE_NAME = 'Collision outline'

	final CameraEntity camera
	private final BasicShader shader
	private final List<GameLogicComponent> gameLogicComponents = new ArrayList<>()
	private final List<GraphicsComponent> graphicsComponents = new ArrayList<>()
	private final Queue<Closure> changeQueue = new ArrayDeque<>()
	private final InputEventHandler input
	private boolean showCollisionOutlines = false

	/**
	 * Constructor, create a new scene to the given dimensions.
	 */
	AsteroidsScene() {

		var window = WINDOW.get()
		input = INPUT_EVENT_HANDLER.get()

		camera = new CameraEntity(WIDTH, HEIGHT, window)
		shader = new BasicShader()

		addChild(camera)
		addChild(new GridLinesEntity(new Rectanglef(0f, 0f, WIDTH, HEIGHT).center(), 64f, Colour.RED, Colour.GREY)
			.withName('Grid lines'))
		addChild(new Player())
		addChild(new AsteroidSpawner())
	}

	@Override
	void close() {

		traverse { node ->
			if (node instanceof AutoCloseable) {
				node.close()
			}
		}
	}

	/**
	 * Queue some scene modification to be performed at the end of the current
	 * update cycle.
	 */
	void queueChange(Closure change) {

		changeQueue.add(change)
	}

	/**
	 * Draw out all the graphical components of the scene.
	 */
	void render() {

		graphicsComponents.clear()
		traverse { node ->
			if (node instanceof Entity) {
				node.findComponentsByType(GraphicsComponent, graphicsComponents)
			}
		}

		shader.useShader { shaderContext ->
			camera.render(shaderContext)
			graphicsComponents.each { component ->
				component.render(shaderContext)
			}
		}
	}

	/**
	 * Perform a scene update in the game loop.
	 */
	void update(float delta) {

		// Toggle the addition/removal of a mesh to draw the collision outlines on entities
		if (input.keyPressed(GLFW_KEY_C, true)) {
			showCollisionOutlines = !showCollisionOutlines
		}

		// TODO: Similar to the update method, these look like they should be the "S" part of ECS
		gameLogicComponents.clear()
		traverse { node ->
			if (node instanceof Entity) {
				if (showCollisionOutlines) {
					var collision = node.findComponentByType(BoxCollisionComponent) as BoxCollisionComponent
					var collisionOutline = node.findComponent { it.name == COLLISION_OUTLINE_NAME } as MeshComponent
					if (collision && !collisionOutline) {
						var width = collision.width
						var height = collision.height
						node.addComponent(
							new MeshComponent(Type.LINE_LOOP, new Vertex[]{
								new Vertex(new Vector3f(-width / 2 as float, -height / 2 as float, 0), Colour.YELLOW),
								new Vertex(new Vector3f(width / 2 as float, -height / 2 as float, 0), Colour.YELLOW),
								new Vertex(new Vector3f(width / 2 as float, height / 2 as float, 0), Colour.YELLOW),
								new Vertex(new Vector3f(-width / 2 as float, height / 2 as float, 0), Colour.YELLOW)
							})
								.withName(COLLISION_OUTLINE_NAME))
					}
				}
				else {
					var collisionOutline = node.findComponent { it.name == COLLISION_OUTLINE_NAME } as MeshComponent
					if (collisionOutline) {
						node.removeComponent(collisionOutline)
						collisionOutline.close()
					}
				}

				node.findComponentsByType(GameLogicComponent, gameLogicComponents)
			}
		}
		gameLogicComponents.each { component ->
			component.update(delta)
		}

		while (changeQueue) {
			changeQueue.poll().call()
		}
	}
}
