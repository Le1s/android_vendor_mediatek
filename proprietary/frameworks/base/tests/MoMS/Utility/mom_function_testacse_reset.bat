@SET TencentApk=Tencent_Permission_Manager_CTA.apk
@SET PermCtrlApk=PermissionControl.apk

@adb remount
@adb uninstall com.mediatek.mom.test.func
@adb uninstall com.mediatek.mom.test.app.mgremu
@adb uninstall com.tencent.tcuser
@adb uninstall com.mediatek.cta;
@adb shell rm -rf /sdcard/MoMS
@adb push %PermCtrlApk% /system/app/
@DEL /Q %PermCtrlApk%
