package com.mydroid.switchbootslots;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;

public class CopyFileThread extends Thread {

    public static final String TAG = "CopyFileThread";


    @NonNull
    private Handler handler;

    private CopyFileHelper copyFileHelper;

    private Context context;

    public CopyFileThread() {

    }
    public CopyFileThread(Context context,Handler handler,CopyFileHelper helper){
        this.context = context;
        this.copyFileHelper = helper;
        this.handler = handler;
    }

    public void setCopyFileHelper(CopyFileHelper helper) {
        this.copyFileHelper = helper;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void sendMessage(int result,String msg) {
        Message m = new Message();
        m.what = Contants.HANDLER_THREAD_COPY_FILE;
        m.arg1 = result;
        m.obj = msg;
        handler.sendMessage(m);
    }

    @Override
    public void run() {
        super.run();


        if(copyFileHelper.isSourceFileExist()) {

            try {


                boolean status = copyFileHelper.copySourceFileToTargetFile();
                boolean exist = copyFileHelper.isTargetFileExist();

                if(status && exist) {
                    sendMessage(Contants.RESULT_COPY_FILE_SUCCESS,"");
                }else {
                    sendMessage(Contants.RESULT_COPY_FILE_FAILED,"");
                }

            } catch (IOException e) {
                sendMessage(Contants.RESULT_COPY_FILE_FAILED,"");
                Log.e(TAG, "run: Error, copy file source: "+copyFileHelper.getSourceFilePath()+"   target: "+copyFileHelper.getTargetFilePath(),e.getCause() );

               // throw new RuntimeException(e);

            }

        }

    }
}
