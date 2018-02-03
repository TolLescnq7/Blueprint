/*
 * Copyright (c) 2018. Jahir Fiquitiva
 *
 * Licensed under the CreativeCommons Attribution-ShareAlike
 * 4.0 International License. You may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *    http://creativecommons.org/licenses/by-sa/4.0/legalcode
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jahirfiquitiva.libs.blueprint.ui.adapters.viewholders

import android.content.Context
import android.graphics.ColorFilter
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import ca.allanwang.kau.utils.isAppInstalled
import ca.allanwang.kau.utils.toBitmap
import com.bumptech.glide.Glide
import jahirfiquitiva.libs.blueprint.R
import jahirfiquitiva.libs.blueprint.data.models.Launcher
import jahirfiquitiva.libs.blueprint.helpers.extensions.blueprintFormat
import jahirfiquitiva.libs.frames.helpers.extensions.loadResource
import jahirfiquitiva.libs.frames.helpers.extensions.releaseFromGlide
import jahirfiquitiva.libs.frames.helpers.utils.GlideRequestCallback
import jahirfiquitiva.libs.kauextensions.extensions.bestSwatch
import jahirfiquitiva.libs.kauextensions.extensions.bind
import jahirfiquitiva.libs.kauextensions.extensions.clearChildrenAnimations
import jahirfiquitiva.libs.kauextensions.extensions.formatCorrectly
import jahirfiquitiva.libs.kauextensions.extensions.getBoolean
import jahirfiquitiva.libs.kauextensions.extensions.getIconResource
import jahirfiquitiva.libs.kauextensions.extensions.getSecondaryTextColorFor
import jahirfiquitiva.libs.kauextensions.extensions.secondaryTextColor
import jahirfiquitiva.libs.kauextensions.extensions.withAlpha
import jahirfiquitiva.libs.kauextensions.ui.widgets.CustomCardView

class LauncherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val itemLayout: CustomCardView? by itemView.bind(R.id.launcher_item)
    val bg: LinearLayout? by itemView.bind(R.id.launcher_bg)
    val icon: ImageView? by itemView.bind(R.id.launcher_icon)
    val text: TextView? by itemView.bind(R.id.launcher_name)
    
    private val bnwFilter: ColorFilter
        get() {
            val matrix = ColorMatrix()
            matrix.setSaturation(0F)
            return ColorMatrixColorFilter(matrix)
        }
    
    fun bind(item: Launcher, listener: (Launcher) -> Unit = {}) = with(itemView) {
        val formattedName = item.name.replace("launcher", "", true).formatCorrectly()
        val iconName = formattedName.toLowerCase()
        text?.text = formattedName.blueprintFormat()
        val bits = try {
            ("ic_" + iconName).getIconResource(context)
        } catch (ignored: Exception) {
            "ic_na_launcher".getIconResource(context)
        }
        
        icon?.colorFilter = null
        text?.background = null
        text?.setTextColor(context.secondaryTextColor)
        
        icon?.loadResource(
                Glide.with(itemView.context), bits, true, false, true,
                object : GlideRequestCallback<Drawable>() {
                    override fun onLoadSucceed(resource: Drawable): Boolean {
                        val isInstalled = isLauncherInstalled(context, item.packageNames)
                        setIconResource(resource, isInstalled)
                        resource.toBitmap().bestSwatch?.let {
                            val rightColor = if (isInstalled) it.rgb else context.secondaryTextColor
                            if (context.getBoolean(R.bool.enable_colored_cards)) {
                                itemLayout?.radius = 0F
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                                    itemLayout?.elevation = 0F
                                itemLayout?.cardElevation = 0F
                                itemLayout?.maxCardElevation = 0F
                                bg?.setBackgroundColor(rightColor.withAlpha(0.8F))
                            }
                            text?.setBackgroundColor(rightColor)
                            text?.setTextColor(
                                    context.getSecondaryTextColorFor(rightColor, 0.6F))
                        }
                        return true
                    }
                })
        setOnClickListener { listener(item) }
    }
    
    private fun isLauncherInstalled(context: Context, packages: Array<String>): Boolean {
        packages.forEach {
            if (context.isAppInstalled(it)) return true
        }
        return false
    }
    
    private fun setIconResource(resource: Drawable, isInstalled: Boolean) {
        icon?.setImageDrawable(resource)
        icon?.colorFilter = if (isInstalled) null else bnwFilter
        icon?.clearChildrenAnimations()
    }
    
    fun unbind() {
        icon?.releaseFromGlide()
        icon?.setImageDrawable(null)
    }
}