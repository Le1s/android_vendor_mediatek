@SET CTAApk=MTK_CtaTestAPK_KK.apk
@SET TencentApk=Tencent_Permission_Manager_CTA.apk
@SET PermCtrlApk=PermissionControl.apk

@adb remount
@adb push %CTAApk% /sdcard/MoMS/%CTAApk%
@adb push %TencentApk% /sdcard/MoMS/%TencentApk%
@adb uninstall com.tencent.tcuser
@adb pull /system/app/%PermCtrlApk% %PermCtrlApk%
@adb shell rm -f /system/app/%PermCtrlApk%
@adb install -r ManagerEmulator.apk