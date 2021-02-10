package net.kunlab.kwhc.role

import net.kunlab.kwhc.Kwhc
import org.bukkit.Color
import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.LeatherArmorMeta

class RoleManager(val plugin: Kwhc) {
    private val roles = mutableMapOf<Player, RoleInstance>()
    fun get(p: Player): RoleInstance? {
        return roles.getOrDefault(p, null)
    }

    fun getSide(p: Player): Side? {
        return get(p)?.side
    }

    fun isDead(p: Player): Boolean? {
        return get(p)?.isDead
    }

    fun set(p: Player, r: Role) {
        roles[p] = RoleInstance(p, r, r.defaultSide)
    }

    fun setDead(p: Player, b: Boolean) {
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

enum class Role(val displayName: String, val defaultSide: Side, val dyeColor: DyeColor) {
    Kun("Kun", Side.Kun, DyeColor.GREEN),
    Norunoru("のるのる", Side.Kun, DyeColor.PURPLE),
    Detective("探偵", Side.Kun, DyeColor.BLUE),
    Mystic("霊媒師", Side.Kun, DyeColor.WHITE),
    Knight("騎士", Side.Kun, DyeColor.GRAY),
    Tirno("チルノ", Side.Neutral, DyeColor.LIGHT_BLUE),
    Oblivion("忘却者", Side.Neutral, DyeColor.YELLOW),
    Tomas("トーマス", Side.Tomas, DyeColor.PINK),
    Troll("トロール", Side.Troll, DyeColor.RED),
    Commander("コマンド勢", Side.Troll, DyeColor.BLACK),
    None("無名", Side.Kun, DyeColor.LIME),
    Madman("狂人", Side.Troll, DyeColor.CYAN),
    Dead("死人", Side.Dead, DyeColor.MAGENTA);

    fun getHelmet(): ItemStack {
        val i = ItemStack(Material.LEATHER_HELMET, 1)
        val meta = i.itemMeta as LeatherArmorMeta
        meta.setColor(this.dyeColor.color)
        i.itemMeta = meta
        return i
    }

    companion object{
        fun get(color:DyeColor): Role? {
            return values().toList().filter {
                it.dyeColor === color
            }.getOrNull(0)
        }
    }
}

enum class Side(val displayName: String) {
    Kun("Kunサイド"),
    Tomas("妖狐サイド"),
    Troll("トロールサイド"),
    Neutral("中立サイド"),
    Dead("死人サイド")
}