package net.kunlab.kwhc.flylib

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class TabPart {
    companion object {
        val selectors = TabObject("@a", "@r", "@s", "@e")
        val playerSelector = PlayerSelector()
    }

    class PlayerSelector : TabObject() {
        override fun getAsList(): MutableList<String> = Bukkit.getOnlinePlayers().map { it.displayName }.toMutableList()
    }
}

class SmartTabCompleter(vararg val chains: TabChain) : TabCompleter {
    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String> {
        val strings = args.toMutableList().map { it as String }.toTypedArray()
        val list = mutableListOf<String>()
        chains
            .filter { it.isExist(strings) }
            .forEach { chain ->
                chain.getMatched(strings)!!.getAsList()
                    .forEach { list.add(it) }
            }
        if (list.isEmpty()) list.add("")
        return list
    }
}

/**
 * The one way of command,
 * like,(TabObject("kill","heal","give"),TabObject("@a","@r","@s","@e"))
 */
class TabChain(vararg val tabObjects: TabObject) {

    fun getMatched(args: Array<String>): TabObject? {
        return try {
            tabObjects[args.lastIndex]
        } catch (e: Exception) {
            null
        }
    }

    fun isExist(args: Array<String>) = getMatched(args) != null
}

/**
 * Part Of Tab Chain,
 * like ("@a","@r","@s","@e")
 * this will provide the player selector.
 */
open class TabObject {
    private val strings: MutableList<String> = mutableListOf()
    private val objects: MutableList<TabObject> = mutableListOf()

    constructor(vararg args: String) {
        strings.addAll(args)
    }

    constructor(args: List<String>) {
        strings.addAll(args)
    }

    constructor(vararg objs: TabObject) {
        objects.addAll(objs)
    }

    // Only For Override
    constructor()

    open fun getAsList(): MutableList<String> {
        val list = mutableListOf<String>()
        list.addAll(strings)
        objects.map { it.getAsList() }.forEach { obj -> obj.forEach { list.add(it) } }
        return list
    }
}