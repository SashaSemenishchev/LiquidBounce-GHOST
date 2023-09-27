/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce

import net.ccbluex.liquidbounce.api.ClientUpdate.gitInfo
import net.ccbluex.liquidbounce.api.loadSettings
import net.ccbluex.liquidbounce.cape.CapeService
import net.ccbluex.liquidbounce.event.ClientShutdownEvent
import net.ccbluex.liquidbounce.event.EventManager
import net.ccbluex.liquidbounce.event.EventManager.callEvent
import net.ccbluex.liquidbounce.event.EventManager.registerListener
import net.ccbluex.liquidbounce.event.StartupEvent
import net.ccbluex.liquidbounce.features.command.CommandManager
import net.ccbluex.liquidbounce.features.command.CommandManager.registerCommands
import net.ccbluex.liquidbounce.features.module.ModuleManager
import net.ccbluex.liquidbounce.features.module.ModuleManager.registerModules
import net.ccbluex.liquidbounce.features.module.modules.misc.RenderHider
import net.ccbluex.liquidbounce.features.special.BungeeCordSpoof
import net.ccbluex.liquidbounce.features.special.ClientFixes
import net.ccbluex.liquidbounce.features.special.ClientRichPresence
import net.ccbluex.liquidbounce.features.special.ClientRichPresence.showRichPresenceValue
import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.file.FileManager.loadAllConfigs
import net.ccbluex.liquidbounce.file.FileManager.saveAllConfigs
import net.ccbluex.liquidbounce.lang.LanguageManager.loadLanguages
import net.ccbluex.liquidbounce.script.ScriptManager
import net.ccbluex.liquidbounce.script.ScriptManager.enableScripts
import net.ccbluex.liquidbounce.script.ScriptManager.loadScripts
import net.ccbluex.liquidbounce.script.remapper.Remapper.loadSrg
import net.ccbluex.liquidbounce.tabs.BlocksTab
import net.ccbluex.liquidbounce.tabs.ExploitsTab
import net.ccbluex.liquidbounce.tabs.HeadsTab
import net.ccbluex.liquidbounce.ui.client.GuiClientConfiguration.Companion.updateClientWindow
import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager.Companion.loadActiveGenerators
import net.ccbluex.liquidbounce.ui.client.clickgui.ClickGui
import net.ccbluex.liquidbounce.ui.client.hud.HUD
import net.ccbluex.liquidbounce.ui.font.Fonts.loadFonts
import net.ccbluex.liquidbounce.utils.Background
import net.ccbluex.liquidbounce.utils.ClassUtils.hasForge
import net.ccbluex.liquidbounce.utils.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.ClientUtils.disableFastRender
import net.ccbluex.liquidbounce.utils.InventoryUtils
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.TickedActions
import net.ccbluex.liquidbounce.utils.render.MiniMapRegister
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.DisplayMode
import java.awt.Color
import java.io.File
import java.nio.file.Files
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

object LiquidBounce {

    // Client information
    const val CLIENT_NAME = "LiquidBounce"
    val clientVersionText = gitInfo["git.build.version"]?.toString() ?: "unknown"
    var clientVersionNumber = clientVersionText.substring(1).toIntOrNull() ?: 0 // version format: "b<VERSION>" on legacy
    val clientCommit = gitInfo["git.commit.id.abbrev"]?.let { "git-$it" } ?: "unknown"
    val clientBranch = gitInfo["git.branch"]?.toString() ?: "unknown"
    const val IN_DEV = true
    const val CLIENT_CREATOR = "CCBlueX"
    const val MINECRAFT_VERSION = "1.8.9"
    const val CLIENT_CLOUD = "https://cloud.liquidbounce.net/LiquidBounce"

    val clientTitle = CLIENT_NAME + " " + clientVersionText + " " + clientCommit + "  | " + MINECRAFT_VERSION + if (IN_DEV) " | DEVELOPMENT BUILD" else ""

    var isStarting = true
    val lockfile = File("clientlock")
//    var isLocked: Boolean = true
    val isLocked: Boolean
        get() = RenderHider.state

    var hardLocked: Boolean = false
        get() = lockfile.exists()
    // Managers
    val moduleManager = ModuleManager
    val commandManager = CommandManager
    val eventManager = EventManager
    val fileManager = FileManager
    val scriptManager = ScriptManager
    val executor = Executors.newSingleThreadScheduledExecutor()

    // HUD & ClickGUI
    val hud = HUD

    val clickGui = ClickGui

    // Menu Background
    var background: Background? = null

    // Discord RPC
    val clientRichPresence = ClientRichPresence
    val color = Color(0, 160, 255)

    /**
     * Execute if client will be started
     */
    fun startClient() {
        isStarting = true
        checkLock()
        info("Starting $CLIENT_NAME $clientVersionText $clientCommit, by $CLIENT_CREATOR [GHOST VERSION BY OLEK]")
        // Load languages
        loadLanguages()

        // Register listeners
        registerListener(RotationUtils)
        registerListener(ClientFixes)
        registerListener(BungeeCordSpoof)
        registerListener(CapeService)
        registerListener(InventoryUtils)
        registerListener(MiniMapRegister)
        registerListener(TickedActions)

        // Load client fonts
        loadFonts()

        // Load settings
        loadSettings(false) {
            info("Successfully loaded ${it.count()} settings.")
        }

        // Register commands
        registerCommands()

        // Setup module manager and register modules
        registerModules()

        try {
            // Remapper
            loadSrg()

            // ScriptManager
            loadScripts()
            enableScripts()
        } catch (throwable: Throwable) {
            error("Failed to load scripts.", throwable)
        }

        // Load configs
        loadAllConfigs()

        // Update client window
        updateClientWindow()

        // Tabs (Only for Forge!)
        if (hasForge()) {
            BlocksTab()
            ExploitsTab()
        }

        // Disable optifine fastrender
        disableFastRender()

        // Load alt generators
        loadActiveGenerators()

        // Login into known token if not empty
        if (CapeService.knownToken.isNotBlank()) {
            runCatching {
                CapeService.login(CapeService.knownToken)
            }.onFailure {
                error("Failed to login into known cape token.", it)
            }.onSuccess {
                info("Successfully logged in into known cape token.")
            }
        }

        // Refresh cape service
        CapeService.refreshCapeCarriers {
            info("Successfully loaded ${CapeService.capeCarriers.count()} cape carriers.")
        }

        // Set is starting status
        isStarting = false

        callEvent(StartupEvent())
    }

    fun checkLock(): Boolean {
        val flag = File("clientlock").exists()
        hardLocked = flag
        return flag
    }

    /**
     * Execute if client will be stopped
     */
    fun stopClient() {
        // Call client shutdown
        callEvent(ClientShutdownEvent())

        // Save all available configs
        saveAllConfigs()

        // Shutdown discord rpc
//        clientRichPresence.shutdown()
    }

    fun runLater(delay: Long, command: Runnable) {
        executor.schedule(command, delay, TimeUnit.MILLISECONDS)
    }

    fun info(str: String) {
        if(isLocked) return
        LOGGER.info(str)
    }

    fun error(str: String, throwable: Throwable?=null) {
        if(isLocked) return
        LOGGER.error(str, throwable)
    }
}
