package com.mydroid.switchbootslots;

import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import me.bmax.apatch.Natives;

/**
 * 启动槽位设置和读取的功能类，实现对槽位的设置和判断获取等操作，这个类的操作通常需要在指定的线程中进行
 * 否则会阻塞主线程
 */


public class BootSlotHelper {

    public static final String TAG = "BootSlotHelper";


    static class BootSlot {  //表示一个槽位
        public int number=0;  //槽位的编号
        public boolean active=false; //是否为活动槽位
        public boolean bootable=false;
        public boolean success=false;  //是否标记为成功
        public String suffix="";  //槽位的后缀
        public BootSlot(int number) {
            this.number = number;
        }
    }

    static class CmdResult {  /* 命令返回的数据 */
        public String cmdOutput;  //命令的标准输出
        public int exitCode;  //命令退出代码,0表示成功

        public boolean success;  //命令是否成功执行
    }


    private String bootctlDir;
    private RootHelper helper;


    public BootSlotHelper(String bootctlPath) {

        this.bootctlDir = bootctlPath;

    }

    private CmdResult execCommand(String command) throws IOException, InterruptedException {
        Log.i(TAG, "execCommand: Function Start Executing Argument: "+command);

        CmdResult result1 = new CmdResult();
        StringBuilder builder = new StringBuilder();

        if(!isBootctlBinaryValid()){
            result1.exitCode=10;
            result1.success=false;
            result1.cmdOutput="error";
            return result1;
        }

        Log.i(TAG, "execCommand: Starting Execute Command: "+command);
        Process p = Runtime.getRuntime().exec(command);
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "execCommand: Current Command Process Active Status: "+p.isAlive());
        }

        String s;
        while(( s=reader.readLine() ) != null ) {
            Log.i(TAG, "execCommand: Read Command Output Message! Command: " + command + "   "+"Output: "+s);
            builder.append(s);
        }
        Log.i(TAG, "execCommand: Waitting Current Command Finish...");
        p.waitFor();

        Log.i(TAG, "execCommand: Current Command Finished!   Exit Code: "+p.exitValue());
        
        result1.cmdOutput=builder.toString().replace("\n","");
        result1.exitCode=p.exitValue();
        result1.success=p.exitValue()==0;

        Log.i(TAG, "execCommand: Function Executing Finish! Return... ");
        return result1;
    }

    public boolean isBootctlBinaryValid() throws IOException, InterruptedException {
        File f = new File(bootctlDir+"/"+"bootctl");
        if(f.exists() && f.canExecute() && f.isFile()) {
//            CmdResult result = execCommand(bootctlDir+"/"+"bootctl");
//            if(result!= null && result.exitCode == 64) {
//                return true;
//
//            }
            return true;

        }

        return false;
    }

    public int getCurrentSlot() throws IOException,InterruptedException {

            if(Contants.debugMode) return Contants.DEBUG_MODE_CURRENT_SLOT;


            Log.i(TAG, "getCurrentSlot: Get Device Current Slot Bootctl Binary File Path: "+bootctlDir+"/"+"bootctl");
            CmdResult res = execCommand(bootctlDir+"/"+Commands.GET_CURRENT_SLOT);
            if(res.success && res.cmdOutput != null && !res.cmdOutput.isEmpty()) {
                Log.i(TAG, "getCurrentSlot: Command Executing Success!   Output: "+res.cmdOutput + "     Exit Code: "+res.exitCode);

                if(res.cmdOutput.equals("0")) {
                    return Contants.ACTIVE_SLOT_A;


                }else if(res.cmdOutput.equals("1")) {
                    return Contants.ACTIVE_SLOT_B;
                }
            }

        Log.e(TAG, "getCurrentSlot: Command：" + Commands.GET_CURRENT_SLOT + "   Output: "+res.cmdOutput + "  exit Code: "+res.exitCode, new Throwable());

        return -1;
    }

    public String getHalInfo() throws IOException,InterruptedException {
        if(Contants.debugMode) return Contants.DEBUG_MODE_HAL_INFO;

        CmdResult res = execCommand(bootctlDir+"/"+Commands.GET_HAL_INFO);

        if(res.success && res.cmdOutput != null && !res.cmdOutput.isEmpty()) {
            Log.i(TAG, "getHalInfo: Command Executing Success! Output: "+res.cmdOutput + "   exit Code: "+res.exitCode);

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Log.i(TAG, "getHalInfo: Checked Current System >= Android 11(R)");
                return res.cmdOutput;
            }else {
                return "Boot Control Version <= 1.0";

            }

        }

        Log.e(TAG, "getHalInfo: Error Executing Command：" + Commands.GET_CURRENT_SLOT + "   Output: "+res.cmdOutput + "  exit Code: "+res.exitCode, new Throwable());
        return "";
    }

    public int getSlotsNumber() throws IOException, InterruptedException {
        if(Contants.debugMode) return Contants.DEBUG_MODE_SLOTS;

        CmdResult res = execCommand(bootctlDir+"/"+Commands.GET_SLOT_NUMBER);
        if(res.success && res.cmdOutput != null && !res.cmdOutput.isEmpty()) {
        Log.i(TAG, "getSlotsNumber: device slots number: "+ res.cmdOutput );
            int slots = Integer.parseInt(res.cmdOutput);

            return slots;

        }

        Log.e(TAG, "getSlotsNumber: Error Command Output: "+res.cmdOutput + "    exit Code: "+res.exitCode ,new Throwable());
        return -1;
    }

    public String getSlotSuffix(int slot) throws IOException, InterruptedException {
        if(Contants.debugMode) return Contants.DEBUG_MODE_SLOT_SUFFIX;

        CmdResult res = null;
        if(slot == Contants.ACTIVE_SLOT_A) {
            res = execCommand(bootctlDir+"/"+Commands.GET_SLOT_SUFFIX_A);
        }else if(slot == Contants.ACTIVE_SLOT_B) {
            res = execCommand(bootctlDir+"/"+Commands.GET_SLOT_SUFFIX_B);
        }

        if(res.success && res.cmdOutput != null && !res.cmdOutput.isEmpty()) {

            Log.i(TAG, "getSlotSuffix: Get Device Suffix Success!   Output: "+ res.cmdOutput);
            return res.cmdOutput;

        }

        Log.e(TAG, "getSlotSuffix: Error Command Output: "+res.cmdOutput+"    Exit Code: "+res.exitCode,new Throwable());

        return "";

    }


    public boolean isSlotBootable(int slot) throws IOException, InterruptedException {
        if(Contants.debugMode) return Contants.DEBUG_MODE_IS_BOOTABLE;

        CmdResult res = null;
        if(slot == Contants.ACTIVE_SLOT_A) {
            res = execCommand(bootctlDir+"/"+Commands.IS_SLOT_BOOTABLE_A);

        }else if(slot == Contants.ACTIVE_SLOT_B) {
            res = execCommand(bootctlDir+"/"+Commands.IS_SLOT_BOOTABLE_B);

        }
        if(res.success) {
            Log.i(TAG, "isSlotBootable: Success! Device Slot: "+slot+"   is Bootabled.");
            return res.success;
        }

        Log.w(TAG, "isSlotBootable: Warnning Device Slot: "+slot+" is not Bootabled.", new Exception());
        return false;
    }

    public boolean isSlotBootSuccess(int slot) throws IOException, InterruptedException {
        if(Contants.debugMode) return Contants.DEBUG_MODE_IS_SUCCESS;

        CmdResult res = null;
        if(slot == Contants.ACTIVE_SLOT_A) {
            res = execCommand(bootctlDir+"/"+Commands.IS_SLOT_MARKED_BOOT_SUCCESSFUL_A);

        }else if(slot == Contants.ACTIVE_SLOT_B) {
            res = execCommand(bootctlDir+"/"+Commands.IS_SLOT_MARKED_BOOT_SUCCESSFUL_B);
        }

        if(res.success) {
            Log.i(TAG, "isSlotBootSuccess: Success! Device Slot: " + slot + " is successful bootabled.");
            return res.success;

        }

        Log.w(TAG, "isSlotBootSuccess: Warnning! Device Slot: "+slot+" is not successful bootabled.",new Exception() );
        return false;

    }

    public boolean setActiveSlot(int slot) throws IOException, InterruptedException {
        if(Contants.debugMode) return Contants.DEBUG_MODE_SET_ACTIVE_SLOT_RESULT;

        CmdResult res=null;
        if(slot == Contants.ACTIVE_SLOT_A) {
            res = execCommand(bootctlDir+"/"+Commands.SET_ACTIVE_SLOT_A);


        }else if(slot == Contants.ACTIVE_SLOT_B) {
            res = execCommand(bootctlDir+"/"+Commands.SET_ACTIVE_SLOT_B);

        }

        if(res.success) {
            Log.i(TAG, "setActiveSlot: Success! Setting Device Current Active Slot to Slot: "+ slot+" Done.");

            return res.success;
        }

        Log.e(TAG, "setActiveSlot: Error! Setting Device Currnet Active Slot to Slot: "+slot+" Failed.",new Exception());
        return false;
    }

    public void setCurrentSlotSuccess() throws IOException, InterruptedException {
        CmdResult res = execCommand(bootctlDir+"/"+Commands.MARK_CURRENT_SLOT_BOOT_SUCCESSFUL);
        if(res.success) {
            Log.i(TAG, "setCurrentSlotSuccess: Success! Setting Device Current Active Slot as Marked Boot Successful Done.");

        }

        Log.e(TAG, "setCurrentSlotSuccess: Error! Setting Device Current Active Slot as Marked Boot Successful Failed.", new Throwable());
    }

    public boolean isSelinuxEnabled() throws IOException, InterruptedException {
        Log.i(TAG, "isSelinuxEnabled: Start Checking Device Current SELinux Status!   Use Command: "+Commands.GET_SELINUX_STATUS);

        CmdResult res = execCommand(Commands.GET_SELINUX_STATUS);

        boolean state = false;
        if(res.success && res.cmdOutput != null && !res.cmdOutput.isEmpty()) {
            if(res.cmdOutput.equals("Permissive")) {
                Log.i(TAG, "isSelinuxEnabled: Checking SELinux State Success! Current SELinux Status: "+"Permissive");
                state=false;

            }else if(res.cmdOutput.equals("Enforcing")) {
                Log.i(TAG, "isSelinuxEnabled: Checking SELinux State Success! Current SELinux Status: "+"Enforcing");
                state=true;

            }

        }

        return state;


    }

    public boolean disableSelinux() throws IOException, InterruptedException {
        Log.i(TAG, "disableSelinux: Start Disabling Device SELinux!...");
        CmdResult res = execCommand(Commands.DISABLE_SELINUX);
        if(res.success && res.cmdOutput != null && !res.cmdOutput.isEmpty()){
            Log.i(TAG, "disableSelinux: Disabling Device SELinux Success! SELinux Disabled.");

            return true;
        }
        Log.e(TAG, "disableSelinux: Error! Disabling Device SELinux Failed!",new Throwable());
        return false;

    }

    public boolean enableSelinux() throws IOException, InterruptedException {
        Log.i(TAG, "enableSelinux: Start Enabling Device SELinux...");
        CmdResult res = execCommand(Commands.ENABLE_SELINUX);
        if(res.success && res.cmdOutput != null && !res.cmdOutput.isEmpty()) {
            Log.i(TAG, "enableSelinux: Enabling Device SELinux Success!   SELinux Enabled.");
            return true;

        }
        Log.e(TAG, "enableSelinux: Error! Enabling Device SELinux Failed!",new Throwable());
        return false;
    }

    public void createSlotSwitchedProfile(int slot) throws IOException {
        if(slot == Contants.ACTIVE_SLOT_A) {
            File f = new File(Contants.PROFILE_BOOT_SLOT_SWITCHED_A);
            f.createNewFile();
            f.setWritable(true,false);
            f.setReadable(true,false);

        }else if(slot == Contants.ACTIVE_SLOT_B) {
            File f = new File(Contants.PROFILE_BOOT_SLOT_SWITCHED_B);
            f.createNewFile();
            f.setWritable(true,false);
            f.setReadable(true,false);
        }

    }

    public boolean isSlotSwitched() {
        File profile_a = new File(Contants.PROFILE_BOOT_SLOT_SWITCHED_A);
        File profile_b = new File(Contants.PROFILE_BOOT_SLOT_SWITCHED_B);
        if(profile_a.exists()||profile_b.exists()) {
            return true;
        }
        return false;
    }

    public int getSwitchedSlot() {
        File profile_a = new File(Contants.PROFILE_BOOT_SLOT_SWITCHED_A);
        File profile_b = new File(Contants.PROFILE_BOOT_SLOT_SWITCHED_B);

        if(profile_a.exists()) {
            return Contants.ACTIVE_SLOT_A;

        }else if(profile_b.exists()) {
            return Contants.ACTIVE_SLOT_B;
        }

        return -1;

    }

    public void rebootSystem() throws IOException, InterruptedException {
        Log.i(TAG, "rebootSystem: Start Rebooting System...");
        execCommand(Commands.REBOOT_SYSTEM);

    }



}
