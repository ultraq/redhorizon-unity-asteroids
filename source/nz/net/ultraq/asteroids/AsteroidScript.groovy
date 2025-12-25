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

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Asteroid movement and behaviour.
 *
 * @author Emanuel Rabina
 */
class AsteroidScript extends EntityScript<Asteroid> {

	private static final Logger logger = LoggerFactory.getLogger(AsteroidScript)

	private float baseSpeed = 100f

	@Override
	void update(float delta) {

		var speed = baseSpeed * (entity.size == Size.LARGE ? 1f : entity.size == Size.MEDIUM ? 1.5f : 2f)
		entity.transform.translate(0f, speed * delta as float, 0f)
	}
}
