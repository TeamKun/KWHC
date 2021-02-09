package net.kunlab.kwhc.role

import net.kunlab.kwhc.Kwhc
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

class RoleManager(val plugin: Kwhc) {
    private val roles = mutableMapOf<Player, RoleInstance>()
    fun get(p: Player): RoleInstance? {
        return roles.getOrDefault(p, null)
    }

    fun getSide(p: Player): Side? {
        return get(p)?.side
    }

    fun isDead(p:Player): Boolean? {
        return get(p)?.isDead
    }

    fun set(p: Player, r: Role) {
        roles[p] = RoleInstance(p, r, r.defaultSide)
    }

    fun setDead(p:Player,b:Boolean){
        roles[p]?.isDead = b
    }
}

class RoleInstance(val p: Player, val baseRole: Role, var side: Side) {
    lateinit var deathInfo: DeathInfo
    var isDead = false
        set(b: Boolean) {
            field = b
            if (b) {
                deathInfo = DeathInfo(p, baseRole, side, p.inventory)
            }
        }
}

data class DeathInfo(val player: Player, val role: Role, val side: Side, val deadInventory: Inventory)

enum class Role(val displayName: String, val defaultSide: Side) {
    Kun("Kun", Side.Kun),
    Norunoru("のるのる", Side.Kun),
    Detective("探偵", Side.Kun),
    Mystic("霊媒師", Side.Kun),
    Knight("騎士", Side.Kun),
    Tirno("チルノ", Side.Neutral),
    Oblivion("忘却者", Side.Neutral),
    Tomas("トーマス", Side.Tomas),
    Troll("トロール", Side.Troll),
    Commander("コマンド勢", Side.Troll),
    None("無名", Side.Kun),
    Madman("狂人", Side.Troll),
    Dead("死人", Side.Dead)
}

enum class Side(val displayName: String) {
    Kun("Kunサイド"),
    Tomas("妖狐サイド"),
    Troll("トロールサイド"),
    Neutral("中立サイド"),
    Dead("死人サイド")
}