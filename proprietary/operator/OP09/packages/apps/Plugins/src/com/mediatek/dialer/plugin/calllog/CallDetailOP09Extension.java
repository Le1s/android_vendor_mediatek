package com.mediatek.dialer.plugin.calllog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemProperties;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.telephony.PhoneNumberUtils;
import android.text.format.DateUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.dialer.PhoneCallDetails;
import com.android.dialer.calllog.ContactInfo;
import com.android.dialer.calllog.CallLogQuery;
import com.android.internal.telephony.CallerInfo;
import com.android.internal.telephony.PhoneConstants;
import com.mediatek.dialer.ext.DefaultCallDetailExtension;
import com.mediatek.dialer.plugin.OP09DialerPluginUtil;
import com.mediatek.op09.plugin.R;
import com.mediatek.phone.SIMInfoWrapper;
import com.mediatek.telephony.SimInfoManager.SimInfoRecord;
import com.mediatek.telephony.TelephonyManagerEx;


public class CallDetailOP09Extension extends DefaultCallDetailExtension
                                     implements CallLogQueryHandler.Listener {

    private static final String TAG = "CallDetailOP09Extension";

    private static final String EXTRA_CALL_LOG_IDS = "EXTRA_CALL_LOG_IDS";

    private Activity mActivity;
    private Context mPluginContext;
    private ContactInfo mFirstCallContactInfo;
    private CallLogQueryHandler mCallLogQueryHandler;
    private Context mHostContext;
    private OP09DialerPluginUtil mOP09DialerPlugin;
    private PhoneCallDetails[] mPhoneCallDetails;
    private LayoutInflater mLayoutInflater;

    // sim id of sip call in the call log database
    public static final int CALL_TYPE_SIP = -2;
    private static final int VIEW_TYPE_ALL_CALL_HEADER = 2;

    public void initForCallDetailHistory(Context context, PhoneCallDetails[] phoneCallDetails) {
        mHostContext = context;
        mPhoneCallDetails = phoneCallDetails;
        mOP09DialerPlugin = new OP09DialerPluginUtil(context);
        mPluginContext = mOP09DialerPlugin.getPluginContext();
        mLayoutInflater = (LayoutInflater) mPluginContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getViewTypeCountForCallDetailHistory(int currentViewTypeCount) {
        return currentViewTypeCount + 1;
    }

    public View getViewPostForCallDetailHistory(int position, View convertView, ViewGroup parent) {
        if (0 == position) {
            return convertView;
        }
        if (null == mPhoneCallDetails[position - 1]) {
            return convertView;
        }
        if (0 != mOP09DialerPlugin.getTimezoneRawOffset()) {
            Resources resource = mHostContext.getResources();
            String packageName = mHostContext.getPackageName();
            PhoneCallDetails phoneCallDetails = (PhoneCallDetails) mPhoneCallDetails[position - 1];
            TextView dateView =
                (TextView) convertView.findViewById(resource.getIdentifier("date", "id", packageName));
            if (null != dateView) {
                CharSequence dateValue = DateUtils.formatDateRange(mHostContext,
                        phoneCallDetails.date + mOP09DialerPlugin.getTimezoneOffset(phoneCallDetails.date),
                        phoneCallDetails.date + mOP09DialerPlugin.getTimezoneOffset(phoneCallDetails.date),
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE |
                        DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_YEAR);
                dateView.setText(dateValue);
            }
        }
        if (mPhoneCallDetails[position - 1] instanceof OP09PhoneCallDetails) {
            OP09PhoneCallDetails phoneCallDetails = (OP09PhoneCallDetails) mPhoneCallDetails[position - 1];
            if (OP09PhoneCallDetails.DISPLAY_TYPE_NEW_CALL == phoneCallDetails.displayType) {
                if (null != phoneCallDetails.contactUri) {
                    Resources resource = mHostContext.getResources();
                    String packageName = mHostContext.getPackageName();
                    TextView callTypeTextView =
                        (TextView) convertView.findViewById(resource.getIdentifier("call_type_text", "id", packageName));
                    if (null != callTypeTextView) {
                        callTypeTextView.setText(phoneCallDetails.number + " "
                                + Phone.getTypeLabel(resource, phoneCallDetails.numberType, phoneCallDetails.numberLabel));
                    }
                }
            } else if (OP09PhoneCallDetails.DISPLAY_TYPE_TEXT == phoneCallDetails.displayType) {
                convertView  = mLayoutInflater.inflate(R.layout.call_detail_history_all_calls_list_item, parent, false);
                TextView allCallsText = (TextView) convertView.findViewById(R.id.text);
                if (null != allCallsText) {
                    if (null == mPhoneCallDetails[position - 1].contactUri) {
                        allCallsText.setText(mPluginContext.getString(R.string.all_calls_with_number));
                    } else {
                        allCallsText.setText(mPluginContext.getString(R.string.all_calls_with_contact));
                    }
                }
            }
        }
        return convertView;
    }

    public void onCreate(Activity activity) {
        mActivity = activity;
        mCallLogQueryHandler = new CallLogQueryHandler(mActivity.getContentResolver(), this);
    }

    public void onDestroy() {
        mActivity = null;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(ContextMenu.NONE, R.id.menu_delete_all_calls_with_number, 15,
                mPluginContext.getString(R.string.delete_all_calls_with_number));
        menu.add(ContextMenu.NONE, R.id.menu_delete_all_calls_with_contact, 15,
                mPluginContext.getString(R.string.delete_all_calls_with_contact));
        return false;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        if (null == mFirstCallContactInfo) {
            menu.findItem(R.id.menu_delete_all_calls_with_number).setVisible(false);
            menu.findItem(R.id.menu_delete_all_calls_with_contact).setVisible(false);
            return false;
        }
        if (0 == mFirstCallContactInfo.contactId) {
            menu.findItem(R.id.menu_delete_all_calls_with_number).setVisible(true);
            menu.findItem(R.id.menu_delete_all_calls_with_contact).setVisible(false);
        } else {
            menu.findItem(R.id.menu_delete_all_calls_with_number).setVisible(false);
            menu.findItem(R.id.menu_delete_all_calls_with_contact).setVisible(true);
        }
        return false;
    }

    public boolean onMenuItemSelected(int featureId, MenuItem item) {

        switch (item.getItemId()) {

            case R.id.menu_delete_all_calls_with_number:
                showDeleteAllCallsWithNumberDialog(mFirstCallContactInfo.number);
                // !!! needs to run in anther thread,
                //mActivity.getContentResolver().delete(Calls.CONTENT_URI,
                //        Calls.NUMBER + " = '" + mFirstCallContactInfo.number + "'", null);
                return true;

            case R.id.menu_delete_all_calls_with_contact:
                showDeleteAllCallsWithContactDialog(mFirstCallContactInfo.rawContactId);
                // !!! needs to run in anther thread,
                //mActivity.getContentResolver().delete(Calls.CONTENT_URI,
                //        "calls." + Calls.RAW_CONTACT_ID + " = " + Integer.toString(),
                //        null);
                return true;

            default:
                break;
        }
        return false;
    }

    private void showDeleteAllCallsWithNumberDialog(final String number) {
        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity)
                   .setTitle(mPluginContext.getString(R.string.delete_all_calls_with_number_dialog_title))
                   .setIconAttribute(android.R.attr.alertDialogIcon)
                   .setMessage(mPluginContext.getString(R.string.delete_all_calls_with_number_dialog_message))
                   .setNegativeButton(android.R.string.cancel, null)
                   .setPositiveButton(android.R.string.ok,
                       new DialogInterface.OnClickListener() {
                           public void onClick(DialogInterface dialog, int which) {
                               mCallLogQueryHandler.deleteSpecifiedCalls(Calls.NUMBER + " = '" + number + "'");
                           }
                       });
        dialog = builder.create();
        dialog.show();
    }

    private void showDeleteAllCallsWithContactDialog(final int rawContactId) {
        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity)
                   .setTitle(mPluginContext.getString(R.string.delete_all_calls_with_contact_dialog_title))
                   .setIconAttribute(android.R.attr.alertDialogIcon)
                   .setMessage(mPluginContext.getString(R.string.delete_all_calls_with_contact_dialog_message))
                   .setNegativeButton(android.R.string.cancel, null)
                   .setPositiveButton(android.R.string.ok,
                       new DialogInterface.OnClickListener() {
                           public void onClick(DialogInterface dialog, int which) {
                               mCallLogQueryHandler.deleteSpecifiedCalls(
                                       "calls." + Calls.RAW_CONTACT_ID + " = " + Integer.toString(rawContactId));
                           }
                       });
        dialog = builder.create();
        dialog.show();
    }

    public PhoneCallDetails[] doInBackgroundForUpdateData(final Uri[] callUris, PhoneCallDetails[] phoneCallDetails) {
        /** M: Bug Fix for ALPS01448622 @{ */
        if (null == mActivity) {
            log("The mActivity is null");
            return null;
        }
        /** @} */

        Cursor firstCallCursor = mActivity.getContentResolver().query(callUris[0],
                CallLogQuery._PROJECTION, null, null, null);
        try {
            firstCallCursor.moveToFirst();
            mFirstCallContactInfo = ContactInfo.getContactInfofromCursor(firstCallCursor);
        } finally {
            if (null != firstCallCursor) {
                firstCallCursor.close();
            }
        }
        PhoneCallDetails firstDetails = getPhoneCallDetailsContactInfo(mFirstCallContactInfo,
                OP09PhoneCallDetails.DISPLAY_TYPE_DEFAULT_CALL);

        //log("raw contact id of first call log = " + Integer.toString(mFirstCallContactInfo.rawContactId));
        String where = (mFirstCallContactInfo.contactId > 0) ?
                "calls." + Calls.RAW_CONTACT_ID + " = " + Integer.toString(mFirstCallContactInfo.rawContactId) :
                Calls.NUMBER + " = '" + mFirstCallContactInfo.number + "' OR "
                    + Calls.NUMBER + " = '" + mFirstCallContactInfo.formattedNumber + "'";
        Cursor allCallsCursor = mActivity.getContentResolver().query(
                Uri.parse("content://call_log/callsjoindataview"),
                CallLogQuery._PROJECTION,
                where, null, Calls.DATE + " DESC");

        final int numCalls = callUris.length;
        log("numCall = " + numCalls + ", allCallsCursor.getCount = " + allCallsCursor.getCount());
        PhoneCallDetails[] resultPhoneCallDetails = new PhoneCallDetails[numCalls + allCallsCursor.getCount() + 1];
        //PhoneCallDetails[] resultPhoneCallDetails = new PhoneCallDetails[numCalls + 1 + 1];

        resultPhoneCallDetails[0] = firstDetails;
        int i = 1;
        while (i < numCalls) {
            resultPhoneCallDetails[i] = phoneCallDetails[i];
            ++i;
        }
        // "all calls" text list item
        resultPhoneCallDetails[i++] = new OP09PhoneCallDetails(firstDetails, OP09PhoneCallDetails.DISPLAY_TYPE_TEXT);
        // "all calls" list items
        try {
            if (allCallsCursor.getCount() > 0) {
                allCallsCursor.moveToFirst();
                resultPhoneCallDetails[i++] = getPhoneCallDetailsContactInfo(ContactInfo.getContactInfofromCursor(allCallsCursor),
                        OP09PhoneCallDetails.DISPLAY_TYPE_NEW_CALL);
                while (allCallsCursor.moveToNext()) {
                    resultPhoneCallDetails[i++] = getPhoneCallDetailsContactInfo(ContactInfo.getContactInfofromCursor(allCallsCursor),
                            OP09PhoneCallDetails.DISPLAY_TYPE_NEW_CALL);
                }
            }
        } finally {
            if (null != allCallsCursor) {
                allCallsCursor.close();
            }
        }
        return resultPhoneCallDetails;
    }

    public PhoneCallDetails getPhoneCallDetailsContactInfo(ContactInfo contactInfo, int displayType) {
        log("number = " + contactInfo.number);
        if (!canPlaceCallsTo(contactInfo.number)
                || PhoneNumberUtils.isVoiceMailNumber(contactInfo.number)
                || isEmergencyNumber(contactInfo.number, contactInfo.simId)) {
            contactInfo.formattedNumber = getDisplayNumber(contactInfo.number, null).toString();
            contactInfo.name = "";
            contactInfo.nNumberTypeId = 0;
            contactInfo.label = "";
            contactInfo.lookupUri = null;
        }
    int [] callTypes = new int[] {contactInfo.type};
        return new OP09PhoneCallDetails(contactInfo.number, contactInfo.formattedNumber,
                                        contactInfo.countryIso, contactInfo.geocode,
                                        callTypes, contactInfo.date,
                                        contactInfo.duration, contactInfo.name,
                                        contactInfo.nNumberTypeId, contactInfo.label,
                                        contactInfo.lookupUri, contactInfo.photoUri, contactInfo.simId,
                                        contactInfo.vtCall, 0, contactInfo.ipPrefix,
                                        displayType);
    }

    public void onCallsDeleted() {
        if (null != mActivity) {
            mActivity.finish();
        }
    }

    public boolean setSimInfo(int simId, TextView simIndicator) {
        if (CALL_TYPE_SIP == simId) {
            // The request is sip color
            if (null != simIndicator && simIndicator.getVisibility() == View.VISIBLE) {
                simIndicator.setBackgroundDrawable(
                            mPluginContext.getResources().getDrawable(R.drawable.dark_small_internet_call));
            }
        }
        if (null != simIndicator) {
            simIndicator.setText(" ");
        }
    return false;
    }

    /** Returns true if it is possible to place a call to the given number. */
    public boolean canPlaceCallsTo(CharSequence number) {
        return !(TextUtils.isEmpty(number)
                || number.equals(CallerInfo.UNKNOWN_NUMBER)
                || number.equals(CallerInfo.PRIVATE_NUMBER)
                || number.equals(CallerInfo.PAYPHONE_NUMBER));
    }

    /**
     * Returns the string to display for the given phone number.
     *
     * @param number the number to display
     * @param formattedNumber the formatted number if available, may be null
     */
    public CharSequence getDisplayNumber(CharSequence number, CharSequence formattedNumber) {
        if (TextUtils.isEmpty(number)) {
            return "";
        }
        if (number.equals(CallerInfo.UNKNOWN_NUMBER)) {
            return mPluginContext.getString(R.string.unknown);
        }
        if (number.equals(CallerInfo.PRIVATE_NUMBER)) {
            return mPluginContext.getString(R.string.private_num);
        }
        if (number.equals(CallerInfo.PAYPHONE_NUMBER)) {
            return mPluginContext.getString(R.string.payphone);
        }

        /** M:  delete @ { */
        /*if (isVoicemailNumber(number)) {
            return mResources.getString(R.string.voicemail);
        } */
        /** @ }*/
        if (TextUtils.isEmpty(formattedNumber)) {
            return number;
        } else {
            return formattedNumber;
        }
    }

    /** Returns true if the given number is a emergency number. */
    public boolean isEmergencyNumber(CharSequence number, int simId) {
        if (SystemProperties.getInt("ro.evdo_dt_support", 0) == 1) {
            if (0 == simId) {
                return isEmergencyNumber(number);
            }
            SimInfoRecord simInfo = SIMInfoWrapper.getDefault().getSimInfoById(simId);
            if (null == simInfo || -1 == simInfo.mSimSlotId) {
                return PhoneNumberUtils.isEmergencyNumberExt(number.toString(), PhoneConstants.PHONE_TYPE_CDMA)
                            || PhoneNumberUtils.isEmergencyNumberExt(number.toString(), PhoneConstants.PHONE_TYPE_GSM);
            }
            return PhoneNumberUtils.isEmergencyNumberExt(number.toString(),
                    TelephonyManagerEx.getDefault().getPhoneType(simInfo.mSimSlotId));
        } else {
            return PhoneNumberUtils.isEmergencyNumber(number.toString());
        }
    }

    public boolean isEmergencyNumber(CharSequence number) {
        if (SystemProperties.getInt("ro.evdo_dt_support", 0) == 1) {
            return PhoneNumberUtils.isEmergencyNumberExt(number.toString(), PhoneConstants.PHONE_TYPE_CDMA)
                        || PhoneNumberUtils.isEmergencyNumberExt(number.toString(), PhoneConstants.PHONE_TYPE_GSM);
        } else {
            return PhoneNumberUtils.isEmergencyNumber(number.toString());
        }
    }
    private static void log(String msg) {
        Log.d(TAG, msg);
    }
}
