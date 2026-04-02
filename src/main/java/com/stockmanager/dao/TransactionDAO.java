package com.stockmanager.dao;

import com.stockmanager.model.Transaction;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for {@link Transaction} entities.
 */
public class TransactionDAO {

    private final DatabaseManager db;

    public TransactionDAO() {
        this.db = DatabaseManager.getInstance();
    }

    TransactionDAO(DatabaseManager db) {
        this.db = db;
    }

    /**
     * Records a new transaction for a product action.
     *
     * @param transaction the transaction to persist
     */
    public void add(Transaction transaction) {
        String sql =
            "INSERT INTO transactions (product_id, product_name, action, quantity_change, date)"
            + " VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, transaction.getProductId());
            ps.setString(2, transaction.getProductName());
            ps.setString(3, transaction.getAction().name());
            ps.setInt(4, transaction.getQuantityChange());
            ps.setString(5, transaction.getDate().toString());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    transaction.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error recording transaction", e);
        }
    }

    /**
     * Returns all transactions ordered by most recent first.
     */
    public List<Transaction> findAll() {
        String sql =
            "SELECT id, product_id, product_name, action, quantity_change, date"
            + " FROM transactions ORDER BY date DESC";
        List<Transaction> list = new ArrayList<>();
        try (Statement stmt = db.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving transactions", e);
        }
        return list;
    }

    /**
     * Returns transactions for a specific product.
     *
     * @param productId the product ID to filter by
     */
    public List<Transaction> findByProduct(int productId) {
        String sql =
            "SELECT id, product_id, product_name, action, quantity_change, date"
            + " FROM transactions WHERE product_id = ? ORDER BY date DESC";
        List<Transaction> list = new ArrayList<>();
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving product transactions", e);
        }
        return list;
    }

    private Transaction mapRow(ResultSet rs) throws SQLException {
        return new Transaction(
            rs.getInt("id"),
            rs.getInt("product_id"),
            rs.getString("product_name"),
            Transaction.Action.valueOf(rs.getString("action")),
            rs.getInt("quantity_change"),
            LocalDateTime.parse(rs.getString("date"))
        );
    }
}
