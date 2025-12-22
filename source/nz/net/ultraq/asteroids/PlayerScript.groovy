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

import nz.net.ultraq.redhorizon.engine.graphics.SpriteComponent
import nz.net.ultraq.redhorizon.engine.scripts.EntityScript
import nz.net.ultraq.redhorizon.input.InputEventHandler
import static nz.net.ultraq.asteroids.ScopedValues.INPUT_EVENT_HANDLER

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.*

/**
 * Player movement and behaviour script.
 *
 * @author Emanuel Rabina
 */
class PlayerScript extends EntityScript {

	static final float thrustSpeed = 100f
	static final float turnSpeed = 1f
	private static final Logger logger = LoggerFactory.getLogger(PlayerScript)

	private final InputEventHandler input
	private SpriteComponent sprite
	private boolean thrusting
	private float turnDirection

	/**
	 * Constructor, set the player script up with the scoped values.
	 */
	PlayerScript() {

		input = INPUT_EVENT_HANDLER.get()
	}

	@Override
	void init() {

		sprite = entity.findComponent { it instanceof SpriteComponent } as SpriteComponent
	}

	@Override
	void update(float delta) {

		thrusting = input.keyPressed(GLFW_KEY_W) || input.keyPressed(GLFW_KEY_UP)
		turnDirection =
			input.keyPressed(GLFW_KEY_A) || input.keyPressed(GLFW_KEY_LEFT) ? 1f :
			input.keyPressed(GLFW_KEY_D) || input.keyPressed(GLFW_KEY_RIGHT) ? -1f :
			0f

		if (thrusting) {
			entity.transform.translate(0f, thrustSpeed * delta as float, 0f)
		}
		if (turnDirection != 0f) {
			entity.transform.rotateZ(turnDirection * turnSpeed * delta as float)
		}
	}
}
