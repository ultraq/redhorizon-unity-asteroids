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

package nz.net.ultraq.asteroids.debug

import nz.net.ultraq.asteroids.AsteroidsScene
import nz.net.ultraq.redhorizon.engine.graphics.GridLines
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.imgui.ImGuiModule
import nz.net.ultraq.redhorizon.input.KeyBinding

import org.joml.primitives.Rectanglef
import static org.lwjgl.glfw.GLFW.GLFW_KEY_P

/**
 * Input binding for toggling all debug information with the {@code P} key.
 * This will toggle:
 * <ul>
 *   <li>grid lines</li>
 *   <li>collision outlines</li>
 *   <li>all ImGui debug components</li>
 * </ul>
 *
 * @author Emanuel Rabina
 */
class DebugEverythingBinding extends KeyBinding {

	private static final Colour LIGHT_GREY = new Colour('Light grey', 0.85f, 0.85f, 0.85f, 1f)

	private static boolean enabled = false

	DebugEverythingBinding(AsteroidsScene scene, List<ImGuiModule> debugWindows) {

		super(GLFW_KEY_P, true, { ->
			enabled = !enabled

			debugWindows.each { window ->
				if (enabled) {
					window.enable()
				}
				else {
					window.disable()
				}
			}

			var gridLines = scene.findDescendentByType(GridLines)
			if (enabled) {
				if (!gridLines) {
					scene.insertBefore(
						new GridLines(new Rectanglef(0f, 0f, AsteroidsScene.WIDTH, AsteroidsScene.HEIGHT).center(), 100f,
							LIGHT_GREY, Colour.GREY)
							.withName('Grid lines'),
						scene.player)
				}
				else {
					gridLines.enable()
				}
			}
			else {
				if (gridLines) {
					gridLines.disable()
				}
			}

			scene.showCollisionLines = !scene.showCollisionLines
		})
	}
}
