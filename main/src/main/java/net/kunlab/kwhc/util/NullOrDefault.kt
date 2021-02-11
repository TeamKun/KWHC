package net.kunlab.kwhc.util

fun <T> nullOrDefault(o:T?,d:T): T {
    return o ?: d
}