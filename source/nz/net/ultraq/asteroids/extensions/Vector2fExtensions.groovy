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

package nz.net.ultraq.asteroids.extensions

import org.joml.Vector2f
import org.joml.Vector2fc

/**
 * Extensions to JOML's {@link Vector2f} class.
 *
 * @author Emanuel Rabina
 */
class Vector2fExtensions {

	private static final Vector2fc UP = new Vector2f(0f, 1f)

	/**
	 * Return a reusable vector for the 'up' direction in this game.
	 */
	static Vector2fc getUP(Vector2f self) {

		return UP
	}
}
