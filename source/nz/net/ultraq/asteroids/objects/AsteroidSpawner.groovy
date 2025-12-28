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
import nz.net.ultraq.asteroids.objects.Asteroid.Size
import nz.net.ultraq.redhorizon.engine.Entity
import nz.net.ultraq.redhorizon.engine.scripts.EntityScript
import nz.net.ultraq.redhorizon.engine.scripts.ScriptComponent
import static nz.net.ultraq.asteroids.ScopedValues.SCRIPT_ENGINE

import org.joml.Vector2f
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static java.lang.String.format

/**
 * Game object responsible for creating asteroids.
 *
 * @author Emanuel Rabina
 */
class AsteroidSpawner extends Entity<AsteroidSpawner> {

	private static final Logger logger = LoggerFactory.getLogger(AsteroidSpawner)

	final String name = 'Asteroid spawner'

	/**
	 * Constructor, create a new asteroid spawner.
	 */
	AsteroidSpawner() {

		var scriptEngine = SCRIPT_ENGINE.get()

		addComponent(new ScriptComponent(scriptEngine, AsteroidSpawnerScript))
	}

	/**
	 * Create new asteroids at regular intervals.
	 */
	static class AsteroidSpawnerScript extends EntityScript<AsteroidSpawner> {

		private static float minSpawnRate = 2f
		private static float maxSpawnRate = 1f
		private static float spawnRateIncreaseInterval = 10f

		private AsteroidsScene scene
		private float spawnRate = minSpawnRate
		private float spawnTimer = 0f
		private float spawnRateIncreaseTimer = 0f
		private Vector2f spawnPoint = new Vector2f()
		private Vector2f headingCenter = new Vector2f()

		@Override
		void init() {

			scene = (AsteroidsScene)entity.scene
		}

		@Override
		void update(float delta) {

			spawnTimer += delta

			// Spawn a new asteroid at some random point in a circle that surrounds
			// the visible area.  This asteroid is then rotated towards the center
			// with a little variance so that it flies back towards the play area.
			if (spawnTimer >= spawnRate) {
				var spawnAngle = Math.random() * 2 * Math.PI
				spawnPoint.set(-Math.sin(spawnAngle) as float, Math.cos(spawnAngle) as float).mul(scene.WIDTH / 1.8f as float)
				headingCenter.set(spawnPoint).mul(-1f)
				var spawnRotation = Vector2f.UP.angle(headingCenter) + (Math.toRadians(Math.random() * 30 - 15)) as float
				logger.debug('Spawning asteroid at: {}, {}', format('%.2f', spawnPoint.x), format('%.2f', spawnPoint.y))
				scene.queueChange { ->
					scene.addChild(new Asteroid(Size.LARGE, spawnPoint, spawnRotation)
						.withName("Asteroid ${Asteroid.count++} (large)"))
				}
				spawnTimer -= spawnRate
			}

			spawnRateIncreaseTimer += delta

			// Steadily increase the asteroid spawn rate
			if (spawnRate > maxSpawnRate && spawnRateIncreaseTimer >= spawnRateIncreaseInterval) {
				spawnRate = Math.max(spawnRate - 0.1f, maxSpawnRate) as float
				spawnRateIncreaseTimer -= spawnRateIncreaseInterval
				logger.debug('Spawn rate increased!  Now at {}s', format("%.2f", spawnRate))
			}
		}
	}
}
