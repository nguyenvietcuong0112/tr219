package com.moneytracker.aimoney.moneymanager.finance.utils;

import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.moneytracker.aimoney.moneymanager.finance.R;
import com.moneytracker.aimoney.moneymanager.finance.activity.GuideActivity;
import com.moneytracker.aimoney.moneymanager.finance.activity.IntroActivity;
import com.moneytracker.aimoney.moneymanager.finance.base.BaseActivity;
import com.moneytracker.aimoney.moneymanager.finance.databinding.ActivityNativeFullBinding;


public class ActivityLoadNativeFull extends BaseActivity {
    ActivityNativeFullBinding binding;
    private SharePreferenceUtils sharePreferenceUtils;

    @Override
    public void bind() {
        SystemConfiguration.setStatusBarColor(this, R.color.transparent, SystemConfiguration.IconColor.ICON_DARK);
        binding = ActivityNativeFullBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadNativeFull();
    }

    private void loadNativeFull() {
        Admob.getInstance().loadNativeAds(this, getString(R.string.native_full_language), 1, new NativeCallback() {
            @Override
            public void onAdFailedToLoad() {
                super.onAdFailedToLoad();
                binding.frAdsFull.setVisibility(View.VISIBLE);
                startActivity(new Intent(ActivityLoadNativeFull.this, IntroActivity.class));
                finish();

            }

            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
                NativeAdView adView = (NativeAdView) LayoutInflater.from(ActivityLoadNativeFull.this)
                        .inflate(R.layout.native_full_language, null);

                ImageView closeButton = adView.findViewById(R.id.close);
                closeButton.setOnClickListener(v -> {

                    sharePreferenceUtils = new SharePreferenceUtils(getApplicationContext());
                    int counterValue = sharePreferenceUtils.getCurrentValue();
                    if (counterValue == 0) {
                        startActivity(new Intent(ActivityLoadNativeFull.this, IntroActivity.class));
                    } else {
                        startActivity(new Intent(ActivityLoadNativeFull.this, GuideActivity.class));
                    }

                });
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        closeButton.setVisibility(View.VISIBLE);
                    }
                }, 5000);
                binding.frAdsFull.removeAllViews();
                binding.frAdsFull.addView(adView);
                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
            }
        });
    }
}
