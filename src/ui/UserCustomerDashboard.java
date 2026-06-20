package ui;

import java.util.ArrayList;
import dao.CartDAO;
import dao.OrderDAO;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import model.Account;
import model.Order;

public class UserCustomerDashboard extends BorderPane {

	private Account account;
	private TableView<Order> recentOrdersTable;

	public UserCustomerDashboard(Stage stage, Account account) {

		this.account = account;

		createSideBar();

		showDashboard();

		stage.setScene(new javafx.scene.Scene(this, 1100, 700));

		stage.show();

	}

	private void createSideBar() {

		VBox side = new VBox(15);
		side.setPadding(new Insets(20));
		side.setPrefWidth(200);
		side.setStyle("-fx-background-color:#0B1E3A;");

		Label title = new Label("TECH MARKET");
		title.setStyle("-fx-text-fill:white;" + "-fx-font-size:18px;");

		Button home = menuButton("Dashboard");
		Button products = menuButton("Products");
		Button categories = menuButton("Categories");
		Button cart = menuButton("Shopping Cart");
		Button orders = menuButton("My Orders");

		side.getChildren().addAll(title, home, products, categories, cart, orders);
		setLeft(side);

		// أمر بسيط لتحديث الشاشة
		Runnable refreshTask = () -> showDashboard();

		home.setOnAction(e -> showDashboard());

		// 🔥 تم الحل هنا: مررنا الـ refreshTask للشاشات عشان يروح الإيرور
		products.setOnAction(e -> setCenter(new UserProductInterface(account, refreshTask)));
		categories.setOnAction(e -> setCenter(new UserCategoryInterface(account)));
		cart.setOnAction(e -> setCenter(new UserShoppingCartInterface(account, refreshTask)));

		orders.setOnAction(e -> setCenter(new UserOrdersInterface(account)));

	}

	private void showDashboard() {

		try {
			OrderDAO orderDAO = new OrderDAO();
			CartDAO cartDAO = new CartDAO();

			int totalOrdersCount = orderDAO.countByAccount(account.getAccountId());
			int session = cartDAO.getSession(account.getAccountId());
			int cartItemsCount = cartDAO.countCartItems(session);

			// هادي الدالة اللي بتجيب مجموع المصاريف
			double totalSpentMoney = orderDAO.getTotalSpentByAccount(account.getAccountId());

			HBox cards = new HBox(20);
			cards.setPadding(new Insets(10, 0, 10, 0));
			cards.getChildren().addAll(card("My Orders", "" + totalOrdersCount),
					card("Cart Items", "" + cartItemsCount), card("Total Spent", "$" + totalSpentMoney));

			recentOrdersTable = new TableView<>();

			TableColumn<Order, Integer> idCol = new TableColumn<>("Order ID");
			idCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));

			TableColumn<Order, Double> totalCol = new TableColumn<>("Total Price");
			totalCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

			TableColumn<Order, Integer> statusCol = new TableColumn<>("Status ID");
			statusCol.setCellValueFactory(new PropertyValueFactory<>("statusId"));

			recentOrdersTable.getColumns().addAll(idCol, totalCol, statusCol);
			recentOrdersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
			recentOrdersTable.setPrefHeight(350);

			ArrayList<Order> userOrders = orderDAO.getByAccount(account.getAccountId());
			recentOrdersTable.setItems(FXCollections.observableArrayList(userOrders));

			VBox root = new VBox(20);
			root.setPadding(new Insets(30));

			Label welcome = new Label("Welcome " + account.getFirstName());
			welcome.setStyle("-fx-font-size:25px; -fx-font-weight:bold;");

			Label tableTitle = new Label("My Recent Orders");
			tableTitle.setStyle("-fx-font-size:18px; -fx-font-weight:bold;");

			root.getChildren().addAll(welcome, cards, tableTitle, recentOrdersTable);
			setCenter(root);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private VBox card(String title, String value) {

		VBox box = new VBox(10);
		box.setAlignment(Pos.CENTER);
		box.setPrefSize(200, 110);
		box.setStyle("-fx-background-color:#123B70;" + "-fx-background-radius:15;");

		Label t = new Label(title);
		Label v = new Label(value);

		t.setStyle("-fx-text-fill:white; -fx-font-size:14px;");
		v.setStyle("-fx-text-fill:white;" + "-fx-font-size:26px;" + "-fx-font-weight:bold;");

		box.getChildren().addAll(t, v);
		return box;

	}

	private Button menuButton(String text) {

		Button b = new Button(text);
		b.setPrefWidth(160);
		b.setStyle("-fx-background-color:transparent;" + "-fx-text-fill:white;" + "-fx-alignment:center-left;");
		return b;

	}

}