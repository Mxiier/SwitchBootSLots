package com.mydroid.switchbootslots;

import android.util.Base64;

public class Contants {

    /* 调试模式标志 */
    public static final boolean debugMode=false;
    public static final boolean keyDebugMode = false;

    public static final int SUPER_KEY_ENCODE_MODE = Base64.DEFAULT;
    /* 文件名和链接配置 */
    public static final String BOOTCTL_BINARY_FILE_NAME = "libbootctl.so";
    public static final String SHARED_CONFIGS_FILE_NAME = "configs";
    public static final String PROFILE_BOOT_SLOT_SWITCHED_A = "/dev/.bootctl_slot_switched_a";
    public static final String PROFILE_BOOT_SLOT_SWITCHED_B = "/dev/.bootctl_slot_switched_b";

    public static final String WEB_HELP_LINK = "https://source.android.google.cn/docs/core/ota/ab?hl=zh-cn";

    public static final String PROP_KEY_VIRTUAL_AB_ENABLED = "ro.virtual_ab.enabled";
    public static final String PROP_KEY_VIRTUAL_AB_RETROFIT = "ro.virtual_ab.retrofit";
    public static final String PROP_KEY_BOOT_SLOT_SUFFIX = "ro.boot.slot_suffix";
    public static final String PROP_KEY_BUILD_AB_UPDATE = "ro.build.ab_update";


    /* 线程Handler的what编号配置 */
    public static final int HANDLER_THREAD_COPY_FILE = 0;
    public static final int HANDLER_THREAD_ROOT_COMMAND = 1;

    /* 线程Handler的返回Result编号配置 */
    public static final int RESULT_COPY_FILE_SUCCESS = 0;
    public static final int RESULT_COPY_FILE_FAILED = 1;

    public static final int RESULT_ROOT_COMMAND_SLOTS = 0;   //返回槽位个数
    public static final int RESULT_ROOT_COMMAND_HAL_INFO = 1;  //返回HAL信息
    public static final int RESULT_ROOT_COMMAND_CURRENT_SLOT = 2;  //返回当前槽位
    public static final int RESULT_ROOT_COMMAND_SLOT_SUFFIX = 3;  //返回槽位后缀
    public static final int RESULT_ROOT_COMMAND_SLOT_IS_BOOTABLE = 4;  //返回槽位是否可启动
    public static final int RESULT_ROOT_COMMAND_SLOT_IS_SUCCESS = 5;  //返回槽位是否标记为成功
    public static final int RESULT_ROOT_COMMAND_SLOT_SET_ACTIVE_SUCCESS = 6;  //返回设置活动槽位成功
    public static final int RESULT_ROOT_COMMAND_SLOT_MARK_BOOT_SUCCESS = 7;  //返回标记槽位成功

    public static final int RESULT_BOOTCTL_INVALID = 8;


    /* 槽位编号配置信息 */
    public static final int ACTIVE_SLOT_A = 0;
    public static final int ACTIVE_SLOT_B = 1;


    /* 调试数据 */
    public static final int DEBUG_MODE_CURRENT_SLOT = 1;
    public static final String DEBUG_MODE_HAL_INFO = "android.hardware.bootcontrol:1.0";

    public static final int DEBUG_MODE_SLOTS = 2;

    public static final boolean DEBUG_MODE_IS_BOOTABLE = false;

    public static final boolean DEBUG_MODE_IS_SUCCESS = true;

    public static final String DEBUG_MODE_SLOT_SUFFIX = "_n";

    public static final boolean DEBUG_MODE_SET_ACTIVE_SLOT_RESULT = true;

    public static final boolean DEBUG_MODE_DEVICE_SUPPORTED = true;


}
