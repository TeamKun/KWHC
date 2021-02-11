package net.kunlab.kwhc

import net.kunlab.kwhc.flylib.FlyLib
import net.kunlab.kwhc.role.COManager
import net.kunlab.kwhc.role.RoleManager
import net.kunlab.kwhc.shop.ShopCommand
import net.kunlab.kwhc.shop.ShopInstance
import net.kunlab.kwhc.time.TimeController
import net.kunlab.kwhc.util.Timer
import net.kunlab.kwhc.vote.VoteCommand
import net.kunlab.kwhc.vote.VoteManager
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class Kwhc : JavaPlugin() {
    lateinit var timer: Timer
    lateinit var timeController: TimeController
    lateinit var roleManager: RoleManager
    lateinit var coManager: COManager
    lateinit var shop: ShopInstance
    lateinit var vote: VoteManager
    var isGoingOn = false

    override fun onEnable() {
        // Plugin startup logic
        onInit()
    }

    fun onInit() {
        logger.info("[KWHC]System Start UP Now.")
        FlyLib(this)
        timer = Timer(this)
        timeController = TimeController(this)
        roleManager = RoleManager(this)
        coManager = COManager(this)
        shop = ShopInstance(this)
        vote = VoteManager(this)
        getCommand("v")!!.setExecutor(VoteCommand(this))
        getCommand("s")!!.setExecutor(ShopCommand(this))
    }

    fun onStart() {
        //TODO Role自動割り振り
        isGoingOn = true
        timeController.cycleStart()
    }

    fun onEnd() {
        isGoingOn = false
        timeController = TimeController(this)
    }


    override fun onDisable() {
        // Plugin shutdown logic
        logger.info("[KWHC]System Shut Down Now.")
    }
}

class MainCommand(val plugin: Kwhc) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        return if (sender is Player) {
            if (sender.isOp) run(sender, command, label, args)
            else false
        } else run(sender, command, label, args)
    }

    fun run(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        when (args.size) {
            1 -> {
                when (args[0]) {
                    "s", "start" -> {
                        onStart()
                    }

                    "e", "end" -> {
                        onEnd()
                    }
                    else -> return false
                }
                return true
            }
            else -> return false
        }
    }

    fun onStart() {
        plugin.onStart()
    }

    fun onEnd() {
        plugin.onEnd()
    }
}