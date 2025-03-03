package com.moneytracker.aimoney.moneymanager.finance.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.tabs.TabLayout;
import com.moneytracker.aimoney.moneymanager.finance.R;
import com.moneytracker.aimoney.moneymanager.finance.Utils.AssistiveTouch;
import com.moneytracker.aimoney.moneymanager.finance.Utils.SharePreferenceUtils;
import com.moneytracker.aimoney.moneymanager.finance.adapter.ExpenseAdapter;
import com.moneytracker.aimoney.moneymanager.finance.base.AbsBaseActivity;
import com.moneytracker.aimoney.moneymanager.finance.databinding.OverviewActivityBinding;
import com.moneytracker.aimoney.moneymanager.finance.model.Expense;
import com.moneytracker.aimoney.moneymanager.finance.model.Message;
import com.moneytracker.aimoney.moneymanager.finance.model.TransactionData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class OverviewActivity extends AbsBaseActivity {
    private BroadcastReceiver broadcastReceiver;
    private BarChart barChart;

    private OverviewActivityBinding binding;
    private ExpenseAdapter expenseAdapter;
    private Spinner monthSelector;

    private Spinner timeFilterSpinner;
    private static final String[] TIME_FILTERS = {"Day", "Week", "Month", "Year", "All Time"};
    @Override
    public void bind() {
        binding = OverviewActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        monthSelector = binding.monthSelector;
        binding.imTransaction.setOnTouchListener(new AssistiveTouch());
        timeFilterSpinner = binding.timeFilterSpinner;

        setupTimeFilterSpinner();

//        broadcastReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                if (intent.getAction().equals("CURRENCY_CHANGED")) {
//                    String selectedMonth = monthSelector.getSelectedItem().toString();
//
//                    List<Message> filteredMessages = filterMessagesByMonth(selectedMonth);
//                    updateSavingAmount(filteredMessages);
//
//                    int selectedTabPosition = binding.tabLayout.getSelectedTabPosition();
//                    String currentType = selectedTabPosition == 0 ? "Expense" : "Income";
//                    loadPieChartData(currentType, filteredMessages);
//
//                    if (expenseAdapter != null) {
//                        expenseAdapter.updateData(getExpenses(currentType, filteredMessages));
//                    }
//                }
//            }
//        };
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("CURRENCY_CHANGED")) {
                    updateDataForSelectedFilters();
                }
            }
        };
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(broadcastReceiver, new IntentFilter("CURRENCY_CHANGED"));

        binding.ivBack.setOnClickListener(view -> onBackPressed());
//        binding.imTransaction.setOnClickListener(v -> {
//            Intent intent = new Intent(this, TransactionActivity.class);
//            startActivity(intent);
//        });


