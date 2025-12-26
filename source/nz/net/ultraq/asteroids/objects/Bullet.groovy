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
import nz.net.ultraq.asteroids.engine.BoxCollisionComponent
import nz.net.ultraq.redhorizon.engine.Entity
import nz.net.ultraq.redhorizon.engine.graphics.MeshComponent
import nz.net.ultraq.redhorizon.engine.graphics.SpriteComponent
import nz.net.ultraq.redhorizon.engine.scripts.EntityScript
import nz.net.ultraq.redhorizon.engine.scripts.ScriptComponent
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.Mesh.Type
import nz.net.ultraq.redhorizon.graphics.Vertex
import nz.net.ultraq.redhorizon.graphics.opengl.BasicShader
import static nz.net.ultraq.asteroids.ScopedValues.*

import org.joml.Matrix4fc
import org.joml.Vector2fc
import org.joml.Vector3f
import org.joml.primitives.Rectanglef
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.atomic.AtomicInteger

/**
 * Pew pew.
 *
 * @author Emanuel Rabina
 */
class Bullet extends Entity<Bullet> {

	static final float bulletSpeed = 800f
	static final float bulletLifetime = 1.2f
	private static final Logger logger = LoggerFactory.getLogger(Bullet)
	private static final AtomicInteger count = new AtomicInteger(1)

	final Vector2fc initialVelocity

	/**
	 * Constructor, set up the bullet entity.
	 */
	Bullet(Matrix4fc initialTransform, Vector2fc initialVelocity) {

		var resourceManager = RESOURCE_MANAGER.get()
		var scriptEngine = SCRIPT_ENGINE.get()

		transform.set(initialTransform).translate(0f, 32f, 0f) // Start slightly ahead of the object
		this.initialVelocity = initialVelocity // Include ship velocity for moving bullets

		var bulletImage = resourceManager.loadImage('Square.png')
		var width = bulletImage.width
		var height = bulletImage.height
		addComponent(new SpriteComponent(bulletImage, BasicShader))
		addComponent(new BoxCollisionComponent(width, height))
		addComponent(new ScriptComponent(scriptEngine, BulletScript))

		// TODO: Some debug flag to show collision lines so we don't have to program these in
		addComponent(new MeshComponent(Type.LINE_LOOP, new Vertex[]{
			new Vertex(new Vector3f(-width / 2, -height / 2, 0), Colour.YELLOW),
			new Vertex(new Vector3f(width / 2, -height / 2, 0), Colour.YELLOW),
			new Vertex(new Vector3f(width / 2, height / 2, 0), Colour.YELLOW),
			new Vertex(new Vector3f(-width / 2, height / 2, 0), Colour.YELLOW)
		}))

		withName("Bullet ${count.getAndIncrement()}")
	}

	/**
	 * Bullet behaviour script.
	 */
	static class BulletScript extends EntityScript<Bullet> {

		private float bulletTimer

		@Override
		void onCollision(Rectanglef bulletBounds, Entity otherEntity, Rectanglef otherBounds) {

			if (otherEntity instanceof Asteroid) {
				logger.debug('Bullet collided with {} - removing from scene', otherEntity.name)
				(entity.scene as AsteroidsScene).queueChange { ->
					entity.parent.removeChild(entity)
					entity.close()
				}
			}
		}

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
}
