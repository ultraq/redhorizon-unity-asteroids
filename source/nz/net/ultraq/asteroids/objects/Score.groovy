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
import nz.net.ultraq.redhorizon.scenegraph.NodeAddedEvent

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Track the player score.
 *
 * @author Emanuel Rabina
 */
class Score extends Entity<Score> {

	private static final Logger logger = LoggerFactory.getLogger(Score)

	private int score = 0

	/**
	 * Constructor, tie a score component to the scene so we can know of changes
	 * made to it.
	 */
	Score(AsteroidsScene scene) {

		scene.on(NodeAddedEvent) { nodeAddedEvent ->
			var node = nodeAddedEvent.node()

			if (node instanceof Asteroid) {
				node.on(AsteroidDestroyedEvent) { asteroidDestroyedEvent ->
					var asteroid = asteroidDestroyedEvent.asteroid()
					score += asteroid.size == Size.LARGE ? 25 : asteroid.size == Size.MEDIUM ? 50 : 100
					logger.debug('Score: {}', score)
				}
			}
		}
	}

	/**
	 * Return the current score.
	 */
	int getScore() {

		return score
	}
}
