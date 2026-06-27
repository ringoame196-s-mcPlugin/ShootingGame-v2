package com.github.ringoame196_s_mcPlugin.commands

import com.github.ringoame196_s_mcPlugin.GUN
import com.github.ringoame196_s_mcPlugin.managers.GameManager
import com.github.ringoame196_s_mcPlugin.managers.TargetManager
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class Command(private val plugin: Plugin) : CommandExecutor, TabCompleter {
    private val gameManager = GameManager(plugin)
    private val gun = GUN(plugin)

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val command = args[0]
        when (command) {
            CommandConst.GIVE_COMMAND -> give(sender, args)
            CommandConst.TARGET_COMMAND -> return target(sender, args)
            CommandConst.START_COMMAND -> gameManager.start(sender)
            CommandConst.STOP_COMMAND -> gameManager.stop()
            else -> {
                val message = "${ChatColor.RED}構文が間違っています"
                sender.sendMessage(message)
            }
        }

        return true
    }

    private fun give(sender: CommandSender, args: Array<out String>) {
        val gunItem = gun.makeItem()
        val targetPlayers: List<Player> = if (args.size < 2) {
            if (sender is Player) {
                listOf(sender)
            } else {
                listOf()
            }
        } else {
            Bukkit.selectEntities(sender, args[1]).filterIsInstance<Player>()
        }

        for (targetPlayer in targetPlayers) {
            targetPlayer.inventory.addItem(gunItem)
        }

        val message = "${targetPlayers.size}人に銃を配布しました"
        sender.sendMessage(message)
    }

    private fun target(sender: CommandSender, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("このコマンドはプレイヤーのみ実行可能です")
            return true
        }

        val targetManager = TargetManager(plugin)
        if (args.size < 2) return false

        val subCommand = args[1]
        when (subCommand) {
            CommandConst.ADD_SUB_COMMAND -> targetManager.add(sender)
            CommandConst.REMOVE_SUB_COMMAND -> targetManager.remove(sender)
            CommandConst.LIST_SUB_COMMAND -> targetManager.check(sender)
        }

        return true
    }

    override fun onTabComplete(commandSender: CommandSender, command: Command, label: String, args: Array<out String>): MutableList<String>? {
        return when (args.size) {
            1 -> mutableListOf(
                CommandConst.START_COMMAND,
                CommandConst.STOP_COMMAND,
                CommandConst.TARGET_COMMAND,
                CommandConst.GIVE_COMMAND
            )
            2 -> when (args[0]) {
                CommandConst.TARGET_COMMAND -> mutableListOf(
                    CommandConst.ADD_SUB_COMMAND,
                    CommandConst.REMOVE_SUB_COMMAND,
                    CommandConst.LIST_SUB_COMMAND
                )
                CommandConst.GIVE_COMMAND -> (
                    Bukkit.getOnlinePlayers().map { it.name } + "@a" + "@p" + "@r" + "@s"
                    ).toMutableList()
                else -> mutableListOf()
            }
            else -> mutableListOf()
        }
    }
}
