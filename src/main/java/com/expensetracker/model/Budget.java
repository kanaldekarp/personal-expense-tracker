package com.expensetracker.model;

public class Budget {
    private int id;
    private int userId;
    private String category;
    private double budgetAmount;
    private int month; // 1-12
    private int year;

    public Budget() {}

    public Budget(int userId, String category, double budgetAmount, int month, int year) {
        this.userId = userId;
        this.category = category;
        this.budgetAmount = budgetAmount;
        this.month = month;
        this.year = year;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getBudgetAmount() { return budgetAmount; }
    public void setBudgetAmount(double budgetAmount) { this.budgetAmount = budgetAmount; }

    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
}
