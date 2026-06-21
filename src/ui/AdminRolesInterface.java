package ui;

import dao.AccountDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import model.Account;

public class AdminRolesInterface extends VBox {

	private TableView<Account> table;
	private TableColumn<Account, Integer> idCol;
	private TableColumn<Account, String> emailCol;
	private TableColumn<Account, String> firstNameCol;
	private TableColumn<Account, String> lastNameCol;
	private TableColumn<Account, String> roleCol;
	private TableColumn<Account, Boolean> statusCol;

	private TextField emailField;
	private PasswordField passwordField; // 🔥 حقل كلمة المرور الجديد بالـ UI
	private TextField firstNameField;
	private TextField lastNameField;
	private TextField phoneField;
	private ComboBox<String> roleComboBox;

	private Button addBtn;
	private Button updateBtn;
	private Button deactivateBtn;
	private Button refreshBtn;

	private AccountDAO accountDAO;
	private ObservableList<Account> accountsList = FXCollections.observableArrayList();

	public AdminRolesInterface() {

		accountDAO = new AccountDAO();

		setSpacing(15);
		setPadding(new Insets(20));

		Label mainTitle = new Label("Users & Roles Management");
		mainTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #0B1E3A;");

		HBox contentRow = new HBox(20);
		VBox.setVgrow(contentRow, Priority.ALWAYS);

		// --- إعداد الجدول وتمديده هندسياً ---
		table = new TableView<>();
		HBox.setHgrow(table, Priority.ALWAYS);
		table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

		idCol = new TableColumn<>("ID");
		idCol.setCellValueFactory(new PropertyValueFactory<>("accountId"));

		emailCol = new TableColumn<>("Email Address");
		emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

		firstNameCol = new TableColumn<>("First Name");
		firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));

		lastNameCol = new TableColumn<>("Last Name");
		lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));

		roleCol = new TableColumn<>("Role Name");
		roleCol.setCellValueFactory(new PropertyValueFactory<>("roleName"));

		statusCol = new TableColumn<>("Status");

		statusCol.setCellValueFactory(cellData -> {
			if (cellData.getValue() != null) {
				return new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().isActive());
			}
			return new javafx.beans.property.SimpleObjectProperty<>(false);
		});

		statusCol.setCellFactory(column -> new TableCell<Account, Boolean>() {
			@Override
			protected void updateItem(Boolean item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setText("-");
					setStyle("");
				} else {
					if (item) {
						setText("Active");
						setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
					} else {
						setText("Inactive");
						setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
					}
				}
			}
		});

		idCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.06));
		emailCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.28));
		firstNameCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.17));
		lastNameCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.17));
		roleCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.17));
		statusCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.15));

		table.getColumns().addAll(idCol, emailCol, firstNameCol, lastNameCol, roleCol, statusCol);
		table.setItems(accountsList);

		// --- إعداد الفورم الجانبي (اليمين) ---
		VBox formBox = new VBox(10);
		formBox.setPrefWidth(300);
		formBox.setMinWidth(300);

		Label formTitle = new Label("Account Specifications");
		formTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0B1E3A;");
		formTitle.setPadding(new Insets(0, 0, 5, 0));

		emailField = new TextField();
		emailField.setPromptText("Enter email address");

		// 🔥 تهيئة حقل كلمة المرور الجديد
		passwordField = new PasswordField();
		passwordField.setPromptText("Enter account password");

		firstNameField = new TextField();
		firstNameField.setPromptText("First name");

		lastNameField = new TextField();
		lastNameField.setPromptText("Last name");

		phoneField = new TextField();
		phoneField.setPromptText("Phone (10 digits)");

		roleComboBox = new ComboBox<>();
		roleComboBox.getItems().addAll("Admin", "Customer");
		roleComboBox.setPromptText("Select Role");
		roleComboBox.setPrefWidth(300);

		HBox actionsBox = new HBox(6);
		actionsBox.setAlignment(Pos.CENTER_LEFT);
		actionsBox.setPadding(new Insets(10, 0, 0, 0));

		addBtn = new Button("Add");
		updateBtn = new Button("Update");
		deactivateBtn = new Button("Deactivate");
		refreshBtn = new Button("Refresh");

		String btnStyle = "-fx-font-weight: bold;";
		double uniformWidth = 76;

		addBtn.setStyle(btnStyle);
		addBtn.setPrefWidth(uniformWidth);
		updateBtn.setStyle(btnStyle);
		updateBtn.setPrefWidth(uniformWidth);
		deactivateBtn.setStyle(btnStyle);
		deactivateBtn.setPrefWidth(uniformWidth);
		refreshBtn.setStyle(btnStyle);
		refreshBtn.setPrefWidth(uniformWidth);

		actionsBox.getChildren().addAll(addBtn, updateBtn, deactivateBtn, refreshBtn);

		// 🔥 إضافة حقل الباسورد للفورم الجانبي اليمين لكي يظهر بشكل منسق وعلمي
		formBox.getChildren().addAll(formTitle, new Label("Email Address"), emailField, new Label("Password"),
				passwordField, // زراعة الحقل هان
				new Label("First Name"), firstNameField, new Label("Last Name"), lastNameField,
				new Label("Phone Number"), phoneField, new Label("Account Role"), roleComboBox, actionsBox);

		contentRow.getChildren().addAll(table, formBox);
		getChildren().addAll(mainTitle, contentRow);

		setupActions();
		loadData();
	}

	private void setupActions() {

		table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			if (newSelection != null) {
				emailField.setText(newSelection.getEmail());
				firstNameField.setText(newSelection.getFirstName());
				lastNameField.setText(newSelection.getLastName());
				phoneField.setText(newSelection.getPhoneNumber() != null ? newSelection.getPhoneNumber() : "");
				passwordField.clear(); // أمنياً نترك حقل الباسورد فارغاً عند التحديد لحظر كشف كلمات السر

				String rName = newSelection.getRoleName();
				if (rName != null) {
					if (rName.equalsIgnoreCase("ADMIN")) {
						roleComboBox.setValue("Admin");
					} else {
						roleComboBox.setValue("Customer");
					}
				}
			}
		});

		refreshBtn.setOnAction(e -> {
			loadData();
			clearFields();
		});

		// زر الإضافة المحدث
		addBtn.setOnAction(e -> {
			String email = emailField.getText().trim();
			String password = passwordField.getText().trim(); // 🔥 سحب الباسورد المدخل
			String phone = phoneField.getText().trim();
			String first = firstNameField.getText().trim();
			String last = lastNameField.getText().trim();
			String role = roleComboBox.getValue();

			// الفحص الإلزامي لمنع ترك الباسورد فارغاً
			if (first.isEmpty() || last.isEmpty() || role == null || password.isEmpty()) {
				showAlert(Alert.AlertType.WARNING, "Validation Error",
						"Please fill all personal fields, password, and select a role.");
				return;
			}

			if (!validateInputs(email, phone, role)) {
				return;
			}

			try {
				Account a = new Account();
				a.setEmail(email);
				a.setFirstName(first);
				a.setLastName(last);
				a.setPhoneNumber(phone);
				a.setPassword(password); // 🔥 تمرير الباسورد الديناميكي المكتوب فوراً للداتابيز

				int roleId = 3;
				if (role.equalsIgnoreCase("Admin")) {
					roleId = 1;
				}
				a.setRoleId(roleId);

				accountDAO.insert(a);
				showAlert(Alert.AlertType.INFORMATION, "Success", "Account created successfully and safely verified.");
				loadData();
				clearFields();
			} catch (Exception ex) {
				showExceptionAlert("Insertion Process Failed", ex);
			}
		});

		// زر التعديل
		updateBtn.setOnAction(e -> {
			Account selected = table.getSelectionModel().getSelectedItem();
			if (selected == null) {
				showAlert(Alert.AlertType.WARNING, "Selection Error", "Please select an account from the table first.");
				return;
			}

			String email = emailField.getText().trim();
			String phone = phoneField.getText().trim();
			String role = roleComboBox.getValue();

			if (!validateInputs(email, phone, role)) {
				return;
			}

			try {
				selected.setEmail(email);
				selected.setFirstName(firstNameField.getText().trim());
				selected.setLastName(lastNameField.getText().trim());
				selected.setPhoneNumber(phone);

				int roleId = 3;
				if (role.equalsIgnoreCase("Admin")) {
					roleId = 1;
				}
				selected.setRoleId(roleId);

				accountDAO.update(selected);
				showAlert(Alert.AlertType.INFORMATION, "Success", "Account details updated successfully.");
				loadData();
				clearFields();
			} catch (Exception ex) {
				showExceptionAlert("Update Process Failed", ex);
			}
		});

		deactivateBtn.setOnAction(e -> {
			Account selected = table.getSelectionModel().getSelectedItem();
			if (selected == null) {
				showAlert(Alert.AlertType.WARNING, "Selection Error", "Please select an account from the table first.");
				return;
			}

			if (selected.getAccountId() == 1) {
				showAlert(Alert.AlertType.ERROR, "Security Violation",
						"Access Denied! The primary administrator account cannot be deactivated.");
				return;
			}

			try {
				accountDAO.deactivate(selected.getAccountId());
				showAlert(Alert.AlertType.INFORMATION, "Success",
						"Account status has been successfully set to inactive.");
				loadData();
				clearFields();
			} catch (Exception ex) {
				showExceptionAlert("Deactivation Process Failed", ex);
			}
		});
	}

	private boolean validateInputs(String email, String phone, String selectedRole) {
		if (!phone.matches("\\d{10}")) {
			showAlert(Alert.AlertType.WARNING, "Input Error",
					"Phone number is invalid! It must consist of exactly 10 digits.");
			return false;
		}

		String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
		if (!email.matches(emailRegex)) {
			showAlert(Alert.AlertType.WARNING, "Input Error", "The email address format is invalid.");
			return false;
		}

		String cleanEmail = email.toLowerCase().trim();

		if (selectedRole.equalsIgnoreCase("Admin")) {
			if (!cleanEmail.endsWith("@tech.com")) {
				showAlert(Alert.AlertType.ERROR, "Security Restriction",
						"Access Denied! Admin accounts are strictly restricted to the corporate domain (@tech.com).");
				return false;
			}
		} else {
			if (cleanEmail.endsWith("@tech.com")) {
				showAlert(Alert.AlertType.ERROR, "Access Denied",
						"Security Violation! Regular users cannot register using the corporate admin domain.");
				return false;
			}
			if (!cleanEmail.endsWith(".com")) {
				showAlert(Alert.AlertType.WARNING, "Domain Rejected",
						"Registration failed! Only standard .com email domains are allowed for users.");
				return false;
			}
		}
		return true;
	}

	private void loadData() {
		try {
			accountsList.clear();
			accountsList.addAll(accountDAO.getAllAccounts());
			table.refresh();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void clearFields() {
		emailField.clear();
		passwordField.clear(); // تنظيف حقل الباسورد
		firstNameField.clear();
		lastNameField.clear();
		phoneField.clear();
		roleComboBox.setValue(null);
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
		sb.append("Details: ")
				.append(ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : "No additional data.")
				.append("\n");

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