package ui;

import java.util.ArrayList; // تم حل مشكلة ArrayList هنا
import dao.OrderDAO;
import dao.OrderItemDAO; // استدعاء الـ DAO
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import model.Account;
import model.Order;
import model.OrderItem; // استدعاء الـ Model الخاص بالقطع

public class UserOrdersInterface extends BorderPane {

	private Account account;

	private TableView<Order> table;
	private TableView<OrderItem> detailsTable; // جدول تفاصيل القطع الجديد

	private TableColumn<Order, Integer> idCol;
	private TableColumn<Order, Double> totalCol;
	private TableColumn<Order, String> statusCol;

	private OrderDAO dao;
	private OrderItemDAO orderItemDAO; // تعريف الـ DAO لحل المشكلة الثانية

	private ObservableList<Order> orders = FXCollections.observableArrayList();

	private VBox container;

	public UserOrdersInterface(Account account) {

		this.account = account;

		dao = new OrderDAO();
		orderItemDAO = new OrderItemDAO(); // تهيئة الكائن هنا

		createTable();

		buildLayout();

		loadOrders();

	}

	private void createTable() {

		table = new TableView<>();

		idCol = new TableColumn<>("Order ID");
		idCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));

		totalCol = new TableColumn<>("Total Price");
		totalCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

		statusCol = new TableColumn<>("Status ID");
		statusCol.setCellValueFactory(new PropertyValueFactory<>("statusId"));

		table.getColumns().addAll(idCol, totalCol, statusCol);

		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		table.setItems(orders);

		table.setPrefHeight(300); // تصغير الارتفاع لتكفي الشاشة للجدولين

		// كود المراقبة: لما الزبون يضغط على سطر بالجدول، يعبي الجدول التحتاني تلقائياً
		table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal != null) {
				try {
					// استدعاء دالة جلب القطع بناءً على رقم الطلب
					ArrayList<OrderItem> items = orderItemDAO.getItemsByOrder(newVal.getOrderId());
					detailsTable.setItems(FXCollections.observableArrayList(items));
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});

	}

	private void buildLayout() {

		container = new VBox(10);
		container.setPadding(new Insets(10));

		// إنشاء وتجهيز أعمدة جدول التفاصيل السفلي
		detailsTable = new TableView<>();

		TableColumn<OrderItem, Integer> pIdCol = new TableColumn<>("Product ID");
		pIdCol.setCellValueFactory(new PropertyValueFactory<>("productId"));

		TableColumn<OrderItem, Integer> qtyCol = new TableColumn<>("Quantity");
		qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));

		TableColumn<OrderItem, Double> priceCol = new TableColumn<>("Price At Purchase");
		priceCol.setCellValueFactory(new PropertyValueFactory<>("priceAtPurchase"));

		detailsTable.getColumns().addAll(pIdCol, qtyCol, priceCol);
		detailsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		detailsTable.setPrefHeight(250);

		// إضافة العناوين والجدولين داخل الواجهة
		container.getChildren().addAll(new Label("My Orders:"), table, new Label("Order Details (Items Included):"),
				detailsTable);

		setCenter(container);

	}

	private void loadOrders() {

		try {

			orders.clear();

			orders.addAll(dao.getByAccount(account.getAccountId()));

			table.refresh();

		} catch (Exception e) {

			showAlert(Alert.AlertType.ERROR, "Error", null, "Failed to load orders");

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