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

import nz.net.ultraq.redhorizon.engine.physics.CircleCollider
import nz.net.ultraq.redhorizon.engine.physics.CollisionEvent
import nz.net.ultraq.redhorizon.engine.scripts.Script
import nz.net.ultraq.redhorizon.engine.scripts.ScriptNode
import nz.net.ultraq.redhorizon.graphics.Sprite
import nz.net.ultraq.redhorizon.scenegraph.Node
import static nz.net.ultraq.asteroids.ScopedValues.RESOURCE_MANAGER

import org.joml.Matrix4fc
import org.joml.Vector2fc
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Pew pew.
 *
 * @author Emanuel Rabina
 */
class Bullet extends Node<Bullet> {

	static final float bulletSpeed = 800f
	static final float bulletLifetime = 1.2f
	private static final Logger logger = LoggerFactory.getLogger(Bullet)

	final Vector2fc initialVelocity

	/**
	 * Constructor, set up the bullet entity.
	 */
	Bullet(Matrix4fc initialTransform, Vector2fc initialVelocity) {

		setTransform(initialTransform).translate(0f, 32f) // Start slightly ahead of the object
		this.initialVelocity = initialVelocity // Include ship velocity for moving bullets

		var resourceManager = RESOURCE_MANAGER.get()
		var bulletImage = resourceManager.loadImage('Square.png')
		addChild(new Sprite(bulletImage))
		addChild(new CircleCollider(bulletImage.width / 2))
		addChild(new ScriptNode(BulletScript))
	}

	/**
	 * Bullet behaviour script.
	 */
	static class BulletScript extends Script<Bullet> {

		private float bulletTimer
		private boolean queuedForRemoval = false

		@Override
		void init() {

			node.findByType(CircleCollider).on(CollisionEvent) { event ->
				var otherObject = event.otherObject()

				if (otherObject instanceof Asteroid && !queuedForRemoval) {
					logger.debug('Bullet collided with {} - removing from scene', otherObject.name)
					node.scene.queueUpdate { ->
						node.parent?.removeChild(node)
						node.close()
					}
					queuedForRemoval = true
				}
			}
		}

		@Override
		void update(float delta) {

			bulletTimer += delta

			// Destroy bullet if it reaches the max lifetime
			if (bulletTimer > bulletLifetime && !queuedForRemoval) {
				node.scene.queueUpdate { ->
					node.parent?.removeChild(node)
					node.close()
				}
				queuedForRemoval = true
			}

			// Keep moving along
			else {
				node.translate(0f, (bulletSpeed + node.initialVelocity.length()) * delta as float)
			}
		}
	}
}
