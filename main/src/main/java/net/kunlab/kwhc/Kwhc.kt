package net.kunlab.kwhc

import net.kunlab.kwhc.flylib.FlyLib
import net.kunlab.kwhc.role.COManager
import net.kunlab.kwhc.role.RoleManager
import net.kunlab.kwhc.shop.ShopInstance
import org.bukkit.plugin.java.JavaPlugin

class Kwhc : JavaPlugin() {
    lateinit var roleManager:RoleManager
    lateinit var coManager :COManager
    lateinit var shop:ShopInstance

    override fun onEnable() {
        // Plugin startup logic
        logger.info("[KWHC]System Start UP Now.")
        FlyLib(this)
        roleManager = RoleManager(this)
        coManager = COManager(this)
        shop = ShopInstance(this)
    }


    override fun onDisable() {
        // Plugin shutdown logic
        logger.info("[KWHC]System Shut Down Now.")
    }
}