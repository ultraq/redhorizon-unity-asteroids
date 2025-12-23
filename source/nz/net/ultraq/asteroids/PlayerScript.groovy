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

import nz.net.ultraq.redhorizon.engine.graphics.CameraEntity
import nz.net.ultraq.redhorizon.engine.graphics.SpriteComponent
import nz.net.ultraq.redhorizon.engine.scripts.EntityScript
import nz.net.ultraq.redhorizon.input.InputEventHandler
import static nz.net.ultraq.asteroids.ScopedValues.INPUT_EVENT_HANDLER

import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.primitives.Rectanglef
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W

/**
 * Player movement and behaviour script.
 *
 * @author Emanuel Rabina
 */
class PlayerScript extends EntityScript {

	static final float maxThrustSpeed = 400f
	static final float linearDrag = 0.5f
	private static final Logger logger = LoggerFactory.getLogger(PlayerScript)
	private static final Vector2f up = new Vector2f(0f, 1f)

	private final InputEventHandler input
	private SpriteComponent sprite
	private CameraEntity camera
	private Vector2f worldBoundsMin
	private Vector2f worldBoundsMax

	// Movement and rotation
	private Vector2f positionXY = new Vector2f()
	private Vector2f worldCursorPosition = new Vector2f()
	private Vector3f unprojectResult = new Vector3f()
	private Vector2f headingToCursor = new Vector2f()
	private float heading = 0f
	private Vector2f impulse = new Vector2f()
	private boolean accelerating = false
	private Vector2f velocity = new Vector2f()
	private Vector2f updatedPosition = new Vector2f()

	/**
	 * Constructor, set the player script up with the scoped values.
	 */
	PlayerScript() {

		input = INPUT_EVENT_HANDLER.get()
	}

	@Override
	void init() {

		var scene = (AsteroidsScene)entity.scene
		sprite = entity.findComponent { it instanceof SpriteComponent } as SpriteComponent
		camera = scene.camera
		var worldBounds = new Rectanglef().setMax(scene.WIDTH, scene.HEIGHT).center()
		worldBoundsMin = worldBounds.getMin(new Vector2f())
		worldBoundsMax = worldBounds.getMax(new Vector2f())
	}

	@Override
	void update(float delta) {

		// Update sprite to look at the cursor
		var cursorPosition = input.cursorPosition()
		if (cursorPosition) {
			positionXY.set(entity.position)
			worldCursorPosition.set(camera.unproject(cursorPosition.x(), cursorPosition.y(), unprojectResult))
			worldCursorPosition.sub(positionXY, headingToCursor)
			heading = headingToCursor.angle(up)
			entity.transform.setRotationXYZ(0f, 0f, -heading)
		}

		// Set the direction of the movement force based on inputs
		var impulseDirection = 0f
		if (input.keyPressed(GLFW_KEY_W)) {
			impulseDirection = heading
			accelerating = true
		}
		else {
			accelerating = false
		}

		// Adjust the strength of the force based on acceleration time
		if (accelerating) {
			impulse.set(Math.sin(impulseDirection), Math.cos(impulseDirection)).normalize().mul(maxThrustSpeed).mul(delta)
		}
		else {
			impulse.set(0f, 0f)
		}

		// Calculate the velocity from the above
		velocity.lerp(impulse, linearDrag * delta as float)

		// Adjust position based on velocity
		if (velocity) {
			updatedPosition.set(entity.position).add(velocity).min(worldBoundsMax).max(worldBoundsMin)
			entity.setPosition(updatedPosition.x(), updatedPosition.y(), 0)
		}
	}
}
