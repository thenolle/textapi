package com.nolly.mc.textapi.impl

import net.md_5.bungee.api.ChatColor
import java.awt.Color

object TextTag {
	fun interface TagHandler {
		fun apply(style: TextStyle, args: String?): TextStyle
	}

	private val customTagHandlers = mutableMapOf<String, TagHandler>()

	fun register(tag: String, handler: TagHandler) {
		customTagHandlers[normalize(tag)] = handler
	}

	fun unregister(tag: String) {
		customTagHandlers.remove(normalize(tag))
	}

	fun isRegistered(tag: String): Boolean = normalize(tag) in customTagHandlers

	fun resolveHandler(tag: String): TagHandler? = customTagHandlers[normalize(tag)]

	fun registeredTags(): Set<String> = customTagHandlers.keys.toSet()

	private val customGradients = mutableMapOf<String, List<String>>()

	fun registerGradient(name: String, stops: List<String>) {
		customGradients[normalize(name)] = stops
	}

	fun unregisterGradient(name: String) {
		customGradients.remove(normalize(name))
	}

	fun isCustomGradient(name: String): Boolean =
		normalize(name) in customGradients

	fun resolveCustomGradient(name: String): List<String>? =
		customGradients[normalize(name)]

	fun registeredGradients(): Set<String> = customGradients.keys.toSet()

	private val namedColors: Map<String, ChatColor> = mapOf(
		"black" to ChatColor.BLACK,
		"dark_blue" to ChatColor.DARK_BLUE,
		"dark_green" to ChatColor.DARK_GREEN,
		"dark_aqua" to ChatColor.DARK_AQUA,
		"dark_red" to ChatColor.DARK_RED,
		"dark_purple" to ChatColor.DARK_PURPLE,
		"gold" to ChatColor.GOLD,
		"gray" to ChatColor.GRAY,
		"grey" to ChatColor.GRAY,
		"dark_gray" to ChatColor.DARK_GRAY,
		"dark_grey" to ChatColor.DARK_GRAY,
		"blue" to ChatColor.BLUE,
		"green" to ChatColor.GREEN,
		"aqua" to ChatColor.AQUA,
		"red" to ChatColor.RED,
		"light_purple" to ChatColor.LIGHT_PURPLE,
		"yellow" to ChatColor.YELLOW,
		"white" to ChatColor.WHITE
	)

	val prideGradients: Map<String, List<String>> = mapOf(
		"pride" to listOf("#FF0018", "#FFA52C", "#FFFF41", "#008018", "#0000F9", "#86007D"),
		"trans" to listOf("#55CDFC", "#F7A8B8", "#FFFFFF", "#F7A8B8", "#55CDFC"),
		"bi" to listOf("#D60270", "#D60270", "#9B4F96", "#0038A8", "#0038A8"),
		"lesbian" to listOf("#D62900", "#FF9B55", "#FFFFFF", "#D461A6", "#A50062"),
		"nonbinary" to listOf("#FCF434", "#FFFFFF", "#9C59D1", "#2D2D2D"),
		"pan" to listOf("#FF1B8D", "#FF1B8D", "#FFD700", "#1BB3FF", "#1BB3FF"),
		"ace" to listOf("#000000", "#A4A4A4", "#FFFFFF", "#810081"),
		"aro" to listOf("#3DA542", "#A8D379", "#FFFFFF", "#A9A9A9", "#000000"),
		"aroace" to listOf("#e28c00", "#eccd00", "#ffffff", "#62aedc", "#203856"),
		"genderfluid" to listOf("#FF76A4", "#FFFFFF", "#C011D7", "#000000", "#2F3CBE"),
		"agender" to listOf("#000000", "#B9B9B9", "#FFFFFF", "#B8F483", "#FFFFFF", "#B9B9B9", "#000000"),
		"intersex" to listOf("#FFD800", "#FFD800", "#7902AA", "#7902AA", "#FFD800"),
		"polyam" to listOf("#F61CB9", "#07D569", "#1C92FF"),
		"demi" to listOf("#FFFFFF", "#FFFFFF", "#D2D2D2", "#810081"),
		"genderqueer" to listOf("#B57EDC", "#FFFFFF", "#4A8123"),
		"apogender" to listOf("#447d4b", "#a9f57a", "#000000", "#a9f57a", "#447d4b")
	)

