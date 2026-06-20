package com.nolly.mc.textapi.api

import com.nolly.mc.textapi.impl.TextTag
import org.bukkit.entity.Player

object TextAPI {
	private lateinit var service: TextService

	internal fun initialize(service: TextService) {
		this.service = service
	}

	fun parse(input: String, player: Player? = null, overrides: Map<String, String> = emptyMap()) = service.parse(input, player, overrides)
	fun components(input: String, player: Player? = null, overrides: Map<String, String> = emptyMap()) = service.components(input, player, overrides)
	fun tokens(input: String) = service.tokens(input)

	fun send(player: Player, input: String) = service.send(player, input)

	fun registeredPlaceholders() = service.registeredPlaceholders()
	fun registerPlaceholder(key: String, resolver: (Player?) -> String?) = service.registerPlaceholder(key, resolver)
	fun unregisterPlaceholder(key: String) = service.unregisterPlaceholder(key)

	fun registeredTags() = service.registeredTags()
	fun registerTag(tag: String, handler: TextTag.TagHandler) = service.registerTag(tag, handler)
	fun unregisterTag(tag: String) = service.unregisterTag(tag)

	fun registeredGradient() = service.registeredGradient()
	fun registerGradient(name: String, stops: List<String>) = service.registerGradient(name, stops)
	fun unregisterGradient(name: String) = service.unregisterGradient(name)
}
