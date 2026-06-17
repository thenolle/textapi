package com.nolly.mc.textapi.api

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

	fun registerPlaceholder(key: String, resolver: (Player?) -> String?) = service.registerPlaceholder(key, resolver)
	fun unregisterPlaceholder(key: String) = service.unregisterPlaceholder(key)
}
