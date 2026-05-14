package org.elalezito.swissKnife.objects

import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.title.Title
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.awt.Component

object Toolkit {
	private val mm = MiniMessage.miniMessage()
	private val logPrefix = "»"
	lateinit private var plugin: JavaPlugin

	fun setPlugin(plugin: JavaPlugin) {
		this.plugin = plugin
		startupBanner(plugin)
	}

	fun send(player: Player, message: String) {
		val component = mm.deserialize(message)
		player.sendMessage(component)
	}

	fun title(player: Player, title: String = "", subtitle: String = "") {
		if (title.isBlank() && subtitle.isBlank())
			return

		val mainTitle = mm.deserialize(title)
		val subtitle = mm.deserialize(subtitle)

		val title: Title = Title.title(mainTitle, subtitle)

		player.showTitle(title)
	}

	fun playSound(
		player: Player,
		sound: String,
		source: Sound.Source = Sound.Source.UI,
		volume: Float = 1.0f,
		pitch: Float = 1.0f
	) {
		val som = Sound.sound(
			Key.key(sound),
			source,
			volume,
			pitch
		)

		player.playSound(som)
	}

	fun log(message: String) {
		plugin.logger.info("$logPrefix $message")
	}

	fun startupBanner(plugin: JavaPlugin) {
		log("Thanks for using this plugin! Check updates at")
		log("https://modrinth.com/plugin/gptitle")
	}
}