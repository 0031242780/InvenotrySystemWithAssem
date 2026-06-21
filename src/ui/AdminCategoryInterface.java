package ui;

import dao.DBConnection;
import dao.ProductDAO;
import model.Category;
import model.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class AdminCategoryInterface extends VBox {

	private TableView<Category> categoryTable;
	private ObservableList<Category> categoriesList = FXCollections.observableArrayList();
	private TableView<Product> productTable;
	private ObservableList<Product> productsList = FXCollections.observableArrayList();

	private TextField nameField;
	private TextField descField;

	private Button addBtn;
	private Button updateBtn;
	private Button deleteBtn;
	private Button refreshBtn;
	private Button clearBtn;

	private ProductDAO productDAO;

	public AdminCategoryInterface() {
		productDAO = new ProductDAO();

		setSpacing(15);
		setPadding(new Insets(20));

		Label mainTitle = new Label("Categories Management");
		mainTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #0B1E3A;");

		GridPane form = new GridPane();
		form.setHgap(10);
		form.setVgap(10);

		form.add(new Label("Category Name:"), 0, 0);
		nameField = new TextField();
		nameField.setPrefWidth(250);
		form.add(nameField, 1, 0);

		form.add(new Label("Description:"), 0, 1);
		descField = new TextField();
		descField.setPrefWidth(250);
		form.add(descField, 1, 1);

		HBox buttonsBox = new HBox(12);
		buttonsBox.setAlignment(Pos.CENTER_LEFT);

		addBtn = new Button("Add");
		updateBtn = new Button("Update");
		deleteBtn = new Button("Delete");
		refreshBtn = new Button("Refresh");
		clearBtn = new Button("Clear");

		String commonButtonStyle = "-fx-font-weight: bold;";
		double uniformWidth = 95;

		addBtn.setStyle(commonButtonStyle);
		addBtn.setPrefWidth(uniformWidth);

		updateBtn.setStyle(commonButtonStyle);
		updateBtn.setPrefWidth(uniformWidth);

		deleteBtn.setStyle(commonButtonStyle);
		deleteBtn.setPrefWidth(uniformWidth);

		refreshBtn.setStyle(commonButtonStyle);
		refreshBtn.setPrefWidth(uniformWidth);

		clearBtn.setStyle(commonButtonStyle);
		clearBtn.setPrefWidth(uniformWidth);

		buttonsBox.getChildren().addAll(addBtn, updateBtn, deleteBtn, refreshBtn, clearBtn);

		createCategoryTable();

		Label subTitle = new Label("Products Mapped inside Selected Category");
		subTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0B1E3A;");
		createProductTable();

		getChildren().addAll(mainTitle, form, buttonsBox, categoryTable, subTitle, productTable);

		setupActions();
		loadCategories();
	}

	private void createCategoryTable() {
		categoryTable = new TableView<>();
		categoryTable.setPrefHeight(220);
		categoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		TableColumn<Category, String> nameCol = new TableColumn<>("Category Name");
		nameCol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));

		TableColumn<Category, String> descCol = new TableColumn<>("Description");
		descCol.setCellValueFactory(new PropertyValueFactory<>("description"));

		categoryTable.getColumns().addAll(nameCol, descCol);
		categoryTable.setItems(categoriesList);
	}

	private void createProductTable() {
		productTable = new TableView<>();
		productTable.setPrefHeight(180);
		productTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		TableColumn<Product, String> pNameCol = new TableColumn<>("Product Name");
		pNameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));

		TableColumn<Product, String> pBarcodeCol = new TableColumn<>("Barcode");
		pBarcodeCol.setCellValueFactory(new PropertyValueFactory<>("barcode"));

		TableColumn<Product, Double> pPriceCol = new TableColumn<>("Reg. Price ($)");
		pPriceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

		productTable.getColumns().addAll(pNameCol, pBarcodeCol, pPriceCol);
		productTable.setItems(productsList);
	}

	private void setupActions() {

		categoryTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			if (newSelection != null) {
				nameField.setText(newSelection.getCategoryName());
				descField.setText(newSelection.getDescription());
				loadProductsForCategory(newSelection.getCategoryId());
			} else {
				productsList.clear();
			}
		});

		addBtn.setOnAction(e -> {
			String name = nameField.getText().trim();
			String desc = descField.getText().trim();

			if (name.isEmpty()) {
				showAlert(Alert.AlertType.WARNING, "Validation Warning", "Please enter a Category Name!");
				return;
			}

			String sql = "INSERT INTO category (category_name, descrption) VALUES (?, ?)";
			try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
				ps.setString(1, name);
				ps.setString(2, desc);
				ps.executeUpdate();

				showAlert(Alert.AlertType.INFORMATION, "Success", "New category '" + name + "' added successfully!");
				loadCategories();
				clearFields();
			} catch (Exception ex) {
				showAlert(Alert.AlertType.ERROR, "Database Error", ex.getMessage());
			}
		});

		updateBtn.setOnAction(e -> {
			Category selected = categoryTable.getSelectionModel().getSelectedItem();
			if (selected == null) {
				showAlert(Alert.AlertType.WARNING, "Selection Error",
						"Please select a category from the table to update!");
				return;
			}

			String name = nameField.getText().trim();
			String desc = descField.getText().trim();

			if (name.isEmpty()) {
				showAlert(Alert.AlertType.WARNING, "Validation Warning", "Category Name cannot be empty!");
				return;
			}

			String sql = "UPDATE category SET category_name = ?, descrption = ? WHERE category_id = ?";
			try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
				ps.setString(1, name);
				ps.setString(2, desc);
				ps.setInt(3, selected.getCategoryId());
				ps.executeUpdate();

				showAlert(Alert.AlertType.INFORMATION, "Success", "Category updated successfully!");
				loadCategories();
				clearFields();
			} catch (Exception ex) {
				showAlert(Alert.AlertType.ERROR, "Database Error", ex.getMessage());
			}
		});

		deleteBtn.setOnAction(e -> {
			Category selected = categoryTable.getSelectionModel().getSelectedItem();
			if (selected == null) {
				showAlert(Alert.AlertType.WARNING, "Selection Error",
						"Please select a category from the table to delete!");
				return;
			}

			String sql = "DELETE FROM category WHERE category_id = ?";
			try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
				ps.setInt(1, selected.getCategoryId());
				ps.executeUpdate();

				showAlert(Alert.AlertType.INFORMATION, "Success", "Category deleted successfully!");
				loadCategories();
				clearFields();
			} catch (SQLException ex) {
				if (ex.getErrorCode() == 1451) {
					showAlert(Alert.AlertType.ERROR, "Integrity Constraint Error",
							"Cannot delete this category! It already contains products. Delete or re-map those products first.");
				} else {
					showAlert(Alert.AlertType.ERROR, "Database Error", ex.getMessage());
				}
			} catch (Exception ex) {
				showAlert(Alert.AlertType.ERROR, "Error", ex.getMessage());
			}
		});

		refreshBtn.setOnAction(e -> {
			loadCategories();
			clearFields();
		});

		clearBtn.setOnAction(e -> clearFields());
	}

	private void loadProductsForCategory(int categoryId) {
		try {
			productsList.clear();
			ArrayList<Product> filteredProducts = productDAO.getProductsByCategory(categoryId);
			productsList.addAll(filteredProducts);
			productTable.refresh();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void loadCategories() {
		try {
			categoriesList.clear();
			String sql = "SELECT * FROM category";
			try (Connection con = DBConnection.getConnection();
					PreparedStatement ps = con.prepareStatement(sql);
					ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Category c = new Category();
					c.setCategoryId(rs.getInt("category_id"));
					c.setCategoryName(rs.getString("category_name"));
					c.setDescription(rs.getString("descrption"));
					categoriesList.add(c);
				}
			}
			categoryTable.refresh();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void clearFields() {
		nameField.clear();
		descField.clear();
		categoryTable.getSelectionModel().clearSelection();
		productsList.clear();
	}

	private void showAlert(Alert.AlertType type, String title, String content) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(content);
		alert.showAndWait();
	}
}