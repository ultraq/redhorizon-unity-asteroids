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
import nz.net.ultraq.redhorizon.engine.scripts.EntityScript
import nz.net.ultraq.redhorizon.engine.scripts.ScriptComponent
import static nz.net.ultraq.asteroids.ScopedValues.*

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

	private int lives = 3

	/**
	 * Constructor, tie the lives to the player.
	 */
	Lives() {

		addComponent(new ScriptComponent(SCRIPT_ENGINE.get(), LivesScript))
	}

	/**
	 * Return the current number of lives.
	 */
	int getLives() {

		return lives
	}

	/**
	 * Script for tracking player lives.
	 */
	static class LivesScript extends EntityScript<Lives> implements AutoCloseable {

		private ScheduledExecutorService executor

		@Override
		void close() {

			executor.shutdown()
		}

		@Override
		void init() {

			executor = Executors.newSingleThreadScheduledExecutor()

			var window = WINDOW.get()
			var scene = entity.scene as AsteroidsScene

			scene.player.on(PlayerDestroyedEvent) { event ->
				entity.lives--
				logger.debug('Lives: {}', entity.lives)

				if (!entity.lives) {
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
	}
}
