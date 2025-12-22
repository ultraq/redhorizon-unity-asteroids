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

import nz.net.ultraq.asteroids.inject.CloseableInjector
import nz.net.ultraq.redhorizon.engine.graphics.imgui.NodeList
import nz.net.ultraq.redhorizon.engine.utilities.DeltaTimer
import nz.net.ultraq.redhorizon.graphics.Window
import nz.net.ultraq.redhorizon.graphics.imgui.DebugOverlay
import nz.net.ultraq.redhorizon.input.InputEventHandler

import com.google.inject.Guice
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

	@Override
	void run() {

		new CloseableInjector(Guice.createInjector(new AsteroidsModule())).withCloseable { injector ->

			// Init scene
			var scene = new AsteroidsScene(1920, 1080, injector)

			var window = injector.getInstance(Window)
			window
				.addImGuiComponent(new DebugOverlay()
					.withCursorTracking(scene.camera.camera, scene.camera.transform))
				.addImGuiComponent(new NodeList(scene))
				.show()
			var inputEventHandler = injector.getInstance(InputEventHandler)

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
		}
	}
}
