package dev.jahir.blueprint.extensions

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.fragment.app.FragmentActivity
import dev.jahir.blueprint.data.models.Icon
import dev.jahir.blueprint.ui.activities.IconsCategoryActivity.Companion.PICKER_KEY
import dev.jahir.frames.extensions.context.drawable

@Suppress("DEPRECATION")
internal fun FragmentActivity.pickIcon(icon: Icon, pickerKey: Int) {
    val intent = Intent()
    val bitmap: Bitmap? = try {
        val drawable = drawable(icon.resId) as? BitmapDrawable
        drawable?.bitmap ?: BitmapFactory.decodeResource(resources, icon.resId)
    } catch (e: Exception) {
        null
    }

    if (bitmap != null) {
        if (pickerKey == ICONS_PICKER) {
            try {
                intent.putExtra(
                    "icon",
                    if (bitmap.isRecycled) bitmap
                    else bitmap.copy(bitmap.config, false)
                )
            } catch (e: Exception) {
            }
            val iconRes = Intent.ShortcutIconResource.fromContext(this, icon.resId)
            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes)
        } else if (pickerKey == IMAGE_PICKER) {
            val uri: Uri? = bitmap.getUri(this, icon.name)
            if (uri != null) {
                intent.putExtra(Intent.EXTRA_STREAM, uri)
                intent.data = uri
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            intent.putExtra("return-data", false)
        }
        setResult(RESULT_OK, intent.apply { putExtra(PICKER_KEY, pickerKey) })
    } else {
        setResult(RESULT_CANCELED, intent)
    }
    try {
        bitmap?.let { if (!it.isRecycled) it.recycle() }
    } catch (e: Exception) {
    }
    finish()
}