package com.stockmanager.model;

import java.time.LocalDate;

/**
 * Represents a product in the stock inventory.
 */
public class Product {

    private int id;
    private String name;
    private int quantity;
    private double price;
    private LocalDate dateAdded;

    public Product() {
    }

    public Product(int id, String name, int quantity, double price, LocalDate dateAdded) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.dateAdded = dateAdded;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public LocalDate getDateAdded() { return dateAdded; }
    public void setDateAdded(LocalDate dateAdded) { this.dateAdded = dateAdded; }

    @Override
    public String toString() {
        return "Product{id=" + id + ", name='" + name + "', quantity=" + quantity
                + ", price=" + price + ", dateAdded=" + dateAdded + "}";
    }
}