//        setupMonthSelector();

        setupTabLayout();
        setupPieChart();
        updateSavingAmount(SharePreferenceUtils.loadMessages(this));
        setupRecyclerView();
        setupBarChart();
        updateCharts(SharePreferenceUtils.loadMessages(this));


        updateDataForSelectedFilters();

    }

    private void setupTimeFilterSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item,
                TIME_FILTERS
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeFilterSpinner.setAdapter(adapter);

        // Default to "Month" filter
        timeFilterSpinner.setSelection(2);

        timeFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateDataForSelectedFilters();

                // Show/hide month selector based on time filter
                if (TIME_FILTERS[position].equals("Month")) {
                    binding.monthSelectorContainer.setVisibility(View.VISIBLE);
                } else {
                    binding.monthSelectorContainer.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
    private void updateDataForSelectedFilters() {
        String timeFilter = timeFilterSpinner.getSelectedItem().toString();

        List<Message> filteredMessages;
        if (timeFilter.equals("Month")) {
            String selectedMonth = monthSelector.getSelectedItem().toString();
            filteredMessages = filterMessagesByMonth(selectedMonth);
        } else {
            filteredMessages = filterMessagesByTimePeriod(timeFilter);
        }

        updateSavingAmount(filteredMessages);
        updateCharts(filteredMessages);

        int selectedTabPosition = binding.tabLayout.getSelectedTabPosition();
        String currentType = selectedTabPosition == 0 ? "Expense" : "Income";

        loadPieChartData(currentType, filteredMessages);

        if (expenseAdapter != null) {
            expenseAdapter.updateData(getExpenses(currentType, filteredMessages));
        }
    }

    private List<Message> filterMessagesByTimePeriod(String timePeriod) {
        List<Message> allMessages = SharePreferenceUtils.loadMessages(this);

        if (timePeriod.equals("All Time")) {
            return allMessages;
        }

        List<Message> filteredMessages = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();

        for (Message message : allMessages) {
            if (message.getTransactionData() != null) {
                String transactionDateStr = message.getTransactionData().getDate();
                Date transactionDate = parseDate(transactionDateStr);

                if (transactionDate != null) {
                    if (isDateInTimePeriod(transactionDate, currentDate, timePeriod)) {
                        filteredMessages.add(message);
                    }
                }
            }
        }

        return filteredMessages;
    }

    private boolean isDateInTimePeriod(Date date, Date currentDate, String timePeriod) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(currentDate);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date);

        switch (timePeriod) {
            case "Day":
                return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                        && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);

            case "Week":
                cal1.setFirstDayOfWeek(Calendar.MONDAY);
                cal2.setFirstDayOfWeek(Calendar.MONDAY);
                return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                        && cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR);

            case "Year":
                return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);

            default:
                return false;
        }
    }

    private Date parseDate(String dateString) {
        String[] possibleFormats = {
                "MMMM d, yyyy",
                "MMM d, yyyy"
        };

        for (String format : possibleFormats) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat(format, Locale.ENGLISH);
                return inputFormat.parse(dateString);
            } catch (ParseException e) {
                // Try next format
            }
        }
        return null;
    }


    private void setupBarChart() {
        barChart = binding.barChart;

        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.setHighlightFullBarEnabled(false);

        barChart.setDoubleTapToZoomEnabled(false);


        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{ "Expense","Income"}));

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setEnabled(true);
        leftAxis.setSpaceTop(35f);
        leftAxis.setAxisMinimum(0f);

        leftAxis.setDrawGridLines(true);
        leftAxis.enableGridDashedLine(10f, 10f, 0f);
        leftAxis.setGridColor(Color.GRAY);

        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setEnabled(false);
    }


    private void updateCharts(List<Message> messages) {
        String filterType = binding.tabLayout.getSelectedTabPosition() == 0 ? "Expense" : "Income";
        loadPieChartData(filterType, messages);

        updateBarChart(messages);
    }

    private void updateBarChart(List<Message> messages) {
        float totalIncome = 0f;
        float totalExpense = 0f;

        for (Message message : messages) {
            if (message.getTransactionData() != null) {
                TransactionData data = message.getTransactionData();
                float amount = parseAmount(data.getAmount());

                if ("Income".equals(data.getType())) {
                    totalIncome += amount;
                } else if ("Expense".equals(data.getType())) {
                    totalExpense += amount;
                }
            }
        }

        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, totalExpense));
        entries.add(new BarEntry(1, totalIncome));

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColors(
                getResources().getColor(R.color.red),
                getResources().getColor(R.color.green)
        );

        dataSet.setDrawValues(false);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.3f);

        float finalTotalIncome = totalIncome;
        float finalTotalExpense = totalExpense;

        String currency = SharePreferenceUtils.getSelectedCurrencyCode(this);
        if (currency.isEmpty()) currency = "USD";
        String finalCurrency = currency;


        barChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                if (value == 0) {
                    return "" + (int) finalTotalExpense + " " + finalCurrency;
                } else if (value == 1) {
                    return "" + (int)  finalTotalIncome + " " + finalCurrency;
                }
                return "";
            }
        });
        barChart.setScaleEnabled(false);
        barChart.setDoubleTapToZoomEnabled(false);
        barChart.setData(barData);
        barChart.invalidate();
    }

    private void setupMonthSelector() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.months,
               R.layout.spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSelector.setAdapter(adapter);

        String currentMonth = new SimpleDateFormat("MMMM", Locale.ENGLISH).format(new Date());
        int defaultPosition = adapter.getPosition(currentMonth);
        if (defaultPosition >= 0) {
            monthSelector.setSelection(defaultPosition);
        }

        monthSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedMonth = parent.getItemAtPosition(position).toString();
                updateDataForSelectedMonth(selectedMonth);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void updateDataForSelectedMonth(String selectedMonth) {
        List<Message> filteredMessages = filterMessagesByMonth(selectedMonth);
        updateSavingAmount(filteredMessages);
        updateCharts(filteredMessages);

        int selectedTabPosition = binding.tabLayout.getSelectedTabPosition();
        String currentType = selectedTabPosition == 0 ? "Expense" : "Income";

        loadPieChartData(currentType, filteredMessages);

        if (expenseAdapter != null) {
            expenseAdapter.updateData(getExpenses(currentType, filteredMessages));
        }
    }

    private List<Message> filterMessagesByMonth(String selectedMonth) {
        List<Message> allMessages = SharePreferenceUtils.loadMessages(this);
        List<Message> filteredMessages = new ArrayList<>();

        for (Message message : allMessages) {
            if (message.getTransactionData() != null) {
                String transactionDate = message.getTransactionData().getDate();
                if (isValidDateFormat(transactionDate)) {
                    String transactionMonth = extractMonthFromDate(transactionDate);
                    if (selectedMonth.equalsIgnoreCase(transactionMonth)) {
                        filteredMessages.add(message);
                    }
                }
            }
        }
        return filteredMessages;
    }

    private boolean isValidDateFormat(String date) {
        String[] possibleFormats = {
                "MMMM d, yyyy",
                "MMM d, yyyy"
        };

        for (String format : possibleFormats) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat(format, Locale.ENGLISH);
                inputFormat.parse(date);
                return true;
            } catch (ParseException e) {
            }
        }

        return false;
    }


    private String extractMonthFromDate(String date) {
        String[] possibleFormats = {
                "MMMM d, yyyy",
                "MMM d, yyyy"
        };

        for (String format : possibleFormats) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat(format, Locale.ENGLISH);
                Date parsedDate = inputFormat.parse(date);

                SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.ENGLISH);
                return monthFormat.format(parsedDate);
            } catch (ParseException e) {
            }
        }

        return "";
    }


    private void updateSavingAmount(List<Message> messages) {
        float totalIncome = 0f;
        float totalExpense = 0f;
        String currentCurrency = SharePreferenceUtils.getSelectedCurrencyCode(this);
        if (currentCurrency.isEmpty()) currentCurrency = "USD";

        for (Message message : messages) {
            if (message.getTransactionData() != null) {
                TransactionData transactionData = message.getTransactionData();
                String amountText = transactionData.getAmount();
                String type = transactionData.getType();

                float amount = parseAmount(amountText);

                if ("Income".equals(type)) {
                    totalIncome += amount;
                } else if ("Expense".equals(type)) {
                    totalExpense += amount;
                }
            }
        }

        float savingAmount = totalIncome - totalExpense;
        binding.savingAmount.setText(String.format("%.2f", savingAmount) + " " + currentCurrency);
        binding.expenseAmount.setText(String.format("%.2f", totalExpense) + " " + currentCurrency);
        binding.incomeAmount.setText(String.format("%.2f", totalIncome) + " " + currentCurrency);
    }

    private float parseAmount(String amountText) {
        try {
            String cleanAmount = amountText.replaceAll("[^\\d.]", "");
            return Float.parseFloat(cleanAmount);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 0f;
        }
    }

    private void setupPieChart() {
        binding.pieChart.setDrawHoleEnabled(true);
        binding.pieChart.setUsePercentValues(true);
        binding.pieChart.setDrawCenterText(false);
        binding.pieChart.setEntryLabelTextSize(12);
        binding.pieChart.setCenterTextSize(20);
        binding.pieChart.getDescription().setEnabled(false);
        binding.pieChart.setDrawSliceText(false);

        Legend legend = binding.pieChart.getLegend();

        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);

        legend.setMaxSizePercent(1f);
        legend.setWordWrapEnabled(true);
        legend.setTextSize(12f);
        legend.setXEntrySpace(20f);
        legend.setYEntrySpace(5f);
        legend.setFormSize(10f);
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setDrawInside(false);
        legend.setEnabled(true);
    }

    private void setupTabLayout() {
        TabLayout tabLayout = binding.tabLayout;
        tabLayout.addTab(tabLayout.newTab().setText("Expense"));
        tabLayout.addTab(tabLayout.newTab().setText("Income"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String selectedMonth = monthSelector.getSelectedItem().toString();
                List<Message> filteredMessages = filterMessagesByMonth(selectedMonth);

                String filterType = tab.getPosition() == 0 ? "Expense" : "Income";
                loadPieChartData(filterType, filteredMessages);
                binding.pieChart.setCenterText(filterType);

                ArrayList<Expense> filteredExpenses = getExpenses(filterType, filteredMessages);
                expenseAdapter.updateData(filteredExpenses);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }


    private void loadPieChartData(String filterType, List<Message> messages) {
        HashMap<String, Float> categoryTotals = new HashMap<>();

        for (Message message : messages) {
            if (message.getTransactionData() != null) {
                TransactionData data = message.getTransactionData();
                if (filterType.equals(data.getType())) {
                    String category = data.getCategory();
                    float amount = parseAmount(data.getAmount());
                    categoryTotals.put(category, categoryTotals.getOrDefault(category, 0f) + amount);
                }
            }
        }

        ArrayList<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Float> entry : categoryTotals.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(getPieChartColors());

        dataSet.setSliceSpace(2f);
        PieData data = new PieData(dataSet);
        data.setDrawValues(true);
        data.setValueTextSize(12f);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.1f%%", value);
            }
        });
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

        dataSet.setValueLinePart1OffsetPercentage(110f);

        dataSet.setValueLinePart1Length(0.5f);
        dataSet.setValueLinePart2Length(0.2f);

        dataSet.setValueLineWidth(1f);
        dataSet.setValueLineColor(Color.DKGRAY);

        data.setValueTextSize(11f);
        data.setValueTextColor(Color.DKGRAY);

        dataSet.setSliceSpace(3f);
        binding.pieChart.setData(data);
        binding.pieChart.invalidate();
    }

    private ArrayList<Integer> getPieChartColors() {
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(getResources().getColor(R.color.color_chart2));
        colors.add(getResources().getColor(R.color.color_chart3));
        colors.add(getResources().getColor(R.color.color_chart4));
        colors.add(getResources().getColor(R.color.color_chart5));
        colors.add(getResources().getColor(R.color.color_chart1));
        return colors;
    }

    private void setupRecyclerView() {
        binding.expenseRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        if (expenseAdapter == null) {
            expenseAdapter = new ExpenseAdapter(getExpenses("Expense", SharePreferenceUtils.loadMessages(this)));
            binding.expenseRecyclerView.setAdapter(expenseAdapter);
        }

    }

    private ArrayList<Expense> getExpenses(String filterType, List<Message> messages) {
        String currentCurrency = SharePreferenceUtils.getSelectedCurrencyCode(this);
        if (currentCurrency.isEmpty()) currentCurrency = "USD";
        HashMap<String, Float> categoryTotals = new HashMap<>();
        for (Message message : messages) {
            if (message.getTransactionData() != null) {
                TransactionData transactionData = message.getTransactionData();
                String category = transactionData.getCategory();
                String amountText = transactionData.getAmount();
                String type = transactionData.getType();

                if (filterType.equals(type)) {
                    float amount = parseAmount(amountText);
                    categoryTotals.put(category, categoryTotals.getOrDefault(category, 0f) + amount);
                }
            }
        }

        ArrayList<Expense> expenses = new ArrayList<>();
        for (Map.Entry<String, Float> entry : categoryTotals.entrySet()) {
            String category = entry.getKey();
            float totalAmount = entry.getValue();
            float percentage = (getTotalIncomeExpense(messages) > 0) ? (totalAmount / getTotalIncomeExpense(messages)) * 100 : 0;
            expenses.add(new Expense(
                    category,
                    String.format("%.2f%%", percentage),
                    currentCurrency + " " + String.format("%.2f", totalAmount),
                    filterType
            ));
        }

        return expenses;
    }

    private float getTotalIncomeExpense(List<Message> messages) {
        float total = 0f;
        for (Message message : messages) {
            if (message.getTransactionData() != null) {
                String amountText = message.getTransactionData().getAmount();
                total += parseAmount(amountText);
            }
        }
        return total;
    }
}