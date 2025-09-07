package com.mydroid.switchbootslots;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements Handler.Callback, View.OnClickListener, DialogInterface.OnCancelListener {


    public static final String ITEM_CURRENT_SLOT = "item_current_slot";
    public static final String ITEM_SLOTS_NUMBER = "item_slots_number";
    public static final String ITEM_HAL_INFO = "item_hal_info";

    public static final String ITEM_IS_BOOTCTL_VALID = "item_bootctl_valid";
    public static final String ITEM_ROOT_HELPER = "item_root_helper";
    public static final String ITEM_NEED_REBOOT = "item_need_reboot";

    public static final String ITEM_SLOT_A = "item_slot_a";
    public static final String ITEM_SLOT_B = "item_slot_b";
    public static final String ITEM_SUPER_KEY = "item_super_key";

    public static final String BUNDLE_NAME = "slot_bundle";



    /* MainActivity控件列表 */
    TextView tv_slot_numbers;  //显示设备槽位个数
    TextView tv_help_link;  //帮助链接
    TextView tv_hal_info;  //hal版本信息
    TextView tv_note_msg;
    Button btn_switch_slot;  //切换槽位按钮


    /* A槽位显示控件 */
    TextView tv_slot_name_a;
    TextView tv_slot_number_a;
    TextView tv_slot_suffix_a;
    TextView tv_slot_bootable_a;

    /* B槽位显示控件 */
    TextView tv_slot_name_b;
    TextView tv_slot_number_b;
    TextView tv_slot_suffix_b;
    TextView tv_slot_bootable_b;


    /* 超级密钥输入对话框 */
    TextView dialog_text_superkey_error;
    EditText dialog_edit_superkey_input;
    Button dialog_btn_superkey_check;

    /* 组件类 */
    Handler handler;
    Dialog dialog_superkey_input;
    ProgressDialog dialog_switching_slot;  //切换槽位中对话框
    ProgressDialog dialog_rebooting;  //重启系统中对话框

    /* 工具类 */
    RootHelper rootHelper;
    ConfigsHelper configsHelper;
    CopyFileHelper copyFileHelper;
    BootSlotHelper bootSlotHelper;
    BootSlotHelper.BootSlot slot_a,slot_b;

    /* 相关变量 */
    boolean needReboot=false;  //当前需要重启
    boolean isBootctlValid=false;
    boolean isDeviceSupport=false;

    boolean isShowErrorDialog=false;
    int deviceSlots;  //设备的槽位个数
    int currentSlot;  //当前槽位的编号
    String devicehalInfo;  //设备的HAL信息
    long exitTime=0;

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ITEM_CURRENT_SLOT,currentSlot);
        outState.putString(ITEM_HAL_INFO,devicehalInfo);
        outState.putInt(ITEM_SLOTS_NUMBER,deviceSlots);
        outState.putBoolean(ITEM_IS_BOOTCTL_VALID,isBootctlValid);
        outState.putBoolean(ITEM_NEED_REBOOT,needReboot);
        outState.putString(ITEM_SUPER_KEY,rootHelper.getSuperkeyToBase64());


    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(!savedInstanceState.isEmpty()){
            currentSlot = savedInstanceState.getInt(ITEM_CURRENT_SLOT);
            devicehalInfo = savedInstanceState.getString(ITEM_HAL_INFO);
            deviceSlots = savedInstanceState.getInt(ITEM_SLOTS_NUMBER);
            needReboot = savedInstanceState.getBoolean(ITEM_NEED_REBOOT);
            isBootctlValid = savedInstanceState.getBoolean(ITEM_IS_BOOTCTL_VALID);

            rootHelper = new RootHelper();
            rootHelper.setSuperkeyForBase64(savedInstanceState.getString(ITEM_SUPER_KEY));
//            slot_a = (BootSlotHelper.BootSlot) Objects.requireNonNull(savedInstanceState.getBundle(BUNDLE_NAME)).get(ITEM_SLOT_A);
//            slot_b = (BootSlotHelper.BootSlot) Objects.requireNonNull(savedInstanceState.getBundle(BUNDLE_NAME)).get(ITEM_SLOT_B);

            checkDeviceCurrentAllSlotInfo();
        }

    }


    @Override
    protected void onResume() {
        super.onResume();

        if(isDeviceSupport) {
            if(isBootctlValid && rootHelper.isSuperkeyValid()) {
                checkDeviceCurrentAllSlotInfo();
                updateSlotTextDisplay();
            }else {
                checkCurrentSuperKeyAvailable();
            }
        }else{
            showErrorDialogAndExit(getString(R.string.dialog_error_unsupport_device_title),getString(R.string.dialog_error_unsupport_device_msg),true);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        /* 主界面控件 */
        tv_slot_numbers = findViewById(R.id.text_slot_numbers);
        tv_help_link = findViewById(R.id.text_show_help_link);
        tv_hal_info = findViewById(R.id.text_hal_info);
        tv_note_msg = findViewById(R.id.text_slot_note);
        btn_switch_slot = findViewById(R.id.btn_slot_switch);

        /* A槽位显示 */
        tv_slot_name_a = findViewById(R.id.text_slot_name_a);
        tv_slot_number_a = findViewById(R.id.text_slot_number_a);
        tv_slot_suffix_a = findViewById(R.id.text_slot_suffix_a);
        tv_slot_bootable_a = findViewById(R.id.text_slot_bootable_a);

        /* B槽位显示 */
        tv_slot_name_b = findViewById(R.id.text_slot_name_b);
        tv_slot_number_b = findViewById(R.id.text_slot_number_b);
        tv_slot_suffix_b = findViewById(R.id.text_slot_suffix_b);
        tv_slot_bootable_b = findViewById(R.id.text_slot_bootable_b);

        /* 对话框初始化 */
        dialog_superkey_input = new Dialog(this);
        dialog_superkey_input.setOnCancelListener(this);
        dialog_superkey_input.setCancelable(false);
        dialog_superkey_input.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                return false;
            }
        });

        dialog_switching_slot = new ProgressDialog(this);
        dialog_rebooting = new ProgressDialog(this);

        dialog_switching_slot.setCanceledOnTouchOutside(false);
        dialog_rebooting.setCanceledOnTouchOutside(false);

        dialog_switching_slot.setCancelable(false);
        dialog_rebooting.setCancelable(false);

        dialog_switching_slot.setMessage(getString(R.string.dialog_msg_switching_slot));
        dialog_rebooting.setMessage(getString(R.string.dialog_msg_rebooting));


        /* 一些必须的类初始化 */
        handler = new Handler(getMainLooper(),this);
        rootHelper = new RootHelper("");
        configsHelper = new ConfigsHelper(this);
        bootSlotHelper = new BootSlotHelper(getFilesDir().getAbsolutePath());
        copyFileHelper = new CopyFileHelper();
        /* 设置bootctl 文件的目标,权限 */
        copyFileHelper.setSource(getApplicationInfo().nativeLibraryDir+"/"+Contants.BOOTCTL_BINARY_FILE_NAME);
        copyFileHelper.setTarget(getFilesDir().getAbsolutePath()+"/"+Contants.BOOTCTL_BINARY_FILE_NAME.substring(3,Contants.BOOTCTL_BINARY_FILE_NAME.length()-3));
        copyFileHelper.setPerms(true,true,true,true);

        /* 初始化槽位实例 */
        slot_a = new BootSlotHelper.BootSlot(Contants.ACTIVE_SLOT_A);
        slot_b = new BootSlotHelper.BootSlot(Contants.ACTIVE_SLOT_B);


        /* 按钮点击事件回调设置 */
        tv_help_link.setOnClickListener(this);
        btn_switch_slot.setOnClickListener(this);


        /* 检测当前设备是否支持无缝更新和槽位切换 */
        if(!ABCheck.isDeviceSupported()) {
            isDeviceSupport=false;
        }else {
            isDeviceSupport=true;
        }

        /* 受支持的设备 */
        if(isDeviceSupport) {

            /* 判断bootctl二进制文件是否存在,不存在就复制一份到files目录中 */
            if(!copyFileHelper.isTargetFileExist()) {
                /* 开始复制文件 */
                CopyFileThread copyFileThread = new CopyFileThread(this,handler,copyFileHelper);
                copyFileThread.start();

            }else {
                isBootctlValid=true;  //如果bootctl二进制文件存在
            }

            /* 更新系统的槽位信息 */
            if(isBootctlValid && rootHelper.isSuperkeyValid()) {

                checkDeviceCurrentAllSlotInfo();
                updateSlotTextDisplay();

            }else {
                checkCurrentSuperKeyAvailable();
            }
        }else{
            showErrorDialogAndExit(getString(R.string.dialog_error_unsupport_device_title),getString(R.string.dialog_error_unsupport_device_msg),true);
        }


        checkDeviceCurrentAllSlotInfo();
        updateSlotTextDisplay();
    }

    /* 检测当前程序的超级密钥是否可用，如果不可用就提示用户输入正确的密钥并保存 */
    public void checkCurrentSuperKeyAvailable() {
        if(!isDeviceSupport) return;
        if(!configsHelper.hasSuperkey()) {  //没有保存密钥就提示输入密钥
            if(Contants.keyDebugMode) Toast.makeText(this, "没有密钥,需要输入", Toast.LENGTH_SHORT).show();
            showSuperKeyInputDialog();

        }else {  //保存了密钥就读取保存的密钥
            String key = configsHelper.getSuperkey();
            if(Contants.keyDebugMode) Toast.makeText(this, "读取本地保存密钥: "+key, Toast.LENGTH_SHORT).show();
            rootHelper.setSuperkey(key);
            if(!rootHelper.isSuperkeyValid()) {  //如果保存的密钥无效,就重新让用户输入
                if(Contants.keyDebugMode) Toast.makeText(this, "当前本地保存的密钥已过期!,需要重新保存", Toast.LENGTH_SHORT).show();
                showSuperKeyInputDialog();
            }
            /* 直接进行获取设备的槽位信息并更新槽位显示 */
            checkDeviceCurrentAllSlotInfo();
            updateSlotTextDisplay();
        }


    }

    /* 显示超级密钥输入对话框,从用户获取超级密钥 */
    public void showSuperKeyInputDialog() {
        //对话框布局
        View dialog_layout = LayoutInflater.from(this).inflate(R.layout.dialog_super_key_input,null);

        dialog_edit_superkey_input = dialog_layout.findViewById(R.id.edit_dialog_super_key_input);
        dialog_text_superkey_error = dialog_layout.findViewById(R.id.text_dialog_super_key_error);
        dialog_btn_superkey_check = dialog_layout.findViewById(R.id.btn_dialog_super_key);

        dialog_btn_superkey_check.setOnClickListener(this);  //设置按钮点击回调方法
        dialog_edit_superkey_input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                dialog_text_superkey_error.setVisibility(INVISIBLE);
            }
        });


        dialog_superkey_input.setContentView(dialog_layout);
        dialog_superkey_input.getWindow().setBackgroundDrawableResource(android.R.color.transparent);  //设置对话框背景透明
        dialog_superkey_input.setCanceledOnTouchOutside(false);  //设置对话框不能点击框外关闭
        dialog_superkey_input.show();  //显示出对话框

    }


    /**
     * 接收子线程消息的回调方法
     * @param msg A {@link android.os.Message Message} object
     * @return
     */
    @Override
    public boolean handleMessage(@NonNull Message msg) {

        if(msg.what == Contants.HANDLER_THREAD_ROOT_COMMAND) {

            checkSlotResult(msg.arg1,msg.arg2,(String) msg.obj);
        }else if(msg.what == Contants.HANDLER_THREAD_COPY_FILE) {
            checkCopyFileResult(msg.arg1,msg.obj);
        }

        /* 接收到子线程返回参数后更新界面显示 */
        updateSlotTextDisplay();
        return false;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btn_dialog_super_key) {  //超级密钥输入对话框: 确认按钮

            if(!copyFileHelper.isTargetFileExist() || !isBootctlValid){
                isBootctlValid=false;
                if(dialog_superkey_input.isShowing()) dialog_superkey_input.cancel();
                showErrorDialogAndExit(getString(R.string.dialog_error_bootctl_invalid_title),getString(R.string.dialog_error_bootctl_invalid_msg),false);

            }else {

                String key = dialog_edit_superkey_input.getText().toString().trim();

                if(Contants.keyDebugMode) Toast.makeText(this, "当前输入的密钥: "+key, Toast.LENGTH_SHORT).show();
                rootHelper.setSuperkey(key);

                if(!rootHelper.isSuperkeyValid()) {

                    if(Contants.keyDebugMode) Toast.makeText(this, "超级密钥错误或无效,密钥:"+key, Toast.LENGTH_SHORT).show();
                    dialog_text_superkey_error.setVisibility(VISIBLE);

                }else {
                    if(Contants.keyDebugMode) Toast.makeText(this, "超级密钥有效,密钥:"+rootHelper.getSuperkey()+"  Base64数据:"+rootHelper.getSuperkeyToBase64(), Toast.LENGTH_SHORT).show();
                    configsHelper.saveSuperkey(rootHelper.getSuperkey());
                    dialog_superkey_input.cancel();

                    checkDeviceCurrentAllSlotInfo();
                    updateSlotTextDisplay();
                }

            }




        }else if(v.getId() == R.id.btn_slot_switch) {  //切换槽位

            showIsSwitchSlotDialog(getString(R.string.dialog_switch_slot_title),getString(R.string.dialog_switch_slot_msg,currentSlot==Contants.ACTIVE_SLOT_A?"B":"A"));

        } else if(v.getId() == R.id.text_show_help_link) {  //帮助
            /* 跳转到google官方帮助链接 */
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Contants.WEB_HELP_LINK));
            startActivity(intent);

        }


    }

    /* 更新槽位显示部分 */
    @SuppressLint({"SetTextI18n", "ResourceType"})
    public void updateSlotTextDisplay() {

        /* 显示设备槽位个数 */
        tv_slot_numbers.setText(getString(R.string.string_slots_number,(deviceSlots > 0?deviceSlots:0)));
        tv_hal_info.setText(getString(R.string.string_boot_hal_version,devicehalInfo==null?"未知":devicehalInfo));
        /* 更新槽位切换后需要重新启动提示 */
        if(needReboot){
            tv_note_msg.setTextColor(Color.RED);
            tv_note_msg.setText(getString(R.string.string_text_note_need_reboot));
        }else {
            tv_note_msg.setTextColor(Color.BLUE);
            tv_note_msg.setText(getString(R.string.string_text_note_warn));
        }
        /* 如果槽位已经切换就禁用切换按钮让用户重启设备 */
        btn_switch_slot.setEnabled(!needReboot && isBootctlValid);

        /* 槽位A部分 */
        tv_slot_name_a.setText(  (currentSlot==Contants.ACTIVE_SLOT_A&&slot_a.active?"*":"") + getString(R.string.string_slot_name_a));
        tv_slot_number_a.setText(getString(R.string.string_slot_num_title,slot_a.number));
        tv_slot_suffix_a.setText(getString(R.string.string_slot_suffix_title,  (slot_a.suffix==null||slot_a.suffix.isEmpty())?"未知":slot_a.suffix)  );
        tv_slot_bootable_a.setText(getString(R.string.string_slot_bootable_title,  slot_a.bootable?"是":"否")  );

        /* 槽位B部分 */
        tv_slot_name_b.setText((currentSlot==Contants.ACTIVE_SLOT_B&&slot_b.active?"*":"") + getString(R.string.string_slot_name_b));
        tv_slot_number_b.setText(getString(R.string.string_slot_num_title,slot_b.number));
        tv_slot_suffix_b.setText(getString(R.string.string_slot_suffix_title,  (slot_b.suffix==null||slot_b.suffix.isEmpty())?"未知":slot_b.suffix )  );
        tv_slot_bootable_b.setText(getString(R.string.string_slot_bootable_title,  slot_b.bootable?"是":"否")  );


    }


    /**
     * 检测执行bootctl的槽位操作命令的返回项目和返回值
     * msg.arg1 表示返回的项目
     * msg.arg2 表示操作的槽位，执行切换到某个槽位的操作就返回切换后的槽位
     * msg.obj 是返回的数据,String对象或是其它的
     */
    public void checkSlotResult(int result,int slot,String obj) {

        switch(result) {
            case Contants.RESULT_ROOT_COMMAND_HAL_INFO:  //返回HAL信息
                devicehalInfo = obj.toString().substring(12);

                if(Contants.debugMode) Toast.makeText(this, "返回HAL信息: "+devicehalInfo, Toast.LENGTH_SHORT).show();
                break;

            case Contants.RESULT_ROOT_COMMAND_CURRENT_SLOT:  //返回当前槽位
                if(Contants.debugMode) Toast.makeText(this, "返回当前活动槽位: "+slot, Toast.LENGTH_SHORT).show();

                if(obj!=null && !obj.isEmpty()){
                    if(obj.equals("switched")) needReboot=true;
                }
                if(slot == Contants.ACTIVE_SLOT_A) {
                    currentSlot = Contants.ACTIVE_SLOT_A;
                    slot_a.active = true;
                    //slot_a.number = Contants.ACTIVE_SLOT_A;
                }else if(slot == Contants.ACTIVE_SLOT_B){
                    currentSlot=Contants.ACTIVE_SLOT_B;
                    slot_b.active=true;
                    //slot_b.number = Contants.ACTIVE_SLOT_B;
                }
                break;

            case Contants.RESULT_ROOT_COMMAND_SLOTS:  //返回槽位个数
                deviceSlots = Integer.parseInt(obj);
                if(Contants.debugMode) Toast.makeText(this, "返回设备槽位个数:"+deviceSlots, Toast.LENGTH_SHORT).show();
                break;

            case Contants.RESULT_ROOT_COMMAND_SLOT_SUFFIX:  //返回槽位后缀
                if(Contants.debugMode) Toast.makeText(this, "返回槽位后缀: "+obj, Toast.LENGTH_SHORT).show();

                if(slot == Contants.ACTIVE_SLOT_A)
                    slot_a.suffix = obj.toString();
                else if(slot == Contants.ACTIVE_SLOT_B)
                    slot_b.suffix = obj.toString();
                break;

            case Contants.RESULT_ROOT_COMMAND_SLOT_IS_BOOTABLE:  //返回槽位是否可启动
                if(Contants.debugMode) Toast.makeText(this, "返回槽位可启动: "+slot+" 数据:"+obj, Toast.LENGTH_SHORT).show();
                if(slot == Contants.ACTIVE_SLOT_A)
                    slot_a.bootable = Boolean.parseBoolean(obj);
                else if(slot == Contants.ACTIVE_SLOT_B)
                    slot_b.bootable = Boolean.parseBoolean(obj);
                break;

            case Contants.RESULT_ROOT_COMMAND_SLOT_IS_SUCCESS:  //返回槽位是否被标记为成功启动
                if(Contants.debugMode) Toast.makeText(this, "返回槽位是否启动成功: "+slot+" 数据: "+obj, Toast.LENGTH_SHORT).show();
                if(slot == Contants.ACTIVE_SLOT_A)
                    slot_a.success = Boolean.parseBoolean(obj);
                else if(slot == Contants.ACTIVE_SLOT_B)
                    slot_b.success = Boolean.parseBoolean(obj);
                break;

            case Contants.RESULT_ROOT_COMMAND_SLOT_SET_ACTIVE_SUCCESS:  //返回是否设置活动槽位成功
                if(Contants.debugMode) Toast.makeText(this, "返回设置活动槽位成功, 槽位: "+slot, Toast.LENGTH_SHORT).show();
                needReboot=true;
                if(slot == Contants.ACTIVE_SLOT_A) {
                    slot_a.active = true;
                    slot_b.active = false;
                    currentSlot = Contants.ACTIVE_SLOT_A;
                }else if(slot == Contants.ACTIVE_SLOT_B){
                    slot_a.active=false;slot_b.active=true;
                    currentSlot=Contants.ACTIVE_SLOT_B;}
                dialog_switching_slot.cancel();
                showRebootDialog(getString(R.string.dialog_reboot_title),getString(R.string.dialog_reboot_msg));
                break;

            case Contants.RESULT_ROOT_COMMAND_SLOT_MARK_BOOT_SUCCESS:  //返回是否成功标记当前槽位为成功
                if(Contants.debugMode) Toast.makeText(this, "返回标记槽位为启动成功,槽位: "+slot, Toast.LENGTH_SHORT).show();
                if(slot == Contants.ACTIVE_SLOT_A)
                    slot_a.success=true;
                else if(slot == Contants.ACTIVE_SLOT_B)
                    slot_b.success=true;
                break;

            case Contants.RESULT_BOOTCTL_INVALID:
                if(!isShowErrorDialog){
                    isBootctlValid=false;
                    if(dialog_superkey_input.isShowing()) dialog_superkey_input.cancel();
                    showErrorDialogAndExit(getString(R.string.dialog_error_bootctl_invalid_title),getString(R.string.dialog_error_bootctl_invalid_msg),false);
                    isShowErrorDialog=true;
                }

                break;

        }

    }

    /**
     * Handler 接收到文件复制结果的处理方法
     * @param result
     * @param data
     */
    public void checkCopyFileResult(int result,Object data) {

        /* 复制bootctl二进制文件完成 */
        if(result == Contants.RESULT_COPY_FILE_SUCCESS) {
            if(Contants.debugMode) Toast.makeText(this, "复制文件成功,数据:"+data, Toast.LENGTH_SHORT).show();

            /* 文件被成功复制到指定位置，将标志为置为1 */
            isBootctlValid=true;

            /* 如果bootctl可用 且superkey是有效的就更新设备的槽位数据 */
            if(isBootctlValid && isDeviceSupport && rootHelper.isSuperkeyValid()) {

                /* 重新获取槽位信息 */
                checkCurrentSuperKeyAvailable();
                checkDeviceCurrentAllSlotInfo();
            }

            /* 复制文件失败 */
        }else if(result == Contants.RESULT_COPY_FILE_FAILED) {
            if(Contants.debugMode) Toast.makeText(this, "复制文件失败,数据:"+data, Toast.LENGTH_SHORT).show();
            isBootctlValid=false;

            if(dialog_superkey_input.isShowing()) {
                dialog_superkey_input.cancel();
            }
            /* 复制文件失败, 功能不可用 */
            showErrorDialogAndExit(getString(R.string.dialog_error_copy_file_failed_title),getString(R.string.dialog_error_copy_file_failed_msg),false);
        }

    }

    /**
     * 启动获取槽位信息的子线程,通过Handler中回调函数handleMessage接收子线程发送的消息
     */
    public void checkDeviceCurrentAllSlotInfo() {
        if(!isBootctlValid || !isDeviceSupport || !rootHelper.isSuperkeyValid()) return;

        new SwitchSlotThread(this,handler,rootHelper,SwitchSlotThread.ACTION_GET_HAL_INFO).start();  //获取HAL信息
        new SwitchSlotThread(this,handler,rootHelper,SwitchSlotThread.ACTION_GET_SLOTS).start();  //获取槽位个数
        new SwitchSlotThread(this,handler,rootHelper,SwitchSlotThread.ACTION_GET_CURRENT_SLOT).start(); //获取当前槽位
        new SwitchSlotThread(this,handler,rootHelper,SwitchSlotThread.ACTION_GET_SLOT_SUFFIX).start(); //获取槽位后缀
        new SwitchSlotThread(this,handler,rootHelper,SwitchSlotThread.ACTION_IS_BOOTABLE).start();  //是否可启动
        new SwitchSlotThread(this,handler,rootHelper,SwitchSlotThread.ACTION_IS_SUCCESS).start();  //是否成功启动
    }

    public void showErrorDialogAndExit(String title,String msg,boolean hasbtn) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setIcon(R.drawable.dialog_error_icon);
        builder.setPositiveButton("退出", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();  //关闭程序
            }
        });

        if(hasbtn) {
            builder.setNeutralButton("帮助", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    /* 跳转到google官方帮助链接 */
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Contants.WEB_HELP_LINK));
                    startActivity(intent);
                }
            });

        }
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                return false;
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        if(!dialog.isShowing()){
            dialog.show();
        }


    }

    /**
     * 显示确认切换槽位的对话框
     * @param title
     * @param msg
     */
    public void showIsSwitchSlotDialog(String title,String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.dialog_error_icon);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(currentSlot < 0) return;
                dialog_switching_slot.show();
                if(currentSlot == Contants.ACTIVE_SLOT_A) {
                    new SwitchSlotThread(MainActivity.this,handler,rootHelper,SwitchSlotThread.ACTION_SET_ACTIVE,Contants.ACTIVE_SLOT_B).start();

                }else if(currentSlot == Contants.ACTIVE_SLOT_B){
                    new SwitchSlotThread(MainActivity.this,handler,rootHelper,SwitchSlotThread.ACTION_SET_ACTIVE,Contants.ACTIVE_SLOT_A).start();

                }
                dialog.cancel();

            }
        });

        builder.setNeutralButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        //dialog.setCancelable(false);
        //dialog.setCanceledOnTouchOutside(false);
        dialog.show();

    }

    public void showRebootDialog(String title,String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setIcon(R.drawable.dialog_error_icon);
        builder.setPositiveButton("重启", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog_rebooting.show();
                new SwitchSlotThread(MainActivity.this,handler,rootHelper,SwitchSlotThread.ACTION_REBOOT_SYSTEM).start();
            }
        });

        builder.setNeutralButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                return false;
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){

            if(System.currentTimeMillis() - exitTime > 3000) {
                Toast.makeText(this, "再按一次返回键退出", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            }else {
                //new SwitchSlotThread(this,handler,rootHelper,SwitchSlotThread.ACTION_ENABLE_SELINUX).start();

                finish();

            }
            return true;


        }


        return super.onKeyDown(keyCode, event);
    }

    /* 对话框取消监听事件 */
    @Override
    public void onCancel(DialogInterface dialog) {
        if(isBootctlValid) {
            checkDeviceCurrentAllSlotInfo();
            updateSlotTextDisplay();

        }


    }


    /* 当应用程序切换到后台或被终止时 */
    @Override
    protected void onStop() {
        super.onStop();
        new SwitchSlotThread(this,handler,rootHelper,SwitchSlotThread.ACTION_ENABLE_SELINUX).start();

    }


}