package com.nolly.mc.textapi.impl

import org.bukkit.entity.Player

internal data class TextContext(val values: Map<String, String> = emptyMap()) {
	fun resolve(key: String): String? = values[key.trim().lowercase()]

	companion object {
		fun build(player: Player?, overrides: Map<String, String> = emptyMap()): TextContext {
			val resolved = TextPlaceholders.keys().associateWith { key -> TextPlaceholders.resolve(key, player) ?: "" }.toMutableMap()
			overrides.forEach { (k, v) -> resolved[k.trim().lowercase()] = v }
			return TextContext(resolved)
		}
	}
}
