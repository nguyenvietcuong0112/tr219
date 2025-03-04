package com.moneytracker.aimoney.moneymanager.finance.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.Nullable;

import com.mallegan.ads.util.AppOpenManager;
import com.moneytracker.aimoney.moneymanager.finance.utils.SharePreferenceUtils;
import com.moneytracker.aimoney.moneymanager.finance.base.BaseActivity;
import com.moneytracker.aimoney.moneymanager.finance.databinding.ActivitySettingsBinding;

import java.util.Timer;
import java.util.TimerTask;

public class SettingActivity extends BaseActivity {
    ActivitySettingsBinding binding;
    private boolean isBtnProcessing = false;
    private static final int REQUEST_CURRENCY_SELECT = 100;


    @Override
    public void bind() {
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.icback.setOnClickListener(v -> {
            onBackPressed();
        });

        binding.btnShare.setOnClickListener(v -> {
            if (isBtnProcessing) return;
            isBtnProcessing = true;

            Intent myIntent = new Intent(Intent.ACTION_SEND);
            myIntent.setType("text/plain");
            String body = "có link app thì điền vào";
            String sub = "Flash Alert App";
            myIntent.putExtra(Intent.EXTRA_SUBJECT, sub);
            myIntent.putExtra(Intent.EXTRA_TEXT, body);
            startActivity(Intent.createChooser(myIntent, "Share"));
            AppOpenManager.getInstance().disableAppResumeWithActivity(HomeActivity.class);
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    isBtnProcessing = false;
                }
            }, 1000);
        });

        binding.btnLanguage.setOnClickListener(v -> {
            Intent intent = new Intent(SettingActivity.this, LanguageActivity.class);
            intent.putExtra("from_settings", true);
            startActivity(intent);
        });

        binding.btnRateUs.setOnClickListener(v -> {
            Uri uri = Uri.parse("market://details?id=");
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            try {
                this.startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                this.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=")));
            }
        });


        binding.btnPrivacyPolicy.setOnClickListener(v -> {
            Uri uri = Uri.parse("https://mohamedezzeldin.netlify.app/policy");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });


        binding.llCurrency.setOnClickListener(v -> {
            Intent intent = new Intent(this, CurrencyUnitActivity.class);
            intent.putExtra(CurrencyUnitActivity.EXTRA_FROM_SETTINGS, true);
            startActivityForResult(intent, REQUEST_CURRENCY_SELECT);
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CURRENCY_SELECT && resultCode == RESULT_OK) {
            // Refresh UI để hiển thị currency mới
            updateCurrencyDisplay();
        }
    }

    private void updateCurrencyDisplay() {
        // Lấy currency code đã lưu và cập nhật UI
        String savedCurrencyCode = SharePreferenceUtils.getSelectedCurrencyCode(this);
        // Update TextView hoặc view hiển thị currency
        binding.tvCurrency.setText(savedCurrencyCode);
    }
}
