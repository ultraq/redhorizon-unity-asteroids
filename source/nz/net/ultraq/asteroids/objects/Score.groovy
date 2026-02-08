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

import nz.net.ultraq.asteroids.objects.Asteroid.Size
import nz.net.ultraq.redhorizon.engine.scripts.Script
import nz.net.ultraq.redhorizon.engine.scripts.ScriptNode
import nz.net.ultraq.redhorizon.graphics.imgui.ImGuiContext
import nz.net.ultraq.redhorizon.graphics.imgui.ImGuiModule
import nz.net.ultraq.redhorizon.scenegraph.Node
import nz.net.ultraq.redhorizon.scenegraph.NodeAddedEvent

import imgui.ImFont
import imgui.ImGui
import imgui.type.ImBoolean
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static imgui.flag.ImGuiStyleVar.*
import static imgui.flag.ImGuiWindowFlags.*

/**
 * Track the player score.
 *
 * @author Emanuel Rabina
 */
class Score extends Node<Score> {

	private static final Logger logger = LoggerFactory.getLogger(Score)

	private int score = 0

	/**
	 * Constructor, tie a score component to the scene so we can know of changes
	 * made to it.
	 */
	Score(ImFont squareFont) {

		addChild(new ScriptNode(ScoreScript))
		addChild(new ScoreUiComponent(squareFont))
	}

	/**
	 * Return the current score.
	 */
	int getScore() {

		return score
	}

	/**
	 * Game script for tracking the player score.
	 */
	static class ScoreScript extends Script<Score> {

		@Override
		void init() {

			node.scene.on(NodeAddedEvent) { nodeAddedEvent ->
				var nodeAdded = nodeAddedEvent.node()
				if (nodeAdded instanceof Asteroid) {
					nodeAdded.on(AsteroidDestroyedEvent) { asteroidDestroyedEvent ->
						var asteroid = asteroidDestroyedEvent.asteroid()
						node.score += asteroid.size == Size.LARGE ? 25 : asteroid.size == Size.MEDIUM ? 50 : 100
						logger.debug('Score: {}', node.score)
					}
				}
			}
		}
	}

	/**
	 * UI component for displaying the player's score.
	 */
	class ScoreUiComponent extends ImGuiModule {

		private final ImFont squareFont

		ScoreUiComponent(ImFont squareFont) {

			this.squareFont = squareFont
		}

		@Override
		void render(ImGuiContext context) {

			var uiArea = context.uiArea
			ImGui.setNextWindowBgAlpha(0.4f)
			ImGui.setNextWindowPos(uiArea.minX, uiArea.minY + (24 * context.uiScale) as float)
			ImGui.pushFont(squareFont)
			ImGui.pushStyleVar(WindowBorderSize, 0f)
			ImGui.pushStyleVar(WindowPadding, 8 * context.uiScale as float, 4 * context.uiScale as float)

			ImGui.begin('Score', new ImBoolean(true), NoNav | NoDecoration | NoSavedSettings | NoFocusOnAppearing | NoDocking | AlwaysAutoResize)
			ImGui.text(String.format('%,d', score))

			ImGui.popStyleVar(2)
			ImGui.popFont()
			ImGui.end()
		}
	}
}
