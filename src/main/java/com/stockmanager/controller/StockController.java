package com.stockmanager.controller;

import com.stockmanager.App;
import com.stockmanager.dao.ProductDAO;
import com.stockmanager.dao.TransactionDAO;
import com.stockmanager.model.Product;
import com.stockmanager.model.Transaction;
import com.stockmanager.model.User;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Controller for the main stock view.
 */
public class StockController {

    // --- Product table ---
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Integer> colId;
    @FXML private TableColumn<Product, String>  colName;
    @FXML private TableColumn<Product, Integer> colQty;
    @FXML private TableColumn<Product, Double>  colPrice;
    @FXML private TableColumn<Product, String>  colDate;

    // --- Transaction table ---
    @FXML private TableView<Transaction> transactionTable;
    @FXML private TableColumn<Transaction, String> colTxDate;
    @FXML private TableColumn<Transaction, String> colTxProduct;
    @FXML private TableColumn<Transaction, String> colTxAction;
    @FXML private TableColumn<Transaction, Integer> colTxQty;

    // --- Controls ---
    @FXML private TextField searchField;
    @FXML private Button    addButton;
    @FXML private Button    editButton;
    @FXML private Button    deleteButton;
    @FXML private Label     userLabel;
    @FXML private Label     statusLabel;

    private User currentUser;
    private final ProductDAO productDAO         = new ProductDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    private final ObservableList<Product>     productData     = FXCollections.observableArrayList();
    private final ObservableList<Transaction> transactionData = FXCollections.observableArrayList();

    /** Called by App after the FXML is loaded to pass the logged-in user. */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        userLabel.setText("Utilisateur : " + user.getUsername()
                + "  [" + user.getRole().name() + "]");

        boolean isAdmin = user.isAdmin();
        addButton.setDisable(!isAdmin);
        editButton.setDisable(!isAdmin);
        deleteButton.setDisable(!isAdmin);

        loadProducts();
        loadTransactions();
    }

    @FXML
    private void initialize() {
        // Product table columns
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colPrice.setCellValueFactory(d ->
            new SimpleDoubleProperty(d.getValue().getPrice()).asObject());
        colDate.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getDateAdded().toString()));

        productTable.setItems(productData);

        // Transaction table columns
        colTxDate.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getDate().toString().replace("T", " ")));
        colTxProduct.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getProductName()));
        colTxAction.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getAction().name()));
        colTxQty.setCellValueFactory(d ->
            new SimpleIntegerProperty(d.getValue().getQuantityChange()).asObject());

        transactionTable.setItems(transactionData);
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim();
        List<Product> results = keyword.isEmpty()
            ? productDAO.findAll()
            : productDAO.findByName(keyword);
        productData.setAll(results);
    }

    @FXML
    private void handleAdd() {
        Optional<Product> result = ProductFormController.showDialog(null);
        result.ifPresent(p -> {
            productDAO.add(p);
            recordTransaction(p, Transaction.Action.ADD, p.getQuantity());
            loadProducts();
            loadTransactions();
            setStatus("Produit \"" + p.getName() + "\" ajouté.");
        });
    }

    @FXML
    private void handleEdit() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus("Sélectionnez un produit à modifier.");
            return;
        }
        int oldQty = selected.getQuantity();
        Optional<Product> result = ProductFormController.showDialog(selected);
        result.ifPresent(updated -> {
            productDAO.update(updated);
            int diff = updated.getQuantity() - oldQty;
            recordTransaction(updated, Transaction.Action.UPDATE, diff);
            loadProducts();
            loadTransactions();
            setStatus("Produit \"" + updated.getName() + "\" modifié.");
        });
    }

    @FXML
    private void handleDelete() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus("Sélectionnez un produit à supprimer.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Supprimer le produit \"" + selected.getName() + "\" ?",
            ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        Optional<ButtonType> answer = confirm.showAndWait();
        if (answer.isPresent() && answer.get() == ButtonType.YES) {
            recordTransaction(selected, Transaction.Action.DELETE, -selected.getQuantity());
            productDAO.delete(selected.getId());
            loadProducts();
            loadTransactions();
            setStatus("Produit \"" + selected.getName() + "\" supprimé.");
        }
    }

    @FXML
    private void handleLogout() {
        App.getInstance().showLoginView();
    }

    private void loadProducts() {
        productData.setAll(productDAO.findAll());
    }

    private void loadTransactions() {
        transactionData.setAll(transactionDAO.findAll());
    }

    private void recordTransaction(Product product, Transaction.Action action, int qtyChange) {
        Transaction tx = new Transaction();
        tx.setProductId(product.getId());
        tx.setProductName(product.getName());
        tx.setAction(action);
        tx.setQuantityChange(qtyChange);
        tx.setDate(LocalDateTime.now());
        transactionDAO.add(tx);
    }

    private void setStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }
}