	fun isGradientAlias(name: String): Boolean = normalize(name) in prideGradients || isCustomGradient(name)

	fun resolveColor(value: String): ChatColor? {
		val n = normalize(value)
		namedColors[n]?.let { return it }
		if (n.startsWith("#") && n.length == 7) {
			return runCatching { ChatColor.of(n) }.getOrNull()
		}
		return null
	}

	fun isColorTag(name: String): Boolean = normalize(name).let { namedColors.containsKey(it) }

	fun applyShadow(style: TextStyle, shadow: Color?): TextStyle {
		return style.copy(shadowColor = shadow)
	}

	fun resolveShadowColor(value: String, alpha: Float? = null): Color? {
		val n = normalize(value)
		val base = resolveColor(n) ?: return null
		val awt = base.color ?: return null
		val resolvedAlpha = when {
			alpha != null -> (alpha.coerceIn(0f, 1f) * 255f).toInt()
			awt.alpha != 255 -> awt.alpha
			else -> (0.25f * 255f).toInt()
		}
		return Color(awt.red, awt.green, awt.blue, resolvedAlpha)
	}

	fun transparentShadow(): Color = Color(0, 0, 0, 0)

	fun resolveKeybind(value: String): String? {
		val n = value.trim()
		if (n.isEmpty()) return null
		if (n.startsWith("key.")) return n
		return "key.$n"
	}

	fun parseTranslatable(args: String): Pair<String, List<String>>? {
		val parts = splitTopLevel(args)
		if (parts.isEmpty()) return null
		val key = parts.first().trim()
		if (key.isEmpty()) return null
		val withValues = parts.drop(1).map { unquote(it.trim()) }
		return key to withValues
	}

	private fun splitTopLevel(input: String): List<String> {
		val result = mutableListOf<String>()
		val sb = StringBuilder()
		var quote: Char? = null
		var i = 0
		while (i < input.length) {
			val c = input[i]
			when {
				quote != null -> {
					if (c == quote) quote = null else sb.append(c)
				}
				c == '\'' || c == '"' -> quote = c
				c == ':' -> {
					result.add(sb.toString())
					sb.setLength(0)
				}
				else -> sb.append(c)
			}
			i++
		}
		result.add(sb.toString())
		return result
	}


	private fun unquote(s: String): String {
		if (s.length >= 2) {
			val first = s.first()
			val last = s.last()
			if ((first == '\'' && last == '\'') || (first == '"' && last == '"')) {
				return s.substring(1, s.length - 1)
			}
		}
		return s
	}

	fun parseTranslatableOr(args: String): Triple<String, String, List<String>>? {
		val parts = splitTopLevel(args)
		if (parts.size < 2) return null
		val key = parts[0].trim()
		if (key.isEmpty()) return null
		val fallback = unquote(parts[1].trim())
		val withValues = parts.drop(2).map { unquote(it.trim()) }
		return Triple(key, fallback, withValues)
	}

	fun isColorAlias(name: String): Boolean = normalize(name) in setOf("color", "colour", "c")

	fun isDecorationTag(name: String): Boolean = normalize(name) in decorationAliases

	fun applyDecoration(style: TextStyle, name: String, enabled: Boolean): TextStyle = when (normalize(name)) {
		"bold", "b" -> style.copy(bold = enabled)
		"italic", "em", "i" -> style.copy(italic = enabled)
		"underlined", "underline", "u" -> style.copy(underlined = enabled)
		"strikethrough", "st" -> style.copy(strikethrough = enabled)
		"obfuscated", "obf", "magic" -> style.copy(obfuscated = enabled)
		else -> style
	}

	fun normalize(s: String): String = s.trim().lowercase()

	private val decorationAliases: Set<String> = setOf(
		"bold", "b",
		"italic", "em", "i",
		"underlined", "underline", "u",
		"strikethrough", "st",
		"obfuscated", "obf", "magic"
	)
}
