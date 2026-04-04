/*
 * Copyright (C) 2026 The VoltageOS Project
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

package com.android.customization.model.color

import android.content.Context
import android.content.res.ColorStateList
import android.content.theming.ThemeStyle
import androidx.core.graphics.ColorUtils.setAlphaComponent
import com.android.customization.model.ResourceConstants
import com.android.customization.model.color.ColorOptionsProvider.COLOR_SOURCE_PRESET
import com.android.customization.model.color.ColorUtils.toColorString
import com.android.customization.picker.color.shared.model.ColorType
import com.android.systemui.monet.ColorScheme
import com.android.themepicker.R

class VoltageOSColorProvider(private val context: Context) {

    fun getVoltageOSColors(): List<ColorOptionImpl> {
        return voltageColors.mapIndexed { index, (colorRes, title) ->
            val color = context.resources.getColor(colorRes, context.theme)
            val style = ThemeStyle.VIBRANT
            val lightColorScheme = ColorScheme(color, /* darkTheme= */ false, style)
            val darkColorScheme = ColorScheme(color, /* darkTheme= */ true, style)
            ColorOptionImpl.Builder()
                .apply {
                    this.title = title
                    seedColor = color
                    source = COLOR_SOURCE_PRESET
                    type = ColorType.VOLTAGEOS_COLOR
                    this.style = style
                    this.index = index + 1
                    lightColors = getLightColorPreview(lightColorScheme)
                    darkColors = getDarkColorPreview(darkColorScheme)
                    addOverlayPackage(
                        ResourceConstants.OVERLAY_CATEGORY_COLOR,
                        toColorString(color),
                    )
                    addOverlayPackage(
                        ResourceConstants.OVERLAY_CATEGORY_SYSTEM_PALETTE,
                        toColorString(color),
                    )
                }
                .build()
        }
    }

    private fun getLightColorPreview(colorScheme: ColorScheme): IntArray {
        return intArrayOf(
            setAlphaComponent(colorScheme.accent1.s600, ALPHA_MASK),
            setAlphaComponent(colorScheme.accent1.s600, ALPHA_MASK),
            ColorStateList.valueOf(colorScheme.accent2.s500).withLStar(85f).colors[0],
            setAlphaComponent(colorScheme.accent3.s300, ALPHA_MASK),
        )
    }

    private fun getDarkColorPreview(colorScheme: ColorScheme): IntArray {
        return intArrayOf(
            setAlphaComponent(colorScheme.accent1.s200, ALPHA_MASK),
            setAlphaComponent(colorScheme.accent1.s200, ALPHA_MASK),
            ColorStateList.valueOf(colorScheme.accent2.s500).withLStar(35f).colors[0],
            setAlphaComponent(colorScheme.accent3.s300, ALPHA_MASK),
        )
    }

    companion object {
        private const val ALPHA_MASK = 0xFF

        private val voltageColors =
            listOf(
                R.color.voltage_arc_blue to "Arc Blue",
                R.color.voltage_plasma_purple to "Plasma Purple",
                R.color.voltage_ion_green to "Ion Green",
                R.color.voltage_surge_orange to "Surge Orange",
                R.color.voltage_neon_red to "Neon Red",
                R.color.voltage_cyber_teal to "Cyber Teal",
                R.color.voltage_high_voltage to "High Voltage",
                R.color.voltage_dark_matter to "Dark Matter",
                R.color.voltage_fusion_pink to "Fusion Pink",
                R.color.voltage_quantum_blue to "Quantum Blue",
                R.color.voltage_toxic_lime to "Toxic Lime",
                R.color.voltage_storm_indigo to "Storm Indigo",
                R.color.voltage_soft_blue to "Soft Blue",
                R.color.voltage_muted_purple to "Muted Purple",
                R.color.voltage_slate to "Slate",
            )
    }
}
