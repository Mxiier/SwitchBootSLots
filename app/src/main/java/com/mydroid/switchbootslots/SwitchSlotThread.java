package com.mydroid.switchbootslots;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import me.bmax.apatch.Natives;

public class SwitchSlotThread extends Thread {

    public static final String TAG = "SwitchSlotThread";

    public static final int ACTION_GET_HAL_INFO = 0;
    public static final int ACTION_GET_SLOTS = 1;
    public static final int ACTION_GET_CURRENT_SLOT = 2;
    public static final int ACTION_GET_SLOT_SUFFIX = 3;
    public static final int ACTION_IS_BOOTABLE = 4;
    public static final int ACTION_IS_SUCCESS = 5;
    public static final int ACTION_SET_ACTIVE = 6;
    public static final int ACTION_REBOOT_SYSTEM = 7;
    public static final int ACTION_ENABLE_SELINUX = 8;


    private int action;
    private int slot;
    private Handler handler;
    private RootHelper rootHelper;
    private BootSlotHelper bootSlotHelper;
    private Context context;

    public SwitchSlotThread(Context context,Handler handler,RootHelper rootHelper, int action) {
        this.handler = handler;
        this.rootHelper = rootHelper;
        this.action = action;
        this.context = context;
        this.slot=-1;
    }

    public SwitchSlotThread(Context context,Handler handler,RootHelper rootHelper, int action, int slot) {
        this.context = context;
        this.handler = handler;
        this.rootHelper = rootHelper;
        this.action = action;
        this.slot = slot;
    }

    public void sendMessage(int result,int slot,String msg) {
        Log.i(TAG, "sendMessage: Send Handler Message: "+msg+"   Result: "+result+"  Slot: "+slot);
        Message m =new Message();
        m.what = Contants.HANDLER_THREAD_ROOT_COMMAND;
        m.arg1 = result;
        m.arg2 = slot;
        m.obj = msg;
        handler.sendMessage(m);

    }

