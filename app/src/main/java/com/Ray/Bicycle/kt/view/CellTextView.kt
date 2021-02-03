package com.Ray.Bicycle.kt.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import com.Ray.Bicycle.R


class CellTextView : androidx.appcompat.widget.AppCompatTextView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    private val linePaint = Paint().apply {
        color = resources.getColor(R.color.blue_RURI)
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //設定線條寬度
        val rectF = RectF()
        rectF.top = 0f
        rectF.bottom = 20f
        rectF.left = 0f
        rectF.right = 1000f
        canvas.drawRoundRect(rectF, 50F, 40F, linePaint)
        //canvas.drawText(testString, targetRect.centerX(), baseline, paint)
    }
}