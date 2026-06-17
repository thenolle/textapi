package com.nolly.mc.textapi

import com.nolly.mc.textapi.api.TextAPI
import com.nolly.mc.textapi.command.TextCommand
import com.nolly.mc.textapi.impl.TextServiceImpl
import com.nolly.mc.textapi.impl.TextTPS
import org.bukkit.plugin.java.JavaPlugin

class TextAPIPlugin : JavaPlugin() {
	companion object {
		lateinit var instance: TextAPIPlugin private set
	}

	override fun onEnable() {
		instance = this
		saveDefaultConfig()
		val service = TextServiceImpl()
		TextAPI.initialize(service)
		if (config.getBoolean("command.enabled", true)) {
			val command = TextCommand(service)
			val cmd = getCommand("textapi")
			if (cmd != null) {
				cmd.setExecutor(command)
				cmd.tabCompleter = command
			} else logger.warning("Command 'textapi' not found in plugin.yml")
		}
		TextTPS.init()
		logger.info("TextAPI enabled")
	}

	override fun onDisable() {
		logger.info("TextAPI disabled")
	}
}
