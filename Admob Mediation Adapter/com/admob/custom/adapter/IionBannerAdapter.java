package com.admob.custom.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.iion.api.AlxAdSDK;
import com.iion.api.AlxBannerView;
import com.iion.api.AlxBannerViewAdListener;
import com.iion.api.AlxSdkInitCallback;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.mediation.Adapter;
import com.google.android.gms.ads.mediation.InitializationCompleteCallback;
import com.google.android.gms.ads.mediation.MediationAdLoadCallback;
import com.google.android.gms.ads.mediation.MediationBannerAd;
import com.google.android.gms.ads.mediation.MediationBannerAdCallback;
import com.google.android.gms.ads.mediation.MediationBannerAdConfiguration;
import com.google.android.gms.ads.mediation.MediationConfiguration;
import com.google.android.gms.ads.mediation.VersionInfo;

import org.json.JSONObject;

import java.util.List;

/**
 * Google Mobile ads IION Banner Adapter
 */
public class IionBannerAdapter extends Adapter implements MediationBannerAd {

    private static final String TAG = "IionBannerAdapter";
    private String unitid = "";
    private String appid = "";
    private String sid = "";
    private String token = "";
    private Boolean isdebug = false;

    private MediationAdLoadCallback<MediationBannerAd, MediationBannerAdCallback> mMediationLoadCallback;
    private MediationBannerAdCallback mMediationEventCallback;

    AlxBannerView mBannerView;

    @Override
    public void initialize(Context context, InitializationCompleteCallback initializationCompleteCallback, List<MediationConfiguration> list) {
        Log.d(TAG, "alx-admob-adapter: initialize");
        if (context == null) {
            initializationCompleteCallback.onInitializationFailed(
                    "Initialization Failed: Context is null.");
            return;
        }
        initializationCompleteCallback.onInitializationSucceeded();
    }

    @Override
    public void loadBannerAd(@NonNull MediationBannerAdConfiguration configuration, @NonNull MediationAdLoadCallback<MediationBannerAd, MediationBannerAdCallback> callback) {
        Log.d(TAG, "alx-admob-adapter-version:" + IionMetaInf.ADAPTER_VERSION);
        Log.d(TAG, "alx-admob-adapter: loadBannerAd");
        mMediationLoadCallback = callback;
        String parameter = configuration.getServerParameters().getString("parameter");
        if (!TextUtils.isEmpty(parameter)) {
            parseServer(parameter);
        }
        initSdk(configuration.getContext());
    }

    private void initSdk(final Context context) {
        if (TextUtils.isEmpty(unitid)) {
            Log.d(TAG, "alx unitid is empty");
            loadError(1, "alx unitid is empty.");
            return;
        }
        if (TextUtils.isEmpty(sid)) {
            Log.d(TAG, "alx sid is empty");
            loadError(1, "alx sid is empty.");
            return;
        }
        if (TextUtils.isEmpty(appid)) {
            Log.d(TAG, "alx appid is empty");
            loadError(1, "alx appid is empty.");
            return;
        }
        if (TextUtils.isEmpty(token)) {
            Log.d(TAG, "alx token is empty");
            loadError(1, "alx token is empty");
            return;
        }

        try {
            Log.i(TAG, "alx token: " + token + " alx appid: " + appid + "alx sid: " + sid);
            // init
            AlxAdSDK.init(context, token, sid, appid, new AlxSdkInitCallback() {
                @Override
                public void onInit(boolean isOk, String msg) {
                    load(context);
                }
            });
//                // set GDPR
//                // Subject to GDPR Flag: Please pass a Boolean value to indicate if the user is subject to GDPR regulations or not.
//                // Your app should make its own determination as to whether GDPR is applicable to the user or not.
//                AlxAdSDK.setSubjectToGDPR(true);
//                // set GDPR Consent
//                AlxAdSDK.setUserConsent("1");
//                // set COPPA
//                AlxAdSDK.setBelowConsentAge(true);
//                // set CCPA
//                AlxAdSDK.subjectToUSPrivacy("1YYY");
            AlxAdSDK.setDebug(isdebug);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
            loadError(1, "alx sdk init error");
        }
    }

    @NonNull
    @Override
    public View getView() {
        return mBannerView;
    }

    private void parseServer(String s) {
        if (TextUtils.isEmpty(s)) {
            Log.d(TAG, "serviceString  is empty ");
            return;
        }
        Log.d(TAG, "serviceString   " + s);
        try {
            JSONObject json = new JSONObject(s);
            appid = json.getString("appid");
            sid = json.getString("sid");
            token = json.getString("token");
            unitid = json.getString("unitid");
            String debug = json.optString("isdebug", "false");
            if (TextUtils.equals(debug, "true")) {
                isdebug = true;
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage() + "");
        }
    }

    private void load(Context context) {
        mBannerView = new AlxBannerView(context);
        // auto refresh ad  default = open = 1, 0 = close
        mBannerView.setBannerRefresh(0);
        //mBannerView.setBannerRefresh(15);
        final AlxBannerViewAdListener alxBannerADListener = new AlxBannerViewAdListener() {
            @Override
            public void onAdLoaded() {
                if (mMediationLoadCallback != null) {
                    mMediationEventCallback = mMediationLoadCallback.onSuccess(IionBannerAdapter.this);
                }
            }

            @Override
            public void onAdError(int errorCode, String errorMsg) {
                loadError(errorCode, errorMsg);
            }

            @Override
            public void onAdClicked() {
                if (mMediationEventCallback != null) {
                    mMediationEventCallback.reportAdClicked();
                }
            }

            @Override
            public void onAdShow() {
                if (mMediationEventCallback != null) {
                    mMediationEventCallback.reportAdImpression();
                    mMediationEventCallback.onAdOpened();
                }
            }

            @Override
            public void onAdClose() {
                if (mMediationEventCallback != null) {
                    mMediationEventCallback.onAdClosed();
                }
            }
        };
        mBannerView.loadAd(unitid, alxBannerADListener);
    }


    private void loadError(int code, String message) {
        if (mMediationLoadCallback != null) {
            mMediationLoadCallback.onFailure(new AdError(code, message, AlxAdSDK.getNetWorkName()));
        }
    }

    @Override
    public VersionInfo getVersionInfo() {
        String versionString = AlxAdSDK.getNetWorkVersion();
        VersionInfo result = getAdapterVersionInfo(versionString);
        if (result != null) {
            return result;
        }
        return new VersionInfo(0, 0, 0);
    }

    @Override
    public VersionInfo getSDKVersionInfo() {
        String versionString = AlxAdSDK.getNetWorkVersion();
        VersionInfo result = getAdapterVersionInfo(versionString);
        if (result != null) {
            return result;
        }
        return new VersionInfo(0, 0, 0);
    }

    private VersionInfo getAdapterVersionInfo(String version) {
        if (TextUtils.isEmpty(version)) {
            return null;
        }
        try {
            String[] arr = version.split("\\.");
            if (arr == null || arr.length < 3) {
                return null;
            }
            int major = Integer.parseInt(arr[0]);
            int minor = Integer.parseInt(arr[1]);
            int micro = Integer.parseInt(arr[2]);
            return new VersionInfo(major, minor, micro);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}