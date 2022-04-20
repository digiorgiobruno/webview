package com.weandlaw.aplicacionlegal

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootBroadcast : BroadcastReceiver() {

    override fun onReceive(ctx: Context, intent: Intent?) {
        ctx.startService(Intent(ctx, MiServicio::class.java))
    }

}