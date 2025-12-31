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

package nz.net.ultraq.asteroids.objects

import nz.net.ultraq.asteroids.AsteroidsScene
import nz.net.ultraq.redhorizon.engine.Entity
import nz.net.ultraq.redhorizon.engine.graphics.imgui.ImGuiComponent
import nz.net.ultraq.redhorizon.engine.scripts.EntityScript
import nz.net.ultraq.redhorizon.engine.scripts.ScriptComponent
import nz.net.ultraq.redhorizon.graphics.Image
import nz.net.ultraq.redhorizon.graphics.Window
import nz.net.ultraq.redhorizon.graphics.imgui.ImGuiContext
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLTexture
import static nz.net.ultraq.asteroids.ScopedValues.*

import imgui.ImFont
import imgui.ImGui
import imgui.type.ImBoolean
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static imgui.flag.ImGuiStyleVar.*
import static imgui.flag.ImGuiWindowFlags.*

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * Lives counter.
 *
 * @author Emanuel Rabina
 */
class Lives extends Entity<Lives> {

	private static final Logger logger = LoggerFactory.getLogger(Lives)

	private int lives = 3

	/**
	 * Constructor, tie the lives to the player.
	 */
	Lives(ImFont squareFont) {

		addComponent(new ScriptComponent(SCRIPT_ENGINE.get(), LivesScript))
		addComponent(new LivesUiComponent(squareFont))
	}

	/**
	 * Return the current number of lives.
	 */
	int getLives() {

		return lives
	}

	/**
	 * Script for tracking player lives.
	 */
	static class LivesScript extends EntityScript<Lives> implements AutoCloseable {

		private ScheduledExecutorService executor

		@Override
		void close() {

			executor.shutdown()
		}

		@Override
		void init() {

			executor = Executors.newSingleThreadScheduledExecutor()

			var window = WINDOW.get()
			var scene = entity.scene as AsteroidsScene

			scene.player.on(PlayerDestroyedEvent) { event ->
				entity.lives--
				logger.debug('Lives: {}', entity.lives)

				if (!entity.lives) {
					logger.debug('Game over!')
					scene.queueChange { ->
						scene.clear()
					}

					executor.schedule({ ->
						window.shouldClose(true)
					}, 1, TimeUnit.SECONDS)
				}
			}
		}
	}

	/**
	 * Component for rendering the number of lives remaining to the UI.
	 */
	static class LivesUiComponent extends ImGuiComponent<LivesUiComponent> {

		private final ImFont squareFont
		private final Image livesImage
		private final Window window

		LivesUiComponent(ImFont squareFont) {

			this.squareFont = squareFont
			this.window = WINDOW.get()
			var resourceManager = RESOURCE_MANAGER.get()
			livesImage = resourceManager.loadImage('Lives.png')
		}

		@Override
		void render(ImGuiContext context) {

			var viewport = window.viewport
			ImGui.setNextWindowBgAlpha(0.4f)
			ImGui.setNextWindowPos(viewport.minX, viewport.minY)
			ImGui.pushFont(squareFont)
			ImGui.pushStyleVar(WindowBorderSize, 0f)
			ImGui.pushStyleVar(WindowPadding, 8 * context.uiScale as float, 4 * context.uiScale as float)

			ImGui.begin('Lives', new ImBoolean(true), NoNav | NoDecoration | NoSavedSettings | NoFocusOnAppearing | NoDocking | AlwaysAutoResize)
			ImGui.image(((OpenGLTexture)livesImage.texture).textureId,
				livesImage.width / (4 / context.uiScale) as float,
				livesImage.height / (4 / context.uiScale) as float,
				0f, 1f, 1f, 0f)
			ImGui.sameLine()
			ImGui.text("x ${((Lives)entity).lives}")

			ImGui.popStyleVar(2)
			ImGui.popFont()
			ImGui.end()
		}
	}
}
