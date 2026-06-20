package com.nolly.mc.textapi.impl

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.KeybindComponent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.TranslatableComponent
import net.md_5.bungee.api.chat.hover.content.Text as HoverText

internal class TextRenderer {
	private data class Segment(val text: String, val style: TextStyle)

	private sealed interface Frame {
		data class Style(val tagName: String, val style: TextStyle) : Frame
		data class Collecting(val tagName: String, val outerStyle: TextStyle, val meta: CollectMeta) : Frame
	}

	private sealed interface CollectMeta {
		data class Gradient(val colorStops: List<ChatColor>) : CollectMeta
		data class Transition(val colorStops: List<ChatColor>, val phase: Float) : CollectMeta
		data class Rainbow(val phase: Int) : CollectMeta
		data class Pride(val palette: String) : CollectMeta
		data class CustomGradient(val name: String) : CollectMeta
	}


	private sealed interface OutNode {
		data class Seg(val segment: Segment) : OutNode
		data class Comp(val component: BaseComponent) : OutNode
	}

	fun renderComponents(tokens: List<TextToken>, context: TextContext = TextContext()): Array<BaseComponent> {
		return collectNodes(tokens, context).map {
			when (it) {
				is OutNode.Seg -> buildComponent(it.segment)
				is OutNode.Comp -> it.component
			}
		}.toTypedArray()
	}

	fun render(tokens: List<TextToken>, context: TextContext = TextContext()): String {
		return collectNodes(tokens, context).joinToString("") {
			when (it) {
				is OutNode.Seg -> applyLegacy(it.segment.style, it.segment.text)
				is OutNode.Comp -> when (val comp = it.component) {
					is KeybindComponent -> "[${comp.keybind}]"
					else -> comp.toLegacyText()
				}
			}
		}
	}

