package com.ironsource.adapters.custom.iion;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.iion.api.AlxAdSDK;
import com.iion.api.AlxSdkInitCallback;
import com.ironsource.mediationsdk.adunit.adapter.BaseAdapter;
import com.ironsource.mediationsdk.adunit.adapter.listener.NetworkInitializationListener;
import com.ironsource.mediationsdk.adunit.adapter.utility.AdData;
import com.ironsource.mediationsdk.adunit.adapter.utility.AdapterErrors;

import java.util.Map;

public class IionCustomAdapter extends BaseAdapter {

    private static final String TAG = "IionCustomAdapter";
    private String unitid = "";
    private String appid = "";
    private String sid = "";
    private String token = "";
    private Boolean isdebug = false;
    private Map<String, Object> serverExtras;
    private NetworkInitializationListener mNetworkInitializationListener;

    @Override
    public void init(AdData adData, Context context, NetworkInitializationListener networkInitializationListener) {
        Log.d(TAG, "alx-ironsource-adapter-version:" + IionMetaInf.ADAPTER_VERSION);
        Log.i(TAG, "IION SDK Init");

        try {
            mNetworkInitializationListener = networkInitializationListener;
            if (parseServer(adData)) {
                Log.i(TAG, "alx ver:" + AlxAdSDK.getNetWorkVersion() + " alx token: " + token + " alx appid: " + appid + " alx sid: " + sid);

                AlxAdSDK.setDebug(isdebug);
                AlxAdSDK.init(context, token, sid, appid, new AlxSdkInitCallback() {
                    @Override
                    public void onInit(boolean isOk, String msg) {
                        if (isOk) {
                            mNetworkInitializationListener.onInitSuccess();
                        } else {
                            mNetworkInitializationListener.onInitFailed(AdapterErrors.ADAPTER_ERROR_MISSING_PARAMS, "AlxSdk Init Failed");
                        }
                    }
                });
                // set GDPRSubject to GDPR Flag: Please pass a Boolean value to indicate if the user is subject to GDPR regulations or not.
                // Your app should make its own determination as to whether GDPR is applicable to the user or not.
                SharedPreferences Preferences = PreferenceManager.getDefaultSharedPreferences(context);
                String tcString = Preferences.getString("IABTCF_TCString", "");
                int gdprApplies = Preferences.getInt("IABTCF_gdprApplies", 0);
                Log.d(TAG, "alx-ironSource-init:tcString= " + tcString + " gdprApplies: " + gdprApplies);
                if (gdprApplies != 0) {
                    AlxAdSDK.setSubjectToGDPR(true);
                } else {
                    AlxAdSDK.setSubjectToGDPR(false);
                }
                if (!tcString.isEmpty()) {
                    AlxAdSDK.setUserConsent(tcString);
                } else {
                    // Set GDPR Consent value
                    String strGDPRConsent = "0";
                    AlxAdSDK.setUserConsent(strGDPRConsent);
                }
//                // set COPPA
//                AlxAdSDK.setBelowConsentAge(true);
//                // set CCPA
//                AlxAdSDK.subjectToUSPrivacy("1YYY");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private boolean parseServer(AdData adData) {
        try {
            serverExtras = adData.getConfiguration();
            if (serverExtras.containsKey("appid")) {
                appid = (String) serverExtras.get("appid");
            }
            if (serverExtras.containsKey("sid")) {
                sid = (String) serverExtras.get("sid");
            }
            if (serverExtras.containsKey("token")) {
                token = (String) serverExtras.get("token");
            }
            if (serverExtras.containsKey("unitid")) {
                unitid = (String) serverExtras.get("unitid");
            }

            if (serverExtras.containsKey("isdebug")) {
                String debug = serverExtras.get("isdebug").toString();
                Log.e(TAG, "alx debug mode:" + debug);
                if (TextUtils.equals(debug,"true")) {
                    isdebug = true;
                } else {
                    isdebug = false;
                }
            } else {
                Log.e(TAG, "alx debug mode: false");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(unitid) || TextUtils.isEmpty(token) || TextUtils.isEmpty(sid) || TextUtils.isEmpty(appid)) {
            Log.i(TAG, "alx unitid | token | sid | appid is empty");
            if (mNetworkInitializationListener != null) {
                mNetworkInitializationListener.onInitFailed(AdapterErrors.ADAPTER_ERROR_MISSING_PARAMS, "alx unitid | token | sid | appid is empty");
            }
            return false;
        }
        return true;
    }

    @Override
    public String getNetworkSDKVersion() {
        return AlxAdSDK.getNetWorkVersion();
    }

    @Override
    public String getAdapterVersion() {
        return IionMetaInf.ADAPTER_VERSION;
    }
}

