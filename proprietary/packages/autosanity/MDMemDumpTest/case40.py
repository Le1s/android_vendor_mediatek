'''
Test Name: TC_040
Test Target: Modem Logger

Version: 1.000
'''

import getopt
import os
import subprocess
import sys
import threading 
import time

    
CMD = 'adb -s %s shell am broadcast -a com.mediatek.mtklogger.ADB_CMD -e cmd_name   force_modem_assert --ei cmd_target 2'
CMD_START_MD = 'adb -s %s shell am broadcast -a com.mediatek.mtklogger.ADB_CMD -e cmd_name start --ei cmd_target 2'
CMD_BACK = 'adb -s %s shell input keyevent 4'
CMD_CLEAR_LOG = 'adb -s %s logcat -c'
CMD_OFF_TAGLAG = 'adb -s %s shell am broadcast -a com.mediatek.mtklogger.ADB_CMD -e cmd_name switch_taglog --ei cmd_target  0'
CMD_SWITCH_LOG_PATH = 'adb -s %s shell setprop persist.mtklog.log2sd.path external_sd'

find_ee = False
time_out = False
test_pass = False
EE_time_out = False
pipe = None

exception_catch = 'Externel (EE)' 
file_created = 'log files have been created'
folder_created = 'is created as log folder'

log_path = 'E:\Temp\log'

#end from gl.py

def analyze_log_thread(pre_count,testSN):
    # clear old log
    cmd = (CMD_CLEAR_LOG)%(testSN)
    os.system(cmd)
    # start a thread to monitor log
    strCmd=["adb", "-s", testSN, "logcat","-v","time"]
    
    global p
    p = subprocess.Popen(strCmd,
                                         stdout = subprocess.PIPE, 
                                         shell = False)   
    t = threading.Thread(target=catpture_ee, args = (pre_count,p.stdout,testSN))     
    t.start() 
    try:
        pipe.terminate()
    except:
        pass
    
def catpture_ee(pre_count,a_log,testSN):
    strLine = ''
    blank = ''
    while EE_time_out == False:
        strLine = a_log.readline()  
        # print strLine 
        if cmp(strLine,blank)==1:
            strLine = strLine.rstrip()
            if  strLine.find(exception_catch) != -1:
                print 'find exception'
                global find_ee
                find_ee = True
                break
    
    print 'kill logcat pipe'
    try:
        global p
        p.terminate()
    except:
        pass        
                 
    print " waiting find EE exception : " ,find_ee 
    while time_out == False:
        cmd = (CMD_BACK)%(testSN)
        os.system(cmd)
        time.sleep(5)
        # send back key        
        count = mdlog_count(testSN)
        if count > pre_count:
	    global test_pass
            test_pass = True
            break
    
def mdlog_count(testSN):
    import subprocess
    device_mdlog_folders = ["/sdcard/mtklog/mdlog/",
                            "/mnt/sdcard/mtklog/mdlog/",
                            "/mnt/sdcard2/mtklog/mdlog/",
                            "/storage/sdcard0/mtklog/mdlog/",
                            "/storage/sdcard1/mtklog/mdlog/",
                            "/storage/sdcard0/mtklog/extmdlog/",
                            "/storage/sdcard1/mtklog/extmdlog/",
                            "/storage/sdcard0/mtklog/dualmdlog/",
                            "/storage/sdcard1/mtklog/dualmdlog/",
                            "/storage/sdcard0/mtklog/mdlog1/",
                            "/storage/sdcard0/mtklog/mdlog2/",
                            "/storage/sdcard0/mtklog/mdlog3/",
                            "/storage/sdcard0/mtklog/mdlog4/",
                            "/storage/sdcard0/mtklog/mdlog5/",
                            "/storage/sdcard0/mtklog/mdlog6/",
                            "/storage/sdcard0/mtklog/mdlog7/",
                            "/storage/sdcard0/mtklog/mdlog8/",
                            "/storage/sdcard1/mtklog/mdlog1/",
                            "/storage/sdcard1/mtklog/mdlog2/",
                            "/storage/sdcard1/mtklog/mdlog3/",
                            "/storage/sdcard1/mtklog/mdlog4/",
                            "/storage/sdcard1/mtklog/mdlog5/",
                            "/storage/sdcard1/mtklog/mdlog6/",
                            "/storage/sdcard1/mtklog/mdlog7/",
                            "/storage/sdcard1/mtklog/mdlog8/",]
    count = 0
    for md_log_folder_path in device_mdlog_folders:
        str_cmd = ["adb", "-s", testSN, "shell", "ls", str(md_log_folder_path)]
        p = subprocess.Popen(str_cmd, stdout=subprocess.PIPE, shell=False)
        p.wait()
        logs = p.stdout.readlines()
        for str_line in logs:
            if not str_line.find("_EE_") == -1:
                count += 1
    print "mdlog_count: ",count
    return count

def aee_count(testSN):
    count = 0
    device_aee_folders = ["/sdcard/mtklog/aee_exp/",
                         "/mnt/sdcard/aee_exp/",
                         "/mnt/sdcard2/aee_exp/",
                         "/storage/sdcard0/aee_exp/",
                         "/storage/sdcard1/aee_exp/",
                         "shell","ls","/data/aee_exp/"
                         ]
    for aee_folder_path in device_aee_folders:
        str_cmd = ["adb", "-s", testSN, "shell", "ls", str(aee_folder_path)]
        p = subprocess.Popen(str_cmd, stdout=subprocess.PIPE, shell=False)
        logs = p.stdout.readlines()
        for str_line in logs:
            if not str_line.find("db") == -1:
                count += 1 
    print "aee_count: ", count
    return count


def getSN(cmdStr):
    ''' get serial number of the target device'''
    serialNO = ""
    options, remainder = getopt.getopt(cmdStr[1:], 's:')
    for opt, arg in options:
        if opt in ('-s'):
            serialNO = arg
        return serialNO
    
if __name__ == "__main__": 
    print("TC_040 start time: %s" % str(time.strftime("%Y/%m/%d %H:%M:%S")))
    testSN = getSN(sys.argv)
    print 'testSN:',testSN
  # off taglog function for 82LTE gsm07mudx NE issue
    cmd = (CMD_OFF_TAGLAG)%(testSN)
    print cmd
    os.system(cmd)
 #swith log path to external 
  #  cmd = (CMD_SWITCH_LOG_PATH)%(testSN)
  #  print cmd
  #  os.system(cmd)
  #  time.sleep(2)
       
    # start modem log
    cmd = (CMD_START_MD)%(testSN)
    print cmd
    os.system(cmd)
    time.sleep(2)
    # get the mdlog file counts before test
    count = mdlog_count(testSN)
    db_count = aee_count(testSN)
    
    # start to monitor logcat
    time.sleep(4)
    analyze_log_thread(count,testSN)
    # send adb force assert command, start modem logger may cost some time, timeout 10s
    time.sleep(4)
    cmd = (CMD)%(testSN)
    print cmd 
    os.system(cmd)
    
    # wait until find EE in log. Time out after 300s.
    index = 0
    while(test_pass == False):
        time.sleep(1)
        index += 1
        if index > 120:
            EE_time_out = True
        if index>300:
            time_out = True
            break
    print " waiting find EE in log time: " ,index
    # wait until aee db done ,time out after 120 seconds
    if test_pass == True:
        index = 0
        while True:
            time.sleep(1)
            db_count2 = aee_count(testSN)
            if db_count2 > db_count:
                break
            index += 1
            if index > 120:
                break
        print " waiting aee db done time: " ,index     
     
        print ("OK (1 tests)")
    else:
         if (find_ee == False):
             print 'AssertFail'
                       
         print ("FAILURES!!!") 
   
    print("TC_040 end time: %s" % str(time.strftime("%Y/%m/%d %H:%M:%S")))
        
