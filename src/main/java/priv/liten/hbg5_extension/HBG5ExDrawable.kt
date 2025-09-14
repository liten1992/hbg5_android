package priv.liten.hbg5_extension

import android.graphics.drawable.Drawable

fun Drawable.new(): Drawable? {
    return mutate().constantState?.newDrawable()
}