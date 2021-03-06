/*
 *
 *  * Apache License
 *  *
 *  * Copyright [2017] Sinyuk
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.sinyuk.fanfou.ui.colormatchtabs.colortabs

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.support.v4.view.animation.PathInterpolatorCompat
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.ui.colormatchtabs.menu.MenuToggleListener
import com.sinyuk.fanfou.ui.colormatchtabs.utils.InvalidNumberOfTabsExeption
import com.sinyuk.fanfou.ui.colormatchtabs.utils.getDimen
import com.sinyuk.fanfou.ui.colormatchtabs.utils.getDimenToFloat

/**
 * Created by anna on 11.05.17.
 *
 */
class SlidingTabStripLayout : LinearLayout, MenuToggleListener {

    companion object {
        private const val CONTROL_X1 = 0.175f
        private const val CONTROL_Y1 = 0.885f
        private const val CONTROL_X2 = 0.360f
        private const val CONTROL_Y2 = 1.200f
        private const val FIRST_TAB_POSITION = 0
        private const val INVALID_TABS_AMOUNT = 5

        const val ANIMATION_DURATION = 200L
        const val ANIMATION_TEXT_APPEARANCE_DURATION = 100L
    }

    private lateinit var backgroundPaint: Paint
    private var backgroundCanvas: Canvas? = null
    internal var isAnimate: Boolean = false
    private var animateLeftX = 0f
    private var animateY = 0f
    private var isMenuToggle: Boolean = false
    internal val menuToggleListener: MenuToggleListener = this

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        gravity = Gravity.CENTER_VERTICAL
        orientation = HORIZONTAL
        setWillNotDraw(false)
        initCanvas()
    }

    /**
     * The method creates the Paint() object to create a rectangle and passes it Paint.ANTI_ALIAS_FLAG
     * that smooths the edges of the rectangle
     */

    private fun initCanvas() {
        backgroundPaint = Paint()
        backgroundPaint.flags = Paint.ANTI_ALIAS_FLAG
    }

    /**
     * The method draw rectangle for selected tab and animate it if it is necessary
     */

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (!isMenuToggle) {
            (0 until childCount).forEach {
                val child = getChildAt(it) as ColorTabView
                backgroundCanvas = canvas
                if (child.tab?.isSelected == true) {
                    backgroundPaint.color = child.tab?.selectedColor ?: Color.WHITE
                }
                if (child.tab?.isSelected == true && isAnimate) {
                    animateRectangle(child, canvas)
                } else if (child.tab?.isSelected == true && !isAnimate && !isMenuToggle) {
                    drawBackgroundTab(child, canvas)
                }
            }
        }
    }

    /**
     * Draw a rectangle if it is not animated
     */

    private fun drawBackgroundTab(child: ColorTabView, canvas: Canvas?) {
        drawRectangle(child, canvas, false)
    }

    /**
     * Animate the selected tab rectangle. Called in onLayout() method of ColorTabView
     */

    internal fun animateDrawTab(child: ColorTabView?) {
        ValueAnimator.ofFloat((parent as ColorMatchTabLayout).previousSelectedTab?.x ?: 0f, child?.x
                ?: 0f).apply {
            duration = ANIMATION_DURATION
            interpolator = PathInterpolatorCompat.create(CONTROL_X1, CONTROL_Y1, CONTROL_X2, CONTROL_Y2)
            addUpdateListener {
                animateLeftX = animatedValue as Float
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {

                override fun onAnimationEnd(animation: Animator?) {
                    child?.clickedTabView = null
                    isAnimate = false
                }

                override fun onAnimationStart(animation: Animator?) {
                    isAnimate = true
                }
            })
        }.start()
    }

    /**
     * Draw a rectangle if it is animated
     */

    private fun animateRectangle(child: ColorTabView, canvas: Canvas?) {
        drawRectangle(child, canvas, true)
    }

    private fun drawRectangle(child: ColorTabView, canvas: Canvas?, isAnimateRectangle: Boolean) {
        val left: Float
        val right: Float

        if (!isAnimateRectangle) {
            left = if (child.tab?.position == FIRST_TAB_POSITION) child.x.minus(getDimen(R.dimen.radius)) else child.x
            right = if (child.tab?.position == (parent as ColorMatchTabLayout).count().minus(1)) child.x.plus(child.width).plus(getDimen(R.dimen.radius)) else child.x.plus(child.width)
        } else {
            val leftX = animateLeftX
            left = if (child.tab?.position == FIRST_TAB_POSITION) leftX.minus(getDimen(R.dimen.radius)) else leftX
            right = if (child.tab?.position == (parent as ColorMatchTabLayout).count().minus(1)) leftX.plus(child.width).plus(getDimen(R.dimen.radius)) else leftX.plus(child.width)
        }
        val rectangle = RectF(left, if (isMenuToggle) animateY else 0f, right, child.height.toFloat())
        canvas?.drawRoundRect(rectangle, getDimenToFloat(R.dimen.radius), getDimenToFloat(R.dimen.radius), backgroundPaint)
    }

    /**
     * Animate the icon listOfTabs moving down when the ArcMenu is open
     */

    override fun onOpenMenu() {
        if ((parent as ColorMatchTabLayout).tabs.count() in 3..INVALID_TABS_AMOUNT) {
            animateIconTabs(0f, (height * 2).toFloat())
        } else {
            throw InvalidNumberOfTabsExeption()
        }
    }

    /**
     * Animate the icon listOfTabs moving up when the ArcMenu is closed
     */

    override fun onCloseMenu() {
        animateIconTabs((height * 2).toFloat(), 0f)
    }

    private fun animateIconTabs(start: Float, end: Float) {
        ValueAnimator.ofFloat(start, end).apply {
            duration = ANIMATION_DURATION
            addUpdateListener {
                (0 until childCount).forEach {
                    val child = getChildAt(it) as ColorTabView
                    child.iconView.translationY = animatedValue as Float
                }
            }
            addListener(object : AnimatorListenerAdapter() {

                override fun onAnimationEnd(animation: Animator?) {
                    isMenuToggle = false
                }

                override fun onAnimationStart(animation: Animator?) {
                    if (end == 0f) {
                        (parent as ColorMatchTabLayout).tabs.forEach {
                            it.icon?.setBounds(0, 0, it.icon?.intrinsicWidth
                                    ?: 0, it.icon?.intrinsicHeight ?: 0)
                        }
                        (0 until childCount).forEach {
                            val child = getChildAt(it) as ColorTabView
                            child.reColorDrawable(child.tab?.isSelected ?: false)
                        }
                    }
                    isMenuToggle = true
                }
            })
        }.start()
    }

}