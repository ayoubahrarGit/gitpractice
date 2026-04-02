# Application de Gestion de Stock Locale

Application de bureau légère pour la gestion de stock, construite avec **Java 17**, **JavaFX 17** et **SQLite**.

## Fonctionnalités

- 🔐 **Authentification** avec deux rôles : `ADMIN` et `USER`
- 📦 **Consultation du stock** : liste des produits avec recherche par nom
- ✏️ **Gestion du stock (admin uniquement)** : ajouter, modifier, supprimer des produits
- 📋 **Historique des mouvements** : chaque action est tracée automatiquement
- 💾 **Sauvegarde automatique** : chaque modification est persistée immédiatement dans SQLite

## Comptes par défaut

| Identifiant | Mot de passe | Rôle  |
|-------------|-------------|-------|
| `admin`     | `admin123`  | ADMIN |
| `user`      | `user123`   | USER  |

## Architecture

```
src/main/java/com/stockmanager/
├── App.java                          # Point d'entrée JavaFX
├── model/
│   ├── User.java                     # Modèle utilisateur (rôle ADMIN/USER)
│   ├── Product.java                  # Modèle produit
│   └── Transaction.java              # Modèle mouvement de stock
├── dao/
│   ├── DatabaseManager.java          # Gestion connexion SQLite (Singleton)
│   ├── UserDAO.java                  # Accès données utilisateurs
│   ├── ProductDAO.java               # Accès données produits
│   └── TransactionDAO.java           # Accès données transactions
├── controller/
│   ├── LoginController.java          # Contrôleur écran connexion
│   ├── StockController.java          # Contrôleur vue stock principale
│   └── ProductFormController.java    # Contrôleur formulaire produit
└── util/
    └── PasswordUtil.java             # Hachage BCrypt des mots de passe

src/main/resources/com/stockmanager/
├── login.fxml          # Vue connexion
├── stock.fxml          # Vue principale du stock
└── product_form.fxml   # Formulaire ajout/modification produit
```

## Schéma de la base de données (SQLite)

```sql
users        (id, username, password_hash, role)
products     (id, name, quantity, price, date_added)
transactions (id, product_id, product_name, action, quantity_change, date)
```

## Stack technique

| Composant        | Technologie              |
|------------------|--------------------------|
| Langage          | Java 17                  |
| Interface        | JavaFX 17 + FXML         |
| Base de données  | SQLite (sqlite-jdbc)     |
| Hachage MdP      | BCrypt (jBCrypt)         |
| Tests            | JUnit 5                  |
| Build            | Maven 3                  |

## Prérequis

- Java 17+
- Maven 3.6+

## Compilation et exécution

```bash
# Compiler
mvn compile

# Lancer les tests
mvn test

# Lancer l'application
mvn javafx:run

# Construire un JAR exécutable
mvn package
```

Le fichier de base de données `stock_manager.db` est créé automatiquement dans le répertoire courant au premier lancement.

