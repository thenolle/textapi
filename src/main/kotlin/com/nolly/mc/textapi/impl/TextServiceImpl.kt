package com.nolly.mc.textapi.impl

import com.nolly.mc.textapi.api.TextService
import net.md_5.bungee.api.chat.BaseComponent
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class TextServiceImpl : TextService {
	override fun parse(input: String, player: Player?, overrides: Map<String, String>): String = Text.parse(input, player, overrides)
	override fun components(input: String, player: Player?, overrides: Map<String, String>): Array<BaseComponent> = Text.components(input, player, overrides)
	override fun tokens(input: String): List<TextToken> = Text.tokens(input)

	override fun broadcast(input: String) = Text.broadcast(input)
	override fun broadcast(input: String, overrides: Map<String, String>) = Text.broadcast(input, overrides)

	override fun send(player: Player, input: String) = Text.send(player, input)
	override fun send(player: Player, input: String, overrides: Map<String, String>) = Text.send(player, input, overrides)
	override fun send(player: CommandSender, input: String) = Text.send(player, input)
	override fun send(player: CommandSender, input: String, overrides: Map<String, String>) = Text.send(player, input, overrides)

	override fun actionbar(player: Player, input: String) = Text.actionbar(player, input)
	override fun actionbar(player: Player, input: String, overrides: Map<String, String>) = Text.actionbar(player, input, overrides)

	override fun title(player: Player, input: String, subtitle: String?, fadein: Int, stay: Int, fadeout: Int) = Text.title(player, input, subtitle, fadein, stay, fadeout)
	override fun title(player: Player, input: String, subtitle: String?, fadein: Int, stay: Int, fadeout: Int, overrides: Map<String, String>, subtitleOverrides: Map<String, String>) = Text.title(player, input, subtitle, fadein, stay, fadeout, overrides, subtitleOverrides)

	override fun registeredPlaceholders() = TextPlaceholders.registered()
	override fun registerPlaceholder(key: String, resolver: (Player?) -> String?) = TextPlaceholders.register(key) { p -> resolver(p) }
	override fun unregisterPlaceholder(key: String) = TextPlaceholders.unregister(key)

	override fun registeredTags() = TextTag.registeredTags()
	override fun registerTag(tag: String, handler: TextTag.TagHandler) = TextTag.register(tag, handler)
	override fun unregisterTag(tag: String) = TextTag.unregister(tag)

	override fun registeredGradient() = TextTag.registeredGradients()
	override fun registerGradient(name: String, stops: List<String>) = TextTag.registerGradient(name, stops)
	override fun unregisterGradient(name: String) = TextTag.unregisterGradient(name)
}
