package com.nolly.mc.textapi.impl

import com.nolly.mc.textapi.api.TextService
import net.md_5.bungee.api.chat.BaseComponent
import org.bukkit.entity.Player

class TextServiceImpl : TextService {
	override fun parse(
		input: String,
		player: Player?,
		overrides: Map<String, String>
	): String {
		return Text.parse(input, player, overrides)
	}

	override fun components(
		input: String,
		player: Player?,
		overrides: Map<String, String>
	): Array<BaseComponent> {
		return Text.components(input, player, overrides)
	}

	override fun tokens(input: String): List<TextToken> {
		return Text.tokens(input)
	}

	override fun send(player: Player, input: String) {
		Text.send(player, input)
	}

	override fun registerPlaceholder(
		key: String,
		resolver: (Player?) -> String?
	) {
		TextPlaceholders.register(key) { p ->
			resolver(p)
		}
	}

	override fun unregisterPlaceholder(key: String) {
		TextPlaceholders.unregister(key)
	}

	override fun registerTag(tag: String) {
		TextTag.register(tag)
	}

	override fun unregisterTag(tag: String) {
		TextTag.unregister(tag)
	}
}
