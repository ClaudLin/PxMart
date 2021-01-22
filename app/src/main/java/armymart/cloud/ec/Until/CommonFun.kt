package armymart.cloud.ec.Until

import android.content.Context
import android.content.DialogInterface
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import armymart.cloud.ec.KeyStore.SharedPreferencesHelper
import armymart.cloud.ec.MainActivity
import armymart.cloud.ec.RSA.Base64
import armymart.cloud.ec.RSA.RSACrypt
import com.lahm.library.EasyProtectorLib
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Files
import java.security.KeyFactory
import java.security.interfaces.RSAPublicKey
import java.security.spec.X509EncodedKeySpec
import kotlin.system.exitProcess

class CommonFun {
    enum class InfoType(val value: Int) {
        account(1) , time(2)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun readPublicKey(file: File): RSAPublicKey? {
        val key = String(Files.readAllBytes(file.toPath()), Charset.defaultCharset())
        val publicKeyPEM = key
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace(System.lineSeparator().toRegex(), "")
            .replace("-----END PUBLIC KEY-----", "")
        val encoded: ByteArray = Base64.decode(publicKeyPEM)
        val keyFactory: KeyFactory = KeyFactory.getInstance("RSA")
        val keySpec = X509EncodedKeySpec(encoded)
        return keyFactory.generatePublic(keySpec) as RSAPublicKey
    }

    fun closeApp(activity: MainActivity){
        val OKlistener = DialogInterface.OnClickListener { dialog, which ->
            exitProcess(-1)
        }
        AlertDialog.Builder(activity).setTitle("即將關閉app").setPositiveButton("OK" ,OKlistener).show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun generateSaveKey(preferencesHelper: SharedPreferencesHelper){
        val keyPair = RSACrypt.generateKeyPair()
        val pub = keyPair.public
        val pri = keyPair.private
        preferencesHelper.setPriKey(RSACrypt.privateKeyToStr(pri))
        preferencesHelper.setPubKey(RSACrypt.publicKeyToStr(pub))
    }

    fun getInfo(urlStr:String ,infoType: InfoType):String?{
        var Info:String? = null
        var urlsplitArray = urlStr.split("/")
        when(infoType){
            (InfoType.account) -> {
                Info = urlsplitArray[urlsplitArray.count() - infoType.value]
            }
            (InfoType.time) -> {
                Info = urlsplitArray[urlsplitArray.count()- infoType.value ]
            }
        }
        return  Info
    }

    fun judgment(context: Context):Boolean{
        return !isRoot() && !isEmulator(context)
    }

    fun isRoot():Boolean{
        return EasyProtectorLib.checkIsRoot()
    }

    fun isEmulator(context: Context):Boolean {
        if (EasyProtectorLib.checkIsRunningInEmulator(context,{ phoneInfo ->
                print(phoneInfo)
            })){
            return true
        }else {
            return false
        }
    }
}