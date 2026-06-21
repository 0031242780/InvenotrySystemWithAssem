package ui;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import dao.DBConnection;
import dao.ProductDAO;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.Product;
import model.Category;

public class AdminProductInterface extends VBox {

	private TableView<Product> table;
	private TableColumn<Product, Integer> idCol;
	private TableColumn<Product, String> nameCol;
	private TableColumn<Product, String> barcodeCol;
	private TableColumn<Product, String> descCol;
	private TableColumn<Product, String> catCol;

	private TableColumn<Product, Double> costCol;
	private TableColumn<Product, Double> priceCol;
	private TableColumn<Product, Double> discountCol;
	private TableColumn<Product, Double> percentCol;

	private TextField nameField;
	private TextField barcodeField;
	private TextField descField;
	private TextField priceField;
	private TextField costField;

	private ComboBox<Category> categoryComboBox;

	private TextField discountPercentField;
	private Button applyDiscountBtn;

	private Button addBtn;
	private Button editBtn;
	private Button deleteBtn;
	private Button refreshBtn;

	private ProductDAO productDAO;
	private ObservableList<Product> productsList = FXCollections.observableArrayList();

	public AdminProductInterface() {
		productDAO = new ProductDAO();

		setSpacing(15);
		setPadding(new Insets(20));

		Label mainTitle = new Label("Products Management");
		mainTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #0B1E3A;");

		GridPane form = new GridPane();
		form.setHgap(10);
		form.setVgap(10);

		form.add(new Label("Product Name:"), 0, 0);
		nameField = new TextField();
		form.add(nameField, 1, 0);

		form.add(new Label("Barcode:"), 0, 1);
		barcodeField = new TextField();
		form.add(barcodeField, 1, 1);

		form.add(new Label("Description:"), 0, 2);
		descField = new TextField();
		form.add(descField, 1, 2);

		form.add(new Label("Category:"), 2, 0);
		categoryComboBox = new ComboBox<>();
		categoryComboBox.setPromptText("Select Category");
		categoryComboBox.setPrefWidth(150);
		form.add(categoryComboBox, 3, 0);

		form.add(new Label("Price ($):"), 2, 1);
		priceField = new TextField();
		form.add(priceField, 3, 1);

		form.add(new Label("Cost ($):"), 0, 3);
		costField = new TextField();
		form.add(costField, 1, 3);

		createTable();

		HBox bottomBox = new HBox(15);
		bottomBox.setAlignment(Pos.CENTER_LEFT);

		addBtn = new Button("Add Product");
		editBtn = new Button("Edit Product");
		deleteBtn = new Button("Delete");
		refreshBtn = new Button("Refresh");

		discountPercentField = new TextField();
		discountPercentField.setPromptText("Discount % (e.g. 20)");
		discountPercentField.setPrefWidth(140);

		applyDiscountBtn = new Button("Save Discount State");

		bottomBox.getChildren().addAll(addBtn, editBtn, deleteBtn, refreshBtn, discountPercentField, applyDiscountBtn);

		getChildren().addAll(mainTitle, form, table, bottomBox);

		setupActions();

		loadData();
		loadCategoriesToCombo();
	}

