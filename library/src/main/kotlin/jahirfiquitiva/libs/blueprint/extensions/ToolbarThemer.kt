/*
 * Copyright (c) 2017. Jahir Fiquitiva
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
 *
 * Special thanks to the project contributors and collaborators
 * 	https://github.com/jahirfiquitiva/Blueprint#special-thanks
 */

package jahirfiquitiva.libs.blueprint.extensions

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.view.menu.ActionMenuItemView
import android.support.v7.widget.ActionMenuView
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import jahirfiquitiva.libs.blueprint.R
import jahirfiquitiva.libs.blueprint.activities.base.ThemedActivity
import jahirfiquitiva.libs.blueprint.ui.views.callbacks.CollapsingToolbarCallback
import jahirfiquitiva.libs.blueprint.utils.CoreUtils
import java.lang.reflect.Field

fun ThemedActivity.updateToolbarColors(toolbar:Toolbar, offset:Int) {
    val defaultIconsColor = getColorFromRes(android.R.color.white)
    var ratio = CoreUtils.round(offset / 255.0, 1)
    if (ratio > 1) ratio = 1.0
    else if (ratio < 0) ratio = 0.0
    val rightIconsColor = defaultIconsColor.blendWith(
            getActiveIconsColorFor(getPrimaryColor(isDarkTheme())), ratio.toFloat())
    tintToolbar(toolbar, rightIconsColor)
    updateStatusBarStyle(
            if (ratio > 0.7) CollapsingToolbarCallback.State.COLLAPSED else CollapsingToolbarCallback.State.EXPANDED)
}

fun ThemedActivity.tintToolbar(toolbar:Toolbar, toolbarIconsColor:Int) {
    val colorFilter = PorterDuffColorFilter(toolbarIconsColor, PorterDuff.Mode.SRC_IN)

    for (i in 0..toolbar.childCount - 1) {
        val v = toolbar.getChildAt(i)

        //Step 1 : Changing the color of back button (or open drawer button).
        if (v is ImageButton) v.drawable.colorFilter = colorFilter

        if (v is ActionMenuView) {
            //Step 2: Changing the color of any ActionMenuViews - icons that are not back
            // button, nor text, nor overflow menu icon.
            (0..v.childCount - 1)
                    .map {
                        v.getChildAt(it)
                    }
                    .filterIsInstance<ActionMenuItemView>()
                    .forEach { innerView ->
                        innerView.compoundDrawables.forEach {
                            if (it != null) {
                                innerView.post { it.colorFilter = colorFilter }
                            }
                        }
                    }
        }
    }

    // Step 3: Changing the color of title and subtitle.
    toolbar.setTitleTextColor(getPrimaryTextColorFor(getPrimaryColor(isDarkTheme())))
    toolbar.setSubtitleTextColor(getSecondaryTextColorFor(getPrimaryColor(isDarkTheme())))

    // Step 4: Tint toolbar menu.
    tintToolbarMenu(toolbar, toolbar.menu, toolbarIconsColor)

    // Step 5: Change the color of overflow menu icon.
    var drawable = toolbar.overflowIcon
    if (drawable != null) {
        drawable = DrawableCompat.wrap(drawable)
        toolbar.overflowIcon = drawable.tintWithColor(toolbarIconsColor)
    }
    setOverflowButtonColor(toolbar, toolbarIconsColor)
}

fun ThemedActivity.tintToolbarMenu(toolbar:Toolbar?, menu:Menu?, @ColorInt iconsColor:Int) {
    if (toolbar == null || menu == null) return
    // The collapse icon displays when action views are expanded (e.g. SearchView)
    try {
        val field = Toolbar::class.java.getDeclaredField("mCollapseIcon")
        field.isAccessible = true
        val collapseIcon = field.get(toolbar) as Drawable
        field.set(toolbar, collapseIcon.tintWithColor(iconsColor))
    } catch (e:Exception) {
        e.printStackTrace()
    }

    // Theme menu action views
    for (i in 0..menu.size() - 1) {
        val item = menu.getItem(i)
        if (item.actionView is SearchView) {
            themeSearchView(iconsColor, item.actionView as SearchView)
        }
    }

    // Display icons for easy UI understanding
    /*
    try {
        val MenuBuilder = menu.javaClass
        val setOptionalIconsVisible = MenuBuilder.getDeclaredMethod("setOptionalIconsVisible",
                Boolean::class.javaPrimitiveType)
        if (!setOptionalIconsVisible.isAccessible) setOptionalIconsVisible.isAccessible = true
        setOptionalIconsVisible.invoke(menu, true)
    } catch (ignored:Exception) {
    }
    */
}

