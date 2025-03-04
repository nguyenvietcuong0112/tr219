package com.moneytracker.aimoney.moneymanager.finance.activity;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mallegan.ads.callback.InterCallback;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.moneytracker.aimoney.moneymanager.finance.R;
import com.moneytracker.aimoney.moneymanager.finance.utils.Constant;
import com.moneytracker.aimoney.moneymanager.finance.utils.SharePreferenceUtils;
import com.moneytracker.aimoney.moneymanager.finance.base.AbsBaseActivity;
import com.moneytracker.aimoney.moneymanager.finance.databinding.HomeActivityBinding;


public class HomeActivity extends AbsBaseActivity {
    private HomeActivityBinding binding;
    private SharePreferenceUtils sharePreferenceUtils;
    private Class<?> destinationClass;

    @Override
    public void bind() {
        binding = HomeActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        sharePreferenceUtils = new SharePreferenceUtils(this);
        sharePreferenceUtils.incrementCounter();


        binding.cardChatbot.setOnClickListener(v -> {
            handleNavigate(TransactionActivity.class);
        });
        binding.cardOverview.setOnClickListener(v -> {
            handleNavigate(OverviewActivity.class);

        });
        binding.cardReport.setOnClickListener(v -> {
            handleNavigate(ReportActivity.class);
        });

        binding.ivSettings.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
        });

        loadBanner();
        loadAds();
        loadInterHome();

    }

    private void handleNavigate(Class<?> destination) {
        destinationClass = destination;
        if (Constant.interHome != null) {
            loadAppOpenAd();
            Admob.getInstance().showInterAds(HomeActivity.this, Constant.interHome, new InterCallback() {
                @Override
                public void onAdFailedToLoad(LoadAdError error) {
                    super.onAdFailedToLoad(error);
                    handleNavigate(destinationClass);
                }

                @Override
                public void onAdFailedToShow(AdError error) {
                    super.onAdFailedToShow(error);
                    handleNavigate(destinationClass);
                }

                @Override
                public void onAdClosedByUser() {
                    super.onAdClosedByUser();
                    showAppOpenAd();
                }

                @Override
                public void onNextAction() {
                    super.onNextAction();
                    loadInterHome();
                }
            });
        } else {
            goToClassName(destinationClass);
        }
    }

    public void goToClassName(Class<?> destination) {
        Intent intent = new Intent(HomeActivity.this, destination);
        startActivity(intent);
    }


    private void loadBanner() {
        if (!SharePreferenceUtils.isOrganic(this)) {
            Admob.getInstance().loadCollapsibleBanner(
                    this,
                    getString(R.string.banner_collap),
                    "top"
            );
        } else {
            binding.llBanner.setVisibility(View.GONE);
        }
    }


    private void loadAds() {
        if (!SharePreferenceUtils.isOrganic(HomeActivity.this)) {
            Admob.getInstance().loadNativeAd(this, getString(R.string.native_home), new NativeCallback() {
                @Override
                public void onNativeAdLoaded(NativeAd nativeAd) {
                    super.onNativeAdLoaded(nativeAd);
                    NativeAdView adView = (NativeAdView) LayoutInflater.from(HomeActivity.this).inflate(R.layout.layout_native_language, null);
                    binding.frAds.setVisibility(View.VISIBLE);
                    binding.frAds.removeAllViews();
                    binding.frAds.addView(adView);
                    Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
                }

                @Override
                public void onAdFailedToLoad() {
                    super.onAdFailedToLoad();
                    binding.frAds.setVisibility(View.GONE);
                }
            });
        } else {
            binding.frAds.removeAllViews();
        }
    }

    private void loadInterHome() {
        if (!SharePreferenceUtils.isOrganic(this)) {
            Admob.getInstance().loadInterAds(this, getString(R.string.inter_home), new InterCallback() {
                @Override
                public void onInterstitialLoad(InterstitialAd interstitialAd) {
                    super.onInterstitialLoad(interstitialAd);
                    Constant.interHome = interstitialAd;
                }
            });
        }

    }

    private AppOpenAd appOpenAd;

    private void showAppOpenAd() {
        if (appOpenAd != null) {
            appOpenAd.show(this);
            appOpenAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    goToClassName(destinationClass);
                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    goToClassName(destinationClass);
                }
            });
        } else {
            goToClassName(destinationClass);
        }
    }


    private void loadAppOpenAd() {
        if (!SharePreferenceUtils.isOrganic(this)) {
            AdRequest adRequest = new AdRequest.Builder().build();
            AppOpenAd.load(this, getString(R.string.open_inter),
                    adRequest, AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, new AppOpenAd.AppOpenAdLoadCallback() {
                        @Override
                        public void onAdLoaded(AppOpenAd ad) {
                            appOpenAd = ad;
                        }

                        @Override
                        public void onAdFailedToLoad(LoadAdError loadAdError) {
                            goToClassName(destinationClass);
                        }
                    });
        }
    }

}

