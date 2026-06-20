package com.nolly.mc.textapi.impl

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent

data class TextStyle(
	val color: ChatColor? = null,
	val bold: Boolean = false,
	val italic: Boolean = false,
	val underlined: Boolean = false,
	val strikethrough: Boolean = false,
	val obfuscated: Boolean = false,
	val clickEvent: ClickEvent? = null,
	val hoverEvent: HoverEvent? = null,
	val insertion: String? = null
)
