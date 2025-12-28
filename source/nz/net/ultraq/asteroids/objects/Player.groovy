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
import nz.net.ultraq.asteroids.engine.CircleCollisionComponent
import nz.net.ultraq.asteroids.engine.CollisionComponent
import nz.net.ultraq.asteroids.engine.EntityScript
import nz.net.ultraq.redhorizon.engine.Entity
import nz.net.ultraq.redhorizon.engine.graphics.CameraEntity
import nz.net.ultraq.redhorizon.engine.graphics.SpriteComponent
import nz.net.ultraq.redhorizon.engine.scripts.ScriptComponent
import nz.net.ultraq.redhorizon.graphics.opengl.BasicShader
import nz.net.ultraq.redhorizon.input.InputEventHandler
import static nz.net.ultraq.asteroids.ScopedValues.*

import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.primitives.Circlef
import org.joml.primitives.Rectanglef
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.*

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * The player spaceship.
 *
 * @author Emanuel Rabina
 */
class Player extends Entity<Player> {

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
		var scriptEngine = SCRIPT_ENGINE.get()

		var playerImage = resourceManager.loadImage('Player.png')
		addComponent(new SpriteComponent(playerImage, BasicShader))
		addComponent(new CircleCollisionComponent(playerImage.width / 2))
		addComponent(new ScriptComponent(scriptEngine, PlayerScript))
	}

	/**
	 * Player movement and behaviour script.
	 */
	static class PlayerScript extends EntityScript<Player> {

		private final InputEventHandler input
		private final ScheduledExecutorService executor
		private AsteroidsScene scene
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

		// Shooting
		private float firingCooldown

		/**
		 * Constructor, set the player script up with the scoped values.
		 */
		PlayerScript() {

			input = INPUT_EVENT_HANDLER.get()
			executor = Executors.newSingleThreadScheduledExecutor()
		}

		@Override
		void init() {

			scene = entity.scene as AsteroidsScene
			sprite = entity.findComponent { it instanceof SpriteComponent } as SpriteComponent
			camera = scene.camera
			var worldBounds = new Rectanglef().setMax(scene.WIDTH, scene.HEIGHT).center()
			worldBoundsMin = worldBounds.getMin(new Vector2f())
			worldBoundsMax = worldBounds.getMax(new Vector2f())
		}

		@Override
		void onCollision(Circlef playerBounds, Entity otherEntity, Circlef otherBounds) {

			if (otherEntity instanceof Asteroid) {
				logger.debug('The player collided with {}!', otherEntity.name)

				scene.queueChange { ->
					scene.removeChild(entity)
					entity.transform.identity()
					entity.findComponentByType(CollisionComponent).disable()
					velocity.zero()

					// Respawn after 3 seconds
					executor.schedule({ ->
						this.scene.queueChange { ->
							this.scene.addChild(entity)
						}
					}, 3, TimeUnit.SECONDS)

					// Enable collision after 5 seconds (2 seconds after respawn)
					executor.schedule({ ->
						this.scene.queueChange { ->
							this.entity.findComponentByType(CollisionComponent).enable()
						}
					}, 5, TimeUnit.SECONDS)
				}
			}
		}

		@Override
		void update(float delta) {

			updateHeading(delta)
			updateMovement(delta)
			updateShooting(delta)
		}

		/**
		 * Keep the player pointed at the cursor.
		 */
		private void updateHeading(float delta) {

			// Update sprite to look at the cursor
			var cursorPosition = input.cursorPosition()
			if (cursorPosition) {
				positionXY.set(entity.position)
				worldCursorPosition.set(camera.unproject(cursorPosition.x(), cursorPosition.y(), unprojectResult))
				worldCursorPosition.sub(positionXY, headingToCursor)
				heading = headingToCursor.angle(Vector2f.UP)
				entity.transform.setRotationXYZ(0f, 0f, -heading)
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
				updatedPosition.set(entity.position).add(velocity).min(worldBoundsMax).max(worldBoundsMin)
				entity.setPosition(updatedPosition.x(), updatedPosition.y(), 0)
			}
		}

		/**
		 * Fire bullets if player is pressing the fire button.
		 */
		private void updateShooting(float delta) {

			firingCooldown -= delta

			if ((input.keyPressed(GLFW_KEY_SPACE) || input.mouseButtonPressed(GLFW_MOUSE_BUTTON_1)) && firingCooldown <= 0f) {
				scene.queueChange { ->
					scene.addChild(new Bullet(entity.transform, velocity)
						.withName("Bullet ${bulletCount++}"))
				}
				firingCooldown = 0.25f
			}
		}
	}
}
