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

import nz.net.ultraq.redhorizon.audio.AudioDevice
import nz.net.ultraq.redhorizon.audio.openal.OpenALAudioDevice
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.Window
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLWindow
import nz.net.ultraq.redhorizon.input.InputEventHandler

import org.slf4j.LoggerFactory
import picocli.CommandLine
import picocli.CommandLine.Command

import java.util.logging.Logger

/**
 * Entry point for the Asteroids game.
 *
 * @author Emanuel Rabina
 */
@Command(name = 'asteroids')
class AsteroidsGame implements Runnable {

	static {
		System.setProperty('org.lwjgl.system.stackSize', '20480')
	}

	static void main(String[] args) {
		System.exit(new CommandLine(new AsteroidsGame()).execute(args))
	}

	private static final Logger logger = LoggerFactory.getLogger(AsteroidsGame)
	private static final WINDOW_WIDTH = 640
	private static final WINDOW_HEIGHT = 480

	private Window window
	private AudioDevice audioDevice
	private InputEventHandler inputEventHandler

	@Override
	void run() {

		try {
			window = new OpenGLWindow(WINDOW_WIDTH, WINDOW_HEIGHT, 'Asteroids')
				.centerToScreen()
				.scaleToFit()
				.withBackgroundColour(Colour.BLACK)
				.withVSync(true)
			audioDevice = new OpenALAudioDevice()
				.withMasterVolume(0.5f)
			inputEventHandler = new InputEventHandler()
				.addInputSource(window)
		}
		finally {
			window?.close()
			audioDevice?.close()
		}
	}
}
