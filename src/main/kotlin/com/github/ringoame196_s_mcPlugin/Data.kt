package com.github.ringoame196_s_mcPlugin

import org.bukkit.Location
import org.bukkit.entity.Pig
import org.bukkit.entity.Player

object Data {
    var gameStatus: Boolean = false
    val targetList = mutableSetOf<Location>()
    var firingRangeDistance: Double = 30.0
    var maxBullet: Int = 15
    var target: Pig? = null
    var targetGoal = 20
    var targetHitCount = 0

    val playerHitData = mutableMapOf<Player, Int>()
}
