package com.moneytracker.aimoney.moneymanager.finance.model;

import java.io.Serializable;

public class TransactionData implements Serializable {

    private String date;
    private String category;
    private String description;
    private String amount;
    private String type;
    private String currency;
    private String comment; // Thêm trường mới


    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public TransactionData(String date, String category, String description,
                           String amount, String type, String currency, String comment) {
        this.date = date;
        this.category = category;
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.currency = currency;
        this.comment = comment;

    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @Override
    public String toString() {
        return "TransactionData{" +
                "type='" + type + '\'' +
                ", category='" + category + '\'' +
                ", amount='" + amount + '\'' +
                ", description='" + description + '\'' +
                ", date='" + date + '\'' +
                ", currency='" + currency + '\'' +
                ", comment='" + comment + '\'' +
                '}';
    }
}