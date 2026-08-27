package com.ipodmodern.audio

import android.app.Application
import android.graphics.Bitmap
import android.os.Build
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy

class iPodApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(64L * 1024 * 1024) // 64MB disk cache
                    .build()
            }
            .bitmapConfig(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Bitmap.Config.HARDWARE
                } else {
                    Bitmap.Config.ARGB_8888
                }
            )
            .allowRgb565(true)
            .allowHardware(true)
            .networkCachePolicy(CachePolicy.DISABLED)
            .crossfade(200)
            .build()
    }
}
