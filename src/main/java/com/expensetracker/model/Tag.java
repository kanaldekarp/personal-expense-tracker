package com.expensetracker.model;

public class Tag {
    private int id;
    private int userId;
    private String name;
    private String color;

    public Tag() { this.color = "#6366f1"; }

    public Tag(int id, int userId, String name, String color) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.color = color;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
