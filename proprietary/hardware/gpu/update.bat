adb remount
adb push .\lib\libGLES_ged.so /system/lib/egl/libGLES.so
adb push .\lib64\libGLES_ged.so /system/lib64/egl/libGLES.so
adb reboot