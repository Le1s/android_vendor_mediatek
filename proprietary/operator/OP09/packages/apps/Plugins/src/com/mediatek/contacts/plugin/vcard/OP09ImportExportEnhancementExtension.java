package com.mediatek.contacts.plugin.vcard;

import android.accounts.Account;
import android.content.Context;

import com.mediatek.contacts.ext.DefaultImportExportExtension;
import com.android.vcard.VCardComposer;
import com.android.vcard.VCardEntryConstructor;

public class OP09ImportExportEnhancementExtension extends DefaultImportExportExtension {
    private static final String TAG = "OP09ImportExportEnhancementExtension";

    private Context mPluginContext;

    public OP09ImportExportEnhancementExtension(Context context) {
        mPluginContext = context;
    }

    @Override
    public VCardEntryConstructor getVCardEntryConstructorExt(int estimatedVCardType,
            Account account,
            String estimatedCharset) {

        return new OP09VCardEntryConstructor(estimatedVCardType, account, estimatedCharset);
    }

    @Override
    public VCardComposer getVCardComposerExt(Context context, int vcardType,
            boolean careHandlerErrors) {

        return new OP09VCardComposer(context, vcardType, careHandlerErrors);
    }

}
