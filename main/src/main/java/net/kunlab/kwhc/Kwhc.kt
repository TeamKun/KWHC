package net.kunlab.kwhc

import net.kunlab.kwhc.event.death.DeathEventListener
import net.kunlab.kwhc.flylib.*
import net.kunlab.kwhc.role.COManager
import net.kunlab.kwhc.role.Role
import net.kunlab.kwhc.role.RoleManager
import net.kunlab.kwhc.shop.ShopCommand
import net.kunlab.kwhc.shop.ShopInstance
import net.kunlab.kwhc.time.TimeController
import net.kunlab.kwhc.util.Timer
import net.kunlab.kwhc.vote.VoteCommand
import net.kunlab.kwhc.vote.VoteManager
import org.bukkit.Bukkit
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
    lateinit var deathEventListener: DeathEventListener
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
        deathEventListener = DeathEventListener(this)
        getCommand("v")!!.setExecutor(VoteCommand(this))
        getCommand("s")!!.setExecutor(ShopCommand(this))
        getCommand("k")!!.setExecutor(MainCommand(this))
        getCommand("r")!!.setExecutor(SetRoleCommand(this))
        getCommand("r")!!.tabCompleter = SetRoleCommand(this).generateTabCompleter()
        getCommand("c")!!.setExecutor(CheckRoleCommand(this))
        getCommand("skip")!!.setExecutor(SkipToVoteCommand(this))
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
        Bukkit.broadcastMessage("ゲーム開始!")
        plugin.onStart()
    }

    fun onEnd() {
        Bukkit.broadcastMessage("ゲーム終了!")
        plugin.onEnd()
    }
}

class SetRoleCommand(val plugin: Kwhc) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        return if (sender is Player) {
            if (sender.isOp) run(sender, command, label, args)
            else false
        } else run(sender, command, label, args)
    }

    fun run(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        when (args.size) {
            1 -> {
                if (sender is Player) {
                    val role = Role.get(args[0])
                    if (role != null) {
                        plugin.roleManager.set(sender, role)
                    } else {
                        sender.sendMessage("役職が見つかりません")
                        return false
                    }
                } else {
                    sender.sendMessage("サーバーからは利用できません")
                }
            }
            2 -> {
                val p = Bukkit.selectEntities(sender, args[0])
                if (p.isNotEmpty() && p is Player) {
                    val role = Role.get(args[1])
                    if (role != null) {
                        plugin.roleManager.set(p, role)
                    } else {
                        sender.sendMessage("役職が見つかりません")
                        return false
                    }
                } else {
                    sender.sendMessage("Player NotFound!")
                }
            }
            else -> return false
        }
        return true
    }

    fun generateTabCompleter() = SmartTabCompleter(
        TabChain(
            Role.getTabObject()
        ),
        TabChain(
            TabPart.playerSelector,
            Role.getTabObject()
        )
    )
}

class CheckRoleCommand(val plugin: Kwhc) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player) {
            if (sender.isOp) {
                return run(sender, command, label, args)
            }
        }
        return false
    }

    fun run(sender: Player, command: Command, label: String, args: Array<out String>): Boolean {
        when (args.size) {
            0 -> {
                val role = plugin.roleManager.get(sender)
                if (role == null) {
                    sender.sendMessage("Role is not Inited")
                } else {
                    sender.sendMessage("Role:${role.baseRole.displayName} isDead:${role.isDead}")
                }
                return true
            }
            1 -> {
                val p = Bukkit.selectEntities(sender, args[0]).getOrNull(0)
                return if (p == null) {
                    sender.sendMessage("Player not Found!")
                    false
                } else if (p is Player) {
                    val role = plugin.roleManager.get(p)
                    if (role == null) {
                        sender.sendMessage("Role is not Inited")
                    } else {
                        sender.sendMessage("Role:${role.baseRole.displayName} isDead:${role.isDead}")
                    }
                    true
                } else {
                    sender.sendMessage("It's not Player!")
                    false
                }
            }
            else -> {
                return false
            }
        }
    }
}

class SkipToVoteCommand(val plugin: Kwhc):CommandExecutor{
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if(sender is Player){
            if(sender.isOp) return run(sender, command, label, args)
        }else return run(sender, command, label, args)
        return false
    }

    fun run(sender: CommandSender, command: Command, label: String, args: Array<out String>):Boolean{
        Bukkit.broadcastMessage("投票までスキップ～！！！！！！")
        plugin.timer.removeAllQue()
        plugin.timeController.doVote()
        return true
    }
}