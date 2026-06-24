package ui;

import dao.AccountDAO;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.Account;

public class AdminCustomerInterface extends VBox {

	private TableView<Account> table;
	private AccountDAO dao;

	public AdminCustomerInterface() {

		dao = new AccountDAO();

		setSpacing(15);
		setPadding(new Insets(20));

		Label mainTitle = new Label("Customers Management");
		mainTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #0B1E3A;");

		table = new TableView<>();

		table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

		TableColumn<Account, Integer> idCol = new TableColumn<>("ID");
		idCol.setCellValueFactory(new PropertyValueFactory<>("accountId"));

		TableColumn<Account, String> firstCol = new TableColumn<>("First Name");
		firstCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));

		TableColumn<Account, String> lastCol = new TableColumn<>("Last Name");
		lastCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));

		TableColumn<Account, String> emailCol = new TableColumn<>("Email");
		emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

		TableColumn<Account, String> phoneCol = new TableColumn<>("Phone");
		phoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));


		idCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.10));
		firstCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.20));
		lastCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.20));
		emailCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.30));
		phoneCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.20));

		table.getColumns().addAll(idCol, firstCol, lastCol, emailCol, phoneCol);

		table.setPrefHeight(400);

		Button refreshBtn = new Button("Refresh Data");
		refreshBtn.setPadding(new Insets(10));
		refreshBtn.setOnAction(e -> load());

		HBox bottomBar = new HBox(10);
		bottomBar.setAlignment(Pos.CENTER_LEFT);
		bottomBar.getChildren().add(refreshBtn);

		getChildren().addAll(mainTitle, table, bottomBar);

		load();
	}

	private void load() {
		try {
			table.setItems(FXCollections.observableArrayList(dao.getCustomers()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}