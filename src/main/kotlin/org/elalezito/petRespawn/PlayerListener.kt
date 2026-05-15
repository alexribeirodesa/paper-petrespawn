package org.elalezito.petRespawn

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.entity.Tameable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.elalezito.swissKnife.objects.Toolkit
import org.bukkit.Sound
import org.bukkit.block.Biome
import org.bukkit.block.BlockFace
import org.bukkit.entity.Egg
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.persistence.PersistentDataType
import org.elalezito.petRespawn.objects.Config
import org.elalezito.petRespawn.objects.PetKeys

class PlayerListener(private val plugin: JavaPlugin, private val petManager: PetManager) : Listener {
	@EventHandler
	fun onPetDeath(event: EntityDeathEvent) {
		val pet = event.entity
		if (pet is Tameable && pet.isTamed) {
			// sai se o pet não tiver dono
			val owner = pet.owner ?: return

			val egg = ItemStack(Material.EGG)
			val meta = egg.itemMeta

			meta.displayName(
				Component.text(Config.localization.soulEgg)
			)

			val lore = mutableListOf<Component>()
			lore.add(Component.text(Config.localization.soulEggLore.replace("{petname}", pet.name)))

			meta.lore(lore)

			// salva a alma
			petManager.savePetSoul(pet)

			// cancela o drop vanilla
			//event.drops.clear()

			// gera o ovo da alma
			meta.persistentDataContainer.set(PetKeys.IS_SOUL_EGG, PersistentDataType.BOOLEAN, true)
			meta.persistentDataContainer.set(PetKeys.IS_CHARGED_SOUL_EGG, PersistentDataType.BOOLEAN, false)
			meta.persistentDataContainer.set(PetKeys.PET_UUID, PersistentDataType.STRING, pet.uniqueId.toString())
			meta.persistentDataContainer.set(PetKeys.OWNER_UUID, PersistentDataType.STRING, pet.ownerUniqueId.toString())

			if(Config.config.useCustomTextures)
				meta.itemModel = NamespacedKey("petrespawn", "soulegg_model")

			egg.itemMeta = meta

			event.drops.add(egg)
		}
	}

	@EventHandler
	fun onEggHit(event: ProjectileHitEvent) {
		val projectile = event.entity

		if (projectile is Egg) {
			val item = projectile.item
			val pdc = item.itemMeta.persistentDataContainer

			// verifica se é um ovo da alma
			if (pdc.has(PetKeys.IS_SOUL_EGG, PersistentDataType.BOOLEAN)) {
				val ownerUuid = pdc.get(PetKeys.OWNER_UUID, PersistentDataType.STRING) ?: return
				val petUuid = pdc.get(PetKeys.PET_UUID, PersistentDataType.STRING) ?: return
				val isCharged = pdc.get(PetKeys.IS_CHARGED_SOUL_EGG, PersistentDataType.BOOLEAN) ?: return
				val location = event.hitBlock?.getRelative(event.hitBlockFace ?: BlockFace.UP)?.location
					?: projectile.location

				// seta que o projétil é um ovo da alma, para cancelar possível spawn de galinhas
				projectile.setMetadata("is_pet_egg", FixedMetadataValue(plugin, true))

				// verifica se o ovo da alma está carregado e se estamos no bioma correto
				if (!isCharged || location.block.biome != Biome.SOUL_SAND_VALLEY) {
					location.world.dropItem(location, item)

					location.world.spawnParticle(Particle.SMOKE, location, 10, 0.2, 0.2, 0.2, 0.05)
					location.world.spawnParticle(Particle.SOUL, location, 10, 0.2, 0.2, 0.2, 0.05)
					location.world.playSound(location, Sound.BLOCK_SOUL_SAND_BREAK, 0.75f, 0.8f)
					location.world.playSound(location, Sound.AMBIENT_SOUL_SAND_VALLEY_MOOD, 0.5f, 0.8f)

					return
				}

				// spawna o pet
				petManager.spawnPetEntity(ownerUuid, petUuid, location)
			}
		}
	}

	@EventHandler
	fun onChickenSpawn(event: CreatureSpawnEvent) {
		if (event.spawnReason == CreatureSpawnEvent.SpawnReason.EGG) {
			val nearbySoulEgg = event.location.getNearbyEntitiesByType(Egg::class.java, 1.0).any {
				it.hasMetadata("is_pet_egg")
			}

			if (nearbySoulEgg) {
				Toolkit.log("spawn cancelado")
				event.isCancelled = true
			}
		}
	}

	@EventHandler
	fun onPrepareSoulCraft(event: PrepareItemCraftEvent) {
		val inventory = event.inventory
		val matrix = inventory.matrix

		val soulEgg = matrix.find {
			it != null &&
				it.type == Material.EGG &&
				it.itemMeta.persistentDataContainer.has(PetKeys.IS_SOUL_EGG, PersistentDataType.BOOLEAN)
		}

		// cancela o craft se não tiver o ovo da alma
		if (soulEgg == null) {
			if (event.recipe?.result?.itemMeta?.displayName() == Component.text(Config.localization.chargedSoulEgg)
			) {
				inventory.result = null
			}

			return
		}

		// isso eu fiz na marra, consigo fazer melhor?
		val result = event.inventory.result ?: return

		val resultMeta = result.itemMeta
		val soulMeta = soulEgg.itemMeta

		val soulPDC = soulMeta.persistentDataContainer
		val resultPDC = resultMeta.persistentDataContainer

		val ownerUuid = soulPDC.get(PetKeys.OWNER_UUID, PersistentDataType.STRING) ?: return
		val petUuid = soulPDC.get(PetKeys.PET_UUID, PersistentDataType.STRING) ?: return

		resultPDC.set(PetKeys.IS_SOUL_EGG, PersistentDataType.BOOLEAN, true)
		resultPDC.set(PetKeys.IS_CHARGED_SOUL_EGG, PersistentDataType.BOOLEAN, true)
		resultPDC.set(PetKeys.OWNER_UUID, PersistentDataType.STRING, ownerUuid)
		resultPDC.set(PetKeys.PET_UUID, PersistentDataType.STRING, petUuid)

		result.itemMeta = resultMeta
		result.lore(soulMeta.lore())
		inventory.result = result
	}
}
