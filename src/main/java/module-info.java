module com.stockmanager {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires jbcrypt;

    opens com.stockmanager to javafx.fxml;
    opens com.stockmanager.controller to javafx.fxml;
    opens com.stockmanager.model to javafx.base;

    exports com.stockmanager;
    exports com.stockmanager.model;
    exports com.stockmanager.dao;
    exports com.stockmanager.controller;
    exports com.stockmanager.util;
}
