'''
Author: MTK54039
Date: Mon Jul 2, 2013
Function: Sanity autotest, test : power key can wakeup & suspend the phone.
'''
import sys
import os
import time
import subprocess, re
from subprocess import Popen
from subprocess import PIPE
from optparse import OptionParser  

'''
#Test step:
    1. make sure the screen is on.
    2. press powerkey, check the screen is off.
    3. press powerkey, check the screen is on.
Version: 2.0
'''

POWER_KEY_EVENT_CODE = 26
HOME_KEY_EVENT_CODE = 3
SCREENONPATTERN = 'mScreenOn=true'
AUTO_SUSPEND_RESUME_TIMER = 50
SPM_PCM_TIMER = 1
AUTO_SUSPEND_RESUME_DELAY = 80
START_DELAY = 120
SUSPEND_PASS = 'SLEEP_PASS'
class SleepWakeupTest:
    def __init__(self,serial='0123456789ABCDEF'):
        self.serial = serial;
        
    def adb(self,cmd, serial=None):
        error = 0
        cmd = "adb "+ cmd + ((" -s "+serial) if serial else "")
        proc = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
        [out,err] = proc.communicate()
        if err == None:
            err=""

        result = filter(lambda x:x, [re.sub(r"[\r\n]","",x) for x in out.split("\n")]
                                + [re.sub(r"[\r\n]","",x) for x in err.split("\n")])
        if len(filter(lambda x:re.search("device not found|protocol fault|waiting for device",x),result)) != 0:
            #raise Fatal("adb command failed. is device still available?")
            error = -1

        return (error, result)

    def adbshell(self,cmd):
        return self.adb('shell "%s"'%cmd, self.serial)
      
    def active_wakelock_get(self):
        error, result = self.adbshell("cat /sys/kernel/debug/wakeup_sources")
        return result
        
    def __getWakeLockAll(self):
        os.system('adb -s %s shell "cat /sys/kernel/debug/wakeup_sources" > wakelock'%(self.serial))
        
    def __getWakeLock(self):
        active_wakelock_list = self.active_wakelock_get()
        f=os.open("wakelock", os.O_RDWR|os.O_CREAT)
        for active_wakelock in active_wakelock_list:
            wakelock_info_pattern = re.search('(.*)(\s+)(\d+)(\s+)(\d+)(\s+)(\d+)(\s+)(\d+)(\s+)(\d+)(\s+)(\d+)(\s+)(\d+)(\s+)(\d+)(\s+)(\d+)', active_wakelock)
            if wakelock_info_pattern:
                if int(wakelock_info_pattern.group(11)) != 0:
                    wakelock_str = wakelock_info_pattern.group(1)
                    wakelock_str += "  [active since: "
                    wakelock_str += wakelock_info_pattern.group(11)
                    wakelock_str += "]"
                    print wakelock_str
                    wakelock_str += "\n"
                    os.write(f,wakelock_str)
        os.close(f)
            
    def __isScreenOn(self):
        lines = Popen('adb -s %s shell cat /sys/class/leds/lcd-backlight/brightness'%(self.serial),stdout=PIPE,shell=True).stdout.readlines()
        for line in lines:
            print int(line)
            if int(line) != 0:
                print 'backlight on'
                return True
        print 'backlight off'
        return False
    def __isSuspend(self):
        lines = Popen('adb -s %s shell "cat /sys/power/spm/auto_suspend_resume"'%(self.serial),stdout=PIPE,shell=True).stdout.readlines()
        for line in lines:
            if line.find(SUSPEND_PASS) != -1:
                return True
        return False
    def __isGoldenSetting(self):
        return True
    def runTest(self):
        self.setUp();
        #TODO: run
        failed = False
        failed_suspend = False
        failed_golden = False
        #suspend the phone
        time.sleep(START_DELAY)
        print 'sleep the phone...'
        os.system('adb -s %s shell am broadcast -a com.mediatek.mtklogger.ADB_CMD -e cmd_name stop --ei cmd_target 6'%self.serial)
        time.sleep(5)
        os.system('adb -s %s shell input keyevent %d'%(self.serial,POWER_KEY_EVENT_CODE))
        time.sleep(5)
        if self.__isScreenOn():#the screen should be off
            print "sleep phone failed" 
            print "FAILURES!!!"
            failed = True
        else:
            print "sleep phone OK!"

        #suspend resume test
        print 'suspend the phone...'    
        if os.path.isfile("wakelock"):
            os.remove("wakelock")
        if os.path.isfile("auto_suspend_resume_result"):
            os.remove("auto_suspend_resume_result")
        #if os.path.isfile("suspend_golden_setting_result"):
        #    os.remove("suspend_golden_setting_result")
        
        os.system('adb -s %s shell am broadcast -a com.mediatek.SCREEN_TIMEOUT_MINIMUM'%self.serial)    
        os.system('adb -s %s shell "echo %d %d > /sys/power/spm/auto_suspend_resume"'%(self.serial,AUTO_SUSPEND_RESUME_TIMER,SPM_PCM_TIMER))
        time.sleep(AUTO_SUSPEND_RESUME_DELAY)
        os.system('adb -s %s shell "cat /sys/power/spm/auto_suspend_resume" > auto_suspend_resume_result'%(self.serial))
        #os.system('adb -s %s shell "cat /proc/golden/golden_test" > suspend_golden_setting_result'%(self.serial))
        if not self.__isSuspend():#check the suspend
            print "suspend phone failed" 
            print "AssertFail"
            print "FAILURES!!!"
            failed_suspend = True
            if self.__isScreenOn():
                print "suspend fail - backlight is on, turning it off to get wakelock..." 
                os.system('adb -s %s shell input keyevent %d'%(self.serial,POWER_KEY_EVENT_CODE))
                time.sleep(5)
            #self.__getWakeLock();
            self.__getWakeLockAll(); 
        else:
            print "suspend phone OK!"
            
        #if not self.__isGoldenSetting():#check the suspend golden setting
        #    print "suspend golden check failed" 
        #    print "AssertFail"
        #    print "FAILURES!!!"
        #    failed_golden = True
        #else:
        #    print "suspend golden setting OK!"
            
        if self.__isScreenOn():#the screen should be off
            print "backlight is not off" 
            print "AssertFail"
            print "FAILURES!!!"
            failed = True
        else:
            print "sleep phone OK!"
            
        os.system('adb -s %s shell am broadcast -a com.mediatek.SCREEN_TIMEOUT_NORMAL'%self.serial)
            
        print 'wakeup the phone...'
        os.system('adb -s %s shell input keyevent %d'%(self.serial,POWER_KEY_EVENT_CODE))
        time.sleep(5)
        if not self.__isScreenOn():#the screen should be on
            print "wakeup phone failed"
            print "FAILURES!!!"
            failed = True
        else:
            print "wakeup phone OK!"
        if not failed and not failed_suspend and not failed_golden:
            print "OK (1 tests)"
        self.tearDown();
    def setUp(self):#make sure the screen is on and phone on homescreen
        if not self.__isScreenOn():
            print 'setUp,screen is off, turning it on...'
            os.system('adb -s %s shell input keyevent %d'%(self.serial,POWER_KEY_EVENT_CODE))
            time.sleep(5)
        unlockPath = os.path.join(os.path.dirname(os.path.realpath(__file__)),'Unlocker.apk')
        os.system('adb -s %s install -r "%s"'%(self.serial, unlockPath))
        os.system('adb -s %s shell am start -n com.mediatek.unlocker/.MainActivity'%self.serial)
        time.sleep(5)
        os.system('adb -s %s shell input keyevent %d'%(self.serial,HOME_KEY_EVENT_CODE))
    def tearDown(self):
        if not self.__isScreenOn():
            print 'tearDown,screen is off, turning it on...'
            os.system('adb -s %s shell input keyevent %d'%(self.serial,POWER_KEY_EVENT_CODE))
            time.sleep(5)
        unlockPath = os.path.join(os.path.dirname(os.path.realpath(__file__)),'Unlocker.apk')
        os.system('adb -s %s install -r "%s"'%(self.serial, unlockPath))
        os.system('adb -s %s shell am start -n com.mediatek.unlocker/.MainActivity'%self.serial)
        time.sleep(5)
        os.system('adb -s %s shell input keyevent %d'%(self.serial,HOME_KEY_EVENT_CODE))
        os.system('adb -s %s shell am broadcast -a com.mediatek.mtklogger.ADB_CMD -e cmd_name start --ei cmd_target 6'%self.serial)
if __name__=='__main__':
    #usage:
    # python Sanity_Sleep_Wakeup_test.py -s SN
    #print("OK (1 tests)") for successes
    #print("FAILURES!!!") for failures
    print sys.argv
    parser = OptionParser()  
    parser.add_option("-s", "--serial", dest="serial",  
                  help="the serial number of the Phone",metavar="SERIAL")  
    (options,args) = parser.parse_args()
    if not options.serial:
        print parser.print_help()
        sys.exit(1)
    print 'run test on phone : ',options.serial
    print SleepWakeupTest(options.serial).runTest()

