'''
Author: MTK54039
Date: Mon Jul 1, 2013
Function: Sanity autotest, test : Storage card can be recognized.
Version:1.0
'''
import sys
import os
import time
from subprocess import Popen
from subprocess import PIPE
from optparse import OptionParser  

'''
#Test step:
    1. generate a text using current time.
    2. echo the text to sdcard1 & sdcard2.
    3. check if the text file exists on sdcard1 & sdcard2.
'''

HOME_KEY_EVENT_CODE = 3

class SDRecognizeTest:
    def __init__(self,serial='0123456789ABCDEF'):
        self.serial = serial;
        self.hasSDCard = self.__isSDCardInserted()
    def __isSDCardInserted(self):
        lines = Popen('adb -s %s shell vdc volume list'%self.serial,stdout=PIPE,shell=True).stdout.readlines()
        for line in lines:
            #110 0 sdcard2 /storage/sdcard1 4
            if line.find('sdcard1') != -1 and line.find('4') != -1:
                return True
        return False
    def __genFileAndPush(self):
        self.timeStamp =  str(time.time())
        cmd = 'adb -s %s shell \"echo \"%s\" > %s\"'%(self.serial,self.timeStamp,'/sdcard/'+self.timeStamp)
        print 'echo cmd --- ',cmd
        os.system(cmd)
        if self.hasSDCard:
            cmd = 'adb -s %s shell \"echo \"%s\" > %s\"'%(self.serial,self.timeStamp,'/storage/sdcard1/'+self.timeStamp)
            print 'echo cmd --- ',cmd
            os.system(cmd)
    def __checkFileOnStorage(self,path='/sdcard/'):
        lines = Popen('adb -s %s shell ls %s'%(self.serial,path),stdout=PIPE,shell=True).stdout.readlines()
        for line in lines:
            if line.find(self.timeStamp) != -1:
                return True
        return False
    def runTest(self):
        self.setUp();
        self.__genFileAndPush();
        failed = False
        if not self.__checkFileOnStorage():
            print("sdcard0 not recognizeed")
            print("AssertFail")
            print("FAILURES!!!")
            failed = True
        else:
            print("sdcard0 OK!")
        if self.hasSDCard:
            if not self.__checkFileOnStorage('/storage/sdcard1/'):
                print("sdcard1 not recognizeed")
                print("AssertFail")
                print("FAILURES!!!")
                failed = True
            else:
                print("sdcard1 OK!")
        self.tearDown();
        if not failed:
            print("OK (1 tests)")
    def setUp(self):
        os.system('adb -s %s shell input keyevent %d'%(self.serial,HOME_KEY_EVENT_CODE))
    def tearDown(self):
        os.system('adb -s %s shell rm -rf /sdcard/%s'%(self.serial,self.timeStamp))
        if self.hasSDCard:
            os.system('adb -s %s shell rm -rf /storage/sdcard1/%s'%(self.serial,self.timeStamp))
        os.system('adb -s %s shell input keyevent %d'%(self.serial,HOME_KEY_EVENT_CODE))

if __name__=='__main__':
    #usage:
    # python SDRecognizeTest.py -s SN
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
    SDRecognizeTest(options.serial).runTest()

