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

import nz.net.ultraq.redhorizon.engine.scripts.ScriptEngine
import nz.net.ultraq.redhorizon.engine.utilities.DeltaTimer
import nz.net.ultraq.redhorizon.engine.utilities.ResourceManager
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.Window
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLWindow
import nz.net.ultraq.redhorizon.input.InputEventHandler
import static nz.net.ultraq.asteroids.ScopedValues.*

import org.lwjgl.system.Configuration
import picocli.CommandLine
import picocli.CommandLine.Command

/**
 * Entry point for the Asteroids game.
 *
 * @author Emanuel Rabina
 */
@Command(name = 'assets')
class Asteroids implements Runnable {

	static {
		Configuration.STACK_SIZE.set(10240)
	}

	static void main(String[] args) {
		System.exit(new CommandLine(new Asteroids()).execute(args))
	}

	private Window window
	private ResourceManager resourceManager
	private AsteroidsScene scene

	@Override
	void run() {

		try {
			// Init devices
			window = new OpenGLWindow(AsteroidsScene.WIDTH / 2 as int, AsteroidsScene.HEIGHT / 2 as int, 'Asteroids')
				.centerToScreen()
				.scaleToFit()
				.withBackgroundColour(Colour.BLACK)
				.withVSync(true)
			var input = new InputEventHandler()
				.addInputSource(window)
				.addEscapeToCloseBinding(window)
				.addVSyncBinding(window)
			resourceManager = new ResourceManager('nz/net/ultraq/asteroids/assets/')
			var scriptEngine = new ScriptEngine('.')

			ScopedValue
				.where(WINDOW, window)
				.where(INPUT_EVENT_HANDLER, input)
				.where(RESOURCE_MANAGER, resourceManager)
				.where(SCRIPT_ENGINE, scriptEngine)
				.run(() -> {

					// Init scene
					scene = new AsteroidsScene()
					window.show()

					// Game loop
					var deltaTimer = new DeltaTimer()
					while (!window.shouldClose()) {
						var delta = deltaTimer.deltaTime()

						// TODO: Each of these parts look like the "S" part of an ECS, so
						//       should be split as such.
						input.processInputs()
						scene.update(delta)
						scene.checkCollisions()
						scene.render()
						scene.processQueuedChanges()

						Thread.yield()
					}
				})
		}
		finally {
			scene?.close()
			resourceManager?.close()
			window?.close()
		}
	}
}
