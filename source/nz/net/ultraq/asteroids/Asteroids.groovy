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

import nz.net.ultraq.redhorizon.engine.graphics.imgui.NodeList
import nz.net.ultraq.redhorizon.engine.scripts.ScriptEngine
import nz.net.ultraq.redhorizon.engine.utilities.DeltaTimer
import nz.net.ultraq.redhorizon.engine.utilities.ResourceManager
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.Window
import nz.net.ultraq.redhorizon.graphics.imgui.DebugOverlay
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLWindow
import nz.net.ultraq.redhorizon.input.InputEventHandler
import static nz.net.ultraq.asteroids.ScopedValues.*

import picocli.CommandLine
import picocli.CommandLine.Command
import static org.lwjgl.glfw.GLFW.*

/**
 * Entry point for the Asteroids game.
 *
 * @author Emanuel Rabina
 */
@Command(name = 'assets')
class Asteroids implements Runnable {

	static {
		System.setProperty('org.lwjgl.system.stackSize', '10240')
	}

	static void main(String[] args) {
		System.exit(new CommandLine(new Asteroids()).execute(args))
	}

	private Window window
	private InputEventHandler inputEventHandler
	private ResourceManager resourceManager
	private ScriptEngine scriptEngine
	private AsteroidsScene scene

	@Override
	void run() {

		try {
			// Init devices
			window = new OpenGLWindow(960, 540, 'Asteroids')
				.centerToScreen()
				.scaleToFit()
				.withBackgroundColour(Colour.BLACK)
				.withVSync(true)
			inputEventHandler = new InputEventHandler()
				.addInputSource(window)
			resourceManager = new ResourceManager('nz/net/ultraq/asteroids/assets/')
			scriptEngine = new ScriptEngine('.')

			ScopedValue
				.where(WINDOW, window)
				.where(INPUT_EVENT_HANDLER, inputEventHandler)
				.where(RESOURCE_MANAGER, resourceManager)
				.where(SCRIPT_ENGINE, scriptEngine)
				.run(() -> {

					// Init scene
					scene = new AsteroidsScene()
					window
						.addImGuiComponent(new DebugOverlay()
							.withCursorTracking(scene.camera.camera, scene.camera.transform))
						.addImGuiComponent(new NodeList(scene))
						.show()

					// Game loop
					var deltaTimer = new DeltaTimer()
					while (!window.shouldClose()) {
						var delta = deltaTimer.deltaTime()

						// Logic/update
						if (inputEventHandler.keyPressed(GLFW_KEY_ESCAPE, true)) {
							window.shouldClose(true)
						}
						if (inputEventHandler.keyPressed(GLFW_KEY_I, true)) {
							window.toggleImGuiWindows()
						}
						if (inputEventHandler.keyPressed(GLFW_KEY_V, true)) {
							window.toggleVSync()
						}
						scene.update(delta)

						// Render
						window.useWindow { ->
							scene.render()
						}

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
