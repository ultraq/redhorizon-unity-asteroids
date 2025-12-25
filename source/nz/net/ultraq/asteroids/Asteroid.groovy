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

import nz.net.ultraq.redhorizon.engine.Entity
import nz.net.ultraq.redhorizon.engine.graphics.SpriteComponent
import nz.net.ultraq.redhorizon.engine.scripts.ScriptComponent
import nz.net.ultraq.redhorizon.graphics.opengl.BasicShader
import static nz.net.ultraq.asteroids.ScopedValues.*

import org.joml.Vector2fc

/**
 * The big rocks we'll be shooting at!
 *
 * @author Emanuel Rabina
 */
class Asteroid extends Entity<Asteroid> {

	/**
	 * The sizes of asteroid available.
	 */
	static enum Size {
		LARGE,
		MEDIUM,
		SMALL
	}

	final Size size

	/**
	 * Constructor, create an asteroid.
	 */
	Asteroid(Size size, Vector2fc initialPosition, float rotation) {

		var resourceManager = RESOURCE_MANAGER.get()
		var scriptEngine = SCRIPT_ENGINE.get()

		this.size = size

		transform
			.translate(initialPosition.x(), initialPosition.y(), 0f)
			.rotateXYZ(0f, 0f, rotation)
			.scale(size == Size.LARGE ? 1f : size == Size.MEDIUM ? 0.5f : 0.25f)

		var asteroidImage = resourceManager.loadImage("Asteroid_0${(Math.random() * 3 + 1) as int}.png")
		addComponent(new SpriteComponent(asteroidImage, BasicShader)
			.translate(-asteroidImage.width / 2 as float, -asteroidImage.height / 2 as float, 0f))
//			.rotate(0f, 0f, (Math.random() * 2 * Math.PI) as float))
		addComponent(new ScriptComponent(scriptEngine, 'AsteroidScript'))
	}
}
