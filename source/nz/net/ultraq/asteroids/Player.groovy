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

/**
 * The player spaceship.
 *
 * @author Emanuel Rabina
 */
class Player extends Entity<Player> {

	final String name = 'Player'

	/**
	 * Constructor, set up the player entity.
	 */
	Player() {

		var resourceManager = RESOURCE_MANAGER.get()
		var scriptEngine = SCRIPT_ENGINE.get()

		var playerImage = resourceManager.loadImage('Player.png')
		addComponent(new SpriteComponent(playerImage, BasicShader)
			.translate(-playerImage.width / 2 as float, -playerImage.height / 2 as float, 0f))
		addComponent(new ScriptComponent(scriptEngine, 'PlayerScript'))
	}
}
