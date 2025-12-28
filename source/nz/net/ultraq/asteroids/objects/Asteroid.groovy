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
import nz.net.ultraq.asteroids.engine.EntityScript
import nz.net.ultraq.eventhorizon.EventTarget
import nz.net.ultraq.redhorizon.engine.Entity
import nz.net.ultraq.redhorizon.engine.graphics.CameraEntity
import nz.net.ultraq.redhorizon.engine.graphics.SpriteComponent
import nz.net.ultraq.redhorizon.engine.scripts.ScriptComponent
import nz.net.ultraq.redhorizon.graphics.opengl.BasicShader
import static nz.net.ultraq.asteroids.ScopedValues.*

import org.joml.FrustumIntersection
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector2fc
import org.joml.Vector3f
import org.joml.primitives.Circlef
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * The big rocks we'll be shooting at!
 *
 * @author Emanuel Rabina
 */
class Asteroid extends Entity<Asteroid> implements EventTarget<Asteroid> {

	static final float baseSpeed = 100f
	static int count = 1
	private static final Logger logger = LoggerFactory.getLogger(Asteroid)

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
			.rotate(0f, 0f, (Math.random() * 2 * Math.PI) as float))
		addComponent(new CircleCollisionComponent(asteroidImage.width / 2))
		addComponent(new ScriptComponent(scriptEngine, AsteroidScript))
	}

	/**
	 * Asteroid movement and behaviour.
	 */
	static class AsteroidScript extends EntityScript<Asteroid> {

		private CameraEntity camera
		private Matrix4f expandedViewProjection = new Matrix4f()
		private FrustumIntersection frustumIntersection = new FrustumIntersection()
		private boolean visible = false
		private Vector2f splitPosition1 = new Vector2f()
		private Vector2f splitPosition2 = new Vector2f()
		private Vector3f splitRotation1 = new Vector3f()
		private Vector3f splitRotation2 = new Vector3f()

		@Override
		void init() {

			camera = ((AsteroidsScene)entity.scene).camera
		}

		@Override
		void onCollision(Circlef asteroidBounds, Entity otherEntity, Circlef otherBounds) {

			if (otherEntity instanceof Bullet) {
				var scene = entity.scene as AsteroidsScene

				if (entity.size == Size.LARGE || entity.size == Size.MEDIUM) {
					logger.debug('{} collided with bullet - splitting', entity.name)
					scene.queueChange { ->
						var newSize = entity.size == Size.LARGE ? Size.MEDIUM : Size.SMALL
						scene.addChild(
							new Asteroid(newSize, splitPosition1.set(entity.position).add(-4f, 0f),
								entity.transform.getEulerAnglesXYZ(splitRotation1)
									.add(0f, 0f, Math.toRadians(Math.random() * 90) as float).z)
								.withName("Asteroid ${Asteroid.count++} (${newSize.name().toLowerCase()})"))
						scene.addChild(
							new Asteroid(newSize, splitPosition2.set(entity.position).add(4f, 0f),
								entity.transform.getEulerAnglesXYZ(splitRotation2)
									.add(0f, 0f, Math.toRadians(Math.random() * -90) as float).z)
								.withName("Asteroid ${Asteroid.count++} (${newSize.name().toLowerCase()})"))
					}
				}
				else {
					logger.debug('{} collided with bullet - destroying', entity.name)
				}

				scene.queueChange { ->
					entity.parent.removeChild(entity)
					entity.close()
				}
				entity.trigger(new AsteroidDestroyedEvent(entity))
			}
		}

		@Override
		void update(float delta) {

			// Track visibility to know when to remove the object
			frustumIntersection.set(expandedViewProjection.set(camera.viewProjection).scale(0.8f), false)
			var lastVisible = visible
			var nowVisible = frustumIntersection.testPoint(entity.position)
			if (lastVisible && !nowVisible) {
				(entity.scene as AsteroidsScene).queueChange { ->
					entity.parent.removeChild(entity)
					entity.close()
				}
				return
			}
			else if (!lastVisible && nowVisible) {
				visible = true
			}

			// Keep moving along
			var speed = baseSpeed * (entity.size == Size.LARGE ? 1f : entity.size == Size.MEDIUM ? 3f : 12f)
			entity.transform.translate(0f, speed * delta as float, 0f)
		}
	}
}
