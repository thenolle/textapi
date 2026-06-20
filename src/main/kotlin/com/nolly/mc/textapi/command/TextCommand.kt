package com.nolly.mc.textapi.command

import com.nolly.mc.textapi.api.TextAPI
import com.nolly.mc.textapi.api.TextService
import com.nolly.mc.textapi.impl.TextTag
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class TextCommand(private val service: TextService) : CommandExecutor, TabCompleter {
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
			"parse" -> {
				val input = args.drop(1).joinToString(" ")
				if (input.isBlank()) {
					sender.sendMessage("§cUsage: /textapi parse <message>")
					return true
				}
				if (sender is Player) service.send(sender, input)
				else sender.sendMessage(service.parse(input, null, emptyMap()))
			}
			"components" -> {
				val input = args.drop(1).joinToString(" ")
				if (input.isBlank()) {
					sender.sendMessage("§cUsage: /textapi components <message>")
					return true
				}
				val components = service.components(input, sender as? Player, emptyMap())
				sender.sendMessage("§7Components §8(${components.size})§7:")
				components.forEachIndexed { i, c ->
					sender.sendMessage("§8[$i] §f${c.toPlainText()} §8| color=§f${c.color} §8bold=§f${c.isBold} §8italic=§f${c.isItalic}")
				}
			}
			"tokens" -> {
				val input = args.drop(1).joinToString(" ")
				if (input.isBlank()) {
					sender.sendMessage("§cUsage: /textapi tokens <message>")
					return true
				}
				val tokens = service.tokens(input)
				sender.sendMessage("§7Tokens §8(${tokens.size})§7:")
				tokens.forEachIndexed { i, t ->
					sender.sendMessage("§8[$i] §f$t")
				}
			}
			"placeholders" -> {
				val keys = TextAPI.registeredPlaceholders().sorted()
				sender.sendMessage("§dPlaceholders §8(${keys.size})§d:")
				keys.forEach { sender.sendMessage("§8- §f{$it}") }
			}
			"tags" -> {
				val tags = TextAPI.registeredTags().sorted()
				if (tags.isEmpty()) {
					sender.sendMessage("§7No custom tags registered.")
					return true
				}
				sender.sendMessage("§dCustom Tags §8(${tags.size})§d:")
				tags.forEach { sender.sendMessage("§8- §f<$it>") }
			}
			"gradients" -> {
				val gradients = TextAPI.registeredGradient().sorted()
				if (gradients.isEmpty()) {
					sender.sendMessage("§7No custom gradients registered.")
					return true
				}
				sender.sendMessage("§dCustom Gradients §8(${gradients.size})§d:")
				gradients.forEach { name ->
					if (sender is Player) {
						service.send(sender, "<$name>$name</$name>")
					} else {
						sender.sendMessage("§8- §f$name")
					}
				}
			}
			"register" -> {
				if (args.size < 2) {
					sender.sendMessage("§cUsage: /textapi register <placeholder|tag|gradient> ...")
					return true
				}
				when (args[1].lowercase()) {
					"placeholder" -> {
						if (args.size < 4) {
							sender.sendMessage("§cUsage: /textapi register placeholder <key> <value...>")
							return true
						}
						val key = args[2]
						val value = args.drop(3).joinToString(" ")
						service.registerPlaceholder(key) { _ -> value }
						sender.sendMessage("§aRegistered placeholder: §f{$key} §8= §f$value")
					}
					"tag" -> {
						if (args.size < 4) {
							sender.sendMessage("§cUsage: /textapi register tag <name> <#hex|colorname> [bold] [italic] [underline] [strikethrough] [obfuscated]")
							return true
						}
						val name = args[2]
						val colorRaw = args[3]
						val flags = args.drop(4).map { it.lowercase() }.toSet()
						val color = TextTag.resolveColor(colorRaw)
						if (color == null) {
							sender.sendMessage("§cUnknown color: §f$colorRaw")
							return true
						}
						service.registerTag(name) { style, _ ->
							style.copy(
								color = color,
								bold = "bold" in flags || style.bold,
								italic = "italic" in flags || style.italic,
								underlined = "underline" in flags || style.underlined,
								strikethrough = "strikethrough" in flags || style.strikethrough,
								obfuscated = "obfuscated" in flags || style.obfuscated
							)
						}
						sender.sendMessage("§aRegistered tag: §f<$name> §8→ §fcolor=$colorRaw flags=$flags")
					}
					"gradient" -> {
						if (args.size < 5) {
							sender.sendMessage("§cUsage: /textapi register gradient <name> <#hex1> <#hex2> [#hex3...]")
							return true
						}
						val name = args[2]
						val stops = args.drop(3)
						service.registerGradient(name, stops)
						sender.sendMessage("§aRegistered gradient: §f<$name> §8with §f${stops.size} stops")
						if (sender is Player) {
							sender.sendMessage("§7Preview:")
							service.send(sender, "<$name>${"█".repeat(20)}</$name>")
						}
					}
					else -> sender.sendMessage("§cUnknown type. Use: placeholder, tag, gradient")
				}
			}
			"unregister" -> {
				if (args.size < 3) {
					sender.sendMessage("§cUsage: /textapi unregister <placeholder|tag|gradient> <key>")
					return true
				}
				val key = args[2]
				when (args[1].lowercase()) {
					"placeholder" -> {
						service.unregisterPlaceholder(key)
						sender.sendMessage("§eUnregistered placeholder: §f{$key}")
					}
					"tag" -> {
						service.unregisterTag(key)
						sender.sendMessage("§eUnregistered tag: §f<$key>")
					}
					"gradient" -> {
						service.unregisterGradient(key)
						sender.sendMessage("§eUnregistered gradient: §f<$key>")
					}
					else -> sender.sendMessage("§cUnknown type. Use: placeholder, tag, gradient")
				}
			}
			"preview" -> {
				val input = args.drop(1).joinToString(" ")
				if (input.isBlank()) {
					sender.sendMessage("§cUsage: /textapi preview <message>")
					return true
				}
				sender.sendMessage("§7Input: §f$input")
				sender.sendMessage("§7Parsed text: §f${service.parse(input, sender as? Player, emptyMap())}")
				if (sender is Player) {
					sender.sendMessage("§7Rendered preview:")
					service.send(sender, input)
				}
			}
			"test" -> {
				if (sender !is Player) {
					sender.sendMessage("§cThis command requires a player.")
					return true
				}
				runFullTest(sender)
			}
			"examples" -> sendExamples(sender)
			else -> sendHelp(sender)
		}
		return true
	}

	private fun runFullTest(player: Player) {
		fun section(title: String) = service.send(player, "\n§8§m                    §r §d§l$title §8§m                    §r")
		fun row(label: String, input: String) = service.send(player, "§8» §7$label§8: $input")
		section("COLORS")
		row(
			"named",
			"<red>red</red> <gold>gold</gold> <green>green</green> <aqua>aqua</aqua> <blue>blue</blue> <light_purple>purple</light_purple>"
		)
		row("hex", "<#ff4800>hex orange</#ff4800> <#00cfff>hex cyan</#00cfff>")
		row("nested", "<red>outer <gold>inner</gold> back</red>")
		row("color alias", "<color:#ff00ff>color tag</color>")
		section("DECORATIONS")
		row("bold", "<bold>bold</bold>")
		row("italic", "<italic>italic</italic>")
		row("underline", "<underlined>underlined</underlined>")
		row("strikethrough", "<strikethrough>strikethrough</strikethrough>")
		row("obfuscated", "<obfuscated>obfuscated</obfuscated>")
		row("combined", "<bold><italic><red>bold+italic+red</red></italic></bold>")
		row("disable bold", "<bold>bold <![bold]>normal</![bold]></bold>")
		section("GRADIENTS")
		row("2-stop", "<gradient:#ff0000:#0000ff>Red to Blue gradient</gradient>")
		row("3-stop", "<gradient:#ff0000:#00ff00:#0000ff>RGB gradient</gradient>")
		row("rainbow", "<rainbow>Rainbow text here!</rainbow>")
		row("rainbow phase", "<rainbow:120>Phase shifted rainbow</rainbow>")
		section("PRIDE GRADIENTS")
		val pride = listOf("pride", "trans", "bi", "lesbian", "nonbinary", "pan", "ace", "aro")
		pride.forEach { name -> row(name, "<$name>${"█".repeat(16)}</$name>") }
		section("CUSTOM TAGS & GRADIENTS")
		val customTags = TextAPI.registeredTags()
		val customGradients = TextAPI.registeredGradient()
		if (customTags.isEmpty() && customGradients.isEmpty()) {
			service.send(player, "§7None registered. Use §f/textapi register tag§7 or §f/textapi register gradient§7.")
		}
		customTags.forEach { tag -> row("<$tag>", "<$tag>Custom tag sample</$tag>") }
		customGradients.forEach { name -> row("<$name>", "<$name>${"█".repeat(20)}</$name>") }
		section("INTERACTIONS")
		row("hover", "<hover:show_text:<red>You hovered!</red>>§eHover over me</hover>")
		row("click run", "<click:run_command:/textapi help><aqua>Click to run /textapi help</aqua></click>")
		row("click suggest", "<click:suggest_command:/say hello><yellow>Click to suggest</yellow></click>")
		row("click url", "<click:open_url:https://nolly.mc><gold>Open URL</gold></click>")
		row("insert", "<insert:inserted text><light_purple>Shift-click me</light_purple></insert>")
		row(
			"hover+click",
			"<hover:show_text:<green>Go home!</green>><click:run_command:/spawn><green>Spawn</green></click></hover>"
		)
		section("PLACEHOLDERS")
		row("player name", "Hello <gold>{player}</gold>!")
		row(
			"server info",
			"Online: <aqua>{server_online}</aqua>/<aqua>{server_max}</aqua> — TPS: <green>{server_tps}</green>"
		)
		row("player info", "World: <yellow>{player_world}</yellow> — Ping: <yellow>{player_ping}</yellow>ms")
		row("time", "Time: <aqua>{time}</aqua> — Date: <aqua>{date}</aqua>")
		row("unknown placeholder", "Missing: {this_does_not_exist}")
		section("RESET & ESCAPING")
		row("reset", "<red>red <reset>back to normal</reset>")
		row("newline", "Line 1<newline>Line 2")
		row("escape", "\\<red\\> is not a tag")
		row("unclosed tag", "<red>no close tag")
		row("empty tag", "<> empty brackets <>")
	}

	private fun sendHelp(sender: CommandSender) {
		sender.sendMessage("§d§lTextAPI Commands")
		sender.sendMessage("§8/textapi parse <msg>            §7- Render a message")
		sender.sendMessage("§8/textapi preview <msg>          §7- Raw + rendered side by side")
		sender.sendMessage("§8/textapi components <msg>       §7- Component dump")
		sender.sendMessage("§8/textapi tokens <msg>           §7- Token dump")
		sender.sendMessage("§8/textapi test                   §7- Full feature test (player only)")
		sender.sendMessage("§8/textapi examples               §7- Quick usage examples")
		sender.sendMessage("§8/textapi placeholders           §7- List all placeholders")
		sender.sendMessage("§8/textapi tags                   §7- List custom tags")
		sender.sendMessage("§8/textapi gradients              §7- List custom gradients")
		sender.sendMessage("§8/textapi register placeholder <key> <value>")
		sender.sendMessage("§8/textapi register tag <name> <color> [bold] [italic] [underline] [strikethrough] [obfuscated]")
		sender.sendMessage("§8/textapi register gradient <name> <#hex1> <#hex2> [#hex3...]")
		sender.sendMessage("§8/textapi unregister <placeholder|tag|gradient> <key>")
	}

	private fun sendExamples(sender: CommandSender) {
		sender.sendMessage("§d§lTextAPI Examples")
		sender.sendMessage("§7Colors:          §f<red>Red</red> <#ff4800>Hex</#ff4800>")
		sender.sendMessage("§7Shadow:          §f<shadow:yellow>Hello <shadow:aqua:0.5>World</shadow>!")
		sender.sendMessage("§7Shadow off:      §f<!shadow> disables shadow")
		sender.sendMessage("§7Gradient:        §f<gradient:#ff0000:#00ff00>Gradient</gradient>")
		sender.sendMessage("§7Rainbow:         §f<rainbow>Rainbow</rainbow>")
		sender.sendMessage("§7Pride:           §f<pride>Pride</pride> <trans>Trans</trans>")
		sender.sendMessage("§7Decorations:     §f<bold>Bold</bold> <italic>Italic</italic>")
		sender.sendMessage("§7Hover:           §f<hover:show_text:<red>Danger</red>>Hover Me</hover>")
		sender.sendMessage("§7Click:           §f<click:run_command:/spawn>Run /spawn</click>")
		sender.sendMessage("§7Placeholders:    §fHello {player} — TPS: {server_tps}")
		sender.sendMessage("§7Custom tag:      §f/textapi register tag vip #ffd700 bold")
		sender.sendMessage("§7Custom gradient: §f/textapi register gradient sunset #ff6600 #ff0099 #aa00ff")
	}

	override fun onTabComplete(
		sender: CommandSender,
		command: Command,
		alias: String,
		args: Array<out String>
	): List<String> {
		if (!sender.hasPermission("com.nolly.mc.textapi.command")) return emptyList()
		val filter = { list: List<String> -> list.filter { it.startsWith(args.last(), ignoreCase = true) }.sorted() }
		if (args.size == 1) {
			return filter(
				listOf(
					"parse", "preview", "components", "tokens", "test",
					"examples", "placeholders", "tags", "gradients",
					"register", "unregister"
				)
			)
		}
		val sub = args[0].lowercase()
		if (sub in listOf("parse", "preview", "components", "tokens")) {
			val suggestions = mutableListOf<String>()
			suggestions += TextAPI.registeredPlaceholders().map { "{$it}" }
			suggestions += listOf(
				"<red>", "<gold>", "<green>", "<aqua>", "<blue>", "<light_purple>", "<yellow>", "<white>",
				"<#ff4800>", "<bold>", "<italic>", "<underlined>", "<strikethrough>", "<obfuscated>",
				"<gradient:#ff0000:#0000ff>", "<rainbow>", "<shadow:yellow>", "<!shadow>",
				"<pride>", "<trans>", "<bi>", "<lesbian>", "<nonbinary>", "<pan>",
				"<hover:show_text:>", "<click:run_command:>", "<click:suggest_command:>",
				"<click:open_url:>", "<insert:>", "<reset>"
			)
			suggestions += TextAPI.registeredTags().map { "<$it>" }
			suggestions += TextAPI.registeredGradient().map { "<$it>" }
			return filter(suggestions)
		}
		if (sub == "register") {
			if (args.size == 2) return filter(listOf("placeholder", "tag", "gradient"))
			when (args[1].lowercase()) {
				"placeholder" -> return when (args.size) {
					3 -> filter(listOf("key_name", "rank", "prefix", "server"))
					4 -> filter(listOf("value_here", "Admin", "VIP", "Player"))
					else -> emptyList()
				}
				"tag" -> return when (args.size) {
					3 -> filter(listOf("vip", "admin", "danger", "success", "warning"))
					4 -> filter(listOf("#ffd700", "#ff0000", "#00ff00", "#00cfff", "gold", "red", "green", "aqua"))
					else -> filter(listOf("bold", "italic", "underline", "strikethrough", "obfuscated"))
				}
				"gradient" -> return when (args.size) {
					3 -> filter(listOf("sunset", "ocean", "fire", "forest", "neon"))
					else -> filter(listOf("#ff0000", "#ff6600", "#ffff00", "#00ff00", "#0000ff", "#ff00ff", "#ffffff"))
				}
			}
		}
		if (sub == "unregister") {
			if (args.size == 2) return filter(listOf("placeholder", "tag", "gradient"))
			when (args[1].lowercase()) {
				"placeholder" -> return filter(TextAPI.registeredPlaceholders().toList())
				"tag" -> return filter(TextAPI.registeredTags().toList())
				"gradient" -> return filter(TextAPI.registeredGradient().toList())
			}
		}
		if (sub == "placeholders" || sub == "tags" || sub == "gradients") return emptyList()
		return emptyList()
	}
}
