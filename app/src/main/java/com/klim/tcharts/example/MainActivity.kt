package com.klim.tcharts.example

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.Toast
import com.klim.tcharts.TChart
import com.klim.tcharts.entities.ChartData
import com.klim.tcharts.entities.ChartItem
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
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        setContentView(R.layout.activity_main)

        statusBarBack = findViewById<View>(R.id.statusBarBack)
        menuIconNightMode = findViewById<View>(R.id.mIconNightMode)
        lloContent = findViewById<LinearLayout?>(R.id.lloContent)

        statusBarBack.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            U.getStatusBarHeight(this, resources.getDimension(R.dimen.statusBarHeight).toInt())
        )

        menuIconNightMode.setOnClickListener(this)

        GlobalScope.launch(Dispatchers.Main) {
            val json = withContext(Dispatchers.IO) {
                loadJSONFromAsset()
            }
            json?.let {
                val charts = withContext(Dispatchers.Default) {
                    convertJsonToChartData(it)
                }

                val chart = this@MainActivity.findViewById<TChart>(R.id.tchart)
                chart.setData(charts[0])

                addChartsIntoActivity(charts)
            } ?: run {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        R.string.error_loading_example,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun addChartsIntoActivity(charts: ArrayList<ChartData>) {
        var i = 0
        charts.forEach { chartData ->
            val layout = LinearLayout(this)
            val linearLayoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            linearLayoutParams.topMargin = resources.getDimension(R.dimen.chartBottomMargin).toInt()
            linearLayoutParams.bottomMargin =
                resources.getDimension(R.dimen.chartBottomMargin).toInt()
            layout.layoutParams = linearLayoutParams
            layout.orientation = LinearLayout.VERTICAL
            lloContent.addView(layout)

            val tChart = TChart(this)
            val layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            tChart.layoutParams = layoutParams
            tChart.id = View.generateViewId()
            tChart.setData(chartData)
            tChart.setPadding(
                resources.getDimension(R.dimen.cbHorizontalMargin).toInt(),
                resources.getDimension(R.dimen.cbHorizontalMargin).toInt(),
                resources.getDimension(R.dimen.cbHorizontalMargin).toInt(),
                resources.getDimension(R.dimen.cbHorizontalMargin).toInt()
            )
            tChart.setTitle(String.format("Chart #%d", i))
            layout.addView(tChart)

            for (i in 0 until chartData.keys.size) {
                val checkBox = CheckBox(this)
                checkBox.tag = chartData.keys[i]
                checkBox.text = chartData.names[i]
                checkBox.isChecked = true
                checkBox.setTextColor(this@MainActivity.resources.getColor(R.color.textColor))
                checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                    tChart.showLine(buttonView.tag.toString(), isChecked)
                }

                val states = arrayOf(
                    intArrayOf(android.R.attr.state_checked),
                    intArrayOf(-android.R.attr.state_checked)
                )
                val colors = intArrayOf(chartData.colors[i], chartData.colors[i])

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    checkBox.buttonTintList = ColorStateList(states, colors)
                }
                val layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                layoutParams.leftMargin = resources.getDimension(R.dimen.cbHorizontalMargin).toInt()
                layoutParams.rightMargin =
                    resources.getDimension(R.dimen.cbHorizontalMargin).toInt()
                checkBox.layoutParams = layoutParams
                layout.addView(checkBox)
            }

            i++
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
            val jsArray = JSONArray(json)
            for (i in 0 until jsArray.length()) {
                val jsChart = jsArray.getJSONObject(i)
                val jsTypes = jsChart.getJSONObject("types");
                val jsNames = jsChart.getJSONObject("names")
                val jsColors = jsChart.getJSONObject("colors")
                val jsColumns = jsChart.getJSONObject("columns")

                val keys = ArrayList<String>(jsNames.length())
                val names = ArrayList<String>(jsNames.length())
                val colors = ArrayList<Int>(jsColors.length())
                val rows = ArrayList<JSONArray>(jsColumns.length())
                var xRow = JSONArray()

                //find x line and other lines
//                var xName: String = ""
                val iteratorTypes = jsTypes.keys()
                while (iteratorTypes.hasNext()) {
                    val key = iteratorTypes.next()
                    if (jsTypes.getString(key) == "x") {
                        xRow = jsColumns.getJSONArray(key)
                    } else {
                        keys.add(key)
                        //names
                        names.add(jsNames.getString(key))
                        //colors
                        colors.add(Color.parseColor(jsColors.getString(key)))
                        //columns
                        rows.add(jsColumns.getJSONArray(key))
                    }
                }

                val chartItems: ArrayList<ChartItem> = ArrayList<ChartItem>()

                for (x in 0 until xRow.length()) {
                    val valuesForOneX = ArrayList<Int>(keys.size)
                    for (row in rows) {
                        valuesForOneX.add(row.getInt(x))
                    }
                    chartItems.add(ChartItem(xRow.getLong(x), valuesForOneX))
                }

                charts.add(ChartData(keys, names, colors, chartItems))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return charts
    }
}