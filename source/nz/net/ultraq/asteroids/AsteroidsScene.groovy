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

import nz.net.ultraq.asteroids.engine.CollisionComponent
import nz.net.ultraq.asteroids.objects.AsteroidSpawner
import nz.net.ultraq.asteroids.objects.Lives
import nz.net.ultraq.asteroids.objects.Player
import nz.net.ultraq.asteroids.objects.Score
import nz.net.ultraq.redhorizon.engine.Entity
import nz.net.ultraq.redhorizon.engine.graphics.CameraEntity
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsComponent
import nz.net.ultraq.redhorizon.engine.scripts.GameLogicComponent
import nz.net.ultraq.redhorizon.graphics.Window
import nz.net.ultraq.redhorizon.graphics.opengl.BasicShader
import nz.net.ultraq.redhorizon.scenegraph.Scene
import static nz.net.ultraq.asteroids.ScopedValues.getWINDOW

/**
 * Scene setup for the Asteroids game.
 *
 * @author Emanuel Rabina
 */
class AsteroidsScene extends Scene implements AutoCloseable {

	static final int WIDTH = 1920
	static final int HEIGHT = 1440

	final CameraEntity camera
	final Player player
	private final Window window
	private final BasicShader shader
	private final List<CollisionComponent> collisionComponents = new ArrayList<>()
	private final List<GameLogicComponent> gameLogicComponents = new ArrayList<>()
	private final List<GraphicsComponent> graphicsComponents = new ArrayList<>()
	private final Queue<Closure> changeQueue = new ArrayDeque<>()

	/**
	 * Constructor, create a new scene to the given dimensions.
	 */
	AsteroidsScene() {

		window = WINDOW.get()

		camera = new CameraEntity(WIDTH, HEIGHT, window)
		shader = new BasicShader()
		player = new Player()

		addChild(camera)
		addChild(player)
		addChild(new AsteroidSpawner())
		addChild(new Score(this))
		addChild(new Lives(this, player))
	}

	/**
	 * Perform collision checks between all entities in the scene.
	 */
	void checkCollisions() {

		// TODO: Yet another ECS system part
		collisionComponents.clear()
		traverse(Entity) { Entity entity ->
			entity.findComponentsByType(CollisionComponent, collisionComponents)
		}
		for (var i = 0; i < collisionComponents.size(); i++) {
			var collision = collisionComponents.get(i)
			if (!collision.enabled) {
				continue
			}
			for (var j = i + 1; j < collisionComponents.size(); j++) {
				var otherCollision = collisionComponents.get(j)
				if (!otherCollision.enabled) {
					continue
				}
				collision.checkCollision(otherCollision)
			}
		}
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
	 * Apply modifications made by other steps in the game loop.
	 */
	void processQueuedChanges() {

		while (changeQueue) {
			changeQueue.poll().call()
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

		// TODO: Similar to the update method, these look like they should be the "S" part of ECS
		graphicsComponents.clear()
		traverse(Entity) { Entity entity ->
			entity.findComponentsByType(GraphicsComponent, graphicsComponents)
		}

		window.useWindow { ->
			shader.useShader { shaderContext ->
				camera.render(shaderContext)
				graphicsComponents.each { component ->
					if (component.enabled) {
						component.render(shaderContext)
					}
				}
			}
		}
	}

	/**
	 * Perform a scene update in the game loop.
	 */
	void update(float delta) {

		// TODO: Similar to the render method, these look like they should be the "S" part of ECS
		gameLogicComponents.clear()
		traverse(Entity) { Entity entity ->
			entity.findComponentsByType(GameLogicComponent, gameLogicComponents)
		}
		gameLogicComponents.each { component ->
			if (component.enabled) {
				component.update(delta)
			}
		}
	}
}
