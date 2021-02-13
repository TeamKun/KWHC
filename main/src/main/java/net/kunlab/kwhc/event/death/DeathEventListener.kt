package net.kunlab.kwhc.event.death

import net.kunlab.kwhc.Kwhc
import net.kunlab.kwhc.role.Role
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractEvent

class DeathEventListener(val plugin: Kwhc) : Listener {
    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
        plugin.server.scheduler.runTaskTimer(plugin, Runnable { onTick() }, 1, 1)
    }

    @EventHandler
    fun onPlayerDeath(e: PlayerDeathEvent) {
        if (plugin.isGoingOn) {
            val role = plugin.roleManager.get(e.entity)
            if (role != null) {
                val loc = e.entity.location
                loc.block.type = Material.OAK_SIGN
                if (loc.block.state is Sign) {
                    val sign = loc.block.state as Sign
                    deathInfos.add(
                        SignInstance(
                            loc, sign, PlayerDeathInfo(
                                e.entity, role.baseRole,
                                DeathReason(
                                    "",
                                    e.entity.killer
                                ),
                                0
                            )
                        )
                    )
                } else {
                    println("Sign is not Sign")
                }
                plugin.roleManager.setDead(e.entity, true)
            }
        }
    }

    @EventHandler
    fun onClick(e: PlayerInteractEvent) {
        if (plugin.isGoingOn) {
            if (e.action === Action.RIGHT_CLICK_BLOCK) {
                if (e.clickedBlock!!.type === Material.OAK_SIGN) {
                    val sign = e.clickedBlock!!.state as Sign
                    deathInfos.filter {
                        it.loc.blockX == sign.location.blockX &&
                        it.loc.blockY == sign.location.blockY &&
                        it.loc.blockZ == sign.location.blockZ
                    }.forEach {
                        it.sendData(e.player, plugin)
                    }
                }
            }
        }
    }

    fun onTick() {
        if (plugin.isGoingOn) {
            deathInfos.forEach {
                it.info.deathDeltaTime++
            }
        }
    }

    val deathInfos = mutableListOf<SignInstance>()
}

class SignInstance(val loc: Location, val sign: Sign, val info: PlayerDeathInfo) {
    fun isExist(world: World) = world.getBlockAt(loc).type === Material.OAK_SIGN

    /**
     * Send DeathInfos to Player Chat
     */
    fun sendData(p: Player, plugin: Kwhc) {
        val role = plugin.roleManager.get(p)
        if (role != null) {
            p.sendMessage("" + ChatColor.BLUE + "-----------------------------")
            p.sendMessage("" + ChatColor.BLUE + "名前:${info.player.displayName}")
            p.sendMessage("" + ChatColor.BLUE + "死亡時刻:${kotlin.math.abs(info.deathDeltaTime / 20)}秒前")
            if (role.baseRole == Role.Mystic) {
                p.sendMessage("" + ChatColor.BLUE + "役職:${info.role.displayName}")
            } else {
                p.sendMessage("" + ChatColor.BLUE + "役職:???")
            }
            p.sendMessage("" + ChatColor.BLUE + "-----------------------------")
        }
    }
}

data class PlayerDeathInfo(
    val player: Player,
    val role: Role,
    val deathReason: DeathReason,
    var deathDeltaTime: Long
)

class DeathReason(val s: String, val killedPlayer: Player?)