package ui;

import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import model.DeliveryCompany;
import dao.DeliveryCompanyDAO;

public class AdminDeliveryCompanyInterface extends VBox {

	private TableView<DeliveryCompany> table;
	private TextField companyField, contactField, phoneField;
	private CheckBox activeCheckBox;
	private DeliveryCompanyDAO dao;

	private Button addBtn;
	private Button updateBtn;
	private Button deleteBtn;
	private Button refreshBtn;

	public AdminDeliveryCompanyInterface() {
		dao = new DeliveryCompanyDAO();

		setSpacing(15);
		setPadding(new Insets(20));

		Label mainTitle = new Label("Delivery Companies Management");
		mainTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #0B1E3A;");

		HBox contentRow = new HBox(20);
		VBox.setVgrow(contentRow, Priority.ALWAYS);

		table = new TableView<>();
		HBox.setHgrow(table, Priority.ALWAYS);
		table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

		TableColumn<DeliveryCompany, Integer> idCol = new TableColumn<>("ID");
		idCol.setCellValueFactory(new PropertyValueFactory<>("companyId"));

		TableColumn<DeliveryCompany, String> companyCol = new TableColumn<>("Company");
		companyCol.setCellValueFactory(new PropertyValueFactory<>("companyName"));

		TableColumn<DeliveryCompany, String> contactCol = new TableColumn<>("Contact Person");
		contactCol.setCellValueFactory(new PropertyValueFactory<>("contactPerson"));

		TableColumn<DeliveryCompany, String> phoneCol = new TableColumn<>("Phone");
		phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));

		TableColumn<DeliveryCompany, Boolean> activeCol = new TableColumn<>("Status");
		activeCol.setCellValueFactory(new PropertyValueFactory<>("active"));

		activeCol.setCellFactory(column -> new TableCell<DeliveryCompany, Boolean>() {
			@Override
			protected void updateItem(Boolean item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setText(null);
					setStyle("");
				} else {
					if (item) {
						setText("Active");
						setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
					} else {
						setText("Inactive");
						setStyle("-fx-text-fill: #7f8c8d;");
					}
				}
			}
		});

		idCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.10));
		companyCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.25));
		contactCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.25));
		phoneCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.25));
		activeCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.15));

		table.getColumns().addAll(idCol, companyCol, contactCol, phoneCol, activeCol);

		VBox formBox = new VBox(10);
		formBox.setPrefWidth(300);
		formBox.setMinWidth(300);

		Label lblCompany = new Label("Company Name");
		companyField = new TextField();

		Label lblContact = new Label("Contact Person");
		contactField = new TextField();

		Label lblPhone = new Label("Phone");
		phoneField = new TextField();

		activeCheckBox = new CheckBox("Active Status");
		activeCheckBox.setSelected(true);

		HBox actionsBox = new HBox(6);
		actionsBox.setAlignment(Pos.CENTER_LEFT);

		addBtn = new Button("Add");
		updateBtn = new Button("Update");
		deleteBtn = new Button("Delete");
		refreshBtn = new Button("Refresh");

		String btnStyle = "-fx-font-weight: bold;";
		double uniformWidth = 68;

		addBtn.setStyle(btnStyle);
		addBtn.setPrefWidth(uniformWidth);
		updateBtn.setStyle(btnStyle);
		updateBtn.setPrefWidth(uniformWidth);
		deleteBtn.setStyle(btnStyle);
		deleteBtn.setPrefWidth(uniformWidth);
		refreshBtn.setStyle(btnStyle);
		refreshBtn.setPrefWidth(uniformWidth);

		actionsBox.getChildren().addAll(addBtn, updateBtn, deleteBtn, refreshBtn);

		formBox.getChildren().addAll(lblCompany, companyField, lblContact, contactField, lblPhone, phoneField,
				activeCheckBox, actionsBox);

		table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			if (newSelection != null) {
				companyField.setText(newSelection.getCompanyName());
				contactField.setText(newSelection.getContactPerson());
				phoneField.setText(newSelection.getPhone());
				activeCheckBox.setSelected(newSelection.isActive());
			}
		});

		setupActions();

		contentRow.getChildren().addAll(table, formBox);
		getChildren().addAll(mainTitle, contentRow);

		loadData();
	}

	private void setupActions() {

		refreshBtn.setOnAction(e -> {
			loadData();
			clearFields();
		});

		addBtn.setOnAction(e -> {
			String name = companyField.getText().trim();
			String contact = contactField.getText().trim();
			String phone = phoneField.getText().trim();
			boolean isActive = activeCheckBox.isSelected();

			if (name.isEmpty()) {
				showAlert(Alert.AlertType.WARNING, "Validation Error", "Company Name cannot be empty!");
				return;
			}

			if (!phone.matches("\\d{10}")) {
				showAlert(Alert.AlertType.WARNING, "Input Error", "Phone number must be exactly 10 digits!");
				return;
			}

			try {
				DeliveryCompany c = new DeliveryCompany();
				c.setCompanyName(name);
				c.setContactPerson(contact);
				c.setPhone(phone);
				c.setActive(isActive);

				dao.insert(c);
				showAlert(Alert.AlertType.INFORMATION, "Success", "Delivery company added successfully!");
				loadData();
				clearFields();
			} catch (Exception ex) {
				showAlert(Alert.AlertType.ERROR, "Database Error", ex.getMessage());
			}
		});


		updateBtn.setOnAction(e -> {
			DeliveryCompany selected = table.getSelectionModel().getSelectedItem();
			if (selected == null) {
				showAlert(Alert.AlertType.WARNING, "Selection Error", "Please select a company from the table first!");
				return;
			}

			String name = companyField.getText().trim();
			String contact = contactField.getText().trim();
			String phone = phoneField.getText().trim();
			boolean isActive = activeCheckBox.isSelected();

			if (name.isEmpty()) {
				showAlert(Alert.AlertType.WARNING, "Validation Error", "Company Name cannot be empty!");
				return;
			}

			if (!phone.matches("\\d{10}")) {
				showAlert(Alert.AlertType.WARNING, "Input Error", "Phone number must be exactly 10 digits!");
				return;
			}

			try {
				selected.setCompanyName(name);
				selected.setContactPerson(contact);
				selected.setPhone(phone);
				selected.setActive(isActive);

				dao.update(selected);
				showAlert(Alert.AlertType.INFORMATION, "Success", "Delivery company updated successfully!");
				loadData();
				clearFields();
			} catch (Exception ex) {
				showAlert(Alert.AlertType.ERROR, "Database Error", ex.getMessage());
			}
		});


		deleteBtn.setOnAction(e -> {
			DeliveryCompany selected = table.getSelectionModel().getSelectedItem();
			if (selected == null) {
				showAlert(Alert.AlertType.WARNING, "Selection Error", "Please select a company from the table first!");
				return;
			}

			try {
				dao.delete(selected.getCompanyId());
				showAlert(Alert.AlertType.INFORMATION, "Success", "Delivery company deleted successfully!");
				loadData();
				clearFields();
			} catch (Exception ex) {

				if (ex.getMessage() != null
						&& (ex.getMessage().contains("foreign key") || ex.getMessage().contains("1451"))) {
					showAlert(Alert.AlertType.ERROR, "Integrity Error",
							"Cannot delete this company because it is linked to existing orders.\nPlease uncheck 'Active Status' instead to deactivate it.");
				} else {
					showAlert(Alert.AlertType.ERROR, "Error", ex.getMessage());
				}
			}
		});
	}

	private void loadData() {
		try {
			ArrayList<DeliveryCompany> list = dao.getAll();
			table.setItems(FXCollections.observableArrayList(list));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void clearFields() {
		companyField.clear();
		contactField.clear();
		phoneField.clear();
		activeCheckBox.setSelected(true);
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
}