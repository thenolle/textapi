package com.nolly.mc.textapi.api

import com.nolly.mc.textapi.impl.TextTag
import com.nolly.mc.textapi.impl.TextToken
import net.md_5.bungee.api.chat.BaseComponent
import org.bukkit.entity.Player

interface TextService {
	fun parse(input: String, player: Player?, overrides: Map<String, String>): String
	fun components(input: String, player: Player?, overrides: Map<String, String>): Array<BaseComponent>
	fun tokens(input: String): List<TextToken>

	fun send(player: Player, input: String)

	fun registeredPlaceholders(): Set<String>
	fun registerPlaceholder(key: String, resolver: (Player?) -> String?)
	fun unregisterPlaceholder(key: String)

	fun registeredTags(): Set<String>
	fun registerTag(tag: String, handler: TextTag.TagHandler)
	fun unregisterTag(tag: String)

	fun registeredGradient(): Set<String>
	fun registerGradient(name: String, stops: List<String>)
	fun unregisterGradient(name: String)
}
