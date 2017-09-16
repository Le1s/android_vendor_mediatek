package com.mediatek.recovery;

import java.util.ArrayList;

public class ActivityManagerExceptionParser extends AbstractExceptionParser {
    @Override
    public ArrayList<String> parseException(RuntimeException e) {
        // Get the root cause
        ParsedException rpe = ParsedException.getNewInstance(e, true);
        ArrayList<String> retList = new ArrayList<String>();
        setLastError(PARSER_EXCEPTION_MISMATCH);
        if (rpe.mThrowMethodName.equals("nativeOpen")
                && rpe.mExceptionClassName
                        .equals("android.database.sqlite.SQLiteCantOpenDatabaseException")
                && rpe.mThrowClassName
                        .equals("android.database.sqlite.SQLiteConnection")) {
            retList.add("/data/data/com.android.providers.settings/databases/settings.db");
            setLastError(PARSER_EXCEPTION_MATCH);
        }
        return retList;
    }
}
