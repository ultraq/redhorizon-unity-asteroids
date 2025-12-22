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

import nz.net.ultraq.redhorizon.engine.scripts.EntityScript
import nz.net.ultraq.redhorizon.input.InputEventHandler
import static nz.net.ultraq.asteroids.ScopedValues.getINPUT_EVENT_HANDLER

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.*

/**
 * Player movement and behaviour script.
 *
 * @author Emanuel Rabina
 */
class PlayerScript extends EntityScript {

	private static final Logger logger = LoggerFactory.getLogger(PlayerScript)

	private InputEventHandler inputEventHandler
	private float turnDirection

	@Override
	void init() {

		inputEventHandler = INPUT_EVENT_HANDLER.get()
	}

	@Override
	void update(float delta) {

		if (inputEventHandler.keyPressed(GLFW_KEY_LEFT)) {
			turnDirection -= 1f
		}
		if (inputEventHandler.keyPressed(GLFW_KEY_RIGHT)) {
			turnDirection += 1f
		}
	}
}
