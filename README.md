# PetRespawn 🐾

![Minecraft](https://img.shields.io/badge/Minecraft-Paper%2026.1.2-orange)
![Build Version](https://img.shields.io/badge/version-1.0--SNAPSHOT-orange)
![Build Status](https://img.shields.io/github/actions/workflow/status/alexribeirodesa/paper-petrespawn/.github/workflows/main_build.yml?branch=main&label=build)
![Kotlin](https://img.shields.io/badge/language-Kotlin-purple)
![License](https://img.shields.io/badge/license-MIT-blue)

Give your loyal companions a second chance at life.
**PetRespawn** is a PaperMC plugin designed to ensure that your adventures don't have to end in heartbreak. When a tamed pet dies, its spirit isn't lost forever—instead, it manifests as a powerful artifact known as the **'Soul Egg'**. By using this egg, players can resurrect their favorite pets, preserving all their stats and memories for a brand new journey together.

## ✨ Features
* **Soul Egg Drops:** Every time a tamed pet dies, it drops a unique *Soul Egg* item.
* **Charged Soul Egg:** Combine a *Soul Egg* with *Ghast Tears* to create a *Charged Soul Egg*, unlocking the power of resurrection.
* **Soul Sand Ritual:** Travel to the *Soul Sand Valley* in the Nether to use the *Charged Egg* and bring your friend back
* **Stat Preservation:** Resurrected pets keep all their original attributes and statistics.
* **Seamless Resurrection:** Simply interact with the Soul Egg to bring your companion back to life instantly.
* **Lightweight & Optimized:** Built with Kotlin for high performance and minimal server impact.

## 🛠️ Requirements
*   **Server:** [PaperMC](https://papermc.io/) (Recommended version: 26.1.2)

## 🚀 Installation
1.  Download the latest `.jar` from the releases page.
2.  Ensure **GriefPrevention** is installed on your server.
3.  Drop the `PetRespawn.jar` into your `/plugins` folder.
4.  Restart or reload the server.

## ⌨️ Commands (TODO)
| Command | Description | Permission |
| :--- | :--- | :--- |

## ⚙️ Configuration
The configuration file will be generated at `plugins/PetRespawn/config.yml`.

Currently, it focuses on localization, allowing you to translate all messages sent to players:

This plugin supports **MiniMessage** (Adventure) tags for rich text formatting and internal placeholders for dynamic information.

```yaml
# Language Settings
messages:
  # Soul Egg
  soul-egg: "<white>Soul Egg"
  charged-soul-egg: "<aqua>Charged Soul Egg"
  soul-egg-lore: "<grey>Pet: {petname}"
```

## 🏗️ Development
Built with **Kotlin** for the Paper API.

### Build from source:
```bash
# Clone the repository
git clone https://github.com/alexribeirodesa/paper-petrespawn.git

# Build with Gradle
./gradlew build
```

## ☕ Support the Project
If this plugin helps your server, consider supporting its development!

[![Buy Me A Coffee](https://img.shields.io/badge/Buy%20Me%20A%20Coffee-Donate-yellow?style=for-the-badge&logo=buy-me-a-coffee)](https://www.buymeacoffee.com/elalezito)

*Every coffee helps me keep the "Paper Projects" series updated and free.*