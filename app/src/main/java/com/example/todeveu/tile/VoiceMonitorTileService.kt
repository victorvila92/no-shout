package com.example.todeveu.tile

import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.example.todeveu.service.VoiceMonitorService

class VoiceMonitorTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onClick() {
        super.onClick()
        val running = VoiceMonitorService.instance?.state?.value?.isListening == true
        val intent = Intent(this, VoiceMonitorService::class.java).setAction(
            if (running) VoiceMonitorService.ACTION_STOP else VoiceMonitorService.ACTION_START
        )
        startForegroundService(intent)
        updateTile()
    }

    private fun updateTile() {
        val tile = qsTile ?: return
        val running = VoiceMonitorService.instance?.state?.value?.isListening == true
        tile.state = if (running) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = if (running) "Escoltant" else "Escolta veu"
        tile.updateTile()
    }
}
