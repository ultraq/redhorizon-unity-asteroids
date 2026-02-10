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
import nz.net.ultraq.redhorizon.engine.scripts.Script
import nz.net.ultraq.redhorizon.engine.scripts.ScriptNode
import nz.net.ultraq.redhorizon.graphics.Image
import nz.net.ultraq.redhorizon.graphics.imgui.ImGuiContext
import nz.net.ultraq.redhorizon.graphics.imgui.ImGuiModule
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLTexture
import nz.net.ultraq.redhorizon.scenegraph.Node
import static nz.net.ultraq.asteroids.ScopedValues.RESOURCE_MANAGER

import imgui.ImFont
import imgui.ImGui
import imgui.type.ImBoolean
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static imgui.flag.ImGuiStyleVar.*
import static imgui.flag.ImGuiWindowFlags.*

/**
 * Lives counter.
 *
 * @author Emanuel Rabina
 */
class Lives extends Node<Lives> {

	private static final Logger logger = LoggerFactory.getLogger(Lives)

	private int lives = 3

	/**
	 * Constructor, tie the lives to the player.
	 */
	Lives(ImFont squareFont) {

		addChild(new ScriptNode(LivesScript))
		addChild(new LivesUI(squareFont))
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
	static class LivesScript extends Script<Lives> {

		@Override
		void init() {

			var scene = node.scene as AsteroidsScene

			scene.player.on(PlayerDestroyedEvent) { event ->
				node.lives--
				logger.debug('Lives: {}', node.lives)

				if (!node.lives) {
					logger.debug('Game over!')
				}
			}
		}
	}

	/**
	 * Component for rendering the number of lives remaining to the UI.
	 */
	class LivesUI extends ImGuiModule {

		private final ImFont squareFont
		private final Image livesImage

		LivesUI(ImFont squareFont) {

			this.squareFont = squareFont
			var resourceManager = RESOURCE_MANAGER.get()
			livesImage = resourceManager.loadImage('Lives.png')
		}

		@Override
		void render(ImGuiContext context) {

			var uiArea = context.uiArea
			ImGui.setNextWindowBgAlpha(0.4f)
			ImGui.setNextWindowPos(uiArea.minX, uiArea.minY)
			ImGui.pushFont(squareFont)
			ImGui.pushStyleVar(WindowBorderSize, 0f)
			ImGui.pushStyleVar(WindowPadding, 8 * context.uiScale as float, 4 * context.uiScale as float)

			ImGui.begin('Lives', new ImBoolean(true), NoNav | NoDecoration | NoSavedSettings | NoFocusOnAppearing | NoDocking | AlwaysAutoResize)
			ImGui.image(((OpenGLTexture)livesImage.texture).textureId,
				livesImage.width / (4 / context.uiScale) as float,
				livesImage.height / (4 / context.uiScale) as float,
				0f, 1f, 1f, 0f)
			ImGui.sameLine()
			ImGui.text("x ${lives}")

			ImGui.popStyleVar(2)
			ImGui.popFont()
			ImGui.end()
		}
	}
}
