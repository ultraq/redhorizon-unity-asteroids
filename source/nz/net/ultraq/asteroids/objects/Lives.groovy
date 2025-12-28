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
import nz.net.ultraq.redhorizon.engine.Entity
import static nz.net.ultraq.asteroids.ScopedValues.WINDOW

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * Lives counter.
 *
 * @author Emanuel Rabina
 */
class Lives extends Entity<Lives> {

	private static final Logger logger = LoggerFactory.getLogger(Lives)

	private final ScheduledExecutorService executor
	private int lives = 3

	/**
	 * Constructor, tie the lives to the player.
	 */
	Lives(AsteroidsScene scene, Player player) {

		executor = Executors.newSingleThreadScheduledExecutor()

		var window = WINDOW.get()

		player.on(PlayerDestroyedEvent) { event ->
			lives--
			logger.debug('Lives: {}', lives)

			if (!lives) {
				logger.debug('Game over!')
				scene.queueChange { ->
					scene.clear()
				}

				executor.schedule({ ->
					window.shouldClose(true)
				}, 1, TimeUnit.SECONDS)
			}
		}
	}

	/**
	 * Return the current number of lives.
	 */
	int getLives() {

		return lives
	}
}
