:: Start loop testing
@SET testRunner=android.test.InstrumentationTestRunner

@CALL:log "Start MoMS TestCase"

:: Test Test Protection Mechanism
:: 1. JPE and Proguard Check
:: 2. API's Permission Check
:: 3. API's License Check
@CALL:log "Test Protection Mechanism"
adb install -r MoMS_Protection_TestCase.apk
@SET testPackage=com.mediatek.mom.test.protection
@adb shell am instrument -w %testPackage%/%testRunner%
@adb uninstall %testPackage%

@CALL:log "Setup function test"
@call mom_function_testacse_steup

:: Test MoMS controller functions
@CALL:log "Test Controller Functions"
@adb install -r MoMS_Function_TestCase.apk
@SET testPackage=com.mediatek.mom.test.func
@adb shell am instrument -w %testPackage%/%testRunner%
::@adb shell am instrument -e class %testPackage%.PermissionControlTestCase#test10_AttachAndPriority -w %testPackage%/%testRunner%
@adb uninstall %testPackage%

@CALL:log "Reset function test"
@call mom_function_testacse_reset

@CALL:log "Finish MoMS TestCase"

@GOTO:EOF

:Log
@ECHO //--------------------------------
@ECHO // [ %1 ]
@ECHO //--------------------------------
@GOTO:EOF