package armymart.cloud.ec.Until

import android.graphics.Bitmap
import android.os.Build

object CommonObject {
    var bitmap: Bitmap? = null
    val deviceName:String = Build.MANUFACTURER
    val privateKey:String? = null
    val publicKey:String? = null
//        val Domain = "https://pxmart.brandstudio-ec.com/"
    val Domain = "https://pxmart2.brandstudio-ec.com/"
//    val Domain = "https://www.armymart.com.tw/"
    val DomainRegister = "account/phoneauth/register"
    val DomainVerify = "account/phoneauth/verify"
    val DomainWebVerify = "account/phoneauth/webverify"
    val DomainChangephone = "account/phoneauth/changephone"
    val checkstat = "api/phonesign/checkstat"
    val pos_sign = "api/phonesign/pos_sign"
    val pos_CancelSign = "api/phonesign/pos_CancelSign"
    val store_sign = "api/phonesign/store_sign"
}