package com.github.ringoame196_s_mcPlugin.managers

import com.github.ringoame196_s_mcPlugin.Data
import com.github.ringoame196_s_mcPlugin.DataBaseManager
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.Pig
import org.bukkit.entity.Player

object TargetManager {
    private lateinit var db: DataBaseManager
    private const val TABLE_NAME = "targets"
    private const val WORLD_NAME_KEY = "world_name"
    private const val X_KEY = "x"
    private const val Y_KEY = "y"
    private const val Z_KEY = "z"

    fun init(database: DataBaseManager) {
        db = database
    }

    fun add(player: Player) {
        val locaiton = player.location.block.location
        Data.targetList.add(locaiton)
        addDB(locaiton)

        val message = "${ChatColor.AQUA}ターゲットを追加しました"
        player.sendMessage(message)
    }

    fun remove(player: Player) {
        val location = player.location.block.location

        if (Data.targetList.contains(location)) {
            Data.targetList.remove(location)
            removeDB(location)
            val message = "${ChatColor.YELLOW}現在の位置のターゲットを削除しました"
            player.sendMessage(message)
        } else {
            val message = "${ChatColor.RED}現在の位置は登録されていません"
            player.sendMessage(message)
        }
    }

    fun check(player: Player) {
        player.sendMessage("${ChatColor.YELLOW}[ターゲット一覧]")
        for (target in Data.targetList) {
            val command = "/tp @s ${target.x} ${target.y} ${target.z}"

            val prefix = TextComponent("${target.world?.name} ${target.x} ${target.y} ${target.z}")

            val clickable = TextComponent("[クリック]")
            clickable.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, command)
            clickable.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder("${ChatColor.YELLOW}クリックでtp").create())
            clickable.color = net.md_5.bungee.api.ChatColor.YELLOW

            prefix.addExtra(clickable)
            player.spigot().sendMessage(prefix)
        }
    }

    fun randomSummon() {
        val name = "${ChatColor.GREEN}ターゲット"
        val location = Data.targetList.random()
        val pig: Pig? = location.world?.spawn(location.clone().add(0.5, 0.0, 0.5), Pig::class.java)
        pig?.let {
            // ゾンビの設定
            it.setAI(false)
            it.customName = name
            it.isCustomNameVisible = true
        }
        Data.target = pig
    }

    private fun addDB(location: Location) {
        val worldName = location.world
        val x = location.x
        val y = location.y
        val z = location.z

        val sql = "INSERT INTO $TABLE_NAME ($WORLD_NAME_KEY,$X_KEY,$Y_KEY,$Z_KEY) VALUES (?,?,?,?);"
        db.executeUpdate(sql, mutableListOf(worldName, x, y, z))
    }

    private fun removeDB(location: Location) {
        val worldName = location.world
        val x = location.x
        val y = location.y
        val z = location.z

        val sql = "DELETE FROM $TABLE_NAME WHERE $WORLD_NAME_KEY = ? AND $X_KEY = ? AND $Y_KEY = ? AND $Z_KEY =?;"
        db.executeUpdate(sql, mutableListOf(worldName, x, y, z))
    }
}
