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

import org.joml.primitives.Rectanglef

/**
 * Give an entity a 2D area which is used for detecting collisions with other
 * collision components.
 *
 * @author Emanuel Rabina
 */
class BoxCollisionComponent extends GameLogicComponent<BoxCollisionComponent> {

	final float width
	final float height
	final Rectanglef bounds
	private List<BoxCollisionComponent> otherCollisions = new ArrayList<>()

	/**
	 * Constructor, set the collision area from width/height values.
	 */
	BoxCollisionComponent(float width, float height) {

		this.width = width
		this.height = height
		bounds = new Rectanglef(0, 0, width, height).center()
	}

	@Override
	void update(float delta) {

		var position = entity.position
		bounds.center().translate(position.x(), position.y())

		// Check this component against all others in the scene
		// TODO: Perform this more efficiently with something like a quad tree to
		//       reduce the number of checks to just those within a close area.
		var scene = entity.scene as AsteroidsScene
		otherCollisions.clear()
		scene.traverse { node ->
			if (node instanceof Entity && node != entity) {
				node.findComponentsByType(BoxCollisionComponent, otherCollisions)
			}
		}

		otherCollisions.each { other ->
			var otherPosition = other.entity.position
			var otherBounds = new Rectanglef(0, 0, other.width, other.height)
				.center()
				.translate(otherPosition.x(), otherPosition.y())

			// If a collision is detected, notify the script attached to the entity
			if (bounds.intersectsRectangle(otherBounds)) {
				var scriptComponent = entity.findComponentByType(ScriptComponent) as ScriptComponent
				scriptComponent?.script?.onCollision(bounds, other.entity, otherBounds)
			}
		}
	}
}
