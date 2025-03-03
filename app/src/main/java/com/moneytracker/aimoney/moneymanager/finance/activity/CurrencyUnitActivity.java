package com.moneytracker.aimoney.moneymanager.finance.activity;


import android.content.Intent;

import com.moneytracker.aimoney.moneymanager.finance.Utils.Utils;
import com.moneytracker.aimoney.moneymanager.finance.adapter.CurrencyUnitAdapter;
//import com.moneytracker.aimoney.moneymanager.finance.base.AbsBaseActivity;
import com.moneytracker.aimoney.moneymanager.finance.databinding.ActivityCurrencyUnitBinding;
import com.moneytracker.aimoney.moneymanager.finance.base.AbsBaseActivity;


public class CurrencyUnitActivity extends AbsBaseActivity {

    public static final String EXTRA_FROM_SETTINGS = "extra_from_settings";


    CurrencyUnitAdapter currencyUnitAdapter;

    private ActivityCurrencyUnitBinding binding;

    @Override
    public void bind() {
        binding = ActivityCurrencyUnitBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        boolean fromSettings = getIntent().getBooleanExtra(EXTRA_FROM_SETTINGS, false);


        currencyUnitAdapter = new CurrencyUnitAdapter(this, Utils.getCurrencyUnit(), data -> {
            binding.ivSelect.setEnabled(true);
            binding.ivSelect.setAlpha(1.0f);

        });

        binding.rvCurrencyUnit.setAdapter(currencyUnitAdapter);


        if (fromSettings) {
            binding.ivSelect.setEnabled(true);
            binding.ivSelect.setAlpha(1.0f);
        } else {
            binding.ivSelect.setEnabled(false);
            binding.ivSelect.setAlpha(0.3f);
        }

        binding.ivSelect.setOnClickListener(v -> {
            if(fromSettings) {
                Intent resultIntent = new Intent();
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                startActivity(new Intent(CurrencyUnitActivity.this, HomeActivity.class));
            }

        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}

