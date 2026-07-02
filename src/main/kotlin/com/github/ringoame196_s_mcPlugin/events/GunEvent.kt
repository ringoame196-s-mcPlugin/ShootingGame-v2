package com.github.ringoame196_s_mcPlugin.events

import com.github.ringoame196_s_mcPlugin.Data
import com.github.ringoame196_s_mcPlugin.GUN
import com.github.ringoame196_s_mcPlugin.managers.GameManager
import com.github.ringoame196_s_mcPlugin.managers.GunManager
import com.github.ringoame196_s_mcPlugin.managers.TargetManager
import org.bukkit.Sound
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.plugin.Plugin

class GunEvent(plugin: Plugin) : Listener {
    private val gunManager = GunManager(plugin)
    private val gun = GUN(plugin)
    private val gameManager = GameManager(plugin)

    @EventHandler
    fun onPlayerInteract(e: PlayerInteractEvent) {
        val player = e.player
        val item = e.item ?: return
        val action = e.action

        if (!gunManager.checkGun(item)) return
        e.isCancelled = true
        if (player.hasCooldown(item.type)) return

        when (action) {
            Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK -> {
                if (gunManager.acquisitionBullet(item) <= 0) {
                    val sound = Sound.BLOCK_DISPENSER_DISPENSE
                    player.playSound(player, sound, 1f, 1f)
                    return
                }
                val hitEntity = gun.shot(player, item) ?: return
                if (hitEntity != Data.target) return
                hit(hitEntity, player)
            }
            else -> {
                gun.reload(item)
                val sound = Sound.BLOCK_IRON_DOOR_OPEN
                player.playSound(player, sound, 1f, 1f)
                player.setCooldown(item.type, 20)
            }
        }
    }

    private fun hit(hit: LivingEntity, player: Player) {
        val sound = Sound.ENTITY_ARROW_HIT_PLAYER
        hit.remove()
        player.playSound(player, sound, 1f, 1f)

        // プレイヤーデータ
        val playerData = Data.playerHitData[player] ?: 0
        Data.playerHitData[player] = playerData + 1

        Data.targetHitCount ++
        if (Data.targetHitCount >= Data.targetGoal) gameManager.stop()
        else TargetManager.randomSummon()
    }
}
