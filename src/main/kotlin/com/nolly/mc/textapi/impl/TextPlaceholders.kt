package com.nolly.mc.textapi.impl

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object TextPlaceholders {
	fun interface Resolver {
		fun resolve(player: Player?): String?
	}

	private val registry = mutableMapOf<String, Resolver>()

	init {
		registerDefaults()
	}

	fun register(key: String, resolver: Resolver) {
		registry[key.trim().lowercase()] = resolver
	}

	fun unregister(key: String) {
		registry.remove(key.trim().lowercase())
	}

	fun registered(): Set<String> = registry.keys.toSet()

	fun resolve(key: String, player: Player?): String? = registry[key.trim().lowercase()]?.resolve(player)

	fun keys(): Set<String> = registry.keys.toSet()
	private fun registerDefaults() {
		// --- Player identity ---
		register("player") { p -> p?.name }
		register("player_name") { p -> p?.name }
		register("player_uuid") { p -> p?.uniqueId?.toString() }
		register("player_display") { p -> p?.displayName }
		register("player_world") { p -> p?.world?.name }
		register("player_x") { p -> p?.location?.blockX?.toString() }
		register("player_y") { p -> p?.location?.blockY?.toString() }
		register("player_z") { p -> p?.location?.blockZ?.toString() }
		register("player_ping") { p -> p?.ping?.toString() }
		register("player_gamemode") { p -> p?.gameMode?.name?.lowercase() }
		register("player_health") { p -> p?.health?.toInt()?.toString() }
		register("player_food") { p -> p?.foodLevel?.toString() }
		register("player_level") { p -> p?.level?.toString() }
		register("player_exp") { p -> "%.2f".format(p?.exp) }
		register("player_ip") { p -> p?.address?.address?.hostAddress }
		register("player_locale") { p -> p?.locale }
		register("player_online") { _ -> Bukkit.getOnlinePlayers().size.toString() }

		// --- Server ---
		register("server_name") { _ -> Bukkit.getServer().name }
		register("server_version") { _ -> Bukkit.getVersion() }
		register("server_motd") { _ -> Bukkit.getMotd() }
		register("server_online") { _ -> Bukkit.getOnlinePlayers().size.toString() }
		register("server_max") { _ -> Bukkit.getMaxPlayers().toString() }
		register("server_tps") { _ -> "%.2f".format(TextTPS.getTps()) }

		// --- Time ---
		register("time") { _ -> LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) }
		register("date") { _ -> LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) }
		register("datetime") { _ -> LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) }
		register("timestamp") { _ -> System.currentTimeMillis().toString() }

		// --- Misc ---
		register("newline") { _ -> "\n" }
		register("prefix") { _ -> "§8[§dDiversia§8]§r" }
	}
}
