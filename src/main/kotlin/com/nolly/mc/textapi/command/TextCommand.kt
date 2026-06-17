package com.nolly.mc.textapi.command

import com.nolly.mc.textapi.api.TextService
import com.nolly.mc.textapi.impl.TextPlaceholders
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class TextCommand(
	private val service: TextService
) : CommandExecutor, TabCompleter {

	override fun onCommand(
		sender: CommandSender,
		command: Command,
		label: String,
		args: Array<out String>
	): Boolean {

		if (!sender.hasPermission("com.nolly.mc.textapi.command")) {
			sender.sendMessage("§cNo permission.")
			return true
		}

		if (args.isEmpty()) {
			sendHelp(sender)
			return true
		}

		when (args[0].lowercase()) {

			// --------------------------------------------------
			// PARSE / TEST
			// --------------------------------------------------
			"parse" -> {
				val input = args.drop(1).joinToString(" ")
				if (input.isBlank()) {
					sender.sendMessage("§cUsage: /text parse <message>")
					return true
				}

				if (sender is Player) {
					service.send(sender, input)
				} else {
					sender.sendMessage(service.parse(input, null, emptyMap()))
				}
			}

			// --------------------------------------------------
			// COMPONENT DEBUG
			// --------------------------------------------------
			"components" -> {
				val input = args.drop(1).joinToString(" ")
				sender.sendMessage("§7Components preview (raw):")
				sender.sendMessage(service.components(input, sender as? Player, emptyMap()).contentToString())
			}

			// --------------------------------------------------
			// TOKENS DEBUG
			// --------------------------------------------------
			"tokens" -> {
				val input = args.drop(1).joinToString(" ")
				val tokens = service.tokens(input)

				sender.sendMessage("§7Tokens:")
				tokens.forEach {
					sender.sendMessage("§8- §f$it")
				}
			}

			// --------------------------------------------------
			// PLACEHOLDERS LIST
			// --------------------------------------------------
			"placeholders" -> {
				sender.sendMessage("§dTextAPI Placeholders:")

				TextPlaceholders.keys()
					.sorted()
					.forEach {
						sender.sendMessage("§8- §f{$it}")
					}
			}

			// --------------------------------------------------
			// REGISTER PLACEHOLDER
			// /text register key value...
			// --------------------------------------------------
			"register" -> {
				if (args.size < 3) {
					sender.sendMessage("§cUsage: /text register <key> <value>")
					return true
				}

				val key = args[1]
				val value = args.drop(2).joinToString(" ")

				service.registerPlaceholder(key) { _ -> value }

				sender.sendMessage("§aRegistered placeholder: §f{$key} = $value")
			}

			// --------------------------------------------------
			// UNREGISTER PLACEHOLDER
			// --------------------------------------------------
			"unregister" -> {
				if (args.size < 2) {
					sender.sendMessage("§cUsage: /text unregister <key>")
					return true
				}

				val key = args[1]
				service.unregisterPlaceholder(key)

				sender.sendMessage("§eUnregistered placeholder: §f{$key}")
			}

			// --------------------------------------------------
			// QUICK EXAMPLES
			// --------------------------------------------------
			"examples" -> {
				sendExamples(sender)
			}

			// --------------------------------------------------
			// HELP
			// --------------------------------------------------
			else -> sendHelp(sender)
		}

		return true
	}

	// --------------------------------------------------
	// HELP MENU
	// --------------------------------------------------
	private fun sendHelp(sender: CommandSender) {
		sender.sendMessage("§d§lTextAPI Commands")
		sender.sendMessage("§8/text parse <msg>")
		sender.sendMessage("§8/text components <msg>")
		sender.sendMessage("§8/text tokens <msg>")
		sender.sendMessage("§8/text placeholders")
		sender.sendMessage("§8/text register <key> <value>")
		sender.sendMessage("§8/text unregister <key>")
		sender.sendMessage("§8/text examples")
	}

	// --------------------------------------------------
	// EXAMPLES
	// --------------------------------------------------
	private fun sendExamples(sender: CommandSender) {

		sender.sendMessage("§d§lTextAPI Examples")

		sender.sendMessage("§7Colors:")
		sender.sendMessage("§f<red>Red</red> <gold>Gold</gold> <#ff4800>Hex</#ff4800>")

		sender.sendMessage("§7Gradient:")
		sender.sendMessage("§f<gradient:#ff0000:#00ff00>Gradient</gradient>")

		sender.sendMessage("§7Rainbow:")
		sender.sendMessage("§f<rainbow>Rainbow</rainbow>")

		sender.sendMessage("§7Hover:")
		sender.sendMessage("<hover:show_text:<red>Danger</red>>Hover Me</hover>")

		sender.sendMessage("§7Click:")
		sender.sendMessage("<click:run_command:/spawn>Run /spawn</click>")

		sender.sendMessage("§7Placeholders:")
		sender.sendMessage("§fHello {player} - Online: {server_online}")
	}

	override fun onTabComplete(
		sender: CommandSender,
		command: Command,
		alias: String,
		args: Array<out String>
	): List<String> {

		if (!sender.hasPermission("com.nolly.mc.textapi.command")) {
			return emptyList()
		}

		// -----------------------------
		// ROOT SUBCOMMANDS
		// -----------------------------
		if (args.size == 1) {
			return listOf(
				"parse",
				"components",
				"tokens",
				"placeholders",
				"register",
				"unregister",
				"examples"
			).filter { it.startsWith(args[0], ignoreCase = true) }
		}

		val sub = args[0].lowercase()

		// -----------------------------
		// PLACEHOLDERS LIST
		// -----------------------------
		if (sub == "placeholders") {
			return TextPlaceholders.keys()
				.filter { it.startsWith(args.last(), ignoreCase = true) }
				.sorted()
		}

		// -----------------------------
		// REGISTER PLACEHOLDER
		// /text register <key> <value>
		// -----------------------------
		if (sub == "register") {
			return when (args.size) {
				2 -> listOf("key_name", "rank", "prefix")
					.filter { it.startsWith(args[1], ignoreCase = true) }

				3 -> listOf("value_here", "Admin", "VIP", "Player")
					.filter { it.startsWith(args[2], ignoreCase = true) }

				else -> emptyList()
			}
		}

		// -----------------------------
		// UNREGISTER PLACEHOLDER
		// -----------------------------
		if (sub == "unregister") {
			return TextPlaceholders.keys()
				.filter { it.startsWith(args.last(), ignoreCase = true) }
				.sorted()
		}

		// -----------------------------
		// PARSE / COMPONENTS / TOKENS
		// (suggest tags + placeholders)
		// -----------------------------
		if (sub in listOf("parse", "components", "tokens")) {

			val suggestions = mutableListOf<String>()

			// placeholders
			suggestions += TextPlaceholders.keys().map { "{${it}}" }

			// common tags
			suggestions += listOf(
				"<red>",
				"<gold>",
				"<gradient:#ff0000:#00ff00>",
				"<rainbow>",
				"<bold>",
				"<italic>",
				"<hover:show_text:>",
				"<click:run_command:/spawn>"
			)

			val last = args.last()

			return suggestions
				.filter { it.startsWith(last, ignoreCase = true) }
				.sorted()
		}

		// -----------------------------
		// DEFAULT FALLBACK
		// -----------------------------
		return emptyList()
	}
}
