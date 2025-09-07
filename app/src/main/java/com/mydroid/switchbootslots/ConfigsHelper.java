package com.mydroid.switchbootslots;


import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import me.bmax.apatch.Natives;

/* 管理配置信息，保存和读取对应的配置项 */
public class ConfigsHelper {

    public static final String KEY_SUPER_KEY = "config_superkey";


    private Context context;

    public ConfigsHelper() {}

    public ConfigsHelper(Context context) {
        this.context = context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

   public String getSuperkey() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Contants.SHARED_CONFIGS_FILE_NAME,MODE_PRIVATE);
        String base64key = sharedPreferences.getString(KEY_SUPER_KEY,"");
        if(base64key!=null && !base64key.isEmpty()){
            String key = new String(Base64.decode(base64key.getBytes(),Contants.SUPER_KEY_ENCODE_MODE));
            return key;

        }

        return "";

   }

   public boolean hasSuperkey() {
        SharedPreferences preferences = context.getSharedPreferences(Contants.SHARED_CONFIGS_FILE_NAME,MODE_PRIVATE);
        String key = preferences.getString(KEY_SUPER_KEY,"");
        if(key !=null && !key.isEmpty()) {
            //RootHelper rh = new RootHelper();
            //rh.setSuperkeyForBase64(key);
            String k = new String(Base64.decode(key.getBytes(),Contants.SUPER_KEY_ENCODE_MODE));
            return Natives.nativeReady(k);

        }
        return false;
   }

    public void saveSuperkey(String val) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Contants.SHARED_CONFIGS_FILE_NAME,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String base64 = Base64.encodeToString(val.getBytes(),Contants.SUPER_KEY_ENCODE_MODE);

        editor.putString(KEY_SUPER_KEY,base64);
        editor.apply();

    }


}
