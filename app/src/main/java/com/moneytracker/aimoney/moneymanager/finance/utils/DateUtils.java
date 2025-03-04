package com.moneytracker.aimoney.moneymanager.finance.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public static String getCurrentDate() {
        return DATE_FORMAT.format(new Date());
    }

    public static String formatDate(String dateStr) {
        try {
            Date date = DATE_FORMAT.parse(dateStr);
            return date != null ? DATE_FORMAT.format(date) : dateStr;
        } catch (Exception e) {
            return dateStr;
        }
    }
}