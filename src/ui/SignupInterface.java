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

		scene = new Scene(root, 400, 400);

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

		try {

			Account a = new Account();

			a.setEmail(emailField.getText());

			a.setPassword(passwordField.getText());

			a.setFirstName(firstField.getText());

			a.setLastName(lastField.getText());

			a.setPhoneNumber(phoneField.getText());

			a.setCity(cityField.getText());

			a.setStreet(streetField.getText());

			// customer role
			a.setRoleId(3);

			dao.insert(a);

			showAlert(Alert.AlertType.INFORMATION, "Success", null, "Account Created");

			clearFields();

		} catch (Exception e) {

			showAlert(Alert.AlertType.ERROR, "Error", null, e.getMessage());

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

	private void showAlert(Alert.AlertType type, String title, String header, String content) {

		Alert alert = new Alert(type);

		alert.setTitle(title);

		alert.setHeaderText(header);

		alert.setContentText(content);

		alert.showAndWait();

	}

	public Scene getScene() {

		return scene;

	}

}