package net.kunlab.kwhc

import net.kunlab.kwhc.role.RoleManager
import org.bukkit.plugin.java.JavaPlugin

class Kwhc : JavaPlugin() {
    lateinit var roleManager:RoleManager

    override fun onEnable() {
        // Plugin startup logic
        logger.info("[KWHC]System Start UP Now.")
        roleManager = RoleManager(this)
    }


    override fun onDisable() {
        // Plugin shutdown logic
        logger.info("[KWHC]System Shut Down Now.")
    }
}