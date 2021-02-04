package armymart.cloud.ec.MyFirebaseService

interface FCMListener {
    fun onRecive(data:Map<String, String>)
}