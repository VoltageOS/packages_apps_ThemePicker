package com.android.customization.model.font

import android.content.Context
import android.os.UserHandle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import com.android.customization.model.CustomizationManager
import com.android.customization.model.CustomizationManager.Callback
import com.android.customization.model.CustomizationManager.OptionsFetchedListener
import com.android.customization.model.ResourceConstants.ANDROID_PACKAGE
import com.android.customization.model.ResourceConstants.OVERLAY_CATEGORY_FONT
import com.android.customization.model.theme.OverlayManagerCompat
import org.json.JSONException
import org.json.JSONObject

class FontManager
internal constructor(
    private val mContext: Context,
    val overlayManager: OverlayManagerCompat,
    private val mProvider: FontOptionProvider,
) : CustomizationManager<FontOption> {

    override fun isAvailable(): Boolean {
        return overlayManager.isAvailable
    }

    override fun apply(option: FontOption, callback: Callback?) {
        val userId = UserHandle.myUserId()
        val previousPackage = getEnabledPackageName()

        try {
            if (previousPackage != option.packageName) {
                applyOverlay(option.packageName, userId)
            }

            if (getEnabledPackageName() != option.packageName) {
                showApplyError(callback, null)
                return
            }

            if (!persistOverlay(option)) {
                restoreOverlay(previousPackage, userId)
                showApplyError(callback, null)
                return
            }

            callback?.onSuccess()
        } catch (e: RuntimeException) {
            Log.e(TAG, "Failed to apply font ${option.packageName}", e)
            restoreOverlay(previousPackage, userId)
            showApplyError(callback, e)
        }
    }

    override fun fetchOptions(callback: OptionsFetchedListener<FontOption>, reload: Boolean) {
        callback.onOptionsLoaded(mProvider.getOptions(reload))
    }

    fun isActive(option: FontOption): Boolean {
        return getEnabledPackageName() == option.packageName
    }

    private fun getEnabledPackageName(): String? {
        return overlayManager.getEnabledPackageName(ANDROID_PACKAGE, OVERLAY_CATEGORY_FONT)
    }

    private fun applyOverlay(packageName: String?, userId: Int) {
        if (packageName != null) {
            overlayManager.setEnabledExclusiveInCategory(packageName, userId)
            return
        }

        overlayManager
            .getOverlayPackagesForCategory(
                OVERLAY_CATEGORY_FONT,
                userId,
                ANDROID_PACKAGE,
            )
            .forEach { overlayManager.disableOverlay(it, userId) }
    }

    private fun restoreOverlay(packageName: String?, userId: Int) {
        try {
            applyOverlay(packageName, userId)
        } catch (e: RuntimeException) {
            Log.e(TAG, "Failed to restore font $packageName", e)
        }
    }

    private fun showApplyError(callback: Callback?, throwable: Throwable?) {
        Toast.makeText(
                mContext,
                "Failed to apply font, reboot to try again.",
                Toast.LENGTH_SHORT,
            )
            .show()
        callback?.onError(throwable)
    }

    private fun persistOverlay(toPersist: FontOption): Boolean {
        val value =
            Settings.Secure.getStringForUser(
                mContext.contentResolver,
                Settings.Secure.THEME_CUSTOMIZATION_OVERLAY_PACKAGES,
                UserHandle.myUserId(),
            )

        val json: JSONObject =
            try {
                if (value == null) JSONObject() else JSONObject(value)
            } catch (e: JSONException) {
                Log.e(TAG, "Error parsing current settings value:\n${e.message}")
                return false
            }
        json.remove(OVERLAY_CATEGORY_FONT)
        try {
            json.put(OVERLAY_CATEGORY_FONT, toPersist.packageName)
        } catch (e: JSONException) {
            Log.e(TAG, "Error adding new settings value:\n${e.message}")
            return false
        }
        return Settings.Secure.putStringForUser(
            mContext.contentResolver,
            Settings.Secure.THEME_CUSTOMIZATION_OVERLAY_PACKAGES,
            json.toString(),
            UserHandle.myUserId(),
        )
    }

    companion object {
        private const val TAG = "FontManager"

        @Volatile private var sFontOptionManager: FontManager? = null

        @JvmStatic
        fun getInstance(context: Context, overlayManager: OverlayManagerCompat): FontManager {
            return sFontOptionManager
                ?: synchronized(this) {
                    sFontOptionManager
                        ?: FontManager(
                                context,
                                overlayManager,
                                FontOptionProvider(context.applicationContext, overlayManager),
                            )
                            .also { sFontOptionManager = it }
                }
        }
    }
}
