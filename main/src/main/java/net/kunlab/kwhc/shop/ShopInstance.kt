package net.kunlab.kwhc.shop

import com.flylib.util.NaturalNumber
import net.kunlab.kwhc.Kwhc
import net.kunlab.kwhc.flylib.ChestGUI
import net.kunlab.kwhc.flylib.GUIObject
import net.kunlab.kwhc.role.Side
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import kotlin.math.min

class ShopInstance(val plugin: Kwhc) {
    companion object {
        /**
         * Get Number of Player Coins
         */
        val coin = Material.GOLD_INGOT
        fun getPlayerValue(p: Player): Int {
            var value = 0
            p.inventory.forEach {
                if (it.type == coin) {
                    value += it.amount
                }
            }
            return value
        }

        /**
         * Add Coins to Player
         */
        fun gainPlayerValue(p: Player, amount: NaturalNumber) {
            val i = amount.i
            p.inventory.addItem(ItemStack(coin, i))
        }

        fun losePlayerValue(p: Player, amount: NaturalNumber): Boolean {
            val i = amount.i
            if (getPlayerValue(p) < i) {
                return false
            } else {
                var removed = 0
                for (i1 in 0 until p.inventory.size) {
                    val item = p.inventory.getItem(i1)
                    if (item != null) {
                        if (item.type == coin) {
                            val count = min(item.amount, i - removed)
                            item.amount -= count
                            removed += count
                        }
                    }

                    if (removed == i) break
                }
                if (removed != i) p.sendMessage("Something went wrong in ShopInstance")
                return true
            }
        }
    }

    private fun canBuy(p: Player, item: ShopItem) = getPlayerValue(p) >= item.value

    fun buy(p: Player, item: ShopItem) {
        if (item.side(p, plugin)) {
            if (canBuy(p, item)) {
                losePlayerValue(p, NaturalNumber(item.value))
                item.itemGenerator().forEach { p.inventory.addItem(it) }
            } else {
                p.sendMessage("You don't have enough money to buy it.")
            }
        } else {
            p.sendMessage("You can't buy it.")
        }
    }

    fun open(p: Player) {
        val gui = ShopGUI(p, plugin)
        gui.open()
    }

    class ShopGUI(p: Player, val plugin: Kwhc) {
        val gui = ChestGUI(p, NaturalNumber(4), "${plugin.roleManager.get(p)!!.baseRole.displayName}用ショップ")

        init {
            ShopItem.values()
                .filter {
                    it.side(p, plugin)
                }
                .forEachIndexed { i, item ->
                    gui.addGUIObject(
                        GUIObject(
                            NaturalNumber(i % 8 + 1),
                            NaturalNumber(i / 8 + 1),
                            item.itemGenerator()[0]
                        ).addCallBack(::onClick)
                    )
                }
        }

        private fun onClick(e: InventoryClickEvent) {
            if (e.currentItem != null) {
                val shopItem = ShopItem.getFromStack(e.currentItem!!)
                if (shopItem == null) {
                    println("Something went wrong in ShopGUI");return
                }
                plugin.shop.buy(e.whoClicked as Player, shopItem)
            }
        }

        fun open() {
            gui.open()
        }
    }
}

val all = { _: Player, _: Kwhc -> true }

enum class ShopItem(
    val itemGenerator: () -> List<ItemStack>,
    val value: Int,
    val side: (Player, Kwhc) -> Boolean
) {
    // SWORDS

    WOODEN_SWORD(
        { listOf(ItemStack(Material.WOODEN_SWORD)) },
        1,
        all
    ),
    STONE_SWORD(
        { listOf(ItemStack(Material.STONE_SWORD)) },
        2,
        all
    ),


    TNT(
        { listOf(ItemStack(Material.TNT, 3)) },
        1,
        { p: Player, k: Kwhc -> k.roleManager.get(p)?.side === Side.Troll }
    ),

    BOW_ARROW(
        {
            listOf(ItemStack(Material.BOW), ItemStack(Material.ARROW))
        },
        1,
        { p: Player, k: Kwhc ->
            net.kunlab.kwhc.util.nullOrDefault(
                k.roleManager.get(p)?.baseRole === net.kunlab.kwhc.role.Role.Commander,
                false
            )
        }
    ),

    SONAR(
        {
            listOf(ItemStack(Material.COMPASS))
        },
        1,
        { p: Player, k: Kwhc ->
            net.kunlab.kwhc.util.nullOrDefault(
                k.roleManager.get(p)?.baseRole === net.kunlab.kwhc.role.Role.Commander,
                false
            )
        }
    ),

    DETECTIVE_BOOK(
        {
            listOf(ItemStack(Material.BOOK))
        },
        1,
        { p: Player, k: Kwhc ->
            net.kunlab.kwhc.util.nullOrDefault(
                k.roleManager.get(p)?.baseRole === net.kunlab.kwhc.role.Role.Detective,
                false
            )
        }
    ),

    READER(
        {
            listOf(ItemStack(Material.COMPASS))
        },
        1,
        { p: Player, k: Kwhc ->
            net.kunlab.kwhc.util.nullOrDefault(
                k.roleManager.get(p)?.baseRole === net.kunlab.kwhc.role.Role.Detective,
                false
            )
        }
    ),

    MysticPray(
        {
            listOf(ItemStack(Material.STICK))
        },
        1,
        { p: Player, k: Kwhc ->
            net.kunlab.kwhc.util.nullOrDefault(
                k.roleManager.get(p)?.baseRole === net.kunlab.kwhc.role.Role.Mystic,
                false
            )
        }
    ),

    KnightPray(
        {
            listOf(ItemStack(Material.STICK))
        },
        1,
        { p: Player, k: Kwhc ->
            net.kunlab.kwhc.util.nullOrDefault(
                k.roleManager.get(p)?.baseRole === net.kunlab.kwhc.role.Role.Knight,
                false
            )
        }
    ),

    KnightSword(
        {
            val item = ItemStack(Material.STONE_SWORD)
            val meta = item.itemMeta
            meta.addEnchant(org.bukkit.enchantments.Enchantment.DAMAGE_ALL, 1, true)
            item.itemMeta = meta
            listOf(item)
        },
        1,
        { p: Player, k: Kwhc ->
            net.kunlab.kwhc.util.nullOrDefault(
                k.roleManager.get(p)?.baseRole === net.kunlab.kwhc.role.Role.Knight,
                false
            )
        }
    );

    companion object {
        fun getFromStack(stack: ItemStack): ShopItem? {
            return values().map { Pair(it, it.itemGenerator()) }
                .filter {
                    var b = false
                    it.second.forEach { s ->
                        b = b || stack.isSimilar(s)
                    }
                    b
                }.map { it.first }
                .getOrNull(0)
        }
    }
}

class ShopCommand(val plugin:Kwhc):CommandExecutor{
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        return if(sender is Player){
            val r = plugin.roleManager.get(sender)
            if(r!=null){
                plugin.shop.open(r.p)
                true
            }else{
                sender.sendMessage("ゲームに参加していないため、ショップを表示できません")
                true
            }
        }else{
            sender.sendMessage("サーバーからは実行できません")
            true
        }
    }
}