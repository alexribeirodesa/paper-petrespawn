package org.elalezito.petRespawn.objects

import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin

object PetKeys {
	lateinit var IS_SOUL_EGG: NamespacedKey
	lateinit var IS_CHARGED_SOUL_EGG: NamespacedKey
	lateinit var PET_UUID: NamespacedKey
	lateinit var OWNER_UUID: NamespacedKey

	fun init(plugin: JavaPlugin) {
		IS_SOUL_EGG = NamespacedKey(plugin, "is_soul_egg")
		IS_CHARGED_SOUL_EGG = NamespacedKey(plugin, "is_charged_soul_egg")
		PET_UUID = NamespacedKey(plugin, "pet_uuid")
		OWNER_UUID = NamespacedKey(plugin, "owner_uuid")
	}
}