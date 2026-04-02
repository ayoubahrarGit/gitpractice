package com.stockmanager.controller;

import com.stockmanager.model.Product;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Controller for the product add/edit dialog.
 */
public class ProductFormController {

    @FXML private TextField nameField;
    @FXML private TextField quantityField;
    @FXML private TextField priceField;
    @FXML private DatePicker dateField;
    @FXML private Label errorLabel;

    private Product product;

    /**
     * Shows the product dialog for adding or editing a product.
     *
     * @param existing the product to edit, or {@code null} to add a new one
     * @return an Optional containing the saved product, or empty if cancelled
     */
    public static Optional<Product> showDialog(Product existing) {
        try {
            FXMLLoader loader = new FXMLLoader(
                ProductFormController.class.getResource("/com/stockmanager/product_form.fxml"));
            DialogPane dialogPane = loader.load();
            ProductFormController controller = loader.getController();

            Dialog<Product> dialog = new Dialog<>();
            dialog.setTitle(existing == null ? "Ajouter un produit" : "Modifier le produit");
            dialog.setDialogPane(dialogPane);
            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    return controller.buildProduct();
                }
                return null;
            });

            if (existing != null) {
                controller.populate(existing);
            }

            // Prevent closing with OK when validation fails
            Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
            okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                if (!controller.validate()) {
                    event.consume();
                }
            });

            return dialog.showAndWait();
        } catch (IOException e) {
            throw new RuntimeException("Could not load product form", e);
        }
    }

    /** Populates form fields from an existing product for editing. */
    private void populate(Product p) {
        this.product = p;
        nameField.setText(p.getName());
        quantityField.setText(String.valueOf(p.getQuantity()));
        priceField.setText(String.valueOf(p.getPrice()));
        dateField.setValue(p.getDateAdded());
    }

    /** Validates the form and shows an error message if invalid. */
    boolean validate() {
        errorLabel.setText("");
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            errorLabel.setText("Le nom du produit est obligatoire.");
            return false;
        }
        try {
            int qty = Integer.parseInt(quantityField.getText().trim());
            if (qty < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            errorLabel.setText("La quantité doit être un entier positif ou nul.");
            return false;
        }
        try {
            double price = Double.parseDouble(priceField.getText().trim());
            if (price < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            errorLabel.setText("Le prix doit être un nombre positif ou nul.");
            return false;
        }
        if (dateField.getValue() == null) {
            errorLabel.setText("La date est obligatoire.");
            return false;
        }
        return true;
    }

    /** Builds a Product from the current form state. */
    private Product buildProduct() {
        Product p = (product != null) ? product : new Product();
        p.setName(nameField.getText().trim());
        p.setQuantity(Integer.parseInt(quantityField.getText().trim()));
        p.setPrice(Double.parseDouble(priceField.getText().trim()));
        p.setDateAdded(dateField.getValue());
        return p;
    }
}
