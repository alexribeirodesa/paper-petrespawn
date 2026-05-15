package org.elalezito.petRespawn.objects

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.plugin.java.JavaPlugin
import org.elalezito.swissKnife.objects.Toolkit

data class SoulEggData(
	val displayName: String,
	val lore: List<Component> = emptyList()
)

data class ItemsData(
	val useCustomTextures: Boolean = false,
	val soulEgg: SoulEggData,
	val ChargedSoulEgg: SoulEggData,
)

data class LocalizationData(
	//var soulEgg: String,
	//var chargedSoulEgg: String,
	//var soulEggLore: String,

	var commandReloadDescription: String
)

object Config {
	private val mm = MiniMessage.miniMessage()
	private val legacySerializer = LegacyComponentSerializer.legacySection()

	private lateinit var plugin: JavaPlugin

	private lateinit var _items: ItemsData
	val items: ItemsData
		get() = _items

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

		// configuração
		_items = ItemsData(
			getBool("items.use-custom-textures", false),
			SoulEggData(
				getString("items.soul-egg.display-name", "Soul Egg"),
				getList<String>("items.soul-egg.lore").map { lore: String -> mm.deserialize(lore)  }
			),
			SoulEggData(
				getString("items.charged-soul-egg.display-name", "Charged Soul Egg"),
				getList<String>("items.charged-soul-egg.lore").map { lore: String -> mm.deserialize(lore)  }
			)
		)

		// localização
		_localization = LocalizationData(
			getString("localization.command-reload-description", "Reload configuration data")
		)

		Toolkit.log("Configuração carregada com sucesso!")
	}

	private fun getString(path: String, default: String = "NULLERR"): String {
		val config = plugin.config
		val component = mm.deserialize(config.getString(path) ?: default)
		return legacySerializer.serialize(component)
	}

	private fun getBool(path: String, default: Boolean = false): Boolean {
		val config = plugin.config
		return config.getBoolean(path, default)
	}

	private inline fun <reified T> getList(path: String): List<T> {
		val list = plugin.config.getList(path) ?: return emptyList()
		return list.filterIsInstance<T>()
	}
}