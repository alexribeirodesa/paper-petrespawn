package org.elalezito.petRespawn

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.DyeColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.Registry
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Cat
import org.bukkit.entity.EntityType
import org.bukkit.entity.Horse
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Llama
import org.bukkit.entity.Parrot
import org.bukkit.entity.Tameable
import org.bukkit.entity.Wolf
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.plugin.java.JavaPlugin
import org.elalezito.petRespawn.objects.Config
import org.elalezito.swissKnife.objects.Toolkit
import java.io.File
import java.util.UUID

class PetManager(private val plugin: JavaPlugin) {
	fun registerChargedSoulEggRecipe() {
		val result = ItemStack(Material.EGG)
		val meta = result.itemMeta

		meta.displayName(Component.text(Config.localization.chargedSoulEgg))
		meta.addEnchant(Enchantment.UNBREAKING, 1, true)
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
		result.itemMeta = meta

		val key = NamespacedKey(plugin, "charged_soul_egg")
		val recipe = ShapedRecipe(key, result)
		recipe.shape(
			" G ",
			"GEG",
			" G "
		)

		recipe.setIngredient('G', Material.GHAST_TEAR)
		recipe.setIngredient('E', Material.EGG)

		Bukkit.addRecipe(recipe)
	}

	private fun serializePetEntity(entity: Tameable): JsonObject {
		val json: JsonObject = JsonObject();

		// identidade
		json.addProperty("type", entity.type.toString())
		json.addProperty("name", entity.name)
		json.addProperty("customName", entity.customName().toString())
		json.addProperty("age", entity.age)
		json.addProperty("ageLock", entity.ageLock)
		json.addProperty("isCustomNameVisible", entity.isCustomNameVisible)

		// estilo e variação
		if (entity is Wolf || entity is Cat) {
			json.addProperty("collarColor", entity.collarColor.name)
		}

		when (entity) {
			is Wolf -> json.addProperty("variant", entity.variant.key.toString())
			is Cat -> json.addProperty("variant", entity.catType.key.toString())
			is Horse -> {
				json.addProperty("color", entity.color.name)
				json.addProperty("style", entity.style.name)
			}

			is Llama -> json.addProperty("color", entity.color.name)
			is Parrot -> json.addProperty("variant", entity.variant.name)
		}

		// atributos
		val attributesJson: JsonObject = JsonObject()
		Registry.ATTRIBUTE.forEach { attribute ->
			val instance = entity.getAttribute(attribute)

			if (instance != null) {
				attributesJson.addProperty("${attribute.key.toString()}", instance.baseValue)
			}
		}
		json.add("attributes", attributesJson);

		/*
		entity.customName()?.let {
			json.addProperty("customName", GsonComponentSerializer.gson().serialize(it))
		}
		json.addProperty("customNameVisible", entity.isCustomNameVisible)

		val pdcJson = JsonObject()
		val pdc = entity.persistentDataContainer
		pdc.keys.forEach { key ->
			runCatching {
				pdc.get(key, org.bukkit.persistence.PersistentDataType.STRING)
					?.let { pdcJson.addProperty(key.toString(), it) }
			}
		}
		json.add("persistentData", pdcJson)
		*/

		return json
	}

	fun spawnPetEntity(ownerUuid: String, petUuid: String, location: Location) {
		val json: JsonObject? = loadPetSoul(ownerUuid, petUuid)

		if (json == null) {
			return
		}

		// tipo do pet
		val type = EntityType.valueOf(json.get("type").asString)

		// spawna a entidade base
		val entity = location.world.spawnEntity(location, type) as Tameable

		// inicia
		entity.customName(Component.text(json.get("name").asString))
		entity.age = json.get("age").asInt
		entity.ageLock = json.get("ageLock").asBoolean
		entity.isCustomNameVisible = json.get("isCustomNameVisible").asBoolean

		// aplica estilo e variação
		if (entity is Wolf || entity is Cat) {
			entity.collarColor = DyeColor.valueOf((json.get("collarColor").asString ?: DyeColor.RED) as String)
		}

		when (entity) {
			is Wolf -> {
				val key = NamespacedKey.fromString(json.get("variant").asString) ?: NamespacedKey.minecraft("pale")
				val wolfRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.WOLF_VARIANT)
				val variant = wolfRegistry.get(key)

				if (variant != null)
					entity.variant = variant
			}

			is Cat -> {
				val key = NamespacedKey.fromString(json.get("variant").asString) ?: NamespacedKey.minecraft("jellie")
				val catRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.CAT_VARIANT)
				val variant = catRegistry.get(key)

				if (variant != null)
					entity.catType = variant
			}

			is Horse -> {
				entity.color = Horse.Color.valueOf((json.get("color").asString ?: Horse.Color.CHESTNUT) as String)
				entity.style = Horse.Style.valueOf((json.get("style").asString ?: Horse.Style.NONE) as String)
			}

			is Llama -> {
				entity.color = Llama.Color.valueOf((json.get("color").asString ?: Llama.Color.WHITE) as String)
			}

			is Parrot -> {
				entity.variant = Parrot.Variant.valueOf((json.get("color").asString ?: Parrot.Variant.GREEN) as String)
			}
		}

		// seta o dono
		val owner = Bukkit.getOfflinePlayer(UUID.fromString(ownerUuid))
		Toolkit.log("$ownerUuid, ${owner.uniqueId}")
		entity.owner = owner;

		val attributesObj = json.getAsJsonObject("attributes")
		attributesObj.entrySet().forEach { (key, value) ->
			val attrKey = NamespacedKey.fromString(key) ?: return@forEach
			val attribute = Registry.ATTRIBUTE.get(attrKey) ?: return@forEach
			(entity as LivingEntity).getAttribute(attribute)?.baseValue = value.asDouble
		}

		// feedback visual
		location.world.strikeLightningEffect(location)
		location.world.spawnParticle(Particle.SOUL, location.add(0.0, 1.0, 0.0), 32, 0.5, 0.5, 0.5, 0.1)
		location.world.playSound(location, Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 2.5f, 1.2f)
		//location.world.playSound(location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 0.8f)
	}

	fun savePetSoul(entity: Tameable) {
		val serializedTameable: JsonObject = serializePetEntity(entity)

		val directory = File(plugin.dataFolder, "souls/${entity.ownerUniqueId}")
		if (!directory.exists()) directory.mkdirs()

		val file: File = File(directory, "${entity.uniqueId}.json")

		val gson = GsonBuilder().setPrettyPrinting().create()
		val prettyJson = gson.toJson(serializedTameable)

		file.writeText(prettyJson)
	}

	fun loadPetSoul(ownerUuid: String, petUuid: String): JsonObject? {
		val file = File(plugin.dataFolder, "souls/$ownerUuid/$petUuid.json")

		if (!file.exists()) {
			// LOG DE ERRO
			return null
		}

		return JsonParser.parseString(file.readText()).asJsonObject
	}

	fun deletePetSoul(ownerUuid: String, petUuid: String): Boolean {
		val file = File(plugin.dataFolder, "souls/$ownerUuid/$petUuid.json")
		if (!file.exists()) {
			return false
		}

		file.delete()
		return true
	}
}