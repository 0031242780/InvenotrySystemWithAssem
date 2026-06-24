package ui;

import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Order;

import dao.AccountDAO;
import dao.OrderDAO;
import dao.ProductDAO;
import dao.SupplierDAO;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class AdminDashboard extends BorderPane {

	public AdminDashboard(Stage stage) {

		VBox side = new VBox(15);
		side.setPadding(new Insets(20));
		side.setPrefWidth(200);
		side.setStyle("-fx-background-color:#0B1E3A;");

		Label title = new Label("TECH MARKET");
		title.setStyle("-fx-text-fill:white; -fx-font-size:18px;");

		Button dashboard = btn("Dashboard");
		Button products = btn("Products");
		Button categories = btn("Categories");
		Button suppliers = btn("Suppliers");
		Button delivery = btn("Delivery");
		Button orders = btn("Orders");
		Button customers = btn("Customers");
		Button stock = btn("Stock Movements");
		Button users = btn("Accounts");
		Button paymentsBtn = btn("Payments History");

		side.getChildren().addAll(title, dashboard, products, categories, suppliers, delivery, orders, customers, stock,
				users, paymentsBtn);
		setLeft(side);

		dashboard.setOnAction(e -> showDashboard());
		products.setOnAction(e -> setCenter(new AdminProductInterface()));
		categories.setOnAction(e -> setCenter(new AdminCategoryInterface()));
		suppliers.setOnAction(e -> setCenter(new AdminSupplierInterface()));
		delivery.setOnAction(e -> setCenter(new AdminDeliveryCompanyInterface()));
		orders.setOnAction(e -> setCenter(new AdminOrderInterface()));
		customers.setOnAction(e -> setCenter(new AdminCustomerInterface()));
		stock.setOnAction(e -> setCenter(new AdminStockMovementInterface()));
		users.setOnAction(e -> setCenter(new AdminRolesInterface()));
		paymentsBtn.setOnAction(e -> setCenter(new AdminPaymentsInterface()));
		showDashboard();

		stage.setScene(new javafx.scene.Scene(this, 1100, 700));
		stage.show();
	}

	private void showDashboard() {
		try {
			ProductDAO pdao = new ProductDAO();
			OrderDAO odao = new OrderDAO();
			AccountDAO adao = new AccountDAO();
			SupplierDAO sdao = new SupplierDAO();

			HBox cardsRow = new HBox(20);
			cardsRow.setPadding(new Insets(10, 0, 10, 0));

			cardsRow.getChildren().addAll(card("Products", "" + pdao.countProducts()),
					card("Orders", "" + odao.countOrders()), card("Customers", "" + adao.getCustomers().size()),
					card("Suppliers", "" + sdao.countSuppliers()));

			TableView<Order> systemOrdersTable = new TableView<>();
			systemOrdersTable.setPrefHeight(350);
			systemOrdersTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

			TableColumn<Order, Integer> idCol = new TableColumn<>("Order ID");
			idCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));

			TableColumn<Order, Double> totalCol = new TableColumn<>("Total Price");
			totalCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

			TableColumn<Order, String> statusCol = new TableColumn<>("Status Name");
			statusCol.setCellValueFactory(new PropertyValueFactory<>("statusName"));

			TableColumn<Order, String> userCol = new TableColumn<>("Customer Name");
			userCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));

			idCol.prefWidthProperty().bind(systemOrdersTable.widthProperty().subtract(2).multiply(0.25));
			totalCol.prefWidthProperty().bind(systemOrdersTable.widthProperty().subtract(2).multiply(0.25));
			statusCol.prefWidthProperty().bind(systemOrdersTable.widthProperty().subtract(2).multiply(0.25));
			userCol.prefWidthProperty().bind(systemOrdersTable.widthProperty().subtract(2).multiply(0.25));

			systemOrdersTable.getColumns().addAll(idCol, totalCol, statusCol, userCol);

			ArrayList<Order> allOrders = odao.getAll();
			systemOrdersTable.setItems(FXCollections.observableArrayList(allOrders));

			HBox topHeaderRow = new HBox(20);
			topHeaderRow.setAlignment(Pos.CENTER_LEFT);

			Label mainTitle = new Label("Admin Control Panel");
			mainTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #0B1E3A;");

			Button refreshBtn = new Button("Refresh");
			refreshBtn.setStyle(
					"-fx-background-color: #123B70; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 8;");

			refreshBtn.setOnAction(e -> showDashboard());

			topHeaderRow.getChildren().addAll(mainTitle, refreshBtn);

			VBox mainLayout = new VBox(15);
			mainLayout.setPadding(new Insets(20));

			Label tableTitle = new Label("Overall System Orders Log");
			tableTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0B1E3A;");

			mainLayout.getChildren().addAll(topHeaderRow, cardsRow, tableTitle, systemOrdersTable);
			setCenter(mainLayout);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private VBox card(String name, String value) {
		VBox box = new VBox(10);
		box.setAlignment(Pos.CENTER);
		box.setPrefSize(180, 120);
		box.setStyle("-fx-background-color:#123B70; -fx-background-radius:15;");

		Label t = new Label(name);
		Label v = new Label(value);

		t.setStyle("-fx-text-fill:white; -fx-font-size:16;");
		v.setStyle("-fx-text-fill:white; -fx-font-size:28; -fx-font-weight:bold;");

		box.getChildren().addAll(t, v);
		return box;
	}

	private Button btn(String text) {
		Button b = new Button(text);
		b.setPrefWidth(170);
		b.setStyle("-fx-background-color:transparent; -fx-text-fill:white; -fx-alignment:CENTER_LEFT;");
		return b;
	}
}