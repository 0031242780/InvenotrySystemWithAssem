package ui;

import dao.AccountDAO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import model.Account;

public class UserProfileInterface extends VBox {

    private Account account;
    private AccountDAO accountDAO;

    private TextField firstNameField;
    private TextField lastNameField;
    private TextField emailField;
    private PasswordField passwordField;
    private TextField cityField;
    private TextField streetField;
    private TextField phoneField;
    private Button saveBtn;

    public UserProfileInterface(Account account) {
        this.account = account;
        this.accountDAO = new AccountDAO();

        setSpacing(20);
        setPadding(new Insets(30));
        setAlignment(Pos.TOP_LEFT);

        Label title = new Label("Edit Account Information");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #0B1E3A;");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);

        grid.add(new Label("First Name:"), 0, 0);
        firstNameField = new TextField();
        grid.add(firstNameField, 1, 0);

        grid.add(new Label("Last Name:"), 0, 1);
        lastNameField = new TextField();
        grid.add(lastNameField, 1, 1);

        grid.add(new Label("Email Address (Read-Only):"), 0, 2);
        emailField = new TextField();
        emailField.setEditable(false);
        emailField.setStyle("-fx-background-color: #e5e7eb; -fx-text-fill: #6b7280;");
        grid.add(emailField, 1, 2);

        grid.add(new Label("New Password:"), 0, 3);
        passwordField = new PasswordField();
        passwordField.setPromptText("Enter new or current password");
        grid.add(passwordField, 1, 3);

        grid.add(new Label("Phone Number:"), 0, 4);
        phoneField = new TextField();
        grid.add(phoneField, 1, 4);

        grid.add(new Label("City:"), 0, 5);
        cityField = new TextField();
        grid.add(cityField, 1, 5);

        grid.add(new Label("Street:"), 0, 6);
        streetField = new TextField();
        grid.add(streetField, 1, 6);

        saveBtn = new Button("Save Changes");
        saveBtn.setPadding(new Insets(10, 20, 10, 20));
        saveBtn.setOnAction(e -> handleUpdate());
        grid.add(saveBtn, 1, 7);

        getChildren().addAll(title, grid);

        populateFields();
    }

    private void populateFields() {
        firstNameField.setText(account.getFirstName());
        lastNameField.setText(account.getLastName());
        emailField.setText(account.getEmail());
        phoneField.setText(account.getPhoneNumber());
        cityField.setText(account.getCity());
        streetField.setText(account.getStreet());
        passwordField.clear();
    }

    private void handleUpdate() {
        String password = passwordField.getText().trim();
        String phone = phoneField.getText().trim();

        if (password.isEmpty() || phone.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Password and Phone Number fields are required to update profiles.");
            return;
        }

        try {
            account.setFirstName(firstNameField.getText().trim());
            account.setLastName(lastNameField.getText().trim());
            account.setPhoneNumber(phone);
            account.setCity(cityField.getText().trim());
            account.setStreet(streetField.getText().trim());

            account.setPassword(password);

            accountDAO.updateProfile(account);

            showAlert(Alert.AlertType.INFORMATION, "Success", "Your profile and security updates have been saved successfully!");
            passwordField.clear();
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "System Failure", "Failed to update profile info:\n" + ex.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}