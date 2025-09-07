package com.mydroid.switchbootslots;

import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 检查当前设备是否支持无缝更新和arm64-v8a系统架构
 */

public class ABCheck {

    public static final String TAG = "ABCheck";


    public static boolean isDeviceSupported() {
        if(Contants.debugMode) return Contants.DEBUG_MODE_DEVICE_SUPPORTED;

        boolean is64bit=false;
        boolean isab = false;
        String[] has64bit = Build.SUPPORTED_ABIS;

        for(String s:has64bit) {
            if(s.equals("arm64-v8a")) {
                is64bit=true;
                Log.i(TAG, "isDeviceSupported: Device supported arm64-v8a abi: "+is64bit);
            }
        }


        if(getProperty(Contants.PROP_KEY_VIRTUAL_AB_ENABLED).equals("true") && getProperty(Contants.PROP_KEY_VIRTUAL_AB_RETROFIT).equals("false") ) {
            isab=true;
            Log.i(TAG, "isDeviceSupported: Device supported virtual ab: "+isab);

        }
        else if(!isBlank(getProperty(Contants.PROP_KEY_BOOT_SLOT_SUFFIX))  || getProperty(Contants.PROP_KEY_BUILD_AB_UPDATE).equals("true")) {
            isab=true;
            Log.i(TAG, "isDeviceSupported: Device supported ab ota update: " +isab);
        }

        return (is64bit && isab);
    }

    public static boolean isBlank(String s) {
        return s.isEmpty();

    }

    public static String getProperty(String prop) {

        try{
            String val = getProp(prop,"");
            return val;
        }
        catch (IOException | InterruptedException e) {
            Log.e(TAG, "getProperty: Error! Call Function Failed!        Message: "+e.getMessage(), e.getCause());
            throw new RuntimeException(e);
        }

    }


    public static String getProp(String prop,String defval) throws IOException, InterruptedException {

        Process process = Runtime.getRuntime().exec("getprop "+prop);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String s = reader.readLine();
        Log.i(TAG, "getProp: Get System Property Key: "+prop+"    Value: "+s);

        if(s!= null && !s.isEmpty()){
            return s.replace("\n","");
        }

        process.waitFor();
        Log.e(TAG, "getProp: Error! Get System Property Failed.    Property Key: "+prop+"   Default Value:"+defval+"    Exit Code: "+process.exitValue(),new Throwable());
        return defval;
    }


}
