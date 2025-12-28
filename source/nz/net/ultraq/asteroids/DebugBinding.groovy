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

import nz.net.ultraq.asteroids.engine.CircleCollisionComponent
import nz.net.ultraq.redhorizon.engine.Entity
import nz.net.ultraq.redhorizon.engine.graphics.GridLinesEntity
import nz.net.ultraq.redhorizon.engine.graphics.MeshComponent
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.Mesh.Type
import nz.net.ultraq.redhorizon.graphics.Vertex
import nz.net.ultraq.redhorizon.graphics.Window
import nz.net.ultraq.redhorizon.input.InputBinding
import nz.net.ultraq.redhorizon.input.InputEventHandler

import org.joml.Vector3f
import org.joml.primitives.Rectanglef
import static org.lwjgl.glfw.GLFW.GLFW_KEY_P

import groovy.transform.TupleConstructor

/**
 * Input binding for toggling all debug information with the {@code P} key.
 * This will toggle:
 * <ul>
 *   <li>grid lines</li>
 *   <li>collision outlines</li>
 *   <li>all ImGui debug components</li>
 * </ul>
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class DebugBinding implements InputBinding {

	private static final String COLLISION_OUTLINE_NAME = 'Collision outline'

	final AsteroidsScene scene
	final Window window

	private boolean debug = false

	@Override
	void process(InputEventHandler input) {

		if (input.keyPressed(GLFW_KEY_P, true)) {
			debug = !debug

			window.toggleImGuiDebugOverlays()
			window.toggleImGuiDebugWindows()

			// Manage scene grid lines
			var gridLines = scene.findDescendent { it instanceof GridLinesEntity } as GridLinesEntity
			if (debug) {
				if (!gridLines) {
					scene.insertBefore(
						new GridLinesEntity(new Rectanglef(0f, 0f, scene.WIDTH, scene.HEIGHT).center(), 50f, Colour.RED, Colour.GREY)
							.withName('Grid lines'),
						scene.player)
				}
				else {
					gridLines.enable()
				}
			}
			else {
				if (gridLines) {
					gridLines.disable()
				}
			}
		}

		// Manage collision outlines
		scene.traverse(Entity) { Entity entity ->
			var collision = entity.findComponentByType(CircleCollisionComponent) as CircleCollisionComponent
			var collisionOutline = entity.findComponent { it.name == COLLISION_OUTLINE_NAME } as MeshComponent
			if (debug) {
				if (collision) {
					if (!collisionOutline) {
						var radius = collision.radius
						collisionOutline = entity.addAndReturnComponent(
							new MeshComponent(Type.LINE_LOOP, new Vertex[]{
								new Vertex(new Vector3f(-radius as float, -radius as float, 0), Colour.YELLOW),
								new Vertex(new Vector3f(radius as float, -radius as float, 0), Colour.YELLOW),
								new Vertex(new Vector3f(radius as float, radius as float, 0), Colour.YELLOW),
								new Vertex(new Vector3f(-radius as float, radius as float, 0), Colour.YELLOW)
							})
								.withName(COLLISION_OUTLINE_NAME))
					}
					if (collision.enabled) {
						collisionOutline.enable()
					}
					else {
						collisionOutline.disable()
					}
				}
			}
			else {
				if (collisionOutline) {
					collisionOutline.disable()
				}
			}
		}
	}
}