	private void createTable() {
		table = new TableView<>();
		table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

		idCol = new TableColumn<>("ID");
		idCol.setCellValueFactory(new PropertyValueFactory<>("productId"));

		nameCol = new TableColumn<>("Product Name");
		nameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));

		barcodeCol = new TableColumn<>("Barcode");
		barcodeCol.setCellValueFactory(new PropertyValueFactory<>("barcode"));

		descCol = new TableColumn<>("Description");
		descCol.setCellValueFactory(new PropertyValueFactory<>("description"));

		catCol = new TableColumn<>("Category");
		catCol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));

		costCol = new TableColumn<>("Cost ($)");
		costCol.setCellValueFactory(new PropertyValueFactory<>("cost"));

		priceCol = new TableColumn<>("Reg. Price ($)");
		priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

		discountCol = new TableColumn<>("Final Price ($)");
		discountCol.setCellValueFactory(new PropertyValueFactory<>("discountPrice"));

		discountCol.setCellFactory(column -> new TableCell<Product, Double>() {
			@Override
			protected void updateItem(Double item, boolean empty) {
				super.updateItem(item, empty);
				if (empty) {
					setText(null);
					setStyle("");
					return;
				}
				Product p = (Product) getTableRow().getItem();
				if (p == null) {
					setText(null);
					setStyle("");
				} else if (item == null || item <= 0) {
					setText(String.format("$%.2f", p.getPrice()));
					setStyle("");
				} else {
					double finalPrice = p.getPrice() - item;
					setText(String.format("$%.2f", finalPrice));
					setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
				}
			}
		});

		percentCol = new TableColumn<>("Discount %");
		percentCol.setCellValueFactory(cellData -> {
			Product p = cellData.getValue();
			if (p != null && p.getDiscountPrice() > 0 && p.getPrice() > 0) {
				double percent = (p.getDiscountPrice() / p.getPrice()) * 100;
				return new SimpleObjectProperty<>(percent);
			}
			return new SimpleObjectProperty<>(0.0);
		});

		percentCol.setCellFactory(column -> new TableCell<Product, Double>() {
			@Override
			protected void updateItem(Double item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null || item <= 0) {
					setText("-");
					setStyle("");
				} else {
					setText(String.format("%.0f%%", item));
					setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
				}
			}
		});

		idCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.04));
		nameCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.18));
		barcodeCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.12));
		descCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.18));
		catCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.12));
		costCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.09));
		priceCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.09));
		discountCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.09));
		percentCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.09));

		table.getColumns().addAll(idCol, nameCol, barcodeCol, descCol, catCol, costCol, priceCol, discountCol,
				percentCol);
		table.setItems(productsList);
		table.setPrefHeight(350);
	}

	private void setupActions() {
		table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			if (newSelection != null) {
				nameField.setText(newSelection.getProductName());
				barcodeField.setText(newSelection.getBarcode());
				descField.setText(newSelection.getDescription());
				priceField.setText(String.valueOf(newSelection.getPrice()));
				costField.setText(String.valueOf(newSelection.getCost()));

				for (Category c : categoryComboBox.getItems()) {
					if (c.getCategoryId() == newSelection.getCategoryId()) {
						categoryComboBox.setValue(c);
						break;
					}
				}

				double originalPrice = newSelection.getPrice();
				double currentDiscountAmount = newSelection.getDiscountPrice();
				if (currentDiscountAmount > 0 && originalPrice > 0) {
					double percent = (currentDiscountAmount / originalPrice) * 100;
					discountPercentField.setText(String.format("%.0f", percent));
				} else {
					discountPercentField.clear();
				}
			}
		});

		addBtn.setOnAction(e -> {
			try {
				Category selectedCat = categoryComboBox.getValue();

				if (selectedCat == null) {
					showAlert(Alert.AlertType.WARNING, "Warning", "Please select Category from the list!");
					return;
				}

				Product p = new Product();
				p.setProductName(nameField.getText());
				p.setBarcode(barcodeField.getText());
				p.setDescription(descField.getText());
				p.setCategoryId(selectedCat.getCategoryId());
				p.setPrice(Double.parseDouble(priceField.getText()));
				p.setCost(Double.parseDouble(costField.getText()));

				productDAO.insertProduct(p);
				showAlert(Alert.AlertType.INFORMATION, "Success", "Product added successfully!");
				loadData();
				clearFields();
			} catch (Exception ex) {
				showExceptionAlert("Add Product Transaction Failed", ex);
			}
		});

		editBtn.setOnAction(e -> {
			Product selected = table.getSelectionModel().getSelectedItem();
			if (selected == null) {
				showAlert(Alert.AlertType.WARNING, "Warning", "Please select a product from the table first!");
				return;
			}
			try {
				Category selectedCat = categoryComboBox.getValue();

				if (selectedCat == null) {
					showAlert(Alert.AlertType.WARNING, "Warning", "Please select Category from the list!");
					return;
				}

				selected.setProductName(nameField.getText());
				selected.setBarcode(barcodeField.getText());
				selected.setDescription(descField.getText());
				selected.setCategoryId(selectedCat.getCategoryId());

				double price = Double.parseDouble(priceField.getText());
				selected.setPrice(price);
				selected.setCost(Double.parseDouble(costField.getText()));

				productDAO.updateProduct(selected);

				String percentText = discountPercentField.getText().trim();
				if (!percentText.isEmpty()) {
					double percent = Double.parseDouble(percentText);
					double discAmount = price * (percent / 100.0);
					selected.setDiscountPrice(discAmount);

					java.sql.Timestamp startDate = new java.sql.Timestamp(System.currentTimeMillis());
					java.sql.Timestamp endDate = new java.sql.Timestamp(System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000));

					productDAO.addDiscount(selected.getProductId(), startDate, endDate, discAmount);
				} else {
					selected.setDiscountPrice(0.0);
					java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
					productDAO.addDiscount(selected.getProductId(), now, now, 0.0);
				}

				showAlert(Alert.AlertType.INFORMATION, "Success", "Product updated successfully!");
				loadData();
				clearFields();
			} catch (Exception ex) {
				showExceptionAlert("Update Product Transaction Failed", ex);
			}
		});

		deleteBtn.setOnAction(e -> {
			Product selected = table.getSelectionModel().getSelectedItem();
			if (selected != null) {
				try {
					productDAO.deleteProduct(selected.getProductId());
					showAlert(Alert.AlertType.INFORMATION, "Success", "Product deleted successfully!");
					loadData();
					clearFields();
				} catch (Exception ex) {
					if (ex.getMessage() != null
							&& (ex.getMessage().contains("foreign key") || ex.getMessage().contains("1451"))) {
						showAlert(Alert.AlertType.ERROR, "Integrity Constraint Error",
								"Cannot delete this product because it is linked to existing transactions or orders in the system!\n\n"
										+ "This item has historical records in customer invoices (order_item) or stock movements.\n\n"
										+ "To preserve data integrity, please delete the associated orders first, or consider updating its stock quantity to zero instead of permanent deletion.");
					} else {
						showExceptionAlert("Delete Operation Failed", ex);
					}
				}
			} else {
				showAlert(Alert.AlertType.WARNING, "Warning", "Please select a product from the table first!");
			}
		});

		refreshBtn.setOnAction(e -> {
			loadData();
			loadCategoriesToCombo();
		});

		applyDiscountBtn.setOnAction(e -> {
			Product selectedProduct = table.getSelectionModel().getSelectedItem();

			if (selectedProduct == null) {
				showAlert(Alert.AlertType.WARNING, "Warning", "Please select a product from the table first!");
				return;
			}

			try {
				String percentText = discountPercentField.getText().trim();
				double calculatedDiscountAmount = 0;
				String message = "";

				java.sql.Timestamp startDate = new java.sql.Timestamp(System.currentTimeMillis());
				java.sql.Timestamp endDate;

				if (!percentText.isEmpty()) {
					double percent = Double.parseDouble(percentText);

					if (percent < 0 || percent > 100) {
						showAlert(Alert.AlertType.ERROR, "Input Error", "Percentage must be between 0 and 100!");
						return;
					}

					calculatedDiscountAmount = selectedProduct.getPrice() * (percent / 100.0);
					double finalPrice = selectedProduct.getPrice() - calculatedDiscountAmount;

					endDate = new java.sql.Timestamp(System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000));

					message = percent + "% Discount applied successfully! New Price: $"
							+ String.format("%.2f", finalPrice) + "\nActive until: " + endDate;
				} else {
					calculatedDiscountAmount = 0;
					endDate = startDate;
					message = "Discount removed successfully! Product returned to original price.";
				}


				productDAO.addDiscount(selectedProduct.getProductId(), startDate, endDate, calculatedDiscountAmount);

				showAlert(Alert.AlertType.INFORMATION, "Success", message);
				loadData();

			} catch (NumberFormatException nfe) {
				showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter a valid number for discount percentage!");
			} catch (Exception ex) {
				showExceptionAlert("Discount Process Failed", ex);
			}
		});
	}

	private void loadCategoriesToCombo() {
		ObservableList<Category> options = FXCollections.observableArrayList();
		String sql = "SELECT category_id, category_name FROM category";
		try (Connection con = DBConnection.getConnection();
				PreparedStatement ps = con.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				Category c = new Category();
				c.setCategoryId(rs.getInt("category_id"));
				c.setCategoryName(rs.getString("category_name"));
				options.add(c);
			}
			categoryComboBox.setItems(options);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void loadData() {
		try {
			productsList.clear();
			productsList.addAll(productDAO.getAllProducts());
			table.refresh();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void clearFields() {
		nameField.clear();
		barcodeField.clear();
		descField.clear();
		priceField.clear();
		costField.clear();
		categoryComboBox.setValue(null);
		discountPercentField.clear();
		table.getSelectionModel().clearSelection();
	}

	private void showAlert(Alert.AlertType type, String title, String content) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(content);
		alert.getDialogPane().setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
		alert.showAndWait();
	}

	private void showExceptionAlert(String title, Throwable ex) {
		ex.printStackTrace();

		StringBuilder sb = new StringBuilder();
		sb.append("An exception occurred in the system:\n\n");
		sb.append("Error Type: ").append(ex.getClass().getSimpleName()).append("\n");
		sb.append("Details: ").append(ex.getMessage() != null ? ex.getMessage() : "No additional data.").append("\n");

		if (ex.getCause() != null) {
			sb.append("Root Cause: ")
					.append(ex.getCause().getMessage() != null ? ex.getCause().getMessage() : ex.getCause().toString())
					.append("\n");
		}

		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText("Exception Diagnostic Context");
		alert.setContentText(sb.toString());

		alert.getDialogPane().setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
		alert.showAndWait();
	}
}