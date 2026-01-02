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

import nz.net.ultraq.asteroids.objects.AsteroidSpawner
import nz.net.ultraq.asteroids.objects.Lives
import nz.net.ultraq.asteroids.objects.Player
import nz.net.ultraq.asteroids.objects.Score
import nz.net.ultraq.redhorizon.engine.graphics.CameraEntity
import nz.net.ultraq.redhorizon.graphics.Window
import nz.net.ultraq.redhorizon.scenegraph.Scene
import static nz.net.ultraq.asteroids.ScopedValues.WINDOW

import imgui.ImFontConfig
import imgui.ImGui

/**
 * Scene setup for the Asteroids game.
 *
 * @author Emanuel Rabina
 */
class AsteroidsScene extends Scene {

	static final int WIDTH = 1920
	static final int HEIGHT = 1440

	final CameraEntity camera
	final Player player
	boolean showCollisionLines = false
	private final Window window

	/**
	 * Constructor, create a new scene to the given dimensions.
	 */
	AsteroidsScene() {

		window = WINDOW.get()

		camera = new CameraEntity(WIDTH, HEIGHT, window)
		player = new Player()
		addChild(camera)
		addChild(player)
		addChild(new AsteroidSpawner())

		var io = ImGui.getIO()
		var imFontConfig = new ImFontConfig()
		var squareFont = getResourceAsStream('nz/net/ultraq/asteroids/assets/Square.ttf').withCloseable { stream ->
			return io.fonts.addFontFromMemoryTTF(stream.bytes, 16 * (window.contentScale / window.renderScale) as float, imFontConfig)
		}
		imFontConfig.destroy()

		addChild(new Lives(squareFont))
		addChild(new Score(squareFont))
	}
}
