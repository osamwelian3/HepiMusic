package com.hepimusic.main.common.image

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.TransformationUtils
import com.bumptech.glide.util.Util
import java.lang.Float.min
import java.security.MessageDigest

class CircularTransparentCenter(private val radiusFactor: Float = defaultRadiusFactor) : BitmapTransformation() {
    private val version = 1
    private val id = "com.hepimusic.main.common.image.CircularTransparentCenter.$version"


    override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        val srcHeight = toTransform.height
        val srcWidth = toTransform.width

        val config = toTransform.config ?: Bitmap.Config.ARGB_8888
        val bitmap = pool.get(srcWidth, srcHeight, config)
        val canvas = Canvas(bitmap)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.TRANSPARENT
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OUT)
        val centerY = (srcHeight / 2).toFloat()
        val centerX = (srcWidth / 2).toFloat()
        canvas.drawBitmap(toTransform, 0F, 0F, defaultPint)
        val innerRadius = (min(centerX, centerY) * radiusFactor)
        canvas.drawCircle(centerX, centerY, innerRadius, paint)
        return bitmap

    }

    override fun equals(other: Any?): Boolean {
        return other != null && other is CircularTransparentCenter && other.radiusFactor == radiusFactor
    }

    override fun hashCode(): Int {
        return Util.hashCode(id.hashCode(), Util.hashCode(radiusFactor))
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update((id + radiusFactor).toByteArray())
    }

}

private val defaultPint = Paint(TransformationUtils.PAINT_FLAGS)
private const val defaultRadiusFactor = .4F