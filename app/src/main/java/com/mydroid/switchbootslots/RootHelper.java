package com.mydroid.switchbootslots;

import android.util.Base64;
import me.bmax.apatch.Natives;

/*
* 用于管理超级密钥的类
*
* */


public class RootHelper {

    private String superkey;

    public RootHelper() {}

    public RootHelper(String superkey){
        this.superkey = superkey;

    }

    public boolean isSuperkeyValid() {
        if(superkey == null || superkey.isEmpty()) {
            return false;
        }
        return Natives.nativeReady(superkey);
    }

    public int getSuperkeyLength() {
        return superkey.length();

    }
    public void setSuperkey(String superkey){
        if(superkey == null || superkey.isEmpty()){
            this.superkey="";
        }
        this.superkey=superkey;
    }

    public String getSuperkey(){
        return superkey;
    }

    public String getSuperkeyToBase64() {
        String k = Base64.encodeToString(superkey.getBytes(),Contants.SUPER_KEY_ENCODE_MODE);
        return (k==null?"":k);
    }

    public void setSuperkeyForBase64(String superkey) {
        String k = new String(Base64.decode(superkey,Contants.SUPER_KEY_ENCODE_MODE));
        this.superkey = (k==null?"":k);
    }


}