    @Override
    public void run() {
        super.run();

        if(Natives.nativeReady(rootHelper.getSuperkey())) {
            boolean su = Natives.su(rootHelper.getSuperkey());
            Log.i(TAG, "run: Get Superuser Permission Status: "+su);
        }else {
            Log.e(TAG, "run: Error, Super Key is Invalid!",new Throwable());
            return;
        }


        Log.i(TAG, "run: Start Switch Handler Current Action: "+action+"      Bootctl Binary Path: "+context.getFilesDir().getAbsoluteFile()+"/"+"bootctl");
            try {

                BootSlotHelper helper = new BootSlotHelper(context.getFilesDir().getAbsolutePath());

                if(helper.isSelinuxEnabled()){
                    helper.disableSelinux();  //关闭SElinux不然读取不到数据
                }

                if(!helper.isBootctlBinaryValid()){
                    sendMessage(Contants.RESULT_BOOTCTL_INVALID,-1,"");
                    return;
                }

                if(action == ACTION_GET_HAL_INFO) {

                    String hal = helper.getHalInfo();
                    Log.i(TAG, "run: Get device Hal Info:"+hal+"          action: "+action);
                    sendMessage(Contants.RESULT_ROOT_COMMAND_HAL_INFO,-1,hal);

                }else if(action == ACTION_GET_SLOTS) {
                    int slots = helper.getSlotsNumber();
                    Log.i(TAG, "run: Get device slots number value:"+slots+"  action:"+action);
                    sendMessage(Contants.RESULT_ROOT_COMMAND_SLOTS,-1,String.valueOf(slots));

                }else if(action == ACTION_GET_CURRENT_SLOT) {
                    if(helper.isSlotSwitched()){
                        int current = helper.getSwitchedSlot();
                        Log.i(TAG, "run: Switched Boot Slot to Slot: "+current);
                        sendMessage(Contants.RESULT_ROOT_COMMAND_CURRENT_SLOT,current,"switched");
                        return;
                    }

                    int current = helper.getCurrentSlot();
                    Log.i(TAG, "run: Get device current active slot: "+current+"  action:"+action);
                    sendMessage(Contants.RESULT_ROOT_COMMAND_CURRENT_SLOT,current,"");

                }else if(action == ACTION_IS_BOOTABLE) {


                    boolean bootable_a = helper.isSlotBootable(Contants.ACTIVE_SLOT_A);
                    boolean bootable_b = helper.isSlotBootable(Contants.ACTIVE_SLOT_B);
                    Log.i(TAG, "run: Get device all slots bootable status, slot a bootable: "+bootable_a+"   slot b bootable:"+bootable_b);
                    sendMessage(Contants.RESULT_ROOT_COMMAND_SLOT_IS_BOOTABLE,Contants.ACTIVE_SLOT_A,String.valueOf(bootable_a));
                    sendMessage(Contants.RESULT_ROOT_COMMAND_SLOT_IS_BOOTABLE,Contants.ACTIVE_SLOT_B,String.valueOf(bootable_b));

                }else if(action == ACTION_IS_SUCCESS){

                    boolean success_a= helper.isSlotBootSuccess(Contants.ACTIVE_SLOT_A);
                    boolean success_b=helper.isSlotBootSuccess(Contants.ACTIVE_SLOT_B);
                    sendMessage(Contants.RESULT_ROOT_COMMAND_SLOT_IS_SUCCESS,Contants.ACTIVE_SLOT_A,String.valueOf(success_a));
                    sendMessage(Contants.RESULT_ROOT_COMMAND_SLOT_IS_SUCCESS,Contants.ACTIVE_SLOT_B,String.valueOf(success_b));

                }else if(action == ACTION_SET_ACTIVE) {

                    Log.w(TAG, "run: Warnning! Start Setting Current Device Active Slot to "+slot ,new Throwable());
                    if(slot < 0) return;

                    boolean active = helper.setActiveSlot(slot);
                    Log.w(TAG, "run: Warnning! Setting Current Device Active Slot Status: "+active ,new Throwable());
                    if(active) {
                        helper.createSlotSwitchedProfile(slot);
                        sendMessage(Contants.RESULT_ROOT_COMMAND_SLOT_SET_ACTIVE_SUCCESS, slot, "");
                    }
                }else if(action == ACTION_GET_SLOT_SUFFIX){

                    String suffix_a = helper.getSlotSuffix(Contants.ACTIVE_SLOT_A);
                    String suffix_b = helper.getSlotSuffix(Contants.ACTIVE_SLOT_B);
                    Log.i(TAG, "run: Get Device Slot Suffix String.    Slot A Value: "+suffix_a+"     Slot B Value: "+suffix_b);

                    sendMessage(Contants.RESULT_ROOT_COMMAND_SLOT_SUFFIX,Contants.ACTIVE_SLOT_A,suffix_a);
                    sendMessage(Contants.RESULT_ROOT_COMMAND_SLOT_SUFFIX,Contants.ACTIVE_SLOT_B,suffix_b);

                }else if(action == ACTION_REBOOT_SYSTEM) {
                    Log.w(TAG, "run: Reboot Current System...",new Throwable());

                    helper.rebootSystem();

                }else if(action == ACTION_ENABLE_SELINUX) {
                    if(!helper.isSelinuxEnabled()) helper.enableSelinux();

                }


                boolean state = Natives.revokeSu(rootHelper.getSuperkey());
                Log.d(TAG, "run: Revoke Super User permission. Status: "+state);
            } catch (IOException e) {
                Log.e(TAG, "run: Error, IOException Message:"+e.getMessage(),e.getCause() );
                sendMessage(Contants.RESULT_BOOTCTL_INVALID,-1,"");
                //throw new RuntimeException(e);
            } catch (InterruptedException e) {
                Log.e(TAG, "run: Error, InterruptException Message: "+e.getMessage(),e.getCause());
                sendMessage(Contants.RESULT_BOOTCTL_INVALID,-1,"");
                //throw new RuntimeException(e);
            }


        }

}
