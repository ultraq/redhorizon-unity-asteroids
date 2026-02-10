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
import nz.net.ultraq.eventhorizon.EventTarget
import nz.net.ultraq.redhorizon.engine.physics.CircleCollider
import nz.net.ultraq.redhorizon.engine.physics.Collider
import nz.net.ultraq.redhorizon.engine.physics.CollisionEvent
import nz.net.ultraq.redhorizon.engine.scripts.Script
import nz.net.ultraq.redhorizon.engine.scripts.ScriptNode
import nz.net.ultraq.redhorizon.graphics.Camera
import nz.net.ultraq.redhorizon.graphics.Sprite
import nz.net.ultraq.redhorizon.scenegraph.Node
import static nz.net.ultraq.asteroids.ScopedValues.RESOURCE_MANAGER

import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.primitives.Rectanglef
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.*

import java.util.concurrent.TimeUnit

/**
 * The player spaceship.
 *
 * @author Emanuel Rabina
 */
class Player extends Node<Player> implements EventTarget<Player> {

	static final float maxThrustSpeed = 500f
	static final float linearDrag = 1f
	private static final Logger logger = LoggerFactory.getLogger(Player)
	private static int bulletCount = 1

	final String name = 'Player'

	/**
	 * Constructor, set up the player entity.
	 */
	Player() {

		var resourceManager = RESOURCE_MANAGER.get()
		var playerImage = resourceManager.loadImage('Player.png')
		addChild(new Sprite(playerImage))
		addChild(new CircleCollider(playerImage.width / 2))
		addChild(new ScriptNode(PlayerScript))
	}

	/**
	 * Player movement and behaviour script.
	 */
	static class PlayerScript extends Script<Player> {

		private Camera camera
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

		// Shooting
		private float firingCooldown

		@Override
		void init() {

			var scene = node.scene as AsteroidsScene

			camera = scene.camera
			var worldBounds = new Rectanglef().setMax(AsteroidsScene.WIDTH, AsteroidsScene.HEIGHT).center()
			worldBoundsMin = worldBounds.getMin(new Vector2f())
			worldBoundsMax = worldBounds.getMax(new Vector2f())

			node.findByType(CircleCollider).on(CollisionEvent) { event ->
				var otherObject = event.otherObject()

				if (otherObject instanceof Asteroid) {
					logger.debug('The player collided with {}!', otherObject.name)
					scene.queueUpdate { ->
						scene.removeChild(node)
						node.resetTransform()
						node.findByType(Collider).disable()
						velocity.zero()

						// Respawn with lives remaining
						var lives = scene.findByType(Lives)
						if (lives.lives > 1) {
							scene.queueUpdate(3, TimeUnit.SECONDS) { ->
								scene.addChild(node)
							}
							scene.queueUpdate(5, TimeUnit.SECONDS) { ->
								node.findByType(Collider).enable()
							}
						}
						// Game over
						else {
							var gameOver = scene.findByType(GameOver)
							gameOver.enable()
						}

						node.trigger(new PlayerDestroyedEvent(node, otherObject))
					}
				}
			}
		}

		@Override
		void update(float delta) {

			updateHeading()
			updateMovement(delta)
			updateShooting(delta)
		}

		/**
		 * Keep the player pointed at the cursor.
		 */
		private void updateHeading() {

			// Update rotation so the sprite will appear to look at the cursor
			var cursorPosition = input.cursorPosition()
			if (cursorPosition) {
				positionXY.set(node.position)
				worldCursorPosition.set(camera.unproject(cursorPosition.x(), cursorPosition.y(), unprojectResult))
				worldCursorPosition.sub(positionXY, headingToCursor)
				heading = headingToCursor.angle(Vector2f.UP)
				node.setRotation(0f, 0f, -heading)
			}
		}

		/**
		 * Keep moving based on acceleration applied.
		 */
		private void updateMovement(float delta) {

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
				updatedPosition.set(node.position).add(velocity).min(worldBoundsMax).max(worldBoundsMin)
				node.setPosition(updatedPosition.x(), updatedPosition.y())
			}
		}

		/**
		 * Fire bullets if player is pressing the fire button.
		 */
		private void updateShooting(float delta) {

			firingCooldown -= delta

			if ((input.keyPressed(GLFW_KEY_SPACE) || input.mouseButtonPressed(GLFW_MOUSE_BUTTON_1)) && firingCooldown <= 0f) {
				var scene = node.scene
				scene.queueUpdate { ->
					scene.addChild(new Bullet(node.transform, velocity)
						.withName("Bullet ${bulletCount++}"))
				}
				firingCooldown = 0.25f
			}
		}
	}
}
