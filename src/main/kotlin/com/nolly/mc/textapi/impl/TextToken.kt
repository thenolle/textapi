package com.nolly.mc.textapi.impl

sealed interface TextToken {
	data class Text(val value: String) : TextToken
	data class OpenTag(val name: String, val arguments: String?): TextToken
	data class CloseTag(val name: String): TextToken
	data class Placeholder(val name: String): TextToken
}
