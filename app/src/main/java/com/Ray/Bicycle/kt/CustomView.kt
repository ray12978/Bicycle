package com.Ray.Bicycle.kt

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import kotlin.math.roundToInt

class CustomView : View{
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)
    private val barWidth = 30.toPx()
    private val barDistance = 30.toPx()
    private val maxValue = 100
    private val barPaint = Paint().apply {
        color = Color.GREEN
    }
    private val linePaint = Paint().apply {
        color = Color.BLACK
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //設定線條寬度
        drawBar(canvas)
        drawAxis(canvas)
    }
    fun drawAxis(canvas: Canvas){
        linePaint.strokeWidth = 8.toPx().toFloat()
        //x 軸
        canvas.drawLine(0f, height.toFloat(), width.toFloat(), height.toFloat(), linePaint)
        //y 軸
        canvas.drawLine(0f, 0f, 0f, height.toFloat(), linePaint)
    }

    fun drawBar(canvas: Canvas){
        barDatas.forEachIndexed { index, barData ->
            val left = index * (barWidth + barDistance)
            val right = left + barWidth
            // 對齊底部
            val bottom = height
            val top = bottom * (1 - barData.value / maxValue)
            canvas.drawRect(left.toFloat(), top, right.toFloat(), bottom.toFloat(), barPaint)
        }
    }
}
fun Int.toPx(): Int {
    return (Resources.getSystem().displayMetrics.density * this).roundToInt()
}
data class BarData(val name: String, val value: Float)

var barDatas: List<BarData> = listOf(
        BarData("2000", 23f),
        BarData("2001", 34f),
        BarData("2002", 10f),
        BarData("2003", 93f),
        BarData("2004", 77f)
)
