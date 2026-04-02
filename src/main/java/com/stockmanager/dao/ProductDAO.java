package com.stockmanager.dao;

import com.stockmanager.model.Product;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for {@link Product} entities.
 */
public class ProductDAO {

    private final DatabaseManager db;

    public ProductDAO() {
        this.db = DatabaseManager.getInstance();
    }

    ProductDAO(DatabaseManager db) {
        this.db = db;
    }

    /**
     * Returns all products ordered by name.
     */
    public List<Product> findAll() {
        String sql = "SELECT id, name, quantity, price, date_added FROM products ORDER BY name";
        List<Product> products = new ArrayList<>();
        try (Statement stmt = db.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                products.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving products", e);
        }
        return products;
    }

    /**
     * Searches products whose name contains the given keyword (case-insensitive).
     *
     * @param keyword search term
     */
    public List<Product> findByName(String keyword) {
        String sql = "SELECT id, name, quantity, price, date_added FROM products"
                   + " WHERE LOWER(name) LIKE ? ORDER BY name";
        List<Product> products = new ArrayList<>();
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, "%" + keyword.toLowerCase() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error searching products", e);
        }
        return products;
    }

    /**
     * Finds a product by its ID.
     */
    public Optional<Product> findById(int id) {
        String sql = "SELECT id, name, quantity, price, date_added FROM products WHERE id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding product by id", e);
        }
        return Optional.empty();
    }

    /**
     * Inserts a new product and updates its generated ID.
     *
     * @param product the product to insert
     */
    public void add(Product product) {
        String sql = "INSERT INTO products (name, quantity, price, date_added) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, product.getName());
            ps.setInt(2, product.getQuantity());
            ps.setDouble(3, product.getPrice());
            ps.setString(4, product.getDateAdded().toString());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    product.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error adding product", e);
        }
    }

    /**
     * Updates an existing product.
     *
     * @param product the product with updated fields (must have a valid ID)
     */
    public void update(Product product) {
        String sql = "UPDATE products SET name = ?, quantity = ?, price = ?, date_added = ?"
                   + " WHERE id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, product.getName());
            ps.setInt(2, product.getQuantity());
            ps.setDouble(3, product.getPrice());
            ps.setString(4, product.getDateAdded().toString());
            ps.setInt(5, product.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating product", e);
        }
    }

    /**
     * Deletes a product by its ID.
     *
     * @param id the product ID to delete
     */
    public void delete(int id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting product", e);
        }
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        return new Product(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getInt("quantity"),
            rs.getDouble("price"),
            LocalDate.parse(rs.getString("date_added"))
        );
    }
}