private fun setOverflowButtonColor(toolbar:Toolbar, @ColorInt color:Int) {
    @SuppressLint("PrivateResource")
    val overflowDescription = toolbar.resources.getString(
            R.string.abc_action_menu_overflow_description)
    val outViews = ArrayList<View>()
    toolbar.findViewsWithText(
            outViews, overflowDescription, View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION)
    if (outViews.isEmpty()) return
    val overflow = outViews[0] as AppCompatImageView
    overflow.setImageDrawable(overflow.drawable.tintWithColor(color))
}

private fun ThemedActivity.themeSearchView(tintColor:Int, view:SearchView) {
    val cls = view.javaClass
    try {
        val mSearchSrcTextViewField = cls.getDeclaredField("mSearchSrcTextView")
        mSearchSrcTextViewField.isAccessible = true
        val mSearchSrcTextView = mSearchSrcTextViewField.get(view) as EditText
        mSearchSrcTextView.setTextColor(tintColor)
        mSearchSrcTextView.setHintTextColor(getHintTextColor(isDarkTheme()))
        setCursorTint(mSearchSrcTextView, tintColor)

        var field = cls.getDeclaredField("mSearchButton")
        tintImageView(view, field, tintColor)
        field = cls.getDeclaredField("mGoButton")
        tintImageView(view, field, tintColor)
        field = cls.getDeclaredField("mCloseButton")
        tintImageView(view, field, tintColor)
        field = cls.getDeclaredField("mVoiceButton")
        tintImageView(view, field, tintColor)

        /* TODO: Fix if necessary
        field = cls.getDeclaredField("mSearchPlate")
        field.isAccessible = true
        TintUtils.setTintAuto(field.get(view) as View, tintColor, true,
                              ColorUtils.isDarkColor(tintColor))
                              */

        field = cls.getDeclaredField("mSearchHintIcon")
        field.isAccessible = true
        field.set(view, (field.get(view) as Drawable).tintWithColor(tintColor))
    } catch (e:Exception) {
        e.printStackTrace()
    }
}

private fun ThemedActivity.updateStatusBarStyle(state:CollapsingToolbarCallback.State) {
    if (state === CollapsingToolbarCallback.State.COLLAPSED) {
        setStatusBarMode(getPrimaryDarkColor(isDarkTheme()).isColorLight())
    } else {
        setStatusBarMode()
    }
}

private fun tintImageView(target:Any, field:Field, tintColor:Int) {
    field.isAccessible = true
    val imageView = field.get(target) as ImageView
    if (imageView.drawable != null) {
        imageView.setImageDrawable(imageView.drawable.tintWithColor(tintColor))
    }
}

private fun setCursorTint(editText:EditText, @ColorInt color:Int) {
    try {
        val fCursorDrawableRes = TextView::class.java.getDeclaredField("mCursorDrawableRes")
        fCursorDrawableRes.isAccessible = true
        val mCursorDrawableRes = fCursorDrawableRes.getInt(editText)
        val fEditor = TextView::class.java.getDeclaredField("mEditor")
        fEditor.isAccessible = true
        val editor = fEditor.get(editText)
        val clazz = editor.javaClass
        val fCursorDrawable = clazz.getDeclaredField("mCursorDrawable")
        fCursorDrawable.isAccessible = true
        val drawables = arrayOfNulls<Drawable>(2)
        drawables[0] = ContextCompat.getDrawable(editText.context, mCursorDrawableRes)
        drawables[0] = drawables[0]?.tintWithColor(color)
        drawables[1] = ContextCompat.getDrawable(editText.context, mCursorDrawableRes)
        drawables[1] = drawables[1]?.tintWithColor(color)
        fCursorDrawable.set(editor, drawables)
    } catch (e:Exception) {
        e.printStackTrace()
    }
}