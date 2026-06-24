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

public class LoginInterface {

	private TextField emailField;
	private PasswordField passwordField;
	private Button loginButton;
	private Button signupButton;
	private GridPane gp;
	private HBox buttonBox;
	private VBox root;
	private Scene scene;
	private AccountDAO dao;
	private Stage stage;

	public LoginInterface(Stage stage) {
		this.stage = stage;
		dao = new AccountDAO();
		createFields();
		createButtons();
		createLayout();
		createActions();
		scene = new Scene(root, 400, 250);
		stage.setScene(scene);
		stage.show();
	}

	private void createFields() {
		emailField = new TextField();
		passwordField = new PasswordField();
		emailField.setPromptText("Email");
		passwordField.setPromptText("Password");
	}

	private void createButtons() {
		loginButton = new Button("Login");
		signupButton = new Button("Sign Up");
		loginButton.setPadding(new Insets(10));
		signupButton.setPadding(new Insets(10));
	}

	private void createLayout() {
		gp = new GridPane();
		gp.setVgap(10);
		gp.setHgap(10);
		gp.setPadding(new Insets(20));
		gp.setAlignment(Pos.CENTER);

		gp.add(new Label("Email"), 0, 0);
		gp.add(emailField, 1, 0);
		gp.add(new Label("Password"), 0, 1);
		gp.add(passwordField, 1, 1);

		buttonBox = new HBox(10);
		buttonBox.getChildren().addAll(loginButton, signupButton);
		buttonBox.setAlignment(Pos.CENTER);

		root = new VBox(15);
		root.getChildren().addAll(gp, buttonBox);
		root.setAlignment(Pos.CENTER);
		root.setPadding(new Insets(30));
	}

	private void createActions() {
		loginButton.setOnAction(e -> login());
		signupButton.setOnAction(e -> {
			new SignupInterface(stage);
		});
	}

	private void login() {
		try {
			Account account = dao.login(emailField.getText(), passwordField.getText());

			if (account == null) {
				showAlert("Login Failed", "Wrong email or password", Alert.AlertType.ERROR);
				return;
			}

			if (account.getEmail().toLowerCase().endsWith("@tech.com")) {
				new AdminDashboard(stage);
			} else {
				new UserCustomerDashboard(stage, account);
			}

		} catch (Exception e) {
			showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
		}
	}

	private void showAlert(String title, String message, Alert.AlertType type) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	public Scene getScene() {
		return scene;
	}
}