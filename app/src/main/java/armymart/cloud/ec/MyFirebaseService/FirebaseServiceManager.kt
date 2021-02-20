package armymart.cloud.ec.MyFirebaseService


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import armymart.cloud.ec.MainActivity
import armymart.cloud.ec.R
import armymart.cloud.ec.Until.CommonObject
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

@RequiresApi(Build.VERSION_CODES.O)
class FirebaseServiceManager : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        if (remoteMessage.data.count() > 0 ){
            CommonObject.currentNotificationInfo?.store_href = remoteMessage.data["store_href"]
            println(CommonObject.currentNotificationInfo)
//        val urlStr: String? = remoteMessage.data["store_href"]
//        if (urlStr != null && CommonObject.mainActivity != null) {
//            CommonObject.mainActivity?.reloadWebview(urlStr)
//        }
            sendNotification(null ,remoteMessage.data)
        }
    }

    override fun onNewToken(s: String) {
        super.onNewToken(s)
        Log.i("MyFirebaseService", "token $s")
    }

    private fun sendNotification(notification: RemoteMessage.Notification?, data: Map<String, String>?) {
        createNotificationChannel()
        val channelId = "all_notifications" // Use same Channel ID
        val intent = Intent()
        intent.putExtra("store_href", data?.get("store_href"))
        intent.setClass(this, MainActivity::class.java)
        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addNextIntent(intent)
        val pendingIntent: PendingIntent
        var mainAlive:Boolean
        if (CommonObject.mainActivity?.MainAlive != null){
            mainAlive = CommonObject.mainActivity?.MainAlive!!
        }else {
            mainAlive = false
        }
        if (mainAlive){
            pendingIntent = stackBuilder.getPendingIntent(System.currentTimeMillis().toInt(), PendingIntent.FLAG_UPDATE_CURRENT)
        }else{
            pendingIntent = PendingIntent.getActivity(this, System.currentTimeMillis().toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        val builder = NotificationCompat.Builder(this, channelId) // Create notification with channel Id
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(data?.get("title"))
                .setContentText("body data ${data}")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
        val mNotificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.notify(123, builder.build())
    }

     private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "all_notifications" // You should create a String resource for this instead of storing in a variable
            val mChannel = NotificationChannel(
                channelId,
                "General Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            mChannel.description = "This is default channel used for all other notifications"

            val notificationManager =
                this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
    }

}