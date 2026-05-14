package org.elalezito.petRespawn.objects

import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.plugin.java.JavaPlugin
import org.elalezito.swissKnife.objects.Toolkit

data class LocalizationData(
	var soulEgg: String,
	var chargedSoulEgg: String,
	var soulEggLore: String,

	var commandReloadDescription: String
)

object Config {
	private val mm = MiniMessage.miniMessage()
	private val legacySerializer = LegacyComponentSerializer.legacySection()

	private lateinit var plugin: JavaPlugin

	private lateinit var _localization: LocalizationData
	val localization: LocalizationData
		get() = _localization

	fun init(javaPlugin: JavaPlugin) {
		plugin = javaPlugin
		plugin.saveDefaultConfig()
		load()
	}

	fun load() {
		Toolkit.log("Carregando configuração.")
		plugin.reloadConfig()

		Toolkit.log(getString("localization.charged-soul-egg", "Charged Soul Egg"))

		// localização
		_localization = LocalizationData(
			getString("localization.soul-egg", "Soul Egg"),
			getString("localization.charged-soul-egg", "Charged Soul Egg"),
			getString("localization.soul-egg-lore", "Pet: {petname} "),

			getString("localization.command-reload-description", "Reload configuration data")
		)

		Toolkit.log("Configuração carregada com sucesso!")
	}

	private fun getString(path: String, default: String = "NULLERR"): String {
		val config = plugin.config
		val component = mm.deserialize(config.getString(path) ?: default)
		return legacySerializer.serialize(component)
	}
}