package com.nolly.mc.textapi.impl

internal class TextParser {
	fun parse(input: String): List<TextToken> {
		val tokens = mutableListOf<TextToken>()
		val buffer = StringBuilder()
		fun flush() {
			if (buffer.isNotEmpty()) {
				tokens += TextToken.Text(buffer.toString())
				buffer.setLength(0)
			}
		}
		var i = 0
		while (i < input.length) {
			val c = input[i]
			if (c == '\\' && i + 1 < input.length) {
				buffer.append(input[i + 1])
				i += 2
				continue
			}
			if (c == '{') {
				val close = input.indexOf('}', i + 1)
				if (close == -1) {
					buffer.append(c)
					i++
					continue
				}
				val key = input.substring(i + 1, close).trim()
				if (key.isEmpty()) {
					buffer.append(input.substring(i, close + 1))
					i = close + 1
					continue
				}
				flush()
				tokens += TextToken.Placeholder(key)
				i = close + 1
				continue
			}
			if (c == '<') {
				val close = input.indexOf('>', i + 1)
				if (close == -1) {
					buffer.append(c)
					i++
					continue
				}
				val raw = input.substring(i + 1, close).trim()
				if (raw.isEmpty()) {
					buffer.append(input.substring(i, close + 1))
					i = close + 1
					continue
				}
				flush()
				when {
					raw.startsWith("/") -> {
						tokens += TextToken.CloseTag(raw.substring(1).trim())
					}
					raw.contains(":") -> {
						val split = raw.indexOf(":")
						tokens += TextToken.OpenTag(
							raw.substring(0, split).trim(),
							raw.substring(split + 1).trim()
						)
					}
					raw.startsWith("#") -> {
						tokens += TextToken.OpenTag(raw, null)
					}
					raw.startsWith("!") -> {
						tokens += TextToken.OpenTag(raw, null)
					}
					isKnownTag(raw) -> {
						tokens += TextToken.OpenTag(raw, null)
					}
					else -> {
						tokens += TextToken.Placeholder(raw)
					}
				}
				i = close + 1
				continue
			}
			buffer.append(c)
			i++
		}
		flush()
		return tokens
	}

	private fun isKnownTag(raw: String): Boolean {
		val n = raw.trim().lowercase()
		return n in knownTags || TextTag.isRegistered(n) || TextTag.isColorTag(n) || TextTag.isDecorationTag(n) || TextTag.isGradientAlias(n)
	}

	private val knownTags: Set<String> = setOf(
		"reset",
		"newline", "br",
		"gradient",
		"rainbow",
		"color", "colour", "c",
		"click", "hover", 	"insert",
		"bold", "b",
		"italic", "em", "i",
		"underlined", "underline", "u",
		"strikethrough", "st",
		"obfuscated", "obf", "magic"
	)
}
