package com.nolly.mc.textapi.api

import com.nolly.mc.textapi.impl.TextTag
import com.nolly.mc.textapi.impl.TextToken
import net.md_5.bungee.api.chat.BaseComponent
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

interface TextService {
	fun parse(input: String, player: Player?, overrides: Map<String, String>): String
	fun components(input: String, player: Player?, overrides: Map<String, String>): Array<BaseComponent>
	fun tokens(input: String): List<TextToken>

	fun broadcast(input: String)
	fun broadcast(input: String, overrides: Map<String, String>)

	fun send(player: Player, input: String)
	fun send(player: Player, input: String, overrides: Map<String, String>)
	fun send(player: CommandSender, input: String)
	fun send(player: CommandSender, input: String, overrides: Map<String, String>)

	fun actionbar(player: Player, input: String)
	fun actionbar(player: Player, input: String, overrides: Map<String, String>)

	fun title(player: Player, input: String, subtitle: String? = null, fadein: Int = 0, stay: Int = 70, fadeout: Int = 0)
	fun title(player: Player, input: String, subtitle: String? = null, fadein: Int = 0, stay: Int = 70, fadeout: Int = 0, overrides: Map<String, String>, subtitleOverrides: Map<String, String>)

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
