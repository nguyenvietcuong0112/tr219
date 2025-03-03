package com.moneytracker.aimoney.moneymanager.finance.model;

import java.util.List;
import java.util.TreeMap;

public class ReportData {
    public TreeMap<String, List<TransactionData>> dailyTransactions;
    public double totalIncome;
    public double totalExpenses;

    public ReportData(TreeMap<String, List<TransactionData>> dailyTransactions,
                      double totalIncome, double totalExpenses) {
        this.dailyTransactions = dailyTransactions;
        this.totalIncome = totalIncome;
        this.totalExpenses = totalExpenses;
    }
}