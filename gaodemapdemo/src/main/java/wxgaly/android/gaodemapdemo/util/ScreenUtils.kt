package wxgaly.android.gaodemapdemo.util

import android.app.Application
import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager


class ScreenUtils(private val application: Application) {

    fun getDisplayMetrics(): DisplayMetrics {
        val wm = application.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val dm = DisplayMetrics()
        wm.defaultDisplay.getMetrics(dm)
        return dm
    }

    //dip和px转换
    fun dip2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    companion object {
        @Volatile
        private var INSTANCE: ScreenUtils? = null

        fun getInstance(application: Application) =
                INSTANCE ?: synchronized(ScreenUtils::class.java) {
                    INSTANCE ?: ScreenUtils(application)
                            .also { INSTANCE = it }
                }

    }
}