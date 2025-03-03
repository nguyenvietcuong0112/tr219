package com.moneytracker.aimoney.moneymanager.finance.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.moneytracker.aimoney.moneymanager.finance.R;
import com.moneytracker.aimoney.moneymanager.finance.Utils.SharePreferenceUtils;
import com.moneytracker.aimoney.moneymanager.finance.adapter.CategoryAdapter;
import com.moneytracker.aimoney.moneymanager.finance.databinding.ActivityTransactionEditBinding;
import com.moneytracker.aimoney.moneymanager.finance.model.TransactionData;

import java.lang.reflect.Type;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TransactionEditActivity extends AppCompatActivity {
    private ActivityTransactionEditBinding binding;
    private TransactionData transactionData;
    private List<String> categories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransactionEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadCategoriesFromPreferences();

        Intent intent = getIntent();
        transactionData = (TransactionData) intent.getSerializableExtra("transaction_data");

        assert transactionData != null;
        binding.tvType.setText(transactionData.getType());
        binding.tvCategory.setText(transactionData.getCategory());
        binding.etAmount.setText(String.format("%s %s", transactionData.getAmount(), ""));
        binding.etDescription.setText(transactionData.getDescription());
        binding.tvDate.setText(transactionData.getDate());

        binding.llCategory.setOnClickListener(v -> showCategoryBottomSheet());
        binding.btnSave.setOnClickListener(v -> saveTransactionData());
        binding.tvDate.setOnClickListener(v -> setupDatePicker());

        binding.llType.setOnClickListener(view -> {
            toggleTransactionType();

        });

        binding.btnDelete.setOnClickListener(v -> {
            showDeleteConfirmationDialog();
        });
        binding.ivBack.setOnClickListener(view -> onBackPressed());
        loadAds();
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
        int transactionIndex = getIntent().getIntExtra("transaction_index", -1);


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

    private void loadAds() {
        if (!SharePreferenceUtils.isOrganic(TransactionEditActivity.this)) {
            Admob.getInstance().loadNativeAd(this, getString(R.string.native_edit_transaction), new NativeCallback() {
                @Override
                public void onNativeAdLoaded(NativeAd nativeAd) {
                    super.onNativeAdLoaded(nativeAd);
                    NativeAdView adView = (NativeAdView) LayoutInflater.from(TransactionEditActivity.this).inflate(R.layout.layout_native_transaction, null);
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


    private void toggleTransactionType() {
        if ("Income".equals(transactionData.getType())) {
            transactionData.setType("Expense");
        } else {
            transactionData.setType("Income");
        }

        binding.tvType.setText(transactionData.getType());

    }



    private void showCategoryBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_categories, null);
        bottomSheetDialog.setContentView(sheetView);

        RecyclerView rvCategories = sheetView.findViewById(R.id.rvCategories);
        Button btnAddCategory = sheetView.findViewById(R.id.btnAddCategory);

        CategoryAdapter adapter = new CategoryAdapter(categories, category -> {
            updateCategory(category);
            bottomSheetDialog.dismiss();
        });

        rvCategories.setAdapter(adapter);
        rvCategories.setLayoutManager(new LinearLayoutManager(this));

        btnAddCategory.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            showAddCategoryBottomSheet();
        });

        bottomSheetDialog.show();
    }



    private void showAddCategoryBottomSheet() {
        BottomSheetDialog addCategoryDialog = new BottomSheetDialog(this);
        View sheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_add_category, null);
        addCategoryDialog.setContentView(sheetView);

        EditText etNewCategory = sheetView.findViewById(R.id.etNewCategory);
        Button btnDone = sheetView.findViewById(R.id.btnDone);

        btnDone.setOnClickListener(v -> {
            String newCategory = etNewCategory.getText().toString().trim();
            if (!newCategory.isEmpty()) {
                categories.add(newCategory);
                saveCategoriesToPreferences();
                Toast.makeText(this, "Category added: " + newCategory, Toast.LENGTH_SHORT).show();
                addCategoryDialog.dismiss();
                showCategoryBottomSheet();
            } else {
                Toast.makeText(this, "Please enter a category name", Toast.LENGTH_SHORT).show();
            }
        });

        addCategoryDialog.show();
    }

    private void saveCategoriesToPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Save categories as JSON
        Gson gson = new Gson();
        String json = gson.toJson(categories);
        editor.putString("categories", json);
        editor.apply();
    }

    private void loadCategoriesFromPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);

        Gson gson = new Gson();
        String json = sharedPreferences.getString("categories", null);

        if (json != null) {
            Type type = new TypeToken<List<String>>() {}.getType();
            categories = gson.fromJson(json, type);
        } else {
            categories = new ArrayList<>(Arrays.asList("Food & Drinks", "Shopping", "Transportation", "Pet", "Beauty"));
        }
    }

    private void saveTransactionData() {
        if (transactionData != null) {
            transactionData.setType(binding.tvType.getText().toString());
            transactionData.setCategory(binding.tvCategory.getText().toString());
            transactionData.setAmount(binding.etAmount.getText().toString());
            transactionData.setDescription(binding.etDescription.getText().toString());
            transactionData.setDate(binding.tvDate.getText().toString());

            Intent resultIntent = new Intent();
            resultIntent.putExtra("updated_transaction", transactionData);

            int transactionIndex = getIntent().getIntExtra("transaction_index", -1);
            resultIntent.putExtra("transaction_index", transactionIndex);

            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }

    private void updateCategory(String category) {
        binding.tvCategory.setText(category);
        transactionData.setCategory(category);
    }

    private void setupDatePicker() {
        DatePickerDialog datePicker = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String newDate = String.format(Locale.US, "%s %d, %d",
                            new DateFormatSymbols().getMonths()[month], dayOfMonth, year);
                    binding.tvDate.setText(newDate);
                    transactionData.setDate(newDate);
                },
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        datePicker.show();
    }
}