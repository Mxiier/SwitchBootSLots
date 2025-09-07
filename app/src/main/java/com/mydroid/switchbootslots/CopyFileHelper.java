package com.mydroid.switchbootslots;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 复制文件线程，用于复制一个文件到目标路径中
 *
 */

public class CopyFileHelper {

    public static final String TAG = "CopyFileHelper";

    /* 复制文件的源文件和目标文件 */
    private File target;
    private File source;


    private boolean ownerOnly=true;

    /* 目标文件的权限 */
    private boolean isRead=true;
    private boolean isWrite=true;
    private boolean isExecute=true;

    public CopyFileHelper() {}

    public CopyFileHelper(String source, String target,boolean ownerOnly) {
        this.source = new File(source);
        this.target = new File(target);
        this.ownerOnly = ownerOnly;

    }

    public CopyFileHelper(String source,String target,boolean read,boolean write,boolean execute,boolean ownerOnly) {
        this.source = new File(source);
        this.target = new File(target);
        this.ownerOnly = ownerOnly;
        this.isRead = read;
        this.isWrite = write;
        this.isExecute = execute;
    }

    public void setPerms(boolean read,boolean write,boolean exec,boolean ownerOnly) {
        this.isRead = read;
        this.isWrite =write;
        this.isExecute = exec;
        this.ownerOnly = ownerOnly;

    }
    public boolean isSourceFileExist() {
        return source.exists();

    }

    public boolean isTargetFileExist() {
        if(!target.isFile()) {
            target.delete();
            return false;
        }
        return target.exists();
    }

    public String getSourceFilePath() {
        return source.getAbsolutePath();

    }

    public String getTargetFilePath() {
        return target.getAbsolutePath();
    }

    public void setSource(String source) {
        this.source = new File(source);
    }

    public void setTarget(String target) {
        this.target = new File(target);
    }

    public void recopyFileTo() throws IOException {
        if(target.exists()){
            target.delete();

        }
        copySourceFileToTargetFile();

    }

    public File getTarget() {
        return target;
    }

    public boolean copySourceFileToTargetFile() throws IOException {
        Log.i(TAG, "copySourceFileToTargetFile: Starting Copy File Source: "+source.getAbsoluteFile()+"    Target: "+target.getAbsoluteFile());


        if(source.exists()) {
            if(!target.exists()) {
                Log.i(TAG, "copySourceFileToTargetFile: Target File is not exist! Call createNewFile() Create Target File.");
                target.createNewFile();
                target.setReadable(isRead,ownerOnly);
                target.setWritable(isWrite,ownerOnly);
                target.setExecutable(isExecute,ownerOnly);

            }

            InputStream inputStream = new FileInputStream(source);
            OutputStream outputStream = new FileOutputStream(target);
            Log.i(TAG, "copySourceFileToTargetFile: Create Source File InputStream Object Data Length: "+inputStream.available()+" Bytes");

            byte[] buf = new byte[inputStream.available()];
            int len;
            while( (len=inputStream.read(buf)) > 0 ) {

                Log.i(TAG, "copySourceFileToTargetFile: InputStream Reading Data Length: "+len+" Bytes ...");
                outputStream.write(buf,0,len);

            }

            Log.i(TAG, "copySourceFileToTargetFile: Copying File Finished! Closing Stream...");
            inputStream.close();
            outputStream.close();

            return true;

        }

        Log.e(TAG, "copySourceFileToTargetFile: Error! Source File is not Exist!  Can't Copy File.", new Throwable());
        return false;
    }


}
