/*
 * Copyright 2026, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import nz.net.ultraq.redhorizon.engine.Entity
import nz.net.ultraq.redhorizon.engine.graphics.imgui.ImGuiComponent
import nz.net.ultraq.redhorizon.graphics.Window
import nz.net.ultraq.redhorizon.graphics.imgui.ImGuiContext
import static nz.net.ultraq.asteroids.ScopedValues.WINDOW

import imgui.ImFont
import imgui.ImGui
import imgui.type.ImBoolean
import static imgui.flag.ImGuiStyleVar.WindowBorderSize
import static imgui.flag.ImGuiWindowFlags.*

/**
 * The game over text.
 *
 * @author Emanuel Rabina
 */
class GameOver extends Entity<GameOver> {

	/**
	 * Constructor, adds a UI component that will be shown when it's game over.
	 */
	GameOver(ImFont squareFont, ImFont squareOutlineFont) {

		addComponent(new GameOverUiComponent(squareFont, squareOutlineFont))
	}

	static class GameOverUiComponent extends ImGuiComponent<GameOverUiComponent> {

		private final ImFont squareFont
		private final ImFont squareOutlineFont
		private final Window window

		GameOverUiComponent(ImFont squareFont, ImFont squareOutlineFont) {

			this.squareFont = squareFont
			this.squareOutlineFont = squareOutlineFont
			this.window = WINDOW.get()
		}

		@Override
		void render(ImGuiContext context) {

			var uiArea = window.uiArea
			ImGui.setNextWindowBgAlpha(0.4f)
			ImGui.setNextWindowPos(
				uiArea.minX + (uiArea.lengthX() / 2) - 130 as float,
				uiArea.minY + (uiArea.lengthY() / 2) - 100 as float)
			ImGui.pushStyleVar(WindowBorderSize, 0f)

			ImGui.begin('Game Over', new ImBoolean(true), NoNav | NoDecoration | NoSavedSettings | NoFocusOnAppearing | NoDocking | AlwaysAutoResize)
			ImGui.pushFont(squareOutlineFont)
			ImGui.text('Game Over')
			ImGui.popFont()
			ImGui.pushFont(squareFont)
			ImGui.setCursorPosX(62f)
			ImGui.text('Press ESC to exit')
			ImGui.popFont()
			ImGui.end()

			ImGui.popStyleVar()
		}
	}
}
