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

import nz.net.ultraq.asteroids.Asteroid.Size
import nz.net.ultraq.redhorizon.engine.scripts.EntityScript

import org.joml.Vector2f
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Create new asteroids at regular intervals.
 *
 * @author Emanuel Rabina
 */
class AsteroidSpawnerScript extends EntityScript<AsteroidSpawner> {

	private static final Logger logger = LoggerFactory.getLogger(AsteroidSpawnerScript)
	private static float spawnInterval = 2f
	private static float spawnTimer = 0f
	private static final Vector2f up = new Vector2f(0f, 1f)

	private AsteroidsScene scene
	private Vector2f spawnPoint = new Vector2f()
	private Vector2f headingCenter = new Vector2f()

	@Override
	void init() {
		scene = (AsteroidsScene)entity.scene
	}

	@Override
	void update(float delta) {

		spawnTimer += delta
		if (spawnTimer >= spawnInterval) {
			var spawnAngle = Math.random() * 2 * Math.PI
			spawnPoint
				.set(-Math.sin(spawnAngle) as float, Math.cos(spawnAngle) as float)
				.mul(scene.WIDTH / 2 as float)
			var spawnRotation = up.angle(headingCenter.set(spawnPoint).mul(-1f)) + (Math.toRadians(Math.random() * 30 - 15)) as float
//			logger.debug('Spawn angle: {}, spawn point: {}, spawn rotation: {}',
//				String.format("%.2f", Math.toDegrees(spawnAngle)), spawnPoint, String.format("%.2f", Math.toDegrees(spawnRotation)))

			scene.queueChange { ->
				scene.addChild(new Asteroid(Size.LARGE, spawnPoint, spawnRotation))
			}
			spawnTimer = 0f
		}
	}
}
