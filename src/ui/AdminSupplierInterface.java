package ui;

import dao.SupplierDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import model.Supplier;

public class AdminSupplierInterface extends BorderPane {

	private TableView<Supplier> table;

	private TableColumn<Supplier, Integer> idCol;
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

	private GridPane form;

	private HBox buttons;

	private SupplierDAO dao;

	private ObservableList<Supplier> suppliers = FXCollections.observableArrayList();

	public AdminSupplierInterface() {

		dao = new SupplierDAO();

		// إضافة حواف داخلية مريحة حول الشاشة بأكملها لتناسق المظهر
		setPadding(new Insets(20));

		createFields();

		createButtons();

		createTable();

		createForm();

		loadSuppliers();
	}

	private void createFields() {

		companyField = new TextField();

		contactField = new TextField();

		emailField = new TextField();

		activeBox = new CheckBox("Active Status");
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
		refreshBtn.setOnAction(e -> loadSuppliers());
		clearBtn.setOnAction(e -> clearFields());
	}

	private void createTable() {

		table = new TableView<>();

		// استخدام السياسة الحرة لمنع حدوث مشاكل في شريط التمرير أو ظهور عمود رمادي زائد
		table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

		idCol = new TableColumn<>("ID");
		idCol.setCellValueFactory(new PropertyValueFactory<>("supplierId"));

		companyCol = new TableColumn<>("Company");
		companyCol.setCellValueFactory(new PropertyValueFactory<>("companyName"));

		contactCol = new TableColumn<>("Contact");
		contactCol.setCellValueFactory(new PropertyValueFactory<>("contactPerson"));

		emailCol = new TableColumn<>("Email");
		emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

		activeCol = new TableColumn<>("Status");
		activeCol.setCellValueFactory(new PropertyValueFactory<>("active"));

		// 🔥 تلوين وتنسيق حالة المورد بشكل فخم (أخضر للنشط وأحمر للمتوقف)
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

		// 🔥 توزيع المساحات هندسياً بالتساوي (المجموع 100%) مع خصم الـ 2 بكسل السحرية
		// لإحكام الأبعاد
		idCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.10)); // 10%
		companyCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.25)); // 25%
		contactCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.25)); // 25%
		emailCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.25)); // 25%
		activeCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.15)); // 15%

		table.getColumns().addAll(idCol, companyCol, contactCol, emailCol, activeCol);

		table.setItems(suppliers);

		table.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue != null)
				fillFields(newValue);
		});
	}

	private void createForm() {

		// 1. 🔥 إضافة العنوان العلوي للشاشة وتنسيقه بلون النظام الموحد
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

		form.add(activeBox, 1, 3); // تعديل السطر ليكون متناسقاً فورياً

		buttons = new HBox(10);
		buttons.getChildren().addAll(addBtn, updateBtn, deleteBtn, refreshBtn, clearBtn);
		buttons.setAlignment(Pos.CENTER_LEFT); // محاذاة لليسار لراحة بصرية أفضل

		form.add(buttons, 1, 4);

		// 2. 🔥 تجميع العنوان مع الفورم في حاوية VBox وتثبيتها في أعلى الـ BorderPane
		VBox topContainer = new VBox(5);
		topContainer.getChildren().addAll(mainTitle, form);

		setTop(topContainer);
		setCenter(table);
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
			showAlert(Alert.AlertType.ERROR, "Error", null, e.getMessage());
		}
	}

	private void loadSuppliers() {
		try {
			suppliers.clear();
			suppliers.addAll(dao.getAll());
			table.refresh();
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
	}

	private void showAlert(Alert.AlertType type, String title, String header, String content) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.showAndWait();
	}
}