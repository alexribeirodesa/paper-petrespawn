package org.elalezito.petRespawn

import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.plugin.java.JavaPlugin
import org.elalezito.petRespawn.objects.Config
import org.elalezito.petRespawn.objects.PetKeys
import org.elalezito.swissKnife.objects.Toolkit

class PetRespawn : JavaPlugin() {
	lateinit var petManager: PetManager

	override fun onEnable() {
		Toolkit.setPlugin(this)
		PetKeys.init(this)

		Config.init(this)

		// registra comandos
		val manager = this.lifecycleManager
		manager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
			val commands = event.registrar()

			commands.register(
				Commands.literal("petrespawn")
					.then(
						Commands.literal("reload")
							.requires { source -> source.sender.hasPermission("petrespawn.admin") }
							.executes { context ->
								Config.load()

								return@executes 1
							}
					).build(),
				Config.localization.commandReloadDescription,
				listOf("pr")
			)
		}

		// inicia gerenciadores
		petManager = PetManager(this)
		petManager.registerChargedSoulEggRecipe()

		// registra eventos
		val playerListener = PlayerListener(this, petManager)
		server.pluginManager.registerEvents(playerListener, this)
	}

	override fun onDisable() {
		// Plugin shutdown logic
	}
}
