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

import nz.net.ultraq.asteroids.debug.DebugCollisionOutlineSystem
import nz.net.ultraq.asteroids.debug.DebugEverythingBinding
import nz.net.ultraq.asteroids.debug.DebugLinesBinding
import nz.net.ultraq.redhorizon.engine.Engine
import nz.net.ultraq.redhorizon.engine.Entity
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsSystem
import nz.net.ultraq.redhorizon.engine.graphics.imgui.ImGuiDebugComponent
import nz.net.ultraq.redhorizon.engine.graphics.imgui.LogPanel
import nz.net.ultraq.redhorizon.engine.graphics.imgui.NodeList
import nz.net.ultraq.redhorizon.engine.input.InputSystem
import nz.net.ultraq.redhorizon.engine.physics.CollisionSystem
import nz.net.ultraq.redhorizon.engine.scene.SceneChangesSystem
import nz.net.ultraq.redhorizon.engine.scripts.ScriptEngine
import nz.net.ultraq.redhorizon.engine.scripts.ScriptSystem
import nz.net.ultraq.redhorizon.engine.utilities.DeltaTimer
import nz.net.ultraq.redhorizon.engine.utilities.ResourceManager
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.Framebuffer
import nz.net.ultraq.redhorizon.graphics.Window
import nz.net.ultraq.redhorizon.graphics.imgui.DebugOverlay
import nz.net.ultraq.redhorizon.graphics.opengl.BasicShader
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLFramebuffer
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
	private Framebuffer framebuffer
	private BasicShader shader
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
			framebuffer = new OpenGLFramebuffer(AsteroidsScene.WIDTH, AsteroidsScene.HEIGHT)
			shader = new BasicShader()
			var inputEventHandler = new InputEventHandler()
				.addInputSource(window)
				.addEscapeToCloseBinding(window)
				.addVSyncBinding(window)
			resourceManager = new ResourceManager('nz/net/ultraq/asteroids/assets/')

			ScopedValue
				.where(WINDOW, window)
				.where(RESOURCE_MANAGER, resourceManager)
				.run(() -> {

					// Init scene and systems
					scene = new AsteroidsScene().tap {
						var debugOverlayComponent = new ImGuiDebugComponent(new DebugOverlay()
							.withCursorTracking(camera.camera, camera.transform, this.window)).disable()
						var nodeListComponent = new ImGuiDebugComponent(new NodeList(it)).disable()
						var logPanelComponent = new ImGuiDebugComponent(new LogPanel()).disable()
						addChild(new Entity()
							.addComponent(debugOverlayComponent)
							.addComponent(nodeListComponent)
							.addComponent(logPanelComponent)
							.withName('Debug UI'))

						var debugLinesBinding = new DebugLinesBinding(it)
						var debugEverythingBinding = new DebugEverythingBinding(
							[debugOverlayComponent, nodeListComponent, logPanelComponent], debugLinesBinding)
						inputEventHandler
							.addImGuiDebugBindings([debugOverlayComponent], [nodeListComponent, logPanelComponent])
							.addInputBinding(debugLinesBinding)
							.addInputBinding(debugEverythingBinding)
					}
					var engine = new Engine()
						.addSystem(new InputSystem(inputEventHandler))
						.addSystem(new DebugCollisionOutlineSystem())
						.addSystem(new ScriptSystem(new ScriptEngine('.'), inputEventHandler))
						.addSystem(new CollisionSystem())
						.addSystem(new GraphicsSystem(window, framebuffer, shader))
						.addSystem(new SceneChangesSystem())
						.withScene(scene)

					// Game loop
					window.show()
					var deltaTimer = new DeltaTimer()
					while (!window.shouldClose()) {
						engine.update(deltaTimer.deltaTime())
						Thread.yield()
					}
				})
		}
		finally {
			scene?.close()
			resourceManager?.close()
			shader?.close()
			framebuffer?.close()
			window?.close()
		}
	}
}
