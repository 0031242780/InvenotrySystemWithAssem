package ui;

import dao.ProductDAO;
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

public class AdminProductInterface extends VBox {

	private TableView<Product> table;
	private TableColumn<Product, Integer> idCol;
	private TableColumn<Product, String> nameCol;
	private TableColumn<Product, String> barcodeCol;
	private TableColumn<Product, String> descCol;
	private TableColumn<Product, Integer> catCol;
	private TableColumn<Product, Integer> supCol;
	private TableColumn<Product, Double> priceCol;
	private TableColumn<Product, Double> discountCol;

	private TextField nameField;
	private TextField barcodeField;
	private TextField descField;
	private TextField catIdField;
	private TextField supIdField;
	private TextField priceField;

	private CheckBox discountCheckBox;
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

		form.add(new Label("Category ID:"), 2, 0);
		catIdField = new TextField();
		form.add(catIdField, 3, 0);

		form.add(new Label("Supplier ID:"), 2, 1);
		supIdField = new TextField();
		form.add(supIdField, 3, 1);

		form.add(new Label("Price ($):"), 2, 2);
		priceField = new TextField();
		form.add(priceField, 3, 2);

		createTable();

		HBox bottomBox = new HBox(15);
		bottomBox.setAlignment(Pos.CENTER_LEFT);

		addBtn = new Button("Add Product");
		editBtn = new Button("Edit Product");
		deleteBtn = new Button("Delete");
		refreshBtn = new Button("Refresh");

		discountCheckBox = new CheckBox("Apply 50% Discount");
		discountCheckBox.setStyle("-fx-font-weight: bold; -fx-text-fill: #2ecc71;");

		applyDiscountBtn = new Button("Save Discount State");

		bottomBox.getChildren().addAll(addBtn, editBtn, deleteBtn, refreshBtn, discountCheckBox, applyDiscountBtn);

		getChildren().addAll(mainTitle, form, table, bottomBox);

		setupActions();
		loadData();
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

		catCol = new TableColumn<>("Category ID");
		catCol.setCellValueFactory(new PropertyValueFactory<>("categoryId"));

		supCol = new TableColumn<>("Supplier ID");
		supCol.setCellValueFactory(new PropertyValueFactory<>("supplierId"));

		priceCol = new TableColumn<>("Reg. Price ($)");
		priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

		discountCol = new TableColumn<>("Disc. Price ($)");
		discountCol.setCellValueFactory(new PropertyValueFactory<>("discountPrice"));

		discountCol.setCellFactory(column -> new TableCell<Product, Double>() {
			@Override
			protected void updateItem(Double item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null || item <= 0) {
					setText("-");
					setStyle("");
				} else {
					setText(String.format("$%.2f", item));
					setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
				}
			}
		});

		idCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.07));
		nameCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.18));
		barcodeCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.13));
		descCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.22));
		catCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.09));
		supCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.09));
		priceCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.11));
		discountCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.11));

		table.getColumns().addAll(idCol, nameCol, barcodeCol, descCol, catCol, supCol, priceCol, discountCol);
		table.setItems(productsList);
		table.setPrefHeight(350);
	}

	private void setupActions() {

		table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			if (newSelection != null) {
				nameField.setText(newSelection.getProductName());
				barcodeField.setText(newSelection.getBarcode());
				descField.setText(newSelection.getDescription());
				catIdField.setText(String.valueOf(newSelection.getCategoryId()));
				supIdField.setText(String.valueOf(newSelection.getSupplierId()));
				priceField.setText(String.valueOf(newSelection.getPrice()));

				try {
					double existingDiscount = newSelection.getDiscountPrice();
					if (existingDiscount > 0) {
						discountCheckBox.setSelected(true);
					} else {
						discountCheckBox.setSelected(false);
					}
				} catch (Exception ex) {
					discountCheckBox.setSelected(false);
				}
			}
		});

		addBtn.setOnAction(e -> {
			try {
				Product p = new Product();
				p.setProductName(nameField.getText());
				p.setBarcode(barcodeField.getText());
				p.setDescription(descField.getText());
				p.setCategoryId(Integer.parseInt(catIdField.getText()));
				p.setSupplierId(Integer.parseInt(supIdField.getText()));
				p.setPrice(Double.parseDouble(priceField.getText()));

				productDAO.insertProduct(p);
				showAlert(Alert.AlertType.INFORMATION, "Success", "Product added successfully!");
				loadData();
				clearFields();
			} catch (Exception ex) {
				showAlert(Alert.AlertType.ERROR, "Error", ex.getMessage());
			}
		});

		editBtn.setOnAction(e -> {
			Product selected = table.getSelectionModel().getSelectedItem();
			if (selected == null) {
				showAlert(Alert.AlertType.WARNING, "Warning", "Please select a product from the table first!");
				return;
			}
			try {
				selected.setProductName(nameField.getText());
				selected.setBarcode(barcodeField.getText());
				selected.setDescription(descField.getText());
				selected.setCategoryId(Integer.parseInt(catIdField.getText()));
				selected.setSupplierId(Integer.parseInt(supIdField.getText()));

				double price = Double.parseDouble(priceField.getText());
				selected.setPrice(price);

				if (discountCheckBox.isSelected()) {
					selected.setDiscountPrice(price * 0.50);
				} else {
					selected.setDiscountPrice(0.0);
				}

				productDAO.updateProduct(selected);
				productDAO.addDiscount(selected.getProductId(), selected.getDiscountPrice());

				showAlert(Alert.AlertType.INFORMATION, "Success", "Product updated successfully!");
				loadData();
				clearFields();
			} catch (Exception ex) {
				showAlert(Alert.AlertType.ERROR, "Error", ex.getMessage());
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
					showAlert(Alert.AlertType.ERROR, "Error", ex.getMessage());
				}
			} else {
				showAlert(Alert.AlertType.WARNING, "Warning", "Please select a product from the table first!");
			}
		});

		refreshBtn.setOnAction(e -> loadData());

		applyDiscountBtn.setOnAction(e -> {
			Product selectedProduct = table.getSelectionModel().getSelectedItem();

			if (selectedProduct == null) {
				showAlert(Alert.AlertType.WARNING, "Warning", "Please select a product from the table first!");
				return;
			}

			try {
				double calculatedDiscount = 0;
				String message = "";

				if (discountCheckBox.isSelected()) {
					calculatedDiscount = selectedProduct.getPrice() * 0.50;
					message = "50% Discount applied successfully! New Price: $"
							+ String.format("%.2f", calculatedDiscount);
				} else {
					calculatedDiscount = 0;
					message = "Discount removed successfully! Product returned to original price.";
				}

				productDAO.addDiscount(selectedProduct.getProductId(), calculatedDiscount);

				showAlert(Alert.AlertType.INFORMATION, "Success", message);
				loadData();

			} catch (Exception ex) {
				showAlert(Alert.AlertType.ERROR, "Database Error", ex.getMessage());
			}
		});
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
		catIdField.clear();
		supIdField.clear();
		priceField.clear();
		discountCheckBox.setSelected(false);
		table.getSelectionModel().clearSelection();
	}

	private void showAlert(Alert.AlertType type, String title, String content) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(content);
		alert.showAndWait();
	}
}