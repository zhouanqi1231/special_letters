package com.example.special_letters

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.Button

// service need not a UI, this is to create the floating buttons
class FloatingService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private lateinit var layoutParams: WindowManager.LayoutParams
    private lateinit var prefs: android.content.SharedPreferences

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()

        prefs = getSharedPreferences("floating_prefs", MODE_PRIVATE)

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        // transfer the layout file into a real view
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_buttons, null)

        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT, // auto
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, // will not override the inputting focus
            PixelFormat.TRANSLUCENT
        )

        layoutParams.gravity = Gravity.TOP or Gravity.START
        layoutParams.x = prefs.getInt("last_x", 0)
        layoutParams.y = prefs.getInt("last_y", 200)

        windowManager.addView(floatingView, layoutParams)

        // to drag the floating window
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        floatingView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams.x
                    initialY = layoutParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    layoutParams.x = initialX + (event.rawX - initialTouchX).toInt()
                    layoutParams.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(floatingView, layoutParams)
                    prefs.edit().putInt("last_x", layoutParams.x).putInt("last_y", layoutParams.y).apply()
                    true
                }
                else -> false
            }
        }

        // when button pressed, send the letter
        floatingView.findViewById<Button>(R.id.btnA).setOnClickListener {
            // inputText is to insert one letter at the cursor's position
            MyAccessibilityService.instance?.inputText("ä")
        }

        floatingView.findViewById<Button>(R.id.btnO).setOnClickListener {
            MyAccessibilityService.instance?.inputText("ö")
        }
    }

    fun showFloatingView() {
        layoutParams.x = prefs.getInt("last_x", layoutParams.x)
        layoutParams.y = prefs.getInt("last_y", layoutParams.y)
        if (floatingView.windowToken == null) {
            windowManager.addView(floatingView, layoutParams)
        } else {
            floatingView.visibility = View.VISIBLE
        }
    }

    fun hideFloatingView() {
        floatingView.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::floatingView.isInitialized) {
            windowManager.removeView(floatingView)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
