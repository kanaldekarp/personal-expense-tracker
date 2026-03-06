package com.expensetracker.model;

import java.time.LocalDate;

public class Income {
    private int id;
    private int userId;
    private String source;
    private double amount;
    private LocalDate date;
    private boolean recurring;
    private String notes;

    public Income() {}

    public Income(int id, int userId, String source, double amount, LocalDate date, boolean recurring, String notes) {
        this.id = id;
        this.userId = userId;
        this.source = source;
        this.amount = amount;
        this.date = date;
        this.recurring = recurring;
        this.notes = notes;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public boolean isRecurring() { return recurring; }
    public void setRecurring(boolean recurring) { this.recurring = recurring; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
