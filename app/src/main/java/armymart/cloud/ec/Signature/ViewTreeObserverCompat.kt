package armymart.cloud.ec.Signature

import android.annotation.SuppressLint
import android.os.Build
import android.view.ViewTreeObserver

object ViewTreeObserverCompat {
    @SuppressLint("NewApi")
    fun removeOnGlobalLayoutListener(
        observer: ViewTreeObserver,
        victim: ViewTreeObserver.OnGlobalLayoutListener?
    ) { // Future (API16+)...
        if (Build.VERSION.SDK_INT >= 16) {
            observer.removeOnGlobalLayoutListener(victim)
        } else {
            observer.removeGlobalOnLayoutListener(victim)
        }
    }
}