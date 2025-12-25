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

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Bullet behaviour script.
 *
 * @author Emanuel Rabina
 */
class BulletScript extends EntityScript<Bullet> {

	static final float bulletSpeed = 800f
	static final float bulletLifetime = 1.2f
	private static final Logger logger = LoggerFactory.getLogger(BulletScript)

	private float bulletTimer

	@Override
	void update(float delta) {

		bulletTimer += delta

		// Destroy bullet if it reaches the max lifetime
		if (bulletTimer > bulletLifetime) {
			(entity.scene as AsteroidsScene).queueChange { ->
				entity.scene.removeChild(entity)
				entity.close()
			}
		}

		// Keep moving along
		else {
			entity.translate(0f, (bulletSpeed + entity.initialVelocity.length()) * delta as float, 0f)
		}
	}
}
