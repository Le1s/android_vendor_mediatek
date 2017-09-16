'''
Author: MTK54039
Date: Mon Jul 3, 2013
Function: Sanity autotest, test : test volume key up/down, menu key, back key, home key
'''
import sys
import os
import time
import re
from subprocess import Popen
from subprocess import PIPE
from optparse import OptionParser  

'''
#Test step:
    press the related key. and check if it have take effect.
'''

KEYCODE_MENU = 82
KEYCODE_HOME = 3
KEYCODE_BACK = 4
KEYCODE_VOLUME_UP = 24
KEYCODE_VOLUME_DOWN = 25
KEYCODE_CAMERA = 27
STREAM_RING = 'STREAM_RING'
class SanityKPDTest:
    def __init__(self,serial='0123456789ABCDEF'):
        self.serial = serial;
        self.num_pattern = re.compile(r'\d+')
    def runTest(self):
        self.setUp();
        if self.__testVolumeKey() and self.__testOtherKeys():
            print "OK (5 tests)"# for successes
        else:
            print("FAILURES!!!")# for failures
        self.tearDown();
    '''
    Volume ====== 1
    - STREAM_RING:
       Mute count: 0
       Current: 40000000: 8, 2: 1,

    Volume ====== 3
    - STREAM_RING:
       Mute count: 0
       Current: 40000000: 8, 2: 3,
    '''
    def __getRingVolume(self):
        is_ring_section = False
        lines = Popen('adb -s %s shell dumpsys audio'%(self.serial),stdout=PIPE,shell=True).stdout.readlines()
        for line in lines:
            if line.find(STREAM_RING) != -1:
                is_ring_section = True
            if is_ring_section and line.find('Current') != -1:
                info = self.num_pattern.findall(line)# info = ['40000000', '8', '2', '3']
                if len(info) == 4:
                    volume = int(info[3])
                    print 'current volume is : ',volume
                    return volume
        return -1
    def __testVolumeKey(self):
        volume = self.__getRingVolume()
        if volume < 1 :#if volume is 0, volume_up first
            self.__sendKey(KEYCODE_VOLUME_UP)
            time.sleep(1)
            volume = self.__getRingVolume()
        print 'testing volume key down...'
        self.__sendKey(KEYCODE_VOLUME_DOWN)
        self.__sendKey(KEYCODE_VOLUME_DOWN)
        time.sleep(5)
        passed = True
        if self.__getRingVolume() < volume:
            print 'volume key down OK !'
        else:
            print("FAILURES!!!")# for failures
            passed = False
        volume = self.__getRingVolume()
        print 'testing volume key up...'
        self.__sendKey(KEYCODE_VOLUME_UP)
        self.__sendKey(KEYCODE_VOLUME_UP)
        time.sleep(5)
        if self.__getRingVolume() > volume:
            print 'volume key up OK !'
        else:
            print("FAILURES!!!")# for failures
            passed = False
        return passed

    def __testOtherKeys(self):
        passed = True
        self.__sendKey(KEYCODE_HOME)
        time.sleep(5)

        print 'testing menu key...'
        self.__sendKey(KEYCODE_MENU)
        time.sleep(5)
        if not self.__isShowingMenu():
            print("FAILURES!!!")# for failures
            passed = False
        else:
            print("Test menu key OK !")

        print 'testing back key...'
        self.__sendKey(KEYCODE_BACK)
        time.sleep(5)
        if not self.__isShowingMenu():
            print("FAILURES!!!")# for failures
            passed = False
        else:
            print("Test back key OK !")

        print 'testing home key...'
        self.__sendKey(KEYCODE_HOME)
        time.sleep(5)
        if not self.__isShowingMenu():
            print("FAILURES!!!")# for failures
            passed = False
        else:
            print("Test home key OK !")

        return passed
    def __isShowingMenu(self):
        lines = Popen('adb -s %s shell dumpsys window windows'%(self.serial),stdout=PIPE,shell=True).stdout.readlines()
        for line in lines:
            if line.find('mCurrentFocus') != -1 and line.find('AtchDlg:com.android.launcher') != -1:
                return True
        return True
    def __sendKey(self,key):
        os.system('adb -s %s shell input keyevent %d'%(self.serial,key))
    def setUp(self):
        self.__sendKey(KEYCODE_HOME)
    def tearDown(self):
        self.__sendKey(KEYCODE_HOME)

if __name__=='__main__':
    #usage:
    # python Sanity_KPD_test.py -s SN
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
    print SanityKPDTest(options.serial).runTest()

