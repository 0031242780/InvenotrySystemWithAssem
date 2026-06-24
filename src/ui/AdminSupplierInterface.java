package ui;

import dao.ProductDAO;
import dao.SupplierDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import model.Product;
import model.Supplier;
import java.util.ArrayList;

public class AdminSupplierInterface extends BorderPane {

	private TableView<Supplier> table;

	private TableView<Product> supplierProductsTable;
	private ProductDAO productDAO = new ProductDAO();

	private TableColumn<Supplier, String> companyCol;
	private TableColumn<Supplier, String> contactCol;
	private TableColumn<Supplier, String> emailCol;
	private TableColumn<Supplier, Boolean> activeCol;

	private TextField companyField;
	private TextField contactField;
	private TextField emailField;

	private CheckBox activeBox;

	private Button addBtn;
	private Button updateBtn;
	private Button deleteBtn;
	private Button refreshBtn;
	private Button clearBtn;

	private ComboBox<Product> productComboBox;
	private TextField supplyCostField;
	private Button linkProductBtn;

	private GridPane form;
	private HBox buttons;
	private SupplierDAO dao;

	private ObservableList<Supplier> suppliers = FXCollections.observableArrayList();

	public AdminSupplierInterface() {
		dao = new SupplierDAO();

		setPadding(new Insets(20));

		createFields();
		createButtons();
		createTable();
		createSupplierProductsTable();

		createForm();
		loadSuppliers();
		loadProductsToCombo();
	}

	private void createFields() {
		companyField = new TextField();
		contactField = new TextField();
		emailField = new TextField();
		activeBox = new CheckBox("Active Status");

		productComboBox = new ComboBox<>();
		productComboBox.setPromptText("Select Product");
		productComboBox.setPrefWidth(200);

		supplyCostField = new TextField();
		supplyCostField.setPromptText("Wholesale Cost ($)");
		supplyCostField.setPrefWidth(130);
	}

	private void createButtons() {
		addBtn = new Button("Add");
		updateBtn = new Button("Update");
		deleteBtn = new Button("Delete");
		refreshBtn = new Button("Refresh");
		clearBtn = new Button("Clear");

		addBtn.setPadding(new Insets(10));
		updateBtn.setPadding(new Insets(10));
		deleteBtn.setPadding(new Insets(10));
		refreshBtn.setPadding(new Insets(10));
		clearBtn.setPadding(new Insets(10));

		addBtn.setOnAction(e -> addSupplier());
		updateBtn.setOnAction(e -> updateSupplier());
		deleteBtn.setOnAction(e -> deleteSupplier());
		refreshBtn.setOnAction(e -> {
			loadSuppliers();
			loadProductsToCombo();
		});
		clearBtn.setOnAction(e -> clearFields());

		linkProductBtn = new Button("Link Product to Supplier");
		linkProductBtn.setStyle("-fx-font-weight: bold;");
		linkProductBtn.setOnAction(e -> handleLinkProductToSupplier());
	}

	private void createTable() {
		table = new TableView<>();
		table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

		companyCol = new TableColumn<>("Company");
		companyCol.setCellValueFactory(new PropertyValueFactory<>("companyName"));

		contactCol = new TableColumn<>("Contact");
		contactCol.setCellValueFactory(new PropertyValueFactory<>("contactPerson"));

		emailCol = new TableColumn<>("Email");
		emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

		activeCol = new TableColumn<>("Status");
		activeCol.setCellValueFactory(new PropertyValueFactory<>("active"));

		activeCol.setCellFactory(column -> new TableCell<Supplier, Boolean>() {
			@Override
			protected void updateItem(Boolean item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setText(null);
					setStyle("");
				} else {
					if (item) {
						setText("Active");
					} else {
						setText("Inactive");
					}
				}
			}
		});

		companyCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.25));
		contactCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.25));
		emailCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.25));
		activeCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.15));

		table.getColumns().addAll(companyCol, contactCol, emailCol, activeCol);
		table.setItems(suppliers);

		table.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue != null) {
				fillFields(newValue);
				loadSubTableProducts(newValue.getSupplierId());
			} else {
				supplierProductsTable.getItems().clear();
			}
		});
	}

	private void createSupplierProductsTable() {
		supplierProductsTable = new TableView<>();
		supplierProductsTable.setPlaceholder(new Label("Select a supplier from above to view their provided products and costs."));
		supplierProductsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		supplierProductsTable.setPrefHeight(220);

		TableColumn<Product, String> pNameCol = new TableColumn<>("Supplied Product Name");
		pNameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));

		TableColumn<Product, String> pCatCol = new TableColumn<>("Category");
		pCatCol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));

		TableColumn<Product, String> pBarcodeCol = new TableColumn<>("Barcode");
		pBarcodeCol.setCellValueFactory(new PropertyValueFactory<>("barcode"));

		TableColumn<Product, Double> pCostCol = new TableColumn<>("Cost ($)");
		pCostCol.setCellValueFactory(new PropertyValueFactory<>("cost"));
		pCostCol.setCellFactory(column -> new TableCell<Product, Double>() {
			@Override
			protected void updateItem(Double item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setText(null);
					setStyle("");
				} else {
					setText(String.format("$%.2f", item));
					setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
				}
			}
		});

		supplierProductsTable.getColumns().addAll(pNameCol, pCatCol, pBarcodeCol, pCostCol);
	}

	private void createForm() {
		Label mainTitle = new Label("Suppliers Management");
		mainTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #0B1E3A;");

		form = new GridPane();
		form.setPadding(new Insets(10, 0, 15, 0));
		form.setHgap(10);
		form.setVgap(10);

		form.add(new Label("Company"), 0, 0);
		form.add(companyField, 1, 0);

		form.add(new Label("Contact"), 0, 1);
		form.add(contactField, 1, 1);

		form.add(new Label("Email"), 0, 2);
		form.add(emailField, 1, 2);

		form.add(activeBox, 1, 3);

		buttons = new HBox(10);
		buttons.getChildren().addAll(addBtn, updateBtn, deleteBtn, refreshBtn, clearBtn);
		buttons.setAlignment(Pos.CENTER_LEFT);

		form.add(buttons, 1, 4);

		VBox topContainer = new VBox(5);
		topContainer.getChildren().addAll(mainTitle, form);

		VBox centerLayout = new VBox(15);
		centerLayout.setPadding(new Insets(10, 0, 0, 0));

		Label subTitle = new Label("Products by Selected Supplier");
		subTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0B1E3A;");

		HBox linkingActionStrip = new HBox(10);
		linkingActionStrip.setAlignment(Pos.CENTER_LEFT);
		linkingActionStrip.setPadding(new Insets(5, 10, 10, 10));
		linkingActionStrip.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 5; -fx-border-color: #e5e7eb;");
		linkingActionStrip.getChildren().addAll(
				new Label("Assign New Product:"), productComboBox,
				new Label("Cost:"), supplyCostField,
				linkProductBtn
		);

		centerLayout.getChildren().addAll(
				table,
				subTitle,
				supplierProductsTable,
				linkingActionStrip
		);

		VBox.setVgrow(table, Priority.ALWAYS);
		VBox.setVgrow(supplierProductsTable, Priority.ALWAYS);

		setTop(topContainer);
		setCenter(centerLayout);
	}

	private void loadSubTableProducts(int supplierId) {
		try {
			ArrayList<Product> items = productDAO.getProductsBySupplierId(supplierId);
			supplierProductsTable.setItems(FXCollections.observableArrayList(items));
		} catch (Exception e) {
			e.printStackTrace();
			showAlert(Alert.AlertType.ERROR, "Data Error", "Failed to retrieve vendor items", e.getMessage());
		}
	}

	private void loadProductsToCombo() {
		try {
			ObservableList<Product> products = FXCollections.observableArrayList(productDAO.getAllProducts());
			productComboBox.setItems(products);

			productComboBox.setCellFactory(lv -> new ListCell<>() {
				@Override protected void updateItem(Product p, boolean empty) {
					super.updateItem(p, empty);
					setText((empty || p == null) ? "" : p.getProductName() + " [" + p.getBarcode() + "]");
				}
			});
			productComboBox.setButtonCell(productComboBox.getCellFactory().call(null));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void handleLinkProductToSupplier() {
		Supplier selectedSupplier = table.getSelectionModel().getSelectedItem();
		Product selectedProduct = productComboBox.getValue();
		String costText = supplyCostField.getText().trim();

		if (selectedSupplier == null) {
			showAlert(Alert.AlertType.WARNING, "Selection Required", null, "Please select a Supplier from the top table first!");
			return;
		}

		if (selectedProduct == null || costText.isEmpty()) {
			showAlert(Alert.AlertType.WARNING, "Missing Data", null, "Please select a product and provide a wholesale cost value.");
			return;
		}

		try {
			double wholesaleCost = Double.parseDouble(costText);
			if (wholesaleCost < 0) {
				showAlert(Alert.AlertType.ERROR, "Invalid Amount", null, "Wholesale cost amount criteria cannot be negative.");
				return;
			}


			productDAO.addProductSupplierLink(selectedProduct.getProductId(), selectedSupplier.getSupplierId(), wholesaleCost);

			showAlert(Alert.AlertType.INFORMATION, "Success", null, "Product successfully linked to this supplier!");

			supplyCostField.clear();
			productComboBox.setValue(null);
			loadSubTableProducts(selectedSupplier.getSupplierId());

		} catch (NumberFormatException nfe) {
			showAlert(Alert.AlertType.ERROR, "Format Error", null, "Please enter a valid numeric value for the supply cost.");
		} catch (Exception ex) {
			ex.printStackTrace();
			showAlert(Alert.AlertType.ERROR, "Database Writing Error", null, ex.getMessage());
		}
	}

	private void addSupplier() {
		try {
			Supplier s = new Supplier();
			s.setCompanyName(companyField.getText());
			s.setContactPerson(contactField.getText());
			s.setEmail(emailField.getText());
			s.setActive(activeBox.isSelected());

			dao.insert(s);
			loadSuppliers();
			clearFields();
			showAlert(Alert.AlertType.INFORMATION, "Success", null, "Supplier Added Successfully!");
		} catch (Exception e) {
			showAlert(Alert.AlertType.ERROR, "Error", null, e.getMessage());
		}
	}

	private void updateSupplier() {
		Supplier s = table.getSelectionModel().getSelectedItem();
		if (s == null) {
			showAlert(Alert.AlertType.WARNING, "Warning", null, "Please select a supplier from the table first!");
			return;
		}

		try {
			s.setCompanyName(companyField.getText());
			s.setContactPerson(contactField.getText());
			s.setEmail(emailField.getText());
			s.setActive(activeBox.isSelected());

			dao.update(s);
			loadSuppliers();
			showAlert(Alert.AlertType.INFORMATION, "Success", null, "Supplier Updated Successfully!");
		} catch (Exception e) {
			showAlert(Alert.AlertType.ERROR, "Error", null, e.getMessage());
		}
	}

	private void deleteSupplier() {
		Supplier s = table.getSelectionModel().getSelectedItem();
		if (s == null) {
			showAlert(Alert.AlertType.WARNING, "Warning", null, "Please select a supplier from the table first!");
			return;
		}

		try {
			dao.delete(s.getSupplierId());
			loadSuppliers();
			clearFields();
			showAlert(Alert.AlertType.INFORMATION, "Success", null, "Supplier Deleted Successfully!");
		} catch (Exception e) {
			if (e.getMessage() != null && (e.getMessage().contains("foreign key") || e.getMessage().contains("1451"))) {
				showAlert(Alert.AlertType.ERROR, "Integrity Constraint Error", null,
						"Cannot delete this supplier because they are linked to existing products or stock logs in the system!\n\n"
								+ "This supplier has historical purchase or inventory records.\n\n"
								+ "To preserve data integrity, please delete or re-assign their associated products first, or simply update the supplier's status to inactive (Deactivated) instead of permanent deletion.");
			} else {
				showAlert(Alert.AlertType.ERROR, "Error", null, e.getMessage());
			}
		}
	}

	private void loadSuppliers() {
		try {
			suppliers.clear();
			suppliers.addAll(dao.getAll());
			table.refresh();
			supplierProductsTable.getItems().clear();
		} catch (Exception e) {
			showAlert(Alert.AlertType.ERROR, "Error", null, e.getMessage());
		}
	}

	private void fillFields(Supplier s) {
		companyField.setText(s.getCompanyName());
		contactField.setText(s.getContactPerson());
		emailField.setText(s.getEmail());
		activeBox.setSelected(s.isActive());
	}

	private void clearFields() {
		companyField.clear();
		contactField.clear();
		emailField.clear();
		activeBox.setSelected(false);
		table.getSelectionModel().clearSelection();
		supplierProductsTable.getItems().clear();
		supplyCostField.clear();
		productComboBox.setValue(null);
	}

	private void showAlert(Alert.AlertType type, String title, String header, String content) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.getDialogPane().setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
		alert.showAndWait();
	}
}