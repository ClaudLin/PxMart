package armymart.cloud.ec.KeyStore;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesHelper {

    private static final String SHARED_PREF_NAME = "KEYSTORE_SETTING";
    private static final String PREF_KEY_AES = "PREF_KEY_AES";
    private static final String PREF_KEY_IV = "PREF_KEY_IV";
    private static final String PREF_KEY_INPUT = "PREF_KEY_INPUT";
    private static final String USER_EMAIL = "USER_EMAIL";
    private static final String PRI_KEY = "PRI_KEY";
    private static final String PUB_KEY = "PUB_KEY";
    private static final String CD = "CD";

    private SharedPreferences sharedPreferences;



    public SharedPreferencesHelper(Context context){
        sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }


    private String getString(String key) {
        return sharedPreferences.getString(key, "");
    }

    private void putString(String key, String value) {
        sharedPreferences.edit()
                .putString(key, value)
                .apply();
    }

    private void deleteString(String key) {
        sharedPreferences.edit().remove(key).commit();
    }

    private boolean getBoolean(String key) {
        return sharedPreferences.getBoolean(key, false);
    }

    private void putBoolean(String key, boolean value) {
        sharedPreferences.edit()
                .putBoolean(key, value)
                .apply();
    }




    public void setIV(String value) {
        putString(PREF_KEY_IV, value);
    }

    public String getIV() {
        return getString(PREF_KEY_IV);
    }

    public void setAESKey(String value) {
        putString(PREF_KEY_AES, value);
    }

    public String getAESKey() {
        return getString(PREF_KEY_AES);
    }

    public void setInput(String value) {
        putString(PREF_KEY_INPUT, value);
    }

    public String getInput() {
        return getString(PREF_KEY_INPUT);
    }

    public void setUserEmail(String value) {
        putString(USER_EMAIL, value);
    }

    public String getUserEmail() { return getString(USER_EMAIL); }

    public void setPriKey(String value) {
        putString(PRI_KEY, value);
    }

    public String getPriKey() { return getString(PRI_KEY); }

    public void setPubKey(String value) {
        putString(PUB_KEY, value);
    }

    public String getPubKey() { return getString(PUB_KEY); }


    public void setCd(String value) {
        putString(CD, value);
    }

    public String getCd() { return getString(CD); }
}
