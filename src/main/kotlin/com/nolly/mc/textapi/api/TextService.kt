package com.nolly.mc.textapi.api

import com.nolly.mc.textapi.impl.TextToken
import org.bukkit.entity.Player
import net.md_5.bungee.api.chat.BaseComponent

interface TextService {
	fun parse(input: String, player: Player?, overrides: Map<String, String>): String
	fun components(input: String, player: Player?, overrides: Map<String, String>): Array<BaseComponent>
	fun tokens(input: String): List<TextToken>
	fun send(player: Player, input: String)
	fun registerPlaceholder(key: String, resolver: (Player?) -> String?)
	fun unregisterPlaceholder(key: String)
}
