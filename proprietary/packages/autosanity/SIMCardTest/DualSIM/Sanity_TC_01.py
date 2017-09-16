'''
Author: MTK54039
Date: Jun 25, 2013
Function: Sanity autotest, test :phone can bootup with no exception and SIM card can be recognized.
Version:1.0
'''
import subprocess
from optparse import OptionParser  
import sys
import time
import os
import re
class Sanity_TC_01:
    iccid_pattern = re.compile(r'\[[a-z0-9]{20}\]')
    aee_exp_pattern = re.compile(r'db\.\d{2}')
    def __init__(self,serial='0123456789ABCDEF'):
        self.serial = serial
    def check_home_screen(self):
        if not self.props_dic.has_key('[dev.bootcomplete]'):
            print 'boot not complete yet'
            return False
        bootcomplete = self.props_dic['[dev.bootcomplete]']
        print 'bootcomplete prop : ',bootcomplete
        if bootcomplete and bootcomplete=='[1]':
            print 'boot complete'
            return True
        print 'boot not complete yet'
        return False
    def check_sim(self):
        if not self.props_dic.has_key('[ril.iccid.sim1]') or not self.props_dic.has_key('[ril.iccid.sim2]'):
            print 'sim card not ready yet'
            return False
        iccid1 = self.props_dic['[ril.iccid.sim1]']
        iccid2 = self.props_dic['[ril.iccid.sim2]']
        print 'iccid1 & 2  prop :_',iccid1,"_",iccid2,"_";
        if iccid1 and iccid2:
            if self.iccid_pattern.search(iccid1) and self.iccid_pattern.search(iccid2):
                print 'sim card ready'
                return True
        print 'sim card not ready yet'
        return False
    def get_exception_count(self):
        lines = subprocess.Popen('adb -s %s shell ls /sdcard/mtklog/aee_exp/'%self.serial,stdout=subprocess.PIPE,shell=True).stdout.readlines()
        count = 0
        for line in lines:
            if self.aee_exp_pattern.search(line):
                count += 1
        print 'current exception count = ', count
        return count
    def run_sanity_tc_01(self):
        print 'Sanity TC 01 start ...'
        self.exception_count = self.get_exception_count()
        print 'Reboot phone ... '
        os.system('adb -s %s reboot'%self.serial)
        #retry times = 6, each time wait 30s ==> total time == 3 mins
        retry = 0
        (options,args) = parser.parse_args()
        while retry < 6:
            retry +=1
            #modified by mtk80721+
            if options.checksim == 'n':
                print 'try to check bootcomplete, retry time : ', retry
            else:
                print 'try to check bootcomplete and sim card , retry time : ', retry
            #modified by mtk80721-    
            time.sleep(30)
            props_pipe = subprocess.Popen('adb -s %s shell getprop'%self.serial,stdout=subprocess.PIPE,shell=True)
            props = props_pipe.stdout.readlines()
            self.props_dic={}
            for prop in props:
                spans = prop.split(': ')
                if len(spans)>1:
                    self.props_dic[spans[0].strip()]=spans[1].strip()
            #modified by mtk80721+        
            #if self.check_home_screen() and self.check_sim():
            if options.checksim == 'n':
                print "only check home screen"
                if self.check_home_screen():
                    if options.checkaee == 'y':
                        new_count = self.get_exception_count()
                        if new_count > self.exception_count:
                            #new exception happend during reboot (bootup)
                            return "FAILURES!!!"
                    else:
                        print "skip aee count checking"        
                    return "OK (1 tests)"
            else:
                print "check home screen&sim card"
                if self.check_home_screen() and self.check_sim():
                    if options.checkaee == 'y':
                        new_count = self.get_exception_count()
                        if new_count > self.exception_count:
                            #new exception happend during reboot (bootup)
                            return "FAILURES!!!"
                    else:
                        print "skip aee count checking"
                    return "OK (1 tests)"
            #modified by mtk80721-
        return "FAILURES!!!"

if __name__=='__main__':
    #usage:
    # python Sanity_TC_01.py -s SN
#print("OK (1 tests)")
#print("FAILURES!!!")
    print sys.argv
    parser = OptionParser()  
    parser.add_option("-s", "--serial", dest="serial",  
                  help="the serial number of the Phone",metavar="SERIAL")  
    #add by mtk80721+           
    parser.add_option("-c", "--checksim", action='store', dest="checksim",
                  help="check sim or not(y/n)",metavar="CHECKSIM")  
    #add by mtk80721-
    parser.add_option("-e", "--checkaee", action='store', dest="checkaee",
                  help="check AEE or not(y/n)",metavar="CHECKAEE")  
    (options,args) = parser.parse_args()
    '''
    print options.serial
    print options.checksim
    print options.checkaee
    if options.checksim == 'n':
        print "not check sim card"
    else:
        print "default flow"
    '''
    if not options.serial:
        print parser.print_help()
        sys.exit(1)
    print 'run test on phone : ',options.serial
    print Sanity_TC_01(options.serial).run_sanity_tc_01()

