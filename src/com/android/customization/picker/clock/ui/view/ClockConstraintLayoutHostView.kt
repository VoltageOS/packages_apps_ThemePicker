/*
 * Copyright (C) 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.customization.picker.clock.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.View.MeasureSpec.EXACTLY
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.android.customization.picker.clock.shared.ClockSize
import com.android.systemui.plugins.keyguard.ui.clocks.ClockController
import com.android.wallpaper.util.ScreenSizeCalculator
import kotlin.math.max
import kotlin.math.min

/**
 * Parent view for the clock view. We will calculate the current display size and the preview size
 * and scale down the clock view to fit in the preview.
 */
class ClockConstraintLayoutHostView(context: Context, attrs: AttributeSet?) :
    ConstraintLayout(context, attrs) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val screenSize = ScreenSizeCalculator.getInstance().getScreenSize(display)
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(screenSize.x, EXACTLY),
            MeasureSpec.makeMeasureSpec(screenSize.y, EXACTLY),
        )
        val previewWidth = resolveAvailableSize(widthMeasureSpec, screenSize.x)
        val previewHeight = resolveAvailableSize(heightMeasureSpec, screenSize.y)
        val widthScale = previewWidth / screenSize.x.toFloat()
        val heightScale = previewHeight / screenSize.y.toFloat()
        val scale = min(widthScale, heightScale)
        pivotX = screenSize.x / 2f
        pivotY = screenSize.y / 2f
        translationX = 0f
        translationY = 0f
        scaleX = scale
        scaleY = scale
    }

    private fun resolveAvailableSize(measureSpec: Int, fallback: Int): Int {
        val size = MeasureSpec.getSize(measureSpec)
        return if (MeasureSpec.getMode(measureSpec) == MeasureSpec.UNSPECIFIED || size <= 0) {
            fallback
        } else {
            size
        }
    }

    companion object {
        fun ClockConstraintLayoutHostView.addClockViews(
            clockController: ClockController,
            size: ClockSize,
            cs: ConstraintSet,
        ) {
            clockController.let { clock ->
                val layout =
                    when (size) {
                        ClockSize.DYNAMIC -> clock.largeClock.layout
                        ClockSize.SMALL -> clock.smallClock.layout
                    }
                layout.views.forEach { view ->
                    (view.parent as? ViewGroup)?.let { it.removeView(view) }
                    disableClipping(view)
                    this.addView(view)

                    // Set the view to be invisible until the constraint set is applied
                    view.visibility = View.INVISIBLE
                    cs.setVisibility(view.id, View.VISIBLE)
                }
            }
        }

        private fun disableClipping(view: View) {
            view.clipBounds = null
            if (view is TextView) {
                view.includeFontPadding = true
                val metrics = view.paint.fontMetricsInt
                val fontHeight = metrics.bottom - metrics.top + view.compoundPaddingTop +
                    view.compoundPaddingBottom
                view.minimumHeight = max(view.minimumHeight, fontHeight)
            }
            if (view is ViewGroup) {
                view.clipChildren = false
                view.clipToPadding = false
                view.children.forEach(::disableClipping)
            }
        }
    }
}
