package com.moneytracker.aimoney.moneymanager.finance.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.moneytracker.aimoney.moneymanager.finance.R;
import com.moneytracker.aimoney.moneymanager.finance.utils.SharePreferenceUtils;
import com.moneytracker.aimoney.moneymanager.finance.databinding.ActivityTransactionDetailsBinding;
import com.moneytracker.aimoney.moneymanager.finance.model.TransactionData;


public class TransactionDetailsActivity extends AppCompatActivity {
    private ActivityTransactionDetailsBinding binding;
    private TransactionData transactionData;
    private int transactionIndex = -1;

    private ActivityResultLauncher<Intent> editLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransactionDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        editLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            TransactionData updatedTransaction =
                                    (TransactionData) data.getSerializableExtra("updated_transaction");

                            if (updatedTransaction != null) {
                                updateTransactionDisplay(updatedTransaction);
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("updated_transaction", updatedTransaction);
                                resultIntent.putExtra("transaction_index", transactionIndex);
                                setResult(RESULT_OK, resultIntent);
                            }
                        }
                    }
                }
        );

        Intent intent = getIntent();
        transactionData = (TransactionData) intent.getSerializableExtra("transaction_data");
        transactionIndex = intent.getIntExtra("transaction_index", -1);

        updateTransactionDisplay(transactionData);

        assert transactionData != null;
        binding.tvType.setText(transactionData.getType());
        binding.tvCategory.setText(transactionData.getCategory());
        String amountString = transactionData.getAmount();
        double amount = 0.0;
        if (amountString != null && amountString.matches("-?\\d+(\\.\\d+)?")) {
            amount = Double.parseDouble(amountString);
        } else {
            amount = 0.0;
        }
        binding.etAmount.setText(String.format("%,.2f", amount));
        binding.etDescription.setText(transactionData.getDescription());
        binding.tvDate.setText(transactionData.getDate());

        binding.btnEdit.setOnClickListener(v -> {
            Intent editIntent = new Intent(this, TransactionEditActivity.class);
            editIntent.putExtra("transaction_data", transactionData);
            editIntent.putExtra("transaction_index", transactionIndex);
            editLauncher.launch(editIntent);
        });

        binding.btnDelete.setOnClickListener(v -> {
            showDeleteConfirmationDialog();
        });

        binding.ivBack.setOnClickListener(view -> onBackPressed());
        loadAds();

    }

    private void loadAds() {
        if (!SharePreferenceUtils.isOrganic(TransactionDetailsActivity.this)) {
            Admob.getInstance().loadNativeAd(this, getString(R.string.native_view_transaction), new NativeCallback() {
                @Override
                public void onNativeAdLoaded(NativeAd nativeAd) {
                    super.onNativeAdLoaded(nativeAd);
                    NativeAdView adView = (NativeAdView) LayoutInflater.from(TransactionDetailsActivity.this).inflate(R.layout.layout_native_transaction, null);
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
        }       else {
        binding.frAds.removeAllViews();
    }
    }

    private void updateTransactionDisplay(TransactionData transaction) {
        this.transactionData = transaction;
        binding.tvType.setText(transaction.getType());
        binding.tvCategory.setText(transaction.getCategory());
        binding.etAmount.setText(String.format("%s %s", transaction.getAmount(), transaction.getCurrency()));
        binding.etDescription.setText(transaction.getDescription());
        binding.tvDate.setText(transaction.getDate());

        if(transaction.getType() == "Expense") {
            binding.etAmount.setTextColor(Color.parseColor("#F2444C"));
        } else {
            binding.etAmount.setTextColor(Color.parseColor("#499A23"));

        }
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_delete_confirmation, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnDelete = dialogView.findViewById(R.id.btnDelete);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnDelete.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("delete_transaction", true);
            resultIntent.putExtra("transaction_index", transactionIndex);
            setResult(RESULT_OK, resultIntent);
            dialog.dismiss();
            finish();
        });

        dialog.show();
    }


}