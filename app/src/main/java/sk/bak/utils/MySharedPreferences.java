package sk.bak.utils;

import static androidx.security.crypto.MasterKey.DEFAULT_MASTER_KEY_ALIAS;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;


/**
 *
 * Custom trieda SharedPreferences. Upravená pre lepšie používanie
 *
 */
public class MySharedPreferences {

    private static final String TAG = "SharedPref";

    private SharedPreferences sharedPreferences;

    //nastavenie sharedPreferences
    public MasterKey inicializeMasterKey(Context context) {
        try {
            KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                    DEFAULT_MASTER_KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build();

            return new MasterKey.Builder(context)
                    .setKeyGenParameterSpec(spec)
                    .build();
        }
        catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    /**
     *
     * Koštruktor triedy
     *
     * @param context
     */
    public MySharedPreferences(Context context) {
        if (sharedPreferences == null) {

            Log.i(TAG, "MySharedPreferences: generating");
            try {
                this.sharedPreferences = EncryptedSharedPreferences.create(
                        context,
                        Constants.PREF_NAME,
                        inicializeMasterKey(context),
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
            }
            catch (GeneralSecurityException | IOException e) {
                Log.i(TAG, "MySharedPreferences: error " + e.getMessage());
            }
        }

        Log.i(TAG, "MySharedPreferences: generated ok");
    }

    private SharedPreferences getPrefs() {
        return sharedPreferences;
    }


    // Metody pre pracu so SharedPref


    public void clearPrefs() {
        SharedPreferences.Editor editor = getPrefs().edit();
        editor.clear()
                .apply();
    }

    public void removeKey(String key) {
        getPrefs().edit().remove(key).apply();
    }

    public void saveString(String name, String value) {
        sharedPreferences.edit()
                .putString(name, value)
                .apply();

    }

    public String getString(String name) {

        return sharedPreferences.getString(name, "");
    }

    public void saveInt(String name, int value) {
        sharedPreferences.edit()
                .putInt(name, value)
                .apply();
    }

    public int getInt(String name) {

        return sharedPreferences.getInt(name, 0);
    }

    public void saveLong(String name, long value) {
        sharedPreferences.edit()
                .putLong(name, value)
                .apply();
    }

    public long getLong(String name) {

        return sharedPreferences.getLong(name, 0);
    }

    public void saveFloat(String name, float value) {
        sharedPreferences.edit()
                .putFloat(name, value)
                .apply();
    }

    public float getFloat(String name) {

        return sharedPreferences.getFloat(name, 0f);
    }

    public boolean getBoolean(String key) {
        return getPrefs().getBoolean(key, false);
    }

    public void saveBoolean(String key, boolean value) {
        sharedPreferences.edit()
                .putBoolean(key, value)
                .apply();
    }

}
