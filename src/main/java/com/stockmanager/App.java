package com.stockmanager;

import com.stockmanager.controller.StockController;
import com.stockmanager.dao.DatabaseManager;
import com.stockmanager.model.User;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX application entry point for the Local Stock Management application.
 *
 * <p>Default credentials seeded on first launch:
 * <ul>
 *   <li>admin / admin123  (role: ADMIN)</li>
 *   <li>user  / user123   (role: USER)</li>
 * </ul>
 */
public class App extends Application {

    private static App instance;
    private Stage primaryStage;

    public static App getInstance() {
        return instance;
    }

    @Override
    public void start(Stage stage) throws IOException {
        instance = this;
        this.primaryStage = stage;
        primaryStage.setTitle("Gestion de Stock");
        showLoginView();
        primaryStage.show();
    }

    /** Loads and displays the login screen. */
    public void showLoginView() {
        loadView("/com/stockmanager/login.fxml", "Connexion - Gestion de Stock", null);
    }

    /** Loads and displays the stock management screen for the given user. */
    public void showStockView(User user) {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/com/stockmanager/stock.fxml"));
        try {
            Parent root = loader.load();
            StockController controller = loader.getController();
            controller.setCurrentUser(user);
            primaryStage.setScene(new Scene(root, 1000, 680));
            primaryStage.setTitle("Gestion de Stock – " + user.getUsername());
        } catch (IOException e) {
            throw new RuntimeException("Could not load stock view", e);
        }
    }

    @Override
    public void stop() {
        DatabaseManager.getInstance().close();
    }

    private void loadView(String fxmlPath, String title, Object controllerData) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            primaryStage.setScene(new Scene(root, 480, 320));
            primaryStage.setTitle(title);
        } catch (IOException e) {
            throw new RuntimeException("Could not load view: " + fxmlPath, e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
