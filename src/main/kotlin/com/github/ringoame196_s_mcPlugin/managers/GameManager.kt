package com.github.ringoame196_s_mcPlugin.managers

import com.github.ringoame196_s_mcPlugin.Data
import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.command.CommandSender
import org.bukkit.plugin.Plugin
import kotlin.collections.iterator

class GameManager {
    fun start(sender: CommandSender) {
        if (Data.gameStatus) {
            val message = "${ChatColor.RED}既にゲームスタートしています"
            sender.sendMessage(message)
            return
        }
        if (TargetManager.isEmpty()) {
            val message = "${ChatColor.RED}ターゲットを1つ以上設定してください"
            sender.sendMessage(message)
            return
        }
        val startMessage = "${ChatColor.YELLOW}シューティングゲーム スタート"
        Bukkit.broadcastMessage(startMessage)
        TargetManager.randomSummon()
        Data.gameStatus = true
    }

    fun stop() {
        if (!Data.gameStatus) return

        Bukkit.broadcastMessage("${ChatColor.GOLD}シューティングゲーム 終了")
        // 結果表示
        val sound = Sound.BLOCK_ANVIL_USE
        Bukkit.broadcastMessage("${ChatColor.YELLOW}[結果]")
        for ((player, hitCount) in Data.playerHitData) {
            player.playSound(player, sound, 1f, 1f)
            Bukkit.broadcastMessage("${ChatColor.GREEN}${player.displayName} > ${hitCount}HIT")
        }

        // リセット処理
        Data.target?.remove()
        Data.gameStatus = false
        Data.targetHitCount = 0
        Data.playerHitData.clear()
    }
}
