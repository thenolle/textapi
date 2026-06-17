package com.nolly.mc.textapi.impl

import com.nolly.mc.textapi.TextAPIPlugin
import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.min

object TextTPS {
	private const val HISTORY_SIZE = 600
	private const val MAX_TPS = 20.0
	private val tickTimes = LongArray(HISTORY_SIZE)
	private var tickCount = 0
	private var started = false

	fun init() {
		if (started) return
		started = true
		object : BukkitRunnable() {
			override fun run() {
				tickTimes[tickCount % HISTORY_SIZE] = System.nanoTime()
				tickCount++
			}
		}.runTaskTimer(TextAPIPlugin.instance, 1L, 1L)
	}

	fun getTps(): Double = getTps(100)

	fun getTps(ticks: Int): Double {
		if (tickCount < ticks + 1) return MAX_TPS
		val past = tickTimes[(tickCount - 1 - ticks) % HISTORY_SIZE]
		val elapsedSeconds = (System.nanoTime() - past) / 1_000_000_000.0
		return min(MAX_TPS, ticks / elapsedSeconds)
	}

	fun getTps1s(): Double = getTps(20)
	fun getTps5s(): Double = getTps(100)
	fun getTps10s(): Double = getTps(200)
	fun getTps30s(): Double = getTps(600)

	fun getMspt(): Double {
		if (tickCount < 2) return 50.0
		val last = tickTimes[(tickCount - 1) % HISTORY_SIZE]
		val previous = tickTimes[(tickCount - 2) % HISTORY_SIZE]
		return (last - previous) / 1_000_000.0
	}

	fun getFormattedTps(): String = "%.2f".format(getTps())
	fun getFormattedMspt(): String = "%.2f".format(getMspt())
}
