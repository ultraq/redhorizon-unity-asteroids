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
import nz.net.ultraq.eventhorizon.EventTarget
import nz.net.ultraq.redhorizon.engine.physics.CircleCollider
import nz.net.ultraq.redhorizon.engine.physics.CollisionEvent
import nz.net.ultraq.redhorizon.engine.scripts.Script
import nz.net.ultraq.redhorizon.engine.scripts.ScriptNode
import nz.net.ultraq.redhorizon.graphics.Sprite
import nz.net.ultraq.redhorizon.scenegraph.Node
import static nz.net.ultraq.asteroids.ScopedValues.RESOURCE_MANAGER

import org.joml.FrustumIntersection
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector2fc
import org.joml.Vector3f
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * The big rocks we'll be shooting at!
 *
 * @author Emanuel Rabina
 */
class Asteroid extends Node<Asteroid> implements EventTarget<Asteroid> {

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

		this.size = size
		this
			.translate(initialPosition.x(), initialPosition.y())
			.rotate(0f, 0f, rotation)
			.scale(size == Size.LARGE ? 1f : size == Size.MEDIUM ? 0.5f : 0.25f)

		var resourceManager = RESOURCE_MANAGER.get()
		var asteroidImage = resourceManager.loadImage("Asteroid_0${(Math.random() * 3 + 1) as int}.png")
		addChild(new Sprite(asteroidImage)
			.rotate(0f, 0f, Math.random() * 2 * Math.PI as float))
		addChild(new CircleCollider(asteroidImage.width / 2))
		addChild(new ScriptNode(AsteroidScript))
	}

	/**
	 * Asteroid movement and behaviour.
	 */
	static class AsteroidScript extends Script<Asteroid> {

		private FrustumIntersection frustumIntersection = new FrustumIntersection()
		private boolean visible = false
		private Vector2f splitPosition1 = new Vector2f()
		private Vector2f splitPosition2 = new Vector2f()
		private Vector3f splitRotation1 = new Vector3f()
		private Vector3f splitRotation2 = new Vector3f()

		@Override
		void init() {

			var scene = node.scene as AsteroidsScene

			frustumIntersection.set(scene.camera.viewProjection.scale(0.8f, new Matrix4f()), false)

			node.findByType(CircleCollider).on(CollisionEvent) { event ->
				var otherObject = event.otherObject().parent

				if (otherObject instanceof Bullet) {
					if (node.size == Size.LARGE || node.size == Size.MEDIUM) {
						logger.debug('{} collided with bullet - splitting', node.name)
						scene.queueUpdate { ->
							var newSize = node.size == Size.LARGE ? Size.MEDIUM : Size.SMALL
							scene.addChild(
								new Asteroid(newSize, splitPosition1.set(node.position).add(-4f, 0f),
									node.getRotation().add(0f, 0f, Math.toRadians(Math.random() * 90) as float, splitRotation1).z)
									.withName("Asteroid ${Asteroid.count++} (${newSize.name().toLowerCase()})"))
							scene.addChild(
								new Asteroid(newSize, splitPosition2.set(node.position).add(4f, 0f),
									node.getRotation().add(0f, 0f, Math.toRadians(Math.random() * -90) as float, splitRotation2).z)
									.withName("Asteroid ${Asteroid.count++} (${newSize.name().toLowerCase()})"))
						}
					}
					else {
						logger.debug('{} collided with bullet - destroying', node.name)
					}

					scene.queueUpdate { ->
						node.parent.removeChild(node)
						node.close()
					}
					node.trigger(new AsteroidDestroyedEvent(node))
				}
			}
		}

		@Override
		void update(float delta) {

			// Remove asteroids that have left the playing field
			var lastVisible = visible
			var nowVisible = frustumIntersection.testPoint(node.position)
			if (lastVisible && !nowVisible) {
				node.scene.queueUpdate { ->
					node.parent.removeChild(node)
					node.close()
				}
				return
			}
			else if (!lastVisible && nowVisible) {
				visible = true
			}

			// Keep moving along
			var speed = baseSpeed * (node.size == Size.LARGE ? 1f : node.size == Size.MEDIUM ? 3f : 12f)
			node.translate(0f, speed * delta as float)
		}
	}
}
