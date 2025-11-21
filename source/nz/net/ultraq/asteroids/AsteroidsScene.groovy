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

import nz.net.ultraq.redhorizon.graphics.Camera
import nz.net.ultraq.redhorizon.graphics.Window
import nz.net.ultraq.redhorizon.scenegraph.Scene

/**
 * Scene setup for the Asteroids game.
 *
 * @author Emanuel Rabina
 */
class AsteroidsScene extends Scene {

	final Camera camera

	/**
	 * Constructor, create a new scene to the given dimensions.
	 */
	AsteroidsScene(int width, int height, Window window) {

		camera = new Camera(width, height, window)

		addChild(camera)
	}
}
