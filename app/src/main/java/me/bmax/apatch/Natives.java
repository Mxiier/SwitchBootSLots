package me.bmax.apatch;

import android.os.Parcelable;

import androidx.annotation.Keep;

public class Natives {

    public static final String DEFAULT_SCONTEXT = "u:r:untrusted_app:s0";

    static {

        System.loadLibrary("apjni");
    }

    @Keep
    abstract static class Profile implements Parcelable {

        int uid = 0;
        int toUid = 0;
        String scontext = DEFAULT_SCONTEXT;

    }


    @Keep
    static class KPMCtlRes {
        long rc = 0;
        String outMsg = null;

        public KPMCtlRes() {

        }

        public KPMCtlRes(long rc, String outMsg){
            this.rc = rc;
            this.outMsg = outMsg;

        }

    }

    //下面是声明的原生方法,这些方法在 libapjni.so 中实现

    public static native int nativeSu(String superKey, int toUid, String scontext);  //切换程序到root权限下

    public static native boolean nativeReady(String superKey);  //检测当前root是否可用

    public static native String nativeSuPath(String superKey);  //获取su二进制文件路径

    public static native int[] nativeSuUids(String superKey); //获取su的uid列表

    public static native long nativeKernelPatchVersion(String superKey); //获取KernelPatch版本

    public static native String nativeKernelPatchBuildTime(String superKey);  //获取KernelPatch的编译时间

    public static native long nativeLoadKernelPatchModule(String superKey, String modulePath, String args);

    public static native long nativeUnloadKernelPatchModule(String superKey, String moduleName);

    public static native long nativeKernelPatchModuleNum(String superKey);

    public static native String nativeKernelPatchModuleList(String superKey);

    public static native String nativeKernelPatchModuleInfo(String superKey, String moduleName);

    public static native KPMCtlRes nativeControlKernelPatchModule(String superKey, String modName, String jctlargs);

    public static native long nativeThreadSu(String superKey, int uid, String scontext);

    public static native long nativeGrantSu(String superKey, int uid, int toUid, String scontext);

    public static native long nativeRevokeSu(String superKey, int uid);  //撤销root权限

    public static native int nativeSetUidExclude(String superKey, int uid, int exclude);

    public static native int nativeGetUidExclude(String superKey, int uid);

    public static native Profile nativeSuProfile(String superKey, int uid);

    public static native boolean nativeResetSuPath(String superKey, String path);

    public static native boolean nativeGetSafeMode(String superKey);


    //以下是调用的方法
    public static boolean su(String superKey, int toUid) {
        return nativeSu(superKey,toUid,"") == 0;
    }

    public static boolean su(String superKey) {
        return nativeSu(superKey,0,"") == 0;

    }

    public static boolean su(String superKey, int toUid, String scontext) {
        return nativeSu(superKey,toUid,scontext) == 0;

    }

    public static boolean revokeSu(String superKey) {
        return nativeRevokeSu(superKey,0)==0;

    }

}

