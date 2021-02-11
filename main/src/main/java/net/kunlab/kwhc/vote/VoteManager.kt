package net.kunlab.kwhc.vote

import net.kunlab.kwhc.Kwhc
import net.kunlab.kwhc.role.RoleInstance
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class VoteManager(val plugin: Kwhc) : Listener{
    companion object {
        val effectType = listOf(
            PotionEffectType.SLOW,
            PotionEffectType.BLINDNESS
        )

        val effect = effectType.map { PotionEffect(it, 9999, 5) }
    }

    init {
        plugin.server.pluginManager.registerEvents(this,plugin)
    }

    /**
     * 投票スタート用
     * 自動的に動けないデバフを付与
     */
    fun start() {
        Bukkit.getOnlinePlayers()
            .mapNotNull { plugin.roleManager.get(it) }
            .filter { !it.isDead }
            .forEach {
                effectStart(it)
            }
        votes = mutableMapOf()
        Bukkit.broadcastMessage("投票開始!")
    }

    /**
     * 投票終了用
     * 自動的にデバフを除去
     */
    fun end() {
        Bukkit.getOnlinePlayers()
            .mapNotNull { plugin.roleManager.get(it) }
            .filter { !it.isDead }
            .forEach {
                effectEnd(it)
            }
        Bukkit.broadcastMessage("投票終了!")
        onEnd()
    }

    private fun effectStart(instance: RoleInstance) {
        effect.forEach {
            instance.p.addPotionEffect(it)
        }
    }

    private fun effectEnd(instance: RoleInstance) {
        effectType.forEach {
            instance.p.removePotionEffect(it)
        }
    }

    /**
     * 投票データ
     */
    var votes = mutableMapOf<RoleInstance, RoleInstance>()

    /**
     * 処刑された人リスト
     */
    val executioner = mutableListOf<RoleInstance>()

    fun onVote(from: RoleInstance, to: RoleInstance) {
        Bukkit.broadcastMessage("${from.p.displayName}が${to.p.displayName}に投票しました")
        votes[from] = to
    }

    /**
     * 投票結果開示
     */
    private fun onEnd() {
        val voted = mutableMapOf<RoleInstance, Int>()
        votes.forEach { (_, to) -> voted[to] = voted[to]!! + 1 }
        val sorted = voted.map { Pair(it.key, it.value) }.sortedBy { it.second }.toMutableList()
        Bukkit.broadcastMessage("投票結果")
        sorted
            .filter {
                it.second != 0
            }
            .forEachIndexed { i, it -> Bukkit.broadcastMessage("${i + 1}位 ${it.first.p.displayName}:${it.second}票") }
        executioner.add(sorted[0].first)
        sorted[0].first.p.health = 0.0
    }

    /**
     * 処刑時用デスメッセージ
     */
    @EventHandler
    fun onDeath(p:PlayerDeathEvent){
        p.deathMessage = ""
        val role = plugin.roleManager.get(p.entity)
        if(role != null){
            if(executioner.contains(role)){
                Bukkit.broadcastMessage("${p.entity.displayName}は処刑された")
            }else{
                println("${p.entity.displayName}が処刑以外の理由で死亡")
            }
        }
    }
}

class VoteCommand(val plugin: Kwhc) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player) {
            val from = plugin.roleManager.get(sender)
            if (from != null) {
                if (args.size != 1) return false
                val to = plugin.roleManager.get(Bukkit.selectEntities(sender, args[0])[0] as Player)
                if (to != null) {
                    plugin.vote.onVote(from, to)
                } else {
                    sender.sendMessage("投票先が無効です")
                    return true
                }
            } else {
                sender.sendMessage("ゲームに参加していないため、投票できません")
                return true
            }
        }
        return false
    }
}