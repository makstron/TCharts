package com.klim.tcharts.example

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import com.klim.tcharts.example.R
import com.klim.tcharts.TChart
import com.klim.tcharts.entities.ChartData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

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

        GlobalScope.launch(Dispatchers.Main) {
            val json = withContext(Dispatchers.IO) {
                loadJSONFromAsset()
            }
            json?.let {
                val charts = withContext(Dispatchers.Default) {
                    convertJsonToChartData(it)
                }
                addChartsIntoActivity(charts)
            } ?: run {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, R.string.error_loading_example, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun addChartsIntoActivity(charts: ArrayList<ChartData>) {
        charts.forEach { chart ->
            val lineChart = TChart(this)
            val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 200)//ViewGroup.LayoutParams.WRAP_CONTENT
            layoutParams.bottomMargin = resources.getDimension(R.dimen.chartBottomMargin).toInt()
            lineChart.setLayoutParams(layoutParams)
            lineChart.setId(View.generateViewId())
            lloContent.addView(lineChart)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.mIconNightMode -> mNightMode!!.toggle()
        }
    }

    private fun loadJSONFromAsset(): String? {
        try {
            val inputStream = assets.open("chart_data.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            return String(buffer)//, "UTF-8"
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
    }

    private fun convertJsonToChartData(json: String): ArrayList<ChartData> {
        val charts = ArrayList<ChartData>()
        try {
            //todo parse here
            val jsArray = JSONArray(json)
            for (i in 0 until jsArray.length()) {
                val jsChart = jsArray.getJSONObject(i)
                val jsTypes = jsChart.getJSONObject("types");
                val jsNames = jsChart.getJSONObject("names")
                val jsColors = jsChart.getJSONObject("colors")
                val jsColumns = jsChart.getJSONObject("columns")

                charts.add(ChartData())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return charts
    }
}