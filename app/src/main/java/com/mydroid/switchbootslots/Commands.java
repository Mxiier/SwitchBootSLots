package com.mydroid.switchbootslots;

public class Commands {

    public static final String DISABLE_SELINUX = "setenforce 0";
    public static final String ENABLE_SELINUX="setenforce 1";

    public static final String  GET_SELINUX_STATUS = "getenforce";
    public static final String GET_HAL_INFO = "bootctl hal-info";  //获取HAL信息
    public static final String GET_SLOT_NUMBER = "bootctl get-number-slots";  //获取当前设备的槽位个数
    public static final String GET_CURRENT_SLOT = "bootctl get-current-slot";  //获取档期槽位,输出0或1

    public static final String GET_SLOT_SUFFIX_A = "bootctl get-suffix 0";  //获取槽位a的后缀
    public static final String GET_SLOT_SUFFIX_B = "bootctl get-suffix 1";  //获取槽位b的后缀

    public static final String MARK_CURRENT_SLOT_BOOT_SUCCESSFUL = "bootctl mark-boot-successful";  //标记当前槽位启动成功

    public static final String SET_ACTIVE_SLOT_A = "bootctl set-active-boot-slot 0";  //设置槽位a为活动槽位
    public static final String SET_ACTIVE_SLOT_B = "bootctl set-active-boot-slot 1";  //设置槽位b为活动槽位

    public static final String IS_SLOT_BOOTABLE_A = "bootctl is-slot-bootable 0";  //判断槽位a是否为下次启动的槽位,退出代码为0表示是
    public static final String IS_SLOT_BOOTABLE_B = "bootctl is-slot-bootable 1";

    public static final String IS_SLOT_MARKED_BOOT_SUCCESSFUL_A = "bootctl is-slot-marked-successful 0";  //判断槽位a是否启动成功,退出代码为0表示是

    public static final String IS_SLOT_MARKED_BOOT_SUCCESSFUL_B = "bootctl is-slot-marked-successful 1";

    //重启系统
    public static final String REBOOT_SYSTEM = "svc power reboot || reboot";




}
