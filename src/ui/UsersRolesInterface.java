package ui;

import dao.AccountDAO;
import dao.RoleDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.Account;

public class UsersRolesInterface extends VBox {

	private TableView<Account> table;

	private TableColumn<Account, Integer> idCol;
	private TableColumn<Account, String> emailCol;
	private TableColumn<Account, String> firstCol;
	private TableColumn<Account, String> lastCol;
	private TableColumn<Account, String> roleCol;

	private Button refreshBtn;

	private AccountDAO accountDAO;
	private RoleDAO roleDAO;

	private ObservableList<Account> accounts = FXCollections.observableArrayList();

	public UsersRolesInterface() {

		accountDAO = new AccountDAO();
		roleDAO = new RoleDAO();

		setSpacing(15);
		setPadding(new Insets(20));

		createTable();
		createButtons();
		buildLayout();
		loadUsers();
	}

	private void createTable() {

		table = new TableView<>();

		table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

		idCol = new TableColumn<>("ID");
		idCol.setCellValueFactory(new PropertyValueFactory<>("accountId"));

		emailCol = new TableColumn<>("Email Address");
		emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

		firstCol = new TableColumn<>("First Name");
		firstCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));

		lastCol = new TableColumn<>("Last Name");
		lastCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));

		roleCol = new TableColumn<>("Role Name");
		roleCol.setCellValueFactory(new PropertyValueFactory<>("roleName"));

		idCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.10));
		emailCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.35));
		firstCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.20));
		lastCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.20));
		roleCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.15));

		table.getColumns().addAll(idCol, emailCol, firstCol, lastCol, roleCol);
		table.setItems(accounts);

		table.setPrefHeight(450);
	}

	private void createButtons() {

		refreshBtn = new Button("Refresh Users Data");
		refreshBtn.setPadding(new Insets(10));
		refreshBtn.setOnAction(e -> loadUsers());
	}

	private void buildLayout() {

		Label mainTitle = new Label("Users & Roles Management");
		mainTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #0B1E3A;");

		HBox bottomBar = new HBox(10);
		bottomBar.setAlignment(Pos.CENTER_LEFT);
		bottomBar.getChildren().add(refreshBtn);

		getChildren().addAll(mainTitle, table, bottomBar);

		VBox.setVgrow(table, javafx.scene.layout.Priority.ALWAYS);
	}

	private void loadUsers() {
		try {
			accounts.clear();
			accounts.addAll(accountDAO.getAllAccounts());
			table.refresh();
		} catch (Exception e) {
			showAlert(Alert.AlertType.ERROR, "Error", null, e.getMessage());
		}
	}

	private void showAlert(Alert.AlertType type, String title, String header, String content) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.showAndWait();
	}
}