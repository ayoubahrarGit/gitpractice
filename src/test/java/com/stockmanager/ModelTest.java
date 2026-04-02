package com.stockmanager;

import com.stockmanager.model.Product;
import com.stockmanager.model.User;
import com.stockmanager.model.Transaction;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the model classes.
 */
class ModelTest {

    // --- User ---

    @Test
    void user_isAdmin_returnsTrueForAdminRole() {
        User user = new User(1, "admin", "hash", User.Role.ADMIN);
        assertTrue(user.isAdmin());
    }

    @Test
    void user_isAdmin_returnsFalseForUserRole() {
        User user = new User(2, "bob", "hash", User.Role.USER);
        assertFalse(user.isAdmin());
    }

    @Test
    void user_gettersReturnExpectedValues() {
        User user = new User(3, "alice", "hashed", User.Role.USER);
        assertEquals(3, user.getId());
        assertEquals("alice", user.getUsername());
        assertEquals("hashed", user.getPasswordHash());
        assertEquals(User.Role.USER, user.getRole());
    }

    // --- Product ---

    @Test
    void product_gettersReturnExpectedValues() {
        LocalDate today = LocalDate.of(2025, 1, 15);
        Product p = new Product(10, "Engrais NPK", 50, 29.99, today);
        assertEquals(10, p.getId());
        assertEquals("Engrais NPK", p.getName());
        assertEquals(50, p.getQuantity());
        assertEquals(29.99, p.getPrice(), 0.001);
        assertEquals(today, p.getDateAdded());
    }

    @Test
    void product_setters_updateValues() {
        Product p = new Product();
        p.setId(5);
        p.setName("Urée");
        p.setQuantity(100);
        p.setPrice(45.0);
        p.setDateAdded(LocalDate.of(2025, 3, 1));
        assertEquals(5, p.getId());
        assertEquals("Urée", p.getName());
        assertEquals(100, p.getQuantity());
        assertEquals(45.0, p.getPrice(), 0.001);
    }

    // --- Transaction ---

    @Test
    void transaction_gettersReturnExpectedValues() {
        LocalDateTime now = LocalDateTime.of(2025, 6, 1, 10, 30);
        Transaction tx = new Transaction(1, 10, "Engrais", Transaction.Action.ADD, 50, now);
        assertEquals(1, tx.getId());
        assertEquals(10, tx.getProductId());
        assertEquals("Engrais", tx.getProductName());
        assertEquals(Transaction.Action.ADD, tx.getAction());
        assertEquals(50, tx.getQuantityChange());
        assertEquals(now, tx.getDate());
    }
}
