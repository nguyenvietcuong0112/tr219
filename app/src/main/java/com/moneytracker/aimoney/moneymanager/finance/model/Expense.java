package com.moneytracker.aimoney.moneymanager.finance.model;

public class Expense {
    private String category;
    private String percentage;
    private String amount;
    private String type; // Thêm trường type


    public Expense(String category, String percentage, String amount,String type) {
        this.category = category;
        this.percentage = percentage;
        this.amount = amount;
        this.type = type;

    }

    public String getCategory() {
        return category;
    }

    public String getPercentage() {
        return percentage;
    }

    public String getAmount() {
        return amount;
    }
    public String getType() {
        return type;
    }
}