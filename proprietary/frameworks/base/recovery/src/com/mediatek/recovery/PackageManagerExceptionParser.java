package com.mediatek.recovery;

import java.util.ArrayList;

public class PackageManagerExceptionParser extends AbstractExceptionParser {
    @Override
    public ArrayList<String> parseException(RuntimeException e) {
        // Get the surface exception cause
        ParsedException pe = ParsedException.getNewInstance(e, false);
        ArrayList<String> retList = new ArrayList<String>();
        setLastError(PARSER_EXCEPTION_MISMATCH);
        if (pe.mThrowMethodName.equals("readLPw")
                && pe.mThrowClassName.equals("com.android.server.pm.Settings")) {
            retList.add("/data/system/packages.xml");
            setLastError(PARSER_EXCEPTION_MATCH);
        }
        return retList;
    }
}
