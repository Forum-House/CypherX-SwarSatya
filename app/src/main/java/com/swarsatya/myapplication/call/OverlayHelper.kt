
package com.swarsatya.myapplication.call

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView

object OverlayHelper {

    private var overlayView: LinearLayout? = null

    fun canDrawOverlay(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else true
    }

    fun askOverlayPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    fun showBlacklistWarning(context: Context, number: String) {
        show(context, "BLACKLISTED CALLER\n$number\nDo not share OTP/money", Color.RED)
    }

    fun showHighRisk(context: Context, number: String, score: Float) {
        val pct = (score * 100).toInt()
        show(context, "AI VOICE CLONE SUSPECTED\n$number\nRisk: $pct%", Color.RED)
    }

    fun showSafe(context: Context, number: String, score: Float) {
        val pct = (score * 100).toInt()
        show(context, "Monitoring call\n$number\nRisk: $pct%", Color.parseColor("#166534"))
    }

    fun showInfo(context: Context, msg: String) {
        show(context, msg, Color.parseColor("#1E3A8A"))
    }

    fun hide(context: Context) {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        overlayView?.let {
            try { wm.removeView(it) } catch (_: Exception) {}
        }
        overlayView = null
    }

    private fun show(context: Context, text: String, bg: Int) {
        if (!canDrawOverlay(context)) return

        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        hide(context)

        val tv = TextView(context).apply {
            this.text = text
            setTextColor(Color.WHITE)
            textSize = 16f
            setPadding(40, 30, 40, 30)
        }

        val layout = LinearLayout(context).apply {
            setBackgroundColor(bg)
            addView(tv)
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP

        overlayView = layout
        wm.addView(layout, params)
    }
}