	private fun collectNodes(tokens: List<TextToken>, context: TextContext): List<OutNode> {
		val output = mutableListOf<OutNode>()
		val frameStack = ArrayDeque<Frame>()
		val collectorBuffers = HashMap<Int, MutableList<OutNode>>()
		frameStack.addLast(Frame.Style("__root__", TextStyle()))

		fun currentStyle(): TextStyle = when (val top = frameStack.last()) {
			is Frame.Style -> top.style
			is Frame.Collecting -> top.outerStyle
		}

		fun emitText(text: String) {
			if (text.isEmpty()) return
			val node = OutNode.Seg(Segment(text, currentStyle()))
			val collectingIdx = frameStack.indexOfLast { it is Frame.Collecting }
			if (collectingIdx != -1) {
				val frame = frameStack[collectingIdx] as Frame.Collecting
				collectorBuffers.getOrPut(System.identityHashCode(frame)) { mutableListOf() }.add(node)
			} else {
				output.add(node)
			}
		}

		fun emitComponent(component: BaseComponent) {
			val node = OutNode.Comp(component)
			val collectingIdx = frameStack.indexOfLast { it is Frame.Collecting }
			if (collectingIdx != -1) {
				val frame = frameStack[collectingIdx] as Frame.Collecting
				collectorBuffers.getOrPut(System.identityHashCode(frame)) { mutableListOf() }.add(node)
			} else {
				output.add(node)
			}
		}

		for (token in tokens) {
			when (token) {
				is TextToken.Text -> emitText(token.value)
				is TextToken.Placeholder -> emitText(context.resolve(token.name) ?: "{${token.name}}")
				is TextToken.OpenTag -> {
					val name = TextTag.normalize(token.name)
					val args = token.arguments
					val style = currentStyle()

					when {
						name == "reset" -> {
							frameStack.clear()
							frameStack.addLast(Frame.Style("__root__", TextStyle()))
						}
						name == "newline" || name == "br" -> emitText("\n")
						name == "key" && args != null -> {
							val keybindKey = TextTag.resolveKeybind(args)
							if (keybindKey != null) {
								emitComponent(buildKeybindComponent(keybindKey, style))
							} else {
								emitText("<${serialize(token)}>")
							}
						}
						name == "shadow" && args != null -> {
							val parts = args.split(":", limit = 2)
							val colorPart = parts.getOrNull(0)?.trim()
							val alphaPart = parts.getOrNull(1)?.trim()?.toFloatOrNull()
							val shadow = when {
								colorPart.isNullOrBlank() -> null
								TextTag.normalize(colorPart) == "#00000000" -> TextTag.transparentShadow()
								else -> TextTag.resolveShadowColor(colorPart, alphaPart)
							}
							if (shadow != null) {
								frameStack.addLast(Frame.Style(name, TextTag.applyShadow(style, shadow)))
							} else {
								emitText("<${serialize(token)}>")
							}
						}
						name == "lang" || name == "tr" || name == "translate" -> {
							if (args != null) {
								val parsed = TextTag.parseTranslatable(args)
								if (parsed != null) {
									val (key, withRaw) = parsed
									emitComponent(buildTranslatableComponent(key, withRaw, style))
								} else {
									emitText("<${serialize(token)}>")
								}
							} else {
								emitText("<${serialize(token)}>")
							}
						}
						name == "lang_or" || name == "tr_or" || name == "translate_or" -> {
							if (args != null) {
								val parsed = TextTag.parseTranslatableOr(args)
								if (parsed != null) {
									val (key, fallback, withRaw) = parsed
									emitComponent(buildTranslatableComponent(key, withRaw, style, fallback))
								} else {
									emitText("<${serialize(token)}>")
								}
							} else {
								emitText("<${serialize(token)}>")
							}
						}
						name == "gradient" && args != null -> {
							val stops = args.split(":").mapNotNull { TextTag.resolveColor(it.trim()) }
							if (stops.size >= 2) {
								val frame = Frame.Collecting(name, style, CollectMeta.Gradient(stops))
								frameStack.addLast(frame)
								collectorBuffers[System.identityHashCode(frame)] = mutableListOf()
							} else {
								emitText("<${serialize(token)}>")
							}
						}
						name == "transition" && args != null -> {
							val parts = args.split(":").map { it.trim() }.filter { it.isNotEmpty() }
							val maybePhase = parts.lastOrNull()?.toFloatOrNull()
							val colorParts = if (maybePhase != null && parts.size > 1) parts.dropLast(1) else parts
							val phase = (maybePhase ?: 0f).coerceIn(-1f, 1f)
							val stops = colorParts.mapNotNull { TextTag.resolveColor(it) }
							if (stops.isNotEmpty()) {
								val frame = Frame.Collecting(name, style, CollectMeta.Transition(stops, phase))
								frameStack.addLast(frame)
								collectorBuffers[System.identityHashCode(frame)] = mutableListOf()
							} else {
								emitText("<${serialize(token)}>")
							}
						}
						name == "rainbow" -> {
							val phase = args?.toIntOrNull() ?: 0
							val frame = Frame.Collecting(name, style, CollectMeta.Rainbow(phase))
							frameStack.addLast(frame)
							collectorBuffers[System.identityHashCode(frame)] = mutableListOf()
						}
						name in TextTag.prideGradients -> {
							val frame = Frame.Collecting(name, style, CollectMeta.Pride(name))
							frameStack.addLast(frame)
							collectorBuffers[System.identityHashCode(frame)] = mutableListOf()
						}
						TextTag.isCustomGradient(name) -> {
							val frame = Frame.Collecting(name, style, CollectMeta.CustomGradient(name))
							frameStack.addLast(frame)
							collectorBuffers[System.identityHashCode(frame)] = mutableListOf()
						}
						TextTag.isRegistered(name) -> {
							val handler = TextTag.resolveHandler(name)
							if (handler != null) {
								frameStack.addLast(Frame.Style(name, handler.apply(style, args)))
							} else {
								emitText("<${serialize(token)}>")
							}
						}
						TextTag.isColorTag(name) && args == null -> {
							val color = TextTag.resolveColor(name)
							frameStack.addLast(Frame.Style(name, style.copy(color = color)))
						}
						name.startsWith("#") && name.length == 7 -> {
							val color = TextTag.resolveColor(name)
							if (color != null) frameStack.addLast(Frame.Style(name, style.copy(color = color)))
							else emitText("<${serialize(token)}>")
						}
						TextTag.isColorAlias(name) && args != null -> {
							val color = TextTag.resolveColor(args)
							if (color != null) frameStack.addLast(Frame.Style(name, style.copy(color = color)))
							else emitText("<${serialize(token)}>")
						}
						TextTag.isDecorationTag(name) -> {
							frameStack.addLast(Frame.Style(name, TextTag.applyDecoration(style, name, true)))
						}
						name.startsWith("!") -> {
							val inner = name.substring(1)
							if (inner == "shadow") {
								frameStack.addLast(Frame.Style(name, TextTag.applyShadow(style, TextTag.transparentShadow())))
							} else if (TextTag.isDecorationTag(inner)) {
								frameStack.addLast(Frame.Style(name, TextTag.applyDecoration(style, inner, false)))
							} else {
								emitText("<$name>")
							}
						}
						name == "click" && args != null -> {
							val event = parseClickEvent(args)
							if (event != null) frameStack.addLast(Frame.Style(name, style.copy(clickEvent = event)))
							else emitText("<${serialize(token)}>")
						}
						name == "hover" && args != null -> {
							val event = parseHoverEvent(args)
							if (event != null) frameStack.addLast(Frame.Style(name, style.copy(hoverEvent = event)))
							else emitText("<${serialize(token)}>")
						}
						name == "insert" && args != null -> {
							frameStack.addLast(Frame.Style(name, style.copy(insertion = args)))
						}
						else -> emitText("<${serialize(token)}>")
					}
				}
				is TextToken.CloseTag -> {
					val name = TextTag.normalize(token.name)
					val matchIdx = frameStack.indexOfLast { frame ->
						when (frame) {
							is Frame.Style -> TextTag.normalize(frame.tagName) == name
							is Frame.Collecting -> TextTag.normalize(frame.tagName) == name
						}
					}
					if (matchIdx <= 0) {
						emitText("</${token.name}>")
						continue
					}
					while (frameStack.size > matchIdx) {
						val popped = frameStack.removeLast()
						if (popped is Frame.Collecting) {
							val key = System.identityHashCode(popped)
							val inner = collectorBuffers.remove(key) ?: emptyList()
							val expanded = expandCollecting(popped, inner)
							val newCollectingIdx = frameStack.indexOfLast { it is Frame.Collecting }
							if (newCollectingIdx != -1) {
								val parentFrame = frameStack[newCollectingIdx] as Frame.Collecting
								collectorBuffers.getOrPut(System.identityHashCode(parentFrame)) { mutableListOf() }.addAll(expanded)
							} else {
								output.addAll(expanded)
							}
						}
					}
				}
			}
		}

		for (frame in frameStack) {
			if (frame is Frame.Collecting) {
				val key = System.identityHashCode(frame)
				val inner = collectorBuffers.remove(key) ?: emptyList()
				output.addAll(expandCollecting(frame, inner))
			}
		}

		return output
	}

