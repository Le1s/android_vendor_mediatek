package com.mediatek.recovery;

import java.util.ArrayList;

public class UserManagerExceptionParser extends AbstractExceptionParser {

    @Override
    public ArrayList<String> parseException(RuntimeException e) {
        // Get the surface exception cause
        AbstractExceptionParser.ParsedException pe = AbstractExceptionParser.ParsedException.getNewInstance(e, false);
        // Get the root cause
        AbstractExceptionParser.ParsedException rpe = AbstractExceptionParser.ParsedException.getNewInstance(e, true);
        ArrayList<String> retList = new ArrayList<String>();
        setLastError(PARSER_EXCEPTION_MISMATCH);
        if (rpe.mThrowMethodName.equals("getApplicationInfo")
                && rpe.mExceptionClassName
                        .equals("android.content.pm.PackageManager$NameNotFoundException")
                && pe.mThrowClassName
                        .equals("com.android.server.am.ActivityManagerService")) {
            retList.add("/data/system/users/0.xml");
            setLastError(PARSER_EXCEPTION_MATCH);
        }
        return retList;
    }
}
