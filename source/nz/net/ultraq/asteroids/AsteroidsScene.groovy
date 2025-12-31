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

import nz.net.ultraq.asteroids.objects.AsteroidSpawner
import nz.net.ultraq.asteroids.objects.Lives
import nz.net.ultraq.asteroids.objects.Player
import nz.net.ultraq.asteroids.objects.Score
import nz.net.ultraq.redhorizon.engine.Entity
import nz.net.ultraq.redhorizon.engine.graphics.CameraEntity
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsComponent
import nz.net.ultraq.redhorizon.engine.graphics.MeshComponent
import nz.net.ultraq.redhorizon.engine.graphics.imgui.ImGuiComponent
import nz.net.ultraq.redhorizon.engine.graphics.imgui.ImGuiDebugComponent
import nz.net.ultraq.redhorizon.engine.graphics.imgui.LogPanel
import nz.net.ultraq.redhorizon.engine.graphics.imgui.NodeList
import nz.net.ultraq.redhorizon.engine.physics.CircleCollisionComponent
import nz.net.ultraq.redhorizon.engine.physics.CollisionComponent
import nz.net.ultraq.redhorizon.engine.scripts.GameLogicComponent
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.Framebuffer
import nz.net.ultraq.redhorizon.graphics.Mesh.Type
import nz.net.ultraq.redhorizon.graphics.Vertex
import nz.net.ultraq.redhorizon.graphics.Window
import nz.net.ultraq.redhorizon.graphics.imgui.DebugOverlay
import nz.net.ultraq.redhorizon.graphics.opengl.BasicShader
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLFramebuffer
import nz.net.ultraq.redhorizon.scenegraph.Scene
import static nz.net.ultraq.asteroids.ScopedValues.*

import imgui.ImFontConfig
import imgui.ImGui
import org.joml.Vector3f

/**
 * Scene setup for the Asteroids game.
 *
 * @author Emanuel Rabina
 */
class AsteroidsScene extends Scene implements AutoCloseable {

	static final int WIDTH = 1920
	static final int HEIGHT = 1440

	final CameraEntity camera
	final Player player
	boolean showCollisionLines = false
	private final Window window
	private final BasicShader shader
	private Framebuffer framebuffer
	private final List<CollisionComponent> collisionComponents = new ArrayList<>()
	private final List<GameLogicComponent> gameLogicComponents = new ArrayList<>()
	private final List<GraphicsComponent> graphicsComponents = new ArrayList<>()
	private final List<ImGuiComponent> imguiComponents = new ArrayList<>()
	private final Queue<Closure> changeQueue = new ArrayDeque<>()

	/**
	 * Constructor, create a new scene to the given dimensions.
	 */
	AsteroidsScene() {

		window = WINDOW.get()
		framebuffer = new OpenGLFramebuffer(WIDTH, HEIGHT)
		shader = new BasicShader()

		camera = new CameraEntity(WIDTH, HEIGHT, window)
		player = new Player()
		addChild(camera)
		addChild(player)
		addChild(new AsteroidSpawner())

		var io = ImGui.getIO()
		var imFontConfig = new ImFontConfig()
		var squareFont = getResourceAsStream('nz/net/ultraq/asteroids/assets/Square.ttf').withCloseable { stream ->
			return io.fonts.addFontFromMemoryTTF(stream.bytes, 16, imFontConfig)
		}
		imFontConfig.destroy()

		addChild(new Lives(squareFont))
		addChild(new Score(squareFont))

		var debugOverlayComponent = new ImGuiDebugComponent(new DebugOverlay()
			.withCursorTracking(camera.camera, camera.transform, window)).disable()
		var nodeListComponent = new ImGuiDebugComponent(new NodeList(this)).disable()
		var logPanelComponent = new ImGuiDebugComponent(new LogPanel()).disable()
		addChild(new Entity()
			.addComponent(debugOverlayComponent)
			.addComponent(nodeListComponent)
			.addComponent(logPanelComponent)
			.withName('Debug UI'))

		var debugLinesBinding = new DebugLinesBinding(this)
		var debugEverythingBinding = new DebugEverythingBinding(
			[debugOverlayComponent, nodeListComponent, logPanelComponent], debugLinesBinding)
		var inputEventHandler = INPUT_EVENT_HANDLER.get()
		inputEventHandler
			.addImGuiDebugBindings([debugOverlayComponent], [nodeListComponent, logPanelComponent])
			.addInputBinding(debugLinesBinding)
			.addInputBinding(debugEverythingBinding)
	}

	/**
	 * Perform collision checks between all entities in the scene.
	 */
	void checkCollisions() {

		// TODO: Yet another ECS system part
		collisionComponents.clear()
		traverse(Entity) { Entity entity ->
			entity.findComponentsByType(CollisionComponent, collisionComponents)
		}
		for (var i = 0; i < collisionComponents.size(); i++) {
			var collision = collisionComponents.get(i)
			if (!collision.enabled) {
				continue
			}
			for (var j = i + 1; j < collisionComponents.size(); j++) {
				var otherCollision = collisionComponents.get(j)
				if (!otherCollision.enabled) {
					continue
				}
				collision.checkCollision(otherCollision)
			}
		}
	}

	@Override
	void close() {

		traverse { node ->
			if (node instanceof AutoCloseable) {
				node.close()
			}
		}
	}

	/**
	 * Apply modifications made by other steps in the game loop.
	 */
	void processQueuedChanges() {

		while (changeQueue) {
			changeQueue.poll().call()
		}
	}

	/**
	 * Queue some scene modification to be performed at the end of the current
	 * update cycle.
	 */
	void queueChange(Closure change) {

		changeQueue.add(change)
	}

	/**
	 * Draw out all the graphical components of the scene.
	 */
	void render() {

		// TODO: Similar to the update method, these look like they should be the "S" part of ECS
		graphicsComponents.clear()
		imguiComponents.clear()
		traverse(Entity) { Entity entity ->
			entity.findComponentsByType(GraphicsComponent, graphicsComponents)
			entity.findComponentsByType(ImGuiComponent, imguiComponents)
		}

		window.useRenderPipeline()
			.scene { ->
				framebuffer.useFramebuffer { ->
					shader.useShader { shaderContext ->
						camera.render(shaderContext)
						graphicsComponents.each { component ->
							if (component.enabled) {
								component.render(shaderContext)
							}
						}
					}
				}
				return framebuffer
			}
			.ui(true) { context ->
				imguiComponents.each { component ->
					if (component.enabled) {
						component.render(context)
					}
				}
			}
			.end()
	}

	/**
	 * Perform a scene update in the game loop.
	 */
	void update(float delta) {

		// TODO: Similar to the render method, these look like they should be the "S" part of ECS
		gameLogicComponents.clear()
		traverse(Entity) { Entity entity ->

			// Manage collision outlines
			var collision = entity.findComponentByType(CircleCollisionComponent) as CircleCollisionComponent
			var collisionOutline = entity.findComponent { it.name == 'Collision outline' } as MeshComponent
			if (showCollisionLines) {
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
								.withName('Collision outline')
						)
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

			entity.findComponentsByType(GameLogicComponent, gameLogicComponents)
		}
		gameLogicComponents.each { component ->
			if (component.enabled) {
				component.update(delta)
			}
		}
	}
}
