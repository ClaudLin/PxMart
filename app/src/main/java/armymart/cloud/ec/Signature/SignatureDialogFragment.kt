package armymart.cloud.ec.Signature

import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Base64
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import armymart.cloud.ec.MainActivity
import armymart.cloud.ec.R
import armymart.cloud.ec.Until.CommonObject
import kotlinx.android.synthetic.main.signature_pad.*
import okhttp3.*
import java.io.ByteArrayOutputStream
import java.io.IOException

class SignatureDialogFragment: DialogFragment(), SignatureView.OnSignedListener{
    var TP:String? = null
    var cd:String? = null
    var OrderNo:String? = null

//    private class checkstatValue {
//        var TP:String? = ""
//        var Status:String? = ""
//        var OrderNo:String? = ""
//        var Timer:Long? = 0
//        var cd:String? = ""
//    }

    private enum class singnatureResult(val postValue:String) {
        finish("1"), cancel("2")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        isCancelable = false
        return inflater.inflate(R.layout.signature_pad, container, false)
    }

    override fun onResume() {
        super.onResume()
        getActivity()?.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

    }

    override fun getTheme(): Int {
        return R.style.Dialog_App
    }

    override fun dismiss() {
        super.dismiss()
        getActivity()?.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CommonObject.isSignature = true

        buttonCancel.setOnClickListener {
            CommonObject.isSignature = false
            upLoad(null,
                singnatureResult.cancel
            )
        }

        buttonClear.setOnClickListener {
            CommonObject.isSignature = false
            signatureView.clear()
        }

        buttonOk.setOnClickListener {
            CommonObject.isSignature = false
            val bitmap = signatureView.getSignatureBitmap()
            CommonObject.bitmap = bitmap
            upLoad(bitmap,
                singnatureResult.finish
            )
//            val previewImage = PreviewImageViewDialogFragment()
//            previewImage.show(fragmentManager!!,"PreviewImage")


//            print(Bitmap)
//            onSignedListener.onSignatureCaptured(signatureView.getSignatureBitmap(), "")
//            dismiss()
//            var builder = AlertDialog.Builder(context)
//            builder.setTitle("簽名")
//            builder.setMessage("上傳成功")
//            builder.setPositiveButton("確定",{dialogInterface: DialogInterface?, which: Int ->
//                dismiss()
//            })
//            builder.show()
        }
        signatureView.setOnSignedListener(this)
        buttonClear.isEnabled = false
        buttonOk.isEnabled = false
    }

    override fun onStartSigning() {
    }

    override fun onSigned() {
        buttonOk.isEnabled = true
        buttonClear.isEnabled = true
    }

    override fun onClear() {
        buttonClear.isEnabled = false
        buttonOk.isEnabled = false
    }

    companion object {

        fun newInstance(MActivity: MainActivity) : SignatureDialogFragment {
            val f = SignatureDialogFragment()
            var arg = Bundle()
            f.arguments = arg
            return f
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun upLoad(bmp: Bitmap?, result: singnatureResult){
        val client = OkHttpClient()
        val stream = ByteArrayOutputStream()
        if (bmp != null) {
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        }
        val byteArray = stream.toByteArray()
        val imgString = Base64.encodeToString(byteArray,Base64.DEFAULT)
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("cd", cd!!)
            .addFormDataPart("OrderNo", OrderNo!!)
            .addFormDataPart("Status", result.postValue)
            .addFormDataPart("sf",imgString)
            .build()
        var postUrlStr = ""
        if(TP == "1"){
            when(result){
                singnatureResult.cancel -> {
                    postUrlStr = "${CommonObject.Domain}${CommonObject.pos_CancelSign}"
                }
                singnatureResult.finish -> {
                    postUrlStr = "${CommonObject.Domain}${CommonObject.pos_sign}"
                }
            }

        }else if(TP == "2"){
            postUrlStr = "${CommonObject.Domain}${CommonObject.store_sign}"
        }
        var request = Request.Builder().url(postUrlStr).post(requestBody).build()
        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                println(e)
                e.printStackTrace()
                Log.e("onFailure",e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                print(response)
                val responseStr = response.body?.string()?.toLowerCase()
                if (responseStr == "Fail"){
                    val dialogBuilder = AlertDialog.Builder(activity!!)
                    dialogBuilder.setMessage("簽名失敗，請確認app版本是否為最新或聯絡客服回報")
                        // if the dialog is cancelable
                        .setCancelable(false)
                        .setPositiveButton("確定", DialogInterface.OnClickListener {
                                dialog, id ->
                            dialog.dismiss()
                        })
                    val alert = dialogBuilder.create()
                    alert.setTitle("簽名失敗，請確認app版本是否為最新或聯絡客服回報")
                    alert.show()
                }
                dismiss()
            }
        })
    }

}