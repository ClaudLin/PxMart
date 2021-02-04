package armymart.cloud.ec.KeyStore

import android.content.Context
import okhttp3.CertificatePinner
import java.io.*
import java.net.MalformedURLException
import java.net.URL
import java.security.KeyManagementException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory

class SSLSelfSender {
    var trustStore: KeyStore? = null

    fun send(context: Context, urlString: String?): String? {
        //建立 ssl context
        val sslContext = prepareSelfSign(context)
        if (trustStore == null || sslContext == null) {
            return null
        }
        // Tell the URLConnection to use a SocketFactory from our SSLContext
        var url: URL? = null
        try {
            url = URL(urlString)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }
        if (url == null) {
            return null
        }
        try {
            val urlConnection = url.openConnection() as HttpsURLConnection
            // 使用ssl context
            urlConnection.sslSocketFactory = sslContext.socketFactory

            // 讀取結果
//            val IS = urlConnection.inputStream
            val IS: InputStream = BufferedInputStream(context.assets.open("crt/serverForssl.crt"))
            if (IS != null) {
                val byteArrayOutputStream = ByteArrayOutputStream()
                val data = ByteArray(256)
                var length = 0
                var getPer = 0
                while (IS.read(data).also { getPer = it } != -1) {
                    length += getPer
                    byteArrayOutputStream.write(data, 0, getPer)
                }
                IS.close()
                byteArrayOutputStream.close()
                return String(byteArrayOutputStream.toByteArray()).trim { it <= ' ' }
            }
        } catch (e: IOException) {
            println(e)
            e.printStackTrace()
        }
        return null
    }

    fun prepareSelfSign(context: Context): SSLContext? {
        try {
            trustStore = KeyStore.getInstance(KeyStore.getDefaultType())
        } catch (e: KeyStoreException) {
            e.printStackTrace()
        }
        if (trustStore == null) {
            return null
        }
        var sslContext: SSLContext? = null
        var crtInput: InputStream? = null
        try {
            // 載入憑證檔
//            crtInput = BufferedInputStream(FileInputStream("serverForssl.crt"))
            crtInput = context.assets.open("crt/serverForssl.crt")
            val cf = CertificateFactory.getInstance("X.509")
            val caInput: InputStream = BufferedInputStream(crtInput)
            val ca = cf.generateCertificate(caInput)
            trustStore!!.load(null, null)
            trustStore!!.setCertificateEntry("ca", ca)
            val tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm()
            val tmf = TrustManagerFactory.getInstance(tmfAlgorithm)
            tmf.init(trustStore)
            sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, tmf.trustManagers, null)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: CertificateException) {
            e.printStackTrace()
        } catch (e: KeyStoreException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: KeyManagementException) {
            e.printStackTrace()
        } finally {
            if (crtInput != null) {
                try {
                    crtInput.close()
                } catch (e: IOException) {
                }
            }
        }
        return sslContext
    }

    fun jugeCrtDomain(context: Context ,urlString: String?): Boolean?{
        var result:Boolean? = null
        val caInput = context.assets.open("crt/serverForssl.crt")
        if (caInput == null) {
            return result
        }
        val cf: CertificateFactory = CertificateFactory.getInstance("X.509")
        val ca: X509Certificate = caInput.use {
            cf.generateCertificate(it) as X509Certificate
        }
        System.out.println("ca=" + ca.subjectDN)
        val subjectDNameArray = ca.subjectDN.name.split(".")
        println(subjectDNameArray[1])
        if (urlString!!.contains(subjectDNameArray[1])){
            result = true
        }else{
            result = false
        }
        caInput.close()
        return result
    }
}