	private fun expandCollecting(frame: Frame.Collecting, inner: List<OutNode>): List<OutNode> {
		val textNodes = inner.filterIsInstance<OutNode.Seg>()
		val fullText = textNodes.joinToString("") { it.segment.text }
		if (fullText.isEmpty()) return inner

		val segments = when (val meta = frame.meta) {
			is CollectMeta.Gradient -> expandGradient(fullText, meta.colorStops, frame.outerStyle)
			is CollectMeta.Transition -> expandTransition(fullText, meta.colorStops, meta.phase, frame.outerStyle)
			is CollectMeta.Rainbow -> expandRainbow(fullText, meta.phase, frame.outerStyle)
			is CollectMeta.Pride -> {
				val stops = TextTag.prideGradients[meta.palette]!!.mapNotNull { TextTag.resolveColor(it) }
				expandGradient(fullText, stops, frame.outerStyle)
			}
			is CollectMeta.CustomGradient -> {
				val stops = TextTag.resolveCustomGradient(meta.name)?.mapNotNull { TextTag.resolveColor(it) } ?: emptyList()
				if (stops.size >= 2) expandGradient(fullText, stops, frame.outerStyle) else textNodes.map { it.segment }
			}
		}

		return segments.map { OutNode.Seg(it) }
	}

	private fun buildTranslatableComponent(
		key: String,
		withRaw: List<String>,
		style: TextStyle,
		fallback: String? = null
	): BaseComponent {
		val c = TranslatableComponent(key)
		if (withRaw.isNotEmpty()) {
			val withComponents = withRaw.map { raw ->
				val parsed = TextParser().parse(raw)
				val rendered = renderComponents(parsed)
				val container = TextComponent()
				rendered.forEach { container.addExtra(it) }
				container as BaseComponent
			}
			c.with = withComponents
		}
		if (fallback != null) {
			runCatching { c.fallback = fallback }
		}
		applyStyleToComponent(c, style)
		return c
	}

	private fun buildComponent(seg: Segment): TextComponent {
		val c = TextComponent(seg.text)
		applyStyleToComponent(c, seg.style)
		return c
	}

	private fun buildKeybindComponent(keybindKey: String, style: TextStyle): BaseComponent {
		val c = KeybindComponent(keybindKey)
		applyStyleToComponent(c, style)
		return c
	}

	private fun applyStyleToComponent(c: BaseComponent, style: TextStyle) {
		style.color?.let { c.color = it }
		style.shadowColor?.let { c.shadowColor = it }
		if (style.bold) c.isBold = true
		if (style.italic) c.isItalic = true
		if (style.underlined) c.isUnderlined = true
		if (style.strikethrough) c.isStrikethrough = true
		if (style.obfuscated) c.isObfuscated = true
		style.clickEvent?.let { c.clickEvent = it }
		style.hoverEvent?.let { c.hoverEvent = it }
		style.insertion?.let { c.insertion = it }
	}

	private fun parseClickEvent(args: String): ClickEvent? {
		val idx = args.indexOf(":")
		if (idx == -1) return null
		val action = args.substring(0, idx).trim().lowercase()
		val value = args.substring(idx + 1)
		return when (action) {
			"open_url" -> ClickEvent(ClickEvent.Action.OPEN_URL, value)
			"run_command" -> ClickEvent(ClickEvent.Action.RUN_COMMAND, value)
			"suggest_command" -> ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, value)
			"copy_to_clipboard" -> ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, value)
			"change_page" -> ClickEvent(ClickEvent.Action.CHANGE_PAGE, value)
			else -> null
		}
	}

	private fun parseHoverEvent(args: String): HoverEvent? {
		val idx = args.indexOf(":")
		if (idx == -1) return null
		val action = args.substring(0, idx).trim().lowercase()
		val value = args.substring(idx + 1)
		return when (action) {
			"show_text" -> {
				val innerComponents = renderComponents(TextParser().parse(value))
				HoverEvent(HoverEvent.Action.SHOW_TEXT, HoverText(innerComponents))
			}
			else -> null
		}
	}

	private fun applyLegacy(style: TextStyle, text: String): String {
		val sb = StringBuilder()
		style.color?.let { sb.append(it) }
		if (style.bold) sb.append(ChatColor.BOLD)
		if (style.italic) sb.append(ChatColor.ITALIC)
		if (style.underlined) sb.append(ChatColor.UNDERLINE)
		if (style.strikethrough) sb.append(ChatColor.STRIKETHROUGH)
		if (style.obfuscated) sb.append(ChatColor.MAGIC)
		sb.append(text)
		return sb.toString()
	}

	private fun serialize(t: TextToken.OpenTag): String = if (t.arguments.isNullOrBlank()) t.name else "${t.name}:${t.arguments}"

	private fun expandGradient(text: String, stops: List<ChatColor>, base: TextStyle): List<Segment> {
		val len = text.length
		return text.mapIndexed { i, char ->
			val progress = if (len <= 1) 0f else i.toFloat() / (len - 1)
			val color = interpolateColorStops(stops, progress)
			Segment(char.toString(), base.copy(color = color))
		}
	}

	private fun expandTransition(
		text: String,
		stops: List<ChatColor>,
		phase: Float,
		base: TextStyle
	): List<Segment> {
		val color = if (stops.size == 1) {
			stops[0]
		} else {
			var p = (phase + 1f) / 2f
			p -= kotlin.math.floor(p)
			interpolateColorStops(stops, p)
		}
		return text.map { Segment(it.toString(), base.copy(color = color)) }
	}

	private fun expandRainbow(text: String, phase: Int, base: TextStyle): List<Segment> {
		val len = text.length
		return text.mapIndexed { i, char ->
			val hue = ((i.toFloat() / len * 360f) + phase) % 360f
			val color = ChatColor.of(hsvToHex(hue))
			Segment(char.toString(), base.copy(color = color))
		}
	}

	private fun interpolateColorStops(stops: List<ChatColor>, progress: Float): ChatColor {
		val segments = stops.size - 1
		val scaled = progress * segments
		val index = scaled.toInt().coerceIn(0, segments - 1)
		val local = scaled - index
		val rgb1 = chatColorToRgb(stops[index])
		val rgb2 = chatColorToRgb(stops[index + 1])
		val r = (rgb1[0] + (rgb2[0] - rgb1[0]) * local).toInt().coerceIn(0, 255)
		val g = (rgb1[1] + (rgb2[1] - rgb1[1]) * local).toInt().coerceIn(0, 255)
		val b = (rgb1[2] + (rgb2[2] - rgb1[2]) * local).toInt().coerceIn(0, 255)
		return ChatColor.of("#%02x%02x%02x".format(r, g, b))
	}

	private fun chatColorToRgb(color: ChatColor): IntArray {
		val awtColor = color.color
		return if (awtColor != null) {
			intArrayOf(awtColor.red, awtColor.green, awtColor.blue)
		} else {
			legacyColorRgb[color] ?: intArrayOf(255, 255, 255)
		}
	}

	private val legacyColorRgb: Map<ChatColor, IntArray> = mapOf(
		ChatColor.BLACK to intArrayOf(0, 0, 0),
		ChatColor.DARK_BLUE to intArrayOf(0, 0, 170),
		ChatColor.DARK_GREEN to intArrayOf(0, 170, 0),
		ChatColor.DARK_AQUA to intArrayOf(0, 170, 170),
		ChatColor.DARK_RED to intArrayOf(170, 0, 0),
		ChatColor.DARK_PURPLE to intArrayOf(170, 0, 170),
		ChatColor.GOLD to intArrayOf(255, 170, 0),
		ChatColor.GRAY to intArrayOf(170, 170, 170),
		ChatColor.DARK_GRAY to intArrayOf(85, 85, 85),
		ChatColor.BLUE to intArrayOf(85, 85, 255),
		ChatColor.GREEN to intArrayOf(85, 255, 85),
		ChatColor.AQUA to intArrayOf(85, 255, 255),
		ChatColor.RED to intArrayOf(255, 85, 85),
		ChatColor.LIGHT_PURPLE to intArrayOf(255, 85, 255),
		ChatColor.YELLOW to intArrayOf(255, 255, 85),
		ChatColor.WHITE to intArrayOf(255, 255, 255)
	)

	private fun hsvToHex(hue: Float): String {
		val h6 = hue / 60f
		val hi = h6.toInt() % 6
		val f = h6 - hi
		val q = 1f - f
		val (r, g, b) = when (hi) {
			0 -> Triple(1f, f, 0f)
			1 -> Triple(q, 1f, 0f)
			2 -> Triple(0f, 1f, f)
			3 -> Triple(0f, q, 1f)
			4 -> Triple(f, 0f, 1f)
			else -> Triple(1f, 0f, q)
		}
		return "#%02x%02x%02x".format((r * 255).toInt(), (g * 255).toInt(), (b * 255).toInt())
	}
}
