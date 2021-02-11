package net.kunlab.kwhc.util

import net.kunlab.kwhc.Kwhc

class Timer(val plugin:Kwhc) {
    init {
        plugin.server.scheduler.runTaskTimer(plugin, Runnable { onTick() }, 1, 1)
    }

    private val queue = mutableMapOf<Runnable,Long>()

    fun onTick() {
        val toRemove = mutableListOf<Runnable>()
        queue.filter { it.value <= 0 }.forEach { it.key.run();toRemove.add(it.key) }
        toRemove.forEach { queue.remove(it) }
        queue.forEach{ queue[it.key] = queue[it.key]!! - 1 }
    }

    fun register(r:Runnable,time:Long){
        queue[r]=time
    }

    fun remove(r:Runnable){
        queue.remove(r)
    }
}