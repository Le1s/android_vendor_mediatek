'''
Author: MTK54039
Date: Mon Jul 4, 2013
Function: Sanity autotest, test : test kpd working.
'''
import sys
import os
from subprocess import Popen
from subprocess import PIPE
from optparse import OptionParser  

KEYCODE_HOME = 3

'''
#Test step:
    check the status of :
    /sys/module/tpd_setting/parameters/tpd_load_status
    value == 1 --> pass
    else --> fail
'''
class TouchPanelTest:
    def __init__(self,serial='0123456789ABCDEF'):
        self.serial = serial;
    def runTest(self):
        self.setUp();
        if self.__get_tpd_load_status():
            print "OK (1 tests)"# for successes
        else:
            print("FAILURES!!!")# for failures
        self.tearDown();
    def __get_tpd_load_status(self):
        lines = Popen('adb -s %s shell cat /sys/module/tpd_setting/parameters/tpd_load_status'%self.serial, stdout = PIPE, shell = True).stdout.readlines()
        for line in lines:
            if line.find('1') != -1:
                return True
        return False
    def setUp(self):
        self.__sendKey(KEYCODE_HOME)
    def tearDown(self):
        self.__sendKey(KEYCODE_HOME)
    def __sendKey(self,key):
        os.system('adb -s %s shell input keyevent %d'%(self.serial,key))

if __name__=='__main__':
    #usage:
    # python TouchPanelTest.py -s SN
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
    print TouchPanelTest(options.serial).runTest()

