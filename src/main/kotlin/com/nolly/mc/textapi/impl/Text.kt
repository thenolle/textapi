package com.nolly.mc.textapi.impl

import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.BaseComponent
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object Text {
	private val parser = TextParser()
	private val renderer = TextRenderer()

	fun tokens(input: String): List<TextToken> = parser.parse(input)
	fun components(input: String, player: Player? = null, overrides: Map<String, String> = emptyMap()): Array<BaseComponent> {
		val context = TextContext.build(player, overrides)
		return renderer.renderComponents(parser.parse(input), context)
	}

	fun parse(input: String, player: Player? = null, overrides: Map<String, String> = emptyMap()): String {
		val context = TextContext.build(player, overrides)
		return renderer.render(parser.parse(input), context)
	}

	fun broadcast(input: String) {
		Bukkit.getOnlinePlayers().forEach { send(it, input) }
	}
	fun broadcast(input: String, overrides: Map<String, String>) {
		Bukkit.getOnlinePlayers().forEach { send(it, input, overrides) }
	}

	fun send(target: Player, input: String) {
		target.spigot().sendMessage(*components(input, target))
	}
	fun send(target: Player, input: String, overrides: Map<String, String>) {
		target.spigot().sendMessage(*components(input, target, overrides))
	}
	fun send(target: CommandSender, input: String) {
		if (target is Player) return send(target, input)
		target.spigot().sendMessage(*components(input, null))
	}
	fun send(target: CommandSender, input: String, overrides: Map<String, String>) {
		if (target is Player) return send(target, input, overrides)
		target.spigot().sendMessage(*components(input, null, overrides))
	}

	fun actionbar(target: Player, input: String) {
		target.spigot().sendMessage(ChatMessageType.ACTION_BAR, *components(input, target))
	}
	fun actionbar(target: Player, input: String, overrides: Map<String, String>) {
		target.spigot().sendMessage(ChatMessageType.ACTION_BAR, *components(input, target, overrides))
	}

	fun title(target: Player, input: String, subtitle: String? = null, fadein: Int = 0, stay: Int = 70, fadeout: Int = 0) {
		val parsedTitle = parse(input, target)
		val parsedSubtitle = subtitle?.let { parse(it, target) }
		target.sendTitle(parsedTitle, parsedSubtitle, fadein, stay, fadeout)
	}
	fun title(target: Player, input: String, 	subtitle: String? = null, fadein: Int = 0, stay: Int = 70, fadeout: Int = 0, overrides: Map<String, String>, subtitleOverrides: Map<String, String>) {
		val parsedTitle = parse(input, target, overrides)
		val parsedSubtitle = subtitle?.let { parse(it, target, subtitleOverrides) }
		target.sendTitle(parsedTitle, parsedSubtitle, fadein, stay, fadeout)
	}
}
