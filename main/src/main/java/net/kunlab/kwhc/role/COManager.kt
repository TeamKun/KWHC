package net.kunlab.kwhc.role

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent
import net.kunlab.kwhc.Kwhc
import org.bukkit.Bukkit
import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.LeatherArmorMeta

class COManager(val plugin: Kwhc):Listener {
    init {
        plugin.server.pluginManager.registerEvents(this,plugin)
    }

    @EventHandler
    fun onWear(e:PlayerArmorChangeEvent){
        if(e.newItem !== null){
            if(e.newItem!!.type === Material.LEATHER_HELMET){
                val meta = e.newItem!!.itemMeta as LeatherArmorMeta
                val role = Role.get(DyeColor.getByColor(meta.color)!!)!!
                onCO(CO(e.player,role))
            }
        }
    }

    private val co = mutableSetOf<CO>()

    fun onCO(c: CO) {
        co.add(c)
        Bukkit.broadcastMessage("${c.p.displayName}が${c.coRole.displayName}COしました")
    }

    fun getCO(p: Player): CO? {
        return co.filter { it.p === p }.getOrNull(0)
    }

    fun giveCOItems(p:Player){
        val list = genCOItems()
        for(i in list.indices){
            p.inventory.setItem(i,list[i])
        }
    }

    fun genCOItems(): MutableList<ItemStack> {
        val list = mutableListOf<ItemStack>()
        Role.values().forEach {
            list.add(it.getHelmet())
        }
        return list
    }
}

data class CO(val p: Player, val coRole: Role)
