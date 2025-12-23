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

import nz.net.ultraq.redhorizon.engine.Entity
import nz.net.ultraq.redhorizon.engine.graphics.CameraEntity
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsComponent
import nz.net.ultraq.redhorizon.engine.graphics.GridLinesEntity
import nz.net.ultraq.redhorizon.engine.graphics.SpriteComponent
import nz.net.ultraq.redhorizon.engine.scripts.GameLogicComponent
import nz.net.ultraq.redhorizon.engine.scripts.ScriptComponent
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.opengl.BasicShader
import nz.net.ultraq.redhorizon.scenegraph.Scene
import static nz.net.ultraq.asteroids.ScopedValues.*

import org.joml.primitives.Rectanglef

/**
 * Scene setup for the Asteroids game.
 *
 * @author Emanuel Rabina
 */
class AsteroidsScene extends Scene implements AutoCloseable {

	static final int WIDTH = 1920
	static final int HEIGHT = 1080

	final CameraEntity camera
	private final BasicShader shader

	/**
	 * Constructor, create a new scene to the given dimensions.
	 */
	AsteroidsScene() {

		var window = WINDOW.get()
		var resourceManager = RESOURCE_MANAGER.get()

		camera = new CameraEntity(WIDTH, HEIGHT, window)
		addChild(camera)

		shader = new BasicShader()

		addChild(new GridLinesEntity(new Rectanglef(0f, 0f, WIDTH, HEIGHT).center(), 64f, Colour.RED, Colour.GREY))

		var playerImage = resourceManager.loadImage('Player.png')
		addChild(new Entity()
			.addComponent(new SpriteComponent(playerImage, BasicShader)
				.translate(-playerImage.width / 2 as float, -playerImage.height / 2 as float, 0f))
			.addComponent(new ScriptComponent(SCRIPT_ENGINE.get(), 'PlayerScript.groovy'))
			.withName('Player'))
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
	 * Draw out all the graphical components of the scene.
	 */
	void render() {

		var graphicsComponents = new ArrayList<GraphicsComponent>()
		traverse { node ->
			if (node instanceof Entity) {
				// TODO: Create an allocation-free method of finding components
				graphicsComponents.addAll(node.findComponents { it instanceof GraphicsComponent })
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

		// TODO: Similar to above, these look like they should be the "S" part of ECS
		var gameLogicComponents = new ArrayList<GameLogicComponent>()
		traverse { node ->
			if (node instanceof Entity) {
				gameLogicComponents.addAll(node.findComponents { it instanceof GameLogicComponent })
			}
		}
		gameLogicComponents.each { component ->
			component.update(delta)
		}
	}
}
