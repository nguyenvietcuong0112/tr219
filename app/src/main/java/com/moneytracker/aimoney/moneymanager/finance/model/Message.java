package com.moneytracker.aimoney.moneymanager.finance.model;

public class Message {
    private String text;
    private boolean isUser;
    private TransactionData transactionData;

    public Message(String text, boolean isUser) {
        this.text = text;
        this.isUser = isUser;
    }

    public Message(String text, boolean isUser, TransactionData transactionData) {
        this.text = text;
        this.isUser = isUser;
        this.transactionData = transactionData;
    }

    public String getText() { return text; }
    public boolean isUser() { return isUser; }
    public TransactionData getTransactionData() { return transactionData; }
    public void setTransactionData(TransactionData transactionData) {
        this.transactionData = transactionData;
    }
}
