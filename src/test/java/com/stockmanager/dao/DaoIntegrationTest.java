package com.stockmanager.dao;

import com.stockmanager.dao.DatabaseManager;
import com.stockmanager.dao.ProductDAO;
import com.stockmanager.dao.TransactionDAO;
import com.stockmanager.dao.UserDAO;
import com.stockmanager.model.Product;
import com.stockmanager.model.Transaction;
import com.stockmanager.model.User;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for DAO classes using an in-memory SQLite database.
 */
class DaoIntegrationTest {

    private static Connection connection;
    private static DatabaseManager dbManager;
    private ProductDAO productDAO;
    private UserDAO userDAO;
    private TransactionDAO transactionDAO;

    @BeforeAll
    static void setUpDatabase() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        connection.createStatement().execute("PRAGMA foreign_keys = ON");

        // Create schema manually (mirrors DatabaseManager)
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS users ("
                + "  id            INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "  username      TEXT    NOT NULL UNIQUE,"
                + "  password_hash TEXT    NOT NULL,"
                + "  role          TEXT    NOT NULL"
                + ")"
            );
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS products ("
                + "  id         INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "  name       TEXT    NOT NULL,"
                + "  quantity   INTEGER NOT NULL DEFAULT 0,"
                + "  price      REAL    NOT NULL DEFAULT 0.0,"
                + "  date_added TEXT    NOT NULL"
                + ")"
            );
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS transactions ("
                + "  id              INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "  product_id      INTEGER NOT NULL,"
                + "  product_name    TEXT    NOT NULL,"
                + "  action          TEXT    NOT NULL,"
                + "  quantity_change INTEGER NOT NULL DEFAULT 0,"
                + "  date            TEXT    NOT NULL"
                + ")"
            );
        }

        // Wrap connection in a test-friendly DatabaseManager using the factory method
        dbManager = DatabaseManager.forTesting(connection);
    }

    @AfterAll
    static void tearDownDatabase() throws SQLException {
        if (connection != null) connection.close();
    }

    @BeforeEach
    void setUp() throws SQLException {
        // Clear tables before each test
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("DELETE FROM transactions");
            stmt.executeUpdate("DELETE FROM products");
            stmt.executeUpdate("DELETE FROM users");
        }
        productDAO     = new ProductDAO(dbManager);
        userDAO        = new UserDAO(dbManager);
        transactionDAO = new TransactionDAO(dbManager);
    }

    // ---- ProductDAO tests ----

    @Test
    void addProduct_assignsGeneratedId() {
        Product p = new Product(0, "Engrais NPK", 100, 25.0, LocalDate.of(2025, 1, 1));
        productDAO.add(p);
        assertTrue(p.getId() > 0);
    }

    @Test
    void findAll_returnsAllProducts() {
        productDAO.add(new Product(0, "Produit A", 10, 5.0, LocalDate.now()));
        productDAO.add(new Product(0, "Produit B", 20, 8.0, LocalDate.now()));
        assertEquals(2, productDAO.findAll().size());
    }

    @Test
    void findByName_filtersCaseInsensitively() {
        productDAO.add(new Product(0, "Engrais NPK", 50, 20.0, LocalDate.now()));
        productDAO.add(new Product(0, "Potasse",     30, 15.0, LocalDate.now()));
        List<Product> results = productDAO.findByName("engrais");
        assertEquals(1, results.size());
        assertEquals("Engrais NPK", results.get(0).getName());
    }

    @Test
    void updateProduct_persistsChanges() {
        Product p = new Product(0, "Urée", 80, 18.0, LocalDate.now());
        productDAO.add(p);
        p.setQuantity(120);
        p.setPrice(22.5);
        productDAO.update(p);
        Optional<Product> found = productDAO.findById(p.getId());
        assertTrue(found.isPresent());
        assertEquals(120, found.get().getQuantity());
        assertEquals(22.5, found.get().getPrice(), 0.001);
    }

    @Test
    void deleteProduct_removesFromDatabase() {
        Product p = new Product(0, "Test", 10, 1.0, LocalDate.now());
        productDAO.add(p);
        productDAO.delete(p.getId());
        assertTrue(productDAO.findById(p.getId()).isEmpty());
    }

    // ---- UserDAO tests ----

    @Test
    void saveUser_thenFindByUsername() {
        User u = new User(0, "alice", "$2a$hash", User.Role.USER);
        userDAO.save(u);
        assertTrue(u.getId() > 0);
        Optional<User> found = userDAO.findByUsername("alice");
        assertTrue(found.isPresent());
        assertEquals(User.Role.USER, found.get().getRole());
    }

    @Test
    void findByUsername_returnsEmptyWhenNotFound() {
        Optional<User> found = userDAO.findByUsername("nobody");
        assertTrue(found.isEmpty());
    }

    // ---- TransactionDAO tests ----

    @Test
    void addTransaction_persistsAndAssignsId() {
        // Add a dummy product first (foreign key)
        Product p = new Product(0, "Produit TX", 10, 1.0, LocalDate.now());
        productDAO.add(p);

        Transaction tx = new Transaction(0, p.getId(), p.getName(),
            Transaction.Action.ADD, 10, LocalDateTime.now());
        transactionDAO.add(tx);
        assertTrue(tx.getId() > 0);
    }

    @Test
    void findAll_returnsTransactionsMostRecentFirst() {
        Product p = new Product(0, "Produit TX", 50, 5.0, LocalDate.now());
        productDAO.add(p);

        LocalDateTime t1 = LocalDateTime.of(2025, 1, 1, 8, 0);
        LocalDateTime t2 = LocalDateTime.of(2025, 1, 2, 9, 0);

        Transaction tx1 = new Transaction(0, p.getId(), p.getName(),
            Transaction.Action.ADD, 50, t1);
        Transaction tx2 = new Transaction(0, p.getId(), p.getName(),
            Transaction.Action.UPDATE, 10, t2);
        transactionDAO.add(tx1);
        transactionDAO.add(tx2);

        List<Transaction> all = transactionDAO.findAll();
        assertEquals(2, all.size());
        // Most recent first
        assertTrue(all.get(0).getDate().isAfter(all.get(1).getDate()));
    }
}
