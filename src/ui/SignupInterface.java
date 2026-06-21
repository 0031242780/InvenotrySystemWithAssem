package ui;

import dao.AccountDAO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Account;

public class SignupInterface {

	private TextField emailField;
	private PasswordField passwordField;

	private TextField firstField;
	private TextField lastField;
	private TextField phoneField;
	private TextField cityField;
	private TextField streetField;

	private Button signupBtn;
	private Button backBtn;

	private GridPane form;
	private HBox buttons;
	private VBox root;
	private Scene scene;
	private AccountDAO dao;
	private Stage stage;

	public SignupInterface(Stage stage) {
		this.stage = stage;
		dao = new AccountDAO();

		createFields();
		createButtons();
		createLayout();
		createActions();

		scene = new Scene(root, 400, 420); // زدنا الحجم تكّة عشان يستوعب التقسيمة الجديدة براحة
		stage.setScene(scene);
		stage.show();
	}

	private void createFields() {
		emailField = new TextField();
		passwordField = new PasswordField();
		firstField = new TextField();
		lastField = new TextField();
		phoneField = new TextField();
		cityField = new TextField();
		streetField = new TextField();

		emailField.setPromptText("Email");
		passwordField.setPromptText("Password");
	}

	private void createButtons() {
		signupBtn = new Button("Sign Up");
		backBtn = new Button("Back");

		signupBtn.setPadding(new Insets(10));
		backBtn.setPadding(new Insets(10));
	}

	private void createLayout() {
		form = new GridPane();
		form.setPadding(new Insets(20));
		form.setHgap(10);
		form.setVgap(10);

		form.add(new Label("Email"), 0, 0);
		form.add(emailField, 1, 0);
		form.add(new Label("Password"), 0, 1);
		form.add(passwordField, 1, 1);
		form.add(new Label("First Name"), 0, 2);
		form.add(firstField, 1, 2);
		form.add(new Label("Last Name"), 0, 3);
		form.add(lastField, 1, 3);
		form.add(new Label("Phone"), 0, 4);
		form.add(phoneField, 1, 4);
		form.add(new Label("City"), 0, 5);
		form.add(cityField, 1, 5);
		form.add(new Label("Street"), 0, 6);
		form.add(streetField, 1, 6);

		buttons = new HBox(10);
		buttons.getChildren().addAll(signupBtn, backBtn);
		buttons.setAlignment(Pos.CENTER);

		root = new VBox(15);
		root.getChildren().addAll(form, buttons);
		root.setPadding(new Insets(20));
		root.setAlignment(Pos.CENTER);
	}

	private void createActions() {
		backBtn.setOnAction(e -> {
			new LoginInterface(stage);
		});

		signupBtn.setOnAction(e -> {
			createAccount();
		});
	}

	private void createAccount() {
		String email = emailField.getText().trim();
		String password = passwordField.getText().trim();
		String first = firstField.getText().trim();
		String last = lastField.getText().trim();
		String phone = phoneField.getText().trim();

		// 1️⃣ فحص الحقول الإلزامية الأساسية لمنع إدخال بيانات فارغة
		if (email.isEmpty() || password.isEmpty() || first.isEmpty() || last.isEmpty() || phone.isEmpty()) {
			showAlert(Alert.AlertType.WARNING, "Validation Error", null, "Please fill all required fields.");
			return;
		}

		// 2️⃣ 🔥 فحص رقم الهاتف الصارم (يجب أن يتكون من 10 خانات رقمية بالظبط)
		if (!phone.matches("\\d{10}")) {
			showAlert(Alert.AlertType.WARNING, "Input Error", null,
					"Phone number is invalid! It must consist of exactly 10 digits.");
			return;
		}

		// 3️⃣ فحص بنية الإيميل الأساسية لحماية السيستم
		String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
		if (!email.matches(emailRegex)) {
			showAlert(Alert.AlertType.WARNING, "Input Error", null, "The email address format is invalid.");
			return;
		}

		// 4️⃣ حماية النطاق الأمني: منع الزبائن العاديين من تسجيل حساب بنطاق الآدمن
		// @tech.com
		if (email.toLowerCase().endsWith("@tech.com")) {
			showAlert(Alert.AlertType.ERROR, "Access Denied", null,
					"Security Violation! Regular users cannot register using the corporate admin domain.");
			return;
		}

		try {
			Account a = new Account();
			a.setEmail(email);
			a.setPassword(password);
			a.setFirstName(first);
			a.setLastName(last);
			a.setPhoneNumber(phone);
			a.setCity(cityField.getText().trim());
			a.setStreet(streetField.getText().trim());

			// حساب زبون عادي تلقائياً يأخذ الرقم 3
			a.setRoleId(3);

			dao.insert(a);

			showAlert(Alert.AlertType.INFORMATION, "Success", null, "Account Created Successfully!");
			clearFields();

			// بعد النجاح، بنرجعه تلقائياً على شاشة الـ Login عشان يسجل دخول
			new LoginInterface(stage);

		} catch (Exception e) {
			showAlert(Alert.AlertType.ERROR, "Database Error", null, e.getMessage());
		}
	}

	private void clearFields() {
		emailField.clear();
		passwordField.clear();
		firstField.clear();
		lastField.clear();
		phoneField.clear();
		cityField.clear();
		streetField.clear();
	}

	// دالة التنبيه المعدلة والمحمية هندسياً لتناسب الحجم التلقائي الملموم
	private void showAlert(Alert.AlertType type, String title, String header, String content) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.getDialogPane().setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
		alert.showAndWait();
	}

	public Scene getScene() {
		return scene;
	}
}