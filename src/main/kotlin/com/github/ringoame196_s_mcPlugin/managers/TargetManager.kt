package com.github.ringoame196_s_mcPlugin.managers

import com.github.ringoame196_s_mcPlugin.Data
import com.github.ringoame196_s_mcPlugin.DataBaseManager
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.Pig
import org.bukkit.entity.Player

object TargetManager {
    private lateinit var db: DataBaseManager

    fun init(database: DataBaseManager) {
        db = database
    }

    fun add(player: Player) {
        Data.targetList.add(player.location.block.location)
        saveFile() // ymlに保存

        val message = "${ChatColor.AQUA}ターゲットを追加しました"
        player.sendMessage(message)
    }

    fun remove(player: Player) {
        val location = player.location.block.location

        if (Data.targetList.contains(location)) {
            Data.targetList.remove(location)
            saveFile() // ymlに保存
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

    fun saveFile() {
        val targetList = Data.targetList
        val list = mutableListOf<String>()

        for (target in targetList) {
            list.add("${target.world?.name},${target.x},${target.y},${target.z}")
        }

        ymlFileManager.setValue(file, saveKey, list)
    }

    fun loadFile() {
        Data.targetList.clear()
        val list = ymlFileManager.acquisitionListValue(file, saveKey) ?: return

        for (value in list) {
            Data.targetList.add(parseLocation(value) ?: continue)
        }
    }

    private fun parseLocation(input: String): Location? {
        val parts = input.split(",")
        if (parts.size != 4) return null

        val world = Bukkit.getWorld(parts[0]) ?: return null
        val x = parts[1].toDoubleOrNull() ?: return null
        val y = parts[2].toDoubleOrNull() ?: return null
        val z = parts[3].toDoubleOrNull() ?: return null

        return Location(world, x, y, z)
    }
}
