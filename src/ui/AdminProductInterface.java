package ui;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import dao.DBConnection;
import dao.DiscountDAO;
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
import javafx.scene.layout.Priority;
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
	private TableColumn<Product, Integer> quantityCol;

	private TableColumn<Product, Double> costCol;
	private TableColumn<Product, Double> priceCol;
	private TableColumn<Product, Double> discountCol;
	private TableColumn<Product, Double> percentCol;

	private TextField nameField;
	private TextField barcodeField;
	private TextField descField;

	private ComboBox<Category> categoryComboBox;

	private Button addBtn;
	private Button editBtn;
	private Button deleteBtn;
	private Button refreshBtn;

	private TextField resupplyField;
	private Button resupplyBtn;

	private ProductDAO productDAO;
	private TextField discountField;
	private Button discountBtn;
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

		form.add(new Label("Category:"), 0, 3);
		categoryComboBox = new ComboBox<>();
		categoryComboBox.setPromptText("Select Category");
		categoryComboBox.setPrefWidth(150);
		form.add(categoryComboBox, 1, 3);

		createTable();

		HBox bottomBox = new HBox(15);
		bottomBox.setAlignment(Pos.CENTER_LEFT);

		addBtn = new Button("Add Product");
		editBtn = new Button("Edit Product");
		deleteBtn = new Button("Delete");
		refreshBtn = new Button("Refresh");

		resupplyField = new TextField();
		resupplyField.setPromptText("Qty (e.g. 50)");
		resupplyField.setPrefWidth(100);
		resupplyBtn = new Button("Resupply");
		discountField = new TextField();
		discountField.setPromptText("Discount Price");
		discountField.setPrefWidth(100);

		discountBtn = new Button("Add Discount");

		bottomBox.getChildren().addAll(addBtn, editBtn, deleteBtn, refreshBtn, resupplyField, resupplyBtn,
				discountField, discountBtn);
		getChildren().addAll(mainTitle, form, table, bottomBox);
		VBox.setVgrow(table, Priority.ALWAYS);

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

		quantityCol = new TableColumn<>("Stock Qty");
		quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
		quantityCol.setCellFactory(column -> new TableCell<Product, Integer>() {
			@Override
			protected void updateItem(Integer item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setText("-");
					setStyle("");
				} else {
					setText(String.valueOf(item));
					if (item <= 5) {
						setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
					} else {
						setStyle("");
					}
				}
			}
		});

		costCol = new TableColumn<>("Cost ($)");
		costCol.setCellValueFactory(new PropertyValueFactory<>("cost"));
		costCol.setCellFactory(column -> new TableCell<Product, Double>() {
			@Override
			protected void updateItem(Double item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setText("-");
				} else {
					setText(String.format("$%.2f", item));
				}
			}
		});

		priceCol = new TableColumn<>("Reg. Price ($)");
		priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
		priceCol.setCellFactory(column -> new TableCell<Product, Double>() {
			@Override
			protected void updateItem(Double item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setText("-");
				} else {
					setText(String.format("$%.2f", item));
				}
			}
		});

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
		nameCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.14));
		barcodeCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.11));
		descCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.14));
		catCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.10));
		quantityCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.08));
		costCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.08));
		priceCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.08));
		discountCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.09));
		percentCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.06));

		table.getColumns().addAll(idCol, nameCol, barcodeCol, descCol, catCol, quantityCol, costCol, priceCol,
				discountCol, percentCol);
		table.setItems(productsList);
		table.setPrefHeight(350);
	}

	private void setupActions() {
		table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			if (newSelection != null) {
				nameField.setText(newSelection.getProductName());
				barcodeField.setText(newSelection.getBarcode());
				descField.setText(newSelection.getDescription());
				discountBtn.setOnAction(e -> handleDiscount());
				for (Category c : categoryComboBox.getItems()) {
					if (c.getCategoryId() == newSelection.getCategoryId()) {
						categoryComboBox.setValue(c);
						break;
					}
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

				productDAO.updateProduct(selected);

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

		resupplyBtn.setOnAction(e -> handleResupply());
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
		categoryComboBox.setValue(null);
		resupplyField.clear();
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

	private void handleResupply() {
		Product selected = table.getSelectionModel().getSelectedItem();
		if (selected == null) {
			showAlert(Alert.AlertType.WARNING, "Warning", "Please select a product from the table first!");
			return;
		}

		if (selected.getCost() <= 0) {
			showAlert(Alert.AlertType.WARNING, "Supply Line Restriction",
					"This product cannot be resupplied because it is not linked to an active supplier source. Please map this product to a supplier vendor first.");
			return;
		}

		String amountText = resupplyField.getText().trim();
		if (amountText.isEmpty()) {
			showAlert(Alert.AlertType.WARNING, "Warning", "Please enter a quantity to resupply.");
			return;
		}

		try {
			int addedQuantity = Integer.parseInt(amountText);
			if (addedQuantity <= 0) {
				showAlert(Alert.AlertType.ERROR, "Error", "Quantity must be greater than zero.");
				return;
			}

			productDAO.updateStock(selected.getProductId(), addedQuantity);

			dao.PaymentDAO paymentDAO = new dao.PaymentDAO();
			paymentDAO.insertWholesaleExpense(selected.getProductId(), selected.getCost(), addedQuantity);

			showAlert(Alert.AlertType.INFORMATION, "Success",
					"Stock resupplied and wholesale payment ledger entry logged successfully!");
			resupplyField.clear();
			loadData();
		} catch (NumberFormatException ex) {
			showAlert(Alert.AlertType.ERROR, "Error", "Please enter a valid whole number for quantity.");
		} catch (Exception ex) {
			showExceptionAlert("Resupply Action Failed", ex);
		}
	}

	private void handleDiscount() {

		Product selected = table.getSelectionModel().getSelectedItem();

		if (selected == null) {
			showAlert(Alert.AlertType.WARNING, "Warning", "Please select a product first!");
			return;
		}

		String value = discountField.getText().trim();

		if (value.isEmpty()) {
			showAlert(Alert.AlertType.WARNING, "Warning", "Enter discount price");
			return;
		}

		try {

			double discountPrice = Double.parseDouble(value);

			if (discountPrice <= 0 || discountPrice >= selected.getPrice()) {
				showAlert(Alert.AlertType.ERROR, "Error", "Discount price must be lower than regular price");
				return;
			}

			DiscountDAO dao = new DiscountDAO();

			dao.addDiscount(selected.getProductId(), discountPrice);

			showAlert(Alert.AlertType.INFORMATION, "Success", "Discount added successfully!");

			discountField.clear();
			loadData();

		} catch (NumberFormatException ex) {

			showAlert(Alert.AlertType.ERROR, "Error", "Enter a valid number");

		} catch (Exception ex) {
			showExceptionAlert("Discount Failed", ex);
		}
	}
}