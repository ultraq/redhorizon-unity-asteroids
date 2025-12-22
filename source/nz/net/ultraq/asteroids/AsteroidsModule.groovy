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

import nz.net.ultraq.redhorizon.engine.scripts.ScriptEngine
import nz.net.ultraq.redhorizon.engine.utilities.ResourceManager
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.Window
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLWindow
import nz.net.ultraq.redhorizon.input.InputEventHandler

import com.google.inject.AbstractModule
import com.google.inject.Provides
import jakarta.inject.Singleton

/**
 * Guice injection module for the Asteroids game.
 *
 * @author Emanuel Rabina
 */
class AsteroidsModule extends AbstractModule {

	@Provides
	@Singleton
	InputEventHandler inputEventHandler(Window window) {
		return new InputEventHandler()
			.addInputSource(window)
	}

	@Provides
	@Singleton
	ResourceManager resourceManager() {
		return new ResourceManager('nz/net/ultraq/asteroids/assets/')
	}

	@Provides
	@Singleton
	ScriptEngine scriptEngine() {
		return new ScriptEngine('.')
	}

	@Provides
	@Singleton
	Window window() {
		return new OpenGLWindow(1920, 1080, 'Asteroids')
			.centerToScreen()
			.scaleToFit()
			.withBackgroundColour(Colour.BLACK)
			.withVSync(true)
	}
}
