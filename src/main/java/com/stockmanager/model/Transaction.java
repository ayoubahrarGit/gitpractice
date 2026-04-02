package com.stockmanager.model;

import java.time.LocalDateTime;

/**
 * Represents a stock movement transaction (add, update, delete).
 */
public class Transaction {

    public enum Action {
        ADD, UPDATE, DELETE
    }

    private int id;
    private int productId;
    private String productName;
    private Action action;
    private int quantityChange;
    private LocalDateTime date;

    public Transaction() {
    }

    public Transaction(int id, int productId, String productName,
                       Action action, int quantityChange, LocalDateTime date) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.action = action;
        this.quantityChange = quantityChange;
        this.date = date;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Action getAction() { return action; }
    public void setAction(Action action) { this.action = action; }

    public int getQuantityChange() { return quantityChange; }
    public void setQuantityChange(int quantityChange) { this.quantityChange = quantityChange; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    @Override
    public String toString() {
        return "Transaction{id=" + id + ", productId=" + productId
                + ", action=" + action + ", quantityChange=" + quantityChange
                + ", date=" + date + "}";
    }
}
