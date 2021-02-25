package com.klim.tcharts

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import com.klim.tcharts.helpers.NightMode
import com.klim.tcharts.helpers.U

class MainActivity : Activity(), View.OnClickListener {

    private var mNightMode: NightMode? = null

    private lateinit var statusBarBack: View
    private lateinit var menuIconNightMode: View
    private lateinit var lloContent: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mNightMode = NightMode(this, R.style.AppTheme)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        setContentView(R.layout.activity_main)

        statusBarBack = findViewById<View>(R.id.statusBarBack)
        menuIconNightMode = findViewById<View>(R.id.mIconNightMode)
        lloContent = findViewById<LinearLayout?>(R.id.lloContent)

        statusBarBack.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, U.getStatusBarHeight(this, resources.getDimension(R.dimen.statusBarHeight).toInt()))

        menuIconNightMode.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.mIconNightMode -> mNightMode!!.toggle()
        }
    }
}