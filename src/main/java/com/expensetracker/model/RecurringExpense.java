package com.expensetracker.model;

import java.time.LocalDate;

public class RecurringExpense {
    private int id;
    private int userId;
    private String title;
    private String category;
    private double amount;
    private String frequency; // daily, weekly, monthly, yearly
    private LocalDate nextDue;
    private String description;
    private boolean active;

    public RecurringExpense() { this.active = true; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public LocalDate getNextDue() { return nextDue; }
    public void setNextDue(LocalDate nextDue) { this.nextDue = nextDue; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
