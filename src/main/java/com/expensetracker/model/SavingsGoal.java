package com.expensetracker.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class SavingsGoal {
    private int id;
    private int userId;
    private String name;
    private double targetAmount;
    private double savedAmount;
    private LocalDate deadline;
    private String icon;
    private String color;
    private LocalDateTime createdAt;

    public SavingsGoal() {
        this.icon = "fa-bullseye";
        this.color = "#4f46e5";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getTargetAmount() { return targetAmount; }
    public void setTargetAmount(double targetAmount) { this.targetAmount = targetAmount; }
    public double getSavedAmount() { return savedAmount; }
    public void setSavedAmount(double savedAmount) { this.savedAmount = savedAmount; }
    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public double getProgressPercent() {
        return targetAmount > 0 ? Math.min((savedAmount / targetAmount) * 100, 100) : 0;
    }

    public double getRemainingAmount() {
        return Math.max(targetAmount - savedAmount, 0);
    }
}
