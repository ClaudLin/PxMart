package armymart.cloud.ec.Until

import android.app.Activity
import android.graphics.Bitmap
import android.os.Build
import armymart.cloud.ec.MainActivity

object CommonObject {
    var bitmap: Bitmap? = null
    val deviceName:String = Build.MANUFACTURER
    val privateKey:String? = null
    val publicKey:String? = null
//    val Domain = "https://pxmart.brandstudio-ec.com/"
    val Domain = "https://pxmart2.brandstudio-ec.com/"
//    val Domain = "https://www.armymart.com.tw/"
    val DomainRegister = "account/phoneauth/register"
    val DomainMain = "common/apphome"
    val DomainVerify = "account/phoneauth/verify"
    val DomainWebVerify = "account/phoneauth/webverify"
    val DomainChangephone = "account/phoneauth/changephone"
    val checkstat = "api/phonesign/checkstat"
    val pos_sign = "api/phonesign/pos_sign"
    val pos_CancelSign = "api/phonesign/pos_CancelSign"
    val store_sign = "api/phonesign/store_sign"
    var currentNotificationInfo:notificationInfo? = null
    var deviceToken:String? = null
    var mainActivity:MainActivity? = null
    val appVersion = "api/app_version"
    var isSignature = false
    var isEnterBackground = false
    var isFirstTime = true
}



public enum class notificationAction(val value:String) {
    signature("sign")
}

public class notificationInfo {
    var store_href:String? = null
    var action:notificationAction? = null
}
