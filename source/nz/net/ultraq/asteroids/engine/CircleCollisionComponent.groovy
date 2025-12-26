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

package nz.net.ultraq.asteroids.engine

import nz.net.ultraq.asteroids.AsteroidsScene
import nz.net.ultraq.redhorizon.engine.Entity
import nz.net.ultraq.redhorizon.engine.scripts.GameLogicComponent
import nz.net.ultraq.redhorizon.engine.scripts.ScriptComponent

import org.joml.primitives.Circlef
import org.joml.primitives.Intersectionf

/**
 * Give an entity a circular area which is used for detecting collisions with
 * other collision components.
 *
 * @author Emanuel Rabina
 */
class CircleCollisionComponent extends GameLogicComponent<CircleCollisionComponent> {

	final float radius
	boolean checked = false
	private final List<CircleCollisionComponent> otherCollisions = new ArrayList<>()

	/**
	 * Constructor, set the collision area with a radius.
	 */
	CircleCollisionComponent(float radius) {

		this.radius = radius
	}

	@Override
	void update(float delta) {

		if (checked) {
			checked = false
			return
		}

		var position = entity.position
		var bounds = new Circlef(position.x(), position.y(), radius)

		// Check this component against all others in the scene
		// TODO: Perform this more efficiently with something like a quad tree to
		//       reduce the number of checks to just those within a close area.
		var scene = entity.scene as AsteroidsScene
		otherCollisions.clear()
		scene.traverse { node ->
			if (node instanceof Entity && node != entity) {
				node.findComponentsByType(CircleCollisionComponent, otherCollisions)
			}
		}

		otherCollisions.each { other ->
			var otherPosition = other.entity.position
			var collision = Intersectionf.testCircleCircle(position.x(), position.y(), radius,
				otherPosition.x(), otherPosition.y(), other.radius)
			if (collision) {
				var otherBounds = new Circlef(otherPosition.x(), otherPosition.y(), other.radius)
				var scriptComponent = entity.findComponentByType(ScriptComponent) as ScriptComponent
				scriptComponent?.script?.onCollision(bounds, other.entity, otherBounds)

				var otherScriptComponent = other.entity.findComponentByType(ScriptComponent) as ScriptComponent
				otherScriptComponent?.script?.onCollision(otherBounds, entity, bounds)
				other.checked = true
			}
		}
	}
}
