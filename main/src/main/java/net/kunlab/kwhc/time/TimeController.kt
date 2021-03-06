package net.kunlab.kwhc.time

import net.kunlab.kwhc.Kwhc

class TimeController(val plugin: Kwhc) {
    companion object {
        /**
         * 各投票の間の時間
         */
        const val VoteTick = 20 * 60 * 5

        /**
         * 各投票の時間
         */
        const val VoteTime = 20 * 60 * 1
    }

    fun cycleStart() {
        println("cycleStart")
        plugin.timer.register({ doVote() }, VoteTick.toLong())
    }

    fun doVote() {
        println("doVote")
        plugin.vote.start()
        plugin.timer.register({ stopVote() }, VoteTime.toLong())
    }

    fun stopVote() {
        println("stopVote")
        plugin.vote.end()
        cycleStart()
    }
}