package armymart.cloud.ec

import android.Manifest
import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.Window
import android.view.WindowManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import armymart.cloud.ec.DetectScreenShot.ScreenShot
import armymart.cloud.ec.Device.DeviceUuidFactory
import armymart.cloud.ec.KeyStore.KeyStoreHelper
import armymart.cloud.ec.KeyStore.SSLSelfSender
import armymart.cloud.ec.KeyStore.SharedPreferencesHelper
import armymart.cloud.ec.RSA.Base64
import armymart.cloud.ec.RSA.RSACrypt
import armymart.cloud.ec.Signature.SignatureDialogFragment
import armymart.cloud.ec.Until.CommonFun
import armymart.cloud.ec.Until.CommonObject
import com.google.firebase.iid.FirebaseInstanceId
import com.jaredrummler.android.device.DeviceName
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder
import java.util.*
import kotlin.concurrent.thread
import kotlin.concurrent.timerTask

@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : AppCompatActivity(){
    private lateinit var webView : WebView
    private lateinit var keyStoreHelper: KeyStoreHelper
    private lateinit var preferencesHelper: SharedPreferencesHelper
    private var detectIsQRCode = false
    private val mScreenShot: ScreenShot = ScreenShot.getInstance()
    private val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 456
    var MainAlive = false

    private class checkstatValue {
        var TP:String? = ""
        var Status:String? = ""
        var OrderNo:String? = ""
        var Timer:Long? = 0
        var cd:String? = ""
    }


    private class androidVersionInfo() {
        var android_version:String? = ""
        var android_min_version:String? = ""
        var android_version_description:String? = ""
        var android_min_version_description:String? = ""
        var android_os_version:String? = ""
        var android_os_min_version:String? = ""
        var android_os_version_description:String? = ""
        var android_os_min_version_description:String? = ""
    }


    private enum class PostType {
        Register ,ChangePhone ,Verify ,WebVerify
    }
    companion object{
        fun newInstance(): SignatureDialogFragment {
            val args = Bundle()
            val fragment = SignatureDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkVersion()
        KeyStoreInit()
        if (CommonObject.isFirstTime) {
            CommonFun().clearAllCookies()
            CommonObject.isFirstTime = false
        }
    }

    private fun checkVersion(){
        val client = OkHttpClient()
        val request = Request.Builder()
            .addHeader("Content-Type", "application/json")
            .url("${CommonObject.Domain}${CommonObject.appVersion}")
            .build()
        val response = client.newCall(request)
        thread {
            response.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    println("Failed to execute request")
                    e.printStackTrace()
                    Log.e("onFailure",e.toString())
                }

                @SuppressLint("SetJavaScriptEnabled")
                override fun onResponse(call: Call, response: Response) {
                    try {
                        val responseStr = response.body?.string()
                        val dic = JSONObject(responseStr)
                        val versionInfoJson = dic["android"] as JSONObject
                        val versionInfo = androidVersionInfo()
                        versionInfo.android_min_version = versionInfoJson["android_min_version"].toString()
                        versionInfo.android_min_version_description = versionInfoJson["android_min_version_description"].toString()
                        versionInfo.android_os_min_version = versionInfoJson["android_os_min_version"].toString()
                        versionInfo.android_os_min_version_description = versionInfoJson["android_os_min_version_description"].toString()
                        versionInfo.android_os_version = versionInfoJson["android_os_version"].toString()
                        versionInfo.android_os_version_description = versionInfoJson["android_os_version_description"].toString()
                        versionInfo.android_version = versionInfoJson["android_version"].toString()
                        versionInfo.android_version_description = versionInfoJson["android_version_description"].toString()
                        val currentVersionCode = BuildConfig.VERSION_CODE.toDouble()
                        val currentOSArray = Build.VERSION.RELEASE.split(".")
                        var currentOSVersion:Double = 0.0
                        if (currentOSArray.size == 1){
                            currentOSVersion = "${currentOSArray[0]}.0".replace(".","").toDouble()
                        }else if (currentOSArray.size == 2){
                            currentOSVersion = "${currentOSArray[0]}${currentOSArray[1]}".toDouble()
                        }
                        val minOSVersion = versionInfo.android_os_min_version!!.replace(".","").toDouble()
                        val minAppVersion = versionInfo.android_min_version!!.toDouble()
                        if (currentOSVersion < minOSVersion) {
                            OSVersionAlert(versionInfo.android_os_min_version_description!!)
                        }else if (currentVersionCode < minAppVersion){
                            appVersionAlert(versionInfo.android_min_version_description!!)
                        } else {
                            judeKey()
                            FirebaseAction()
                            UIInit()
                        }

                    }catch (e: JSONException){
                        println(e)
                    }
                }
            })
        }
    }

    private fun FirebaseAction(){
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener(this){
            instanceIdResult ->
            if (!instanceIdResult.isSuccessful){
                print(instanceIdResult.exception)
                return@addOnCompleteListener
            }
            println(instanceIdResult.getResult()?.token)
            CommonObject.deviceToken = instanceIdResult.getResult()?.token
        }
    }

    private fun OSVersionAlert(Str:String){
        runOnUiThread(){
            var builder = AlertDialog.Builder(this)
            builder.setTitle(Str)
            builder.setMessage("")
            builder.setPositiveButton("確定",{ dialogInterface: DialogInterface?, which: Int ->
                startActivity(Intent(Settings.ACTION_SETTINGS))
            })
            builder.show()
        }
    }

    private fun appVersionAlert(Str:String){
        runOnUiThread(){
            var builder = AlertDialog.Builder(this)
            builder.setTitle(Str)
            builder.setMessage("")
            builder.setPositiveButton("確定",{ dialogInterface: DialogInterface?, which: Int ->
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
                } catch (e: ActivityNotFoundException) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
                }
            })
            builder.show()
        }
    }

    fun detectAction(){
        println(preferencesHelper.cd)
        if (preferencesHelper.cd != ""){
            val decryptCd = URLEncoder.encode(RSACrypt.decryptByPrivateKey(preferencesHelper.cd, RSACrypt.getPrivateKey(preferencesHelper)), "UTF-8").toUpperCase()
            poolingSGTime(decryptCd ,0 ,"${CommonObject.Domain}${CommonObject.checkstat}")
        }
    }

    private fun closeAppAction(){
        runOnUiThread(){
            var builder = AlertDialog.Builder(this)
            builder.setTitle("您的手機有有越獄或是模擬器器開啟app")
            builder.setMessage("即將關閉")
            builder.setPositiveButton("確定",{ dialogInterface: DialogInterface?, which: Int ->
                finish()
            })
            builder.show()
        }
    }



    @RequiresApi(Build.VERSION_CODES.O)
    private fun judeKey(){
        if (preferencesHelper.priKey == "" || preferencesHelper.priKey == null || preferencesHelper.pubKey == "" || preferencesHelper.pubKey == null) {
            CommonFun().generateSaveKey(preferencesHelper)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode){
            123 -> {
                if (grantResults.count() > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,"沒給email權限", Toast.LENGTH_LONG).show()
                    CommonFun().closeApp(this)
                }else{
                    startGetEmail()
                }
            }
            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // do your stuff
                } else {
                    println("MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE")
                }
            }
        }
    }


    override fun onBackPressed() {
        if (webview.canGoBack()){
            webview.goBack()
        }else{
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        CommonObject.isEnterBackground = false
        if (!CommonFun().judgment(this)){
            closeAppAction()
        }else {
            detectAction()
//            startListener()
        }
    }

    override fun onPause() {
        super.onPause()
        print("onPause")
        CommonObject.isEnterBackground = true
    }

    private fun requestPermission(){
        checkPermissionREAD_EXTERNAL_STORAGE(this)
    }

    private fun startListener(){
        mScreenShot.register(this) { path ->
            runOnUiThread() {
                var builder = AlertDialog.Builder(this)
                builder.setTitle("截圖內可能包含個人資訊，敬請妥善保管確保使用安全")
                builder.setMessage("")
                builder.setPositiveButton("確定", { dialogInterface: DialogInterface?, which: Int ->
                })
                builder.show()
            }
        }
    }

    private fun UIInit(){
        runOnUiThread() {
            val window: Window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE);
            MainAlive = true
            CommonObject.mainActivity = this
            webView = findViewById(R.id.webview)
            webView.settings.javaScriptEnabled = true
            webView.getSettings().setDomStorageEnabled(true)
            webView.settings.javaScriptCanOpenWindowsAutomatically = true
            webView.settings.allowUniversalAccessFromFileURLs = true
            webView.settings.userAgentString = "armymart_android"
            webView.settings.allowFileAccessFromFileURLs = true
            webView.settings.domStorageEnabled = true
            webView.webViewClient = object: WebViewClient(){

                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    view?.loadUrl(url)
                    return true
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)

                    if (url!!.contains("/api/phonesign/go/checkstat/")){
                        val account =  CommonFun().getInfo(url, CommonFun.InfoType.account
                        )
                        val time =  CommonFun().getInfo(url, CommonFun.InfoType.time)?.toInt()!! *1000 .toLong()
                        poolingTime(account!!,time,"${CommonObject.Domain}${CommonObject.checkstat}")

//                    val intent = Intent(this@MainActivity, SignatureActivity::class.java)
//                    startActivity(intent)
                    }else if (url.contains("/account/phoneauth/go/register")){
                        post(url, PostType.Register)
                    }else if (url.contains("/account/phoneauth/go/verify")){
                        post(url, PostType.Verify)
                    }else if (url.contains("/account/phoneauth/go/changephone")){
                        post(url, PostType.ChangePhone)
                    }else if (url.contains("/account/logout")){
                        preferencesHelper.cd = ""
                    }else if (url.contains("/account/order/info/go") || url.contains("/account/login/go")){
                        detectAction()
                    }else if (url.contains("/account/qrcode")){
                        detectIsQRCode = true
                    }else if (url.contains("/account/phoneauth/go/webverify")){
                        post(url, PostType.WebVerify)
                    }

                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
//                val intent = Intent(this@MainActivity,SignatureActivity::class.java)
//                startActivity(intent)
                }

                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    return super.shouldOverrideUrlLoading(view, request)
                }

            }

            val extras = intent.extras
            var urlStr = ""
            val domain = CommonObject.Domain

            if (extras?.get("store_href") == null || extras?.get("store_href") == "") {
                urlStr = "${domain}${CommonObject.DomainMain}&version=${BuildConfig.VERSION_CODE}"
            }else {
                urlStr = extras?.get("store_href").toString()
            }
//        SSLAction(domain)
            val https = SSLSelfSender()
            https.send(this,urlStr)
            webView.loadUrl(urlStr)
        }


    }

    private fun SSLAction(domain:String){
//        Thread{
//            val https = SSLSelfSender()
//            val result = https.send(this,domain)
//        }.start()
        val SSLSelfSender = SSLSelfSender()
        val result = SSLSelfSender.jugeCrtDomain(this,domain)
        if (result == null || !result!!){
            closeAppAction()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode){
            42 -> {
                val accountManager = AccountManager.get(this)
                val accounts = accountManager.getAccountsByType("com.google")
                accounts.forEach {
                    when(resultCode){
                        0 -> {
                            CommonFun().closeApp(this)
                        }
                        -1 -> {
//                            preferencesHelper.setUserEmail(accounts[0].name)
                        }
                    }
                }
            }
        }
    }

    fun checkPermissionREAD_EXTERNAL_STORAGE(context: Context?): Boolean {
        val currentAPIVersion = Build.VERSION.SDK_INT
        return if (currentAPIVersion >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) !== PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                ) {
                    showDialog(
                        "External storage", context,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                } else {
                    ActivityCompat
                        .requestPermissions(
                            this,
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE
                        )
                }
                false
            } else {
                true
            }
        } else {
            true
        }
    }

    private fun showDialog(msg: String, context: Context?, permission: String) {
        val alertBuilder =
            AlertDialog.Builder(context!!)
        alertBuilder.setCancelable(true)
        alertBuilder.setTitle("Permission necessary")
        alertBuilder.setMessage("$msg permission is necessary")
        alertBuilder.setPositiveButton(
            android.R.string.yes
        ) { dialog, which ->
            ActivityCompat.requestPermissions(
                (context as Activity?)!!, arrayOf(permission),
                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE
            )
        }
        val alert = alertBuilder.create()
        alert.show()
    }

    private fun poolingTime(cd:String ,delayTime:Long, postURL:String){
        if ( delayTime >= 0 ){
            if (detectIsQRCode){
                detectIsQRCode = true
                runOnUiThread(){
                    webView.loadUrl(CommonObject.Domain)
                }
            }

            Timer().schedule(timerTask {
                val client = OkHttpClient()
                val formBody= FormBody.Builder()
                    .add("cd", URLEncoder.encode(cd,"UTF-8"))
                    .build()

                val request = Request.Builder()
                    .addHeader("Content-Type", "application/json")
                    .url(postURL)
                    .post(formBody)
                    .build()
                val response = client.newCall(request)
                thread {
                    response.enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            println("Failed to execute request")
                            e.printStackTrace()
                            Log.e("onFailure",e.toString())
                        }

                        @SuppressLint("SetJavaScriptEnabled")
                        override fun onResponse(call: Call, response: Response) {
                            try {
                                val responseStr = response.body?.string()
                                val dic = JSONObject(responseStr)
                                var checkstat = checkstatValue()
                                checkstat.TP = dic["TP"] as? String
                                checkstat.Status = dic["Status"] as? String
                                checkstat.Timer = (dic["Timer"] as Int)*1000.toLong()
                                checkstat.cd = dic["cd"] as? String
                                checkstat.OrderNo = dic["OrderNo"] as String
                                afterResponseAction(checkstat)
                            }catch (e: JSONException){
                                println(e)
                            }
                        }
                    })
                }
            },delayTime)
        }
    }

    private fun poolingSGTime(cd:String ,delayTime:Long, postURL:String){
        if ( delayTime >= 0 ){
            Timer().schedule(timerTask {
                val client = OkHttpClient()
                val formBody= FormBody.Builder()
                    .add("cd", URLEncoder.encode(cd,"UTF-8"))
                    .add("sg", URLEncoder.encode("1","UTF-8"))
                    .build()

                val request = Request.Builder()
                    .addHeader("Content-Type", "application/json")
                    .url(postURL)
                    .post(formBody)
                    .build()
                val response = client.newCall(request)
                thread {
                    response.enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            println("Failed to execute request")
                            e.printStackTrace()
                            Log.e("onFailure",e.toString())
                        }

                        @SuppressLint("SetJavaScriptEnabled")
                        override fun onResponse(call: Call, response: Response) {
                            try {
                                val responseStr = response.body?.string()
                                val dic = JSONObject(responseStr)
                                var checkstat = checkstatValue()
                                checkstat.TP = dic["TP"] as? String
                                checkstat.Status = dic["Status"] as? String
                                checkstat.Timer = (dic["Timer"] as Int)*1000.toLong()
                                checkstat.cd = dic["cd"] as? String
                                checkstat.OrderNo = dic["OrderNo"] as String
                                afterResponseAction(checkstat)
                            }catch (e: JSONException){
                                println(e)
                            }
                        }
                    })
                }
            },delayTime)
        }
    }

    private fun afterResponseAction(checkstatValue: checkstatValue){
        val status = checkstatValue.Status
        when (status) {
            "1" -> {
                poolingTime(checkstatValue.cd!!,checkstatValue.Timer!!,"${CommonObject.Domain}${CommonObject.checkstat}")
            }
            "2" -> {
                if (!CommonObject.isSignature && !CommonObject.isEnterBackground) {
                    val dialogFragment = SignatureDialogFragment.newInstance(this@MainActivity)
                    dialogFragment.cd = checkstatValue.cd
                    dialogFragment.TP = checkstatValue.TP
                    dialogFragment.OrderNo = checkstatValue.OrderNo
                    supportFragmentManager.beginTransaction().add(dialogFragment,"signature").commitAllowingStateLoss()
//                    dialogFragment.show(supportFragmentManager,"signature")
                }
            }
            "3" -> {
                println("停止pooling")
            }
        }
    }

    private fun getEmail( postType: PostType?){
        if (preferencesHelper.userEmail == "" || preferencesHelper.userEmail == null){
            if(checkCallingOrSelfPermission(Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED){
                startGetEmail()
            }else{
                val OKlistener = DialogInterface.OnClickListener { dialog, which ->
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.GET_ACCOUNTS) ,123)
                }
                val Cancellistener = DialogInterface.OnClickListener { dialog, which ->
                    CommonFun().closeApp(this)
                }
                AlertDialog.Builder(this).setMessage("因為存取您的Email需要拿取您的聯絡人權限").setTitle("給予拿取聯絡人權限").setPositiveButton("OK" ,OKlistener).setNegativeButton("Cancel",Cancellistener).show()
            }
        }
    }

    private fun startGetEmail(){
        val intent = AccountManager.newChooseAccountIntent(null , null , arrayOf("com.google") , null , null , null , null)
        startActivityForResult(intent,42)
    }

    private fun getDeviceName():String{
        return DeviceName.getDeviceName().toString()
    }

    private fun KeyStoreInit(){
        preferencesHelper = SharedPreferencesHelper(this)
        keyStoreHelper = KeyStoreHelper(this, preferencesHelper)

//        if (keyStoreHelper.checkValue()){
//            getEmail()
//        }else{
//            if(checkCallingOrSelfPermission(Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED){
//                getEmail()
//            }else{
//                val OKlistener = DialogInterface.OnClickListener { dialog, which ->
//                     ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.GET_ACCOUNTS) ,123)
//                }
//                val Cancellistener = DialogInterface.OnClickListener { dialog, which ->
//                    moveTaskToBack(true)
//                }
//
//                AlertDialog.Builder(this).setMessage("因為存取email需要拿取您的聯絡人權限").setTitle("給予拿取聯絡人權限").setPositiveButton("OK" ,OKlistener).setNegativeButton("Cancel",Cancellistener).show()
//            }
//        }
    }

    private fun post(urlStr: String, type: PostType){
        val cd = URLEncoder.encode(CommonFun().getInfo(urlStr, CommonFun.InfoType.account)).toUpperCase()
//        if (cd != null){
        var postData:String = ""
        var privatekey = RSACrypt.getPrivateKey(preferencesHelper)
        var publicKey = RSACrypt.getPublicKey(preferencesHelper)
        val device = "android"
        val pc = URLEncoder.encode(RSACrypt.publicKeyToPemStr(publicKey), "UTF-8")
        val deviceUuidFactory = DeviceUuidFactory()
        deviceUuidFactory.DeviceUuidFactory(this)
//        val daValue = "${preferencesHelper.getUserEmail()}${getDeviceName()}${deviceUuidFactory.getDeviceUuid()}"
        val daValue = "${Build.BRAND}${getDeviceName()}${deviceUuidFactory.getDeviceUuid()}"
        val da = URLEncoder.encode(RSACrypt.sign(daValue,privatekey), "UTF-8")
        val ds = URLEncoder.encode(Base64.encode(daValue.toByteArray()),"UTF-8")
        var postURL:String? = null
        when (type){
            PostType.Register -> {
//                preferencesHelper.cd = RSACrypt.encryptByPublicKey(cd ,RSACrypt.getPublicKey(preferencesHelper))
//                postData = "cd=${cd}&da=${da}&pc=${pc}&device=${device}&dt=${CommonObject.deviceToken}"
//                postURL = "${CommonObject.Domain}${CommonObject.DomainRegister}"
//                webView.postUrl(postURL,postData.toByteArray())
                CommonFun().generateSaveKey(preferencesHelper)
                privatekey = RSACrypt.getPrivateKey(preferencesHelper)
                publicKey = RSACrypt.getPublicKey(preferencesHelper)
                preferencesHelper.cd = RSACrypt.encryptByPublicKey(cd ,publicKey)
                val da = URLEncoder.encode(RSACrypt.sign(daValue,privatekey), "UTF-8")
                val pc = URLEncoder.encode(RSACrypt.publicKeyToPemStr(publicKey), "UTF-8")
                postData = "cd=${cd}&da=${da}&pc=${pc}&device=${device}&dt=${CommonObject.deviceToken}"
                postURL = "${CommonObject.Domain}${CommonObject.DomainRegister}"
                webView.postUrl(postURL,postData.toByteArray())
            }

            PostType.Verify -> {
                preferencesHelper.cd = RSACrypt.encryptByPublicKey(cd ,RSACrypt.getPublicKey(preferencesHelper))
                postData = "cd=${cd}&da=${da}&device=${device}&ds=${ds}"
                postURL = "${CommonObject.Domain}${CommonObject.DomainVerify}"
                webView.postUrl(postURL,postData.toByteArray())
            }

            PostType.ChangePhone -> {
                CommonFun().generateSaveKey(preferencesHelper)
                privatekey = RSACrypt.getPrivateKey(preferencesHelper)
                publicKey = RSACrypt.getPublicKey(preferencesHelper)
                preferencesHelper.cd = RSACrypt.encryptByPublicKey(cd ,publicKey)
                val da = URLEncoder.encode(RSACrypt.sign(daValue,privatekey), "UTF-8")
                val pc = URLEncoder.encode(RSACrypt.publicKeyToPemStr(publicKey), "UTF-8")
                postData = "cd=${cd}&da=${da}&pc=${pc}&device=${device}&dt=${CommonObject.deviceToken}"
                postURL = "${CommonObject.Domain}${CommonObject.DomainChangephone}"
                webView.postUrl(postURL,postData.toByteArray())
            }

            PostType.WebVerify -> {
                preferencesHelper.cd = RSACrypt.encryptByPublicKey(cd ,RSACrypt.getPublicKey(preferencesHelper))
                postData = "cd=${cd}&da=${da}&device=${device}&ds=${ds}"
                postURL = "${CommonObject.Domain}${CommonObject.DomainWebVerify}"
                webView.postUrl(postURL,postData.toByteArray())
            }
        }
//        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation === Configuration.ORIENTATION_LANDSCAPE) {
            println("LANDSCAPE")
        } else {
            println("PORTRAIT")
        }
    }
}

class MyWebView(context: Context?) : WebView(context) {
    private val mDetector: GestureDetector
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        mDetector.onTouchEvent(event)
        performClick()
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    internal inner class MyGestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(event: MotionEvent?): Boolean {
            return true
        }

        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
            //do stuff here
            return false
        }
    }

    init {
        mDetector = GestureDetector(context, MyGestureListener())
    }
}