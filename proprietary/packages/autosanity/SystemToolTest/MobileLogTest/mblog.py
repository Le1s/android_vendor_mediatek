CMD = 'adb -s %s shell am broadcast -a com.mediatek.mtklogger.ADB_CMD -e cmd_name   force_modem_assert --ei cmd_target 2'
CMD_START_MD = 'adb -s %s shell am broadcast -a com.mediatek.mtklogger.ADB_CMD -e cmd_name start --ei cmd_target 1'
CMD_STOP_MD = 'adb -s %s shell am broadcast -a com.mediatek.mtklogger.ADB_CMD -e cmd_name stop --ei cmd_target 1'

def mdlog_result(testSN):
    import subprocess
    count = 0
    strCmd=["adb","-s",testSN, "shell","getprop","debug.MB.running"]
    p = subprocess.Popen(strCmd,
                                         stdout = subprocess.PIPE, 
                                         shell = False)  
    logs = p.stdout.readlines()
    for strLine in logs:
        print strLine
        if strLine.find("1") != -1:
            return True
        else:
            return False

def getSN(cmdStr):
    import getopt, sys
    
    ''' get serial number of the target device'''
    serialNO = ""
    options, remainder = getopt.getopt(cmdStr[1:], 's:')
    for opt, arg in options:
        if opt in ('-s'):
            serialNO = arg
        return serialNO
    
if __name__ == "__main__": 
    import os
    import time
   # import gl
    import sys
    
    testSN = getSN(sys.argv)
    print 'testSN:',testSN
    #create a file to store log    
#    if not os.path.isdir(gl.log_path):
#        os.makedirs(gl.log_path)
#    logFile = open(gl.log_path+'\\log.txt','w')

#    print >>logFile, 'sanity test mdlog start'
#    print >>logFile, 'test SN: ',testSN
    print 'sanity test mdlog start'
    print 'test SN: ',testSN
    # stop modem log
    cmd = (CMD_STOP_MD)%(testSN)
    print cmd
    os.system(cmd)
    time.sleep(2)
    test_result = mdlog_result(testSN)
    time.sleep(2)
    if test_result == True:
#        print >>logFile,("FAILURES!!!") 
        print ("FAILURES!!!") 
# start modem log
    cmd = (CMD_START_MD)%(testSN)
    print cmd
    os.system(cmd)
    time.sleep(2)
    test_result = mdlog_result(testSN)
    time.sleep(2)
    if test_result == True:
#        print >>logFile,("OK (1 tests)")
        print ("OK (1 tests)")
    else:
#        print >>logFile,("FAILURES!!!")   
        print ("FAILURES!!!") 
 
