package ui;

import java.util.ArrayList;
import dao.CartDAO;
import dao.OrderDAO;
import dao.OrderItemDAO;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import model.Account;
import model.CartItem;
import model.OrderItem;

public class UserShoppingCartInterface extends BorderPane {

	private Account account;
	private TableView<CartItem> table;
	private Button checkoutBtn;
	private Runnable onOrderPlaced; // Callback لتحديث لوحة التحكم بعد الشراء

	public UserShoppingCartInterface(Account account, Runnable onOrderPlaced) {
		this.account = account;
		this.onOrderPlaced = onOrderPlaced;

		createTable();

		checkoutBtn = new Button("Checkout");
		checkoutBtn.setOnAction(e -> checkout());

		VBox bottom = new VBox(10);
		bottom.setPadding(new Insets(10));
		bottom.getChildren().add(checkoutBtn);

		setCenter(table);
		setBottom(bottom);

		loadCart();
	}

	private void createTable() {
		table = new TableView<>();

		TableColumn<CartItem, Integer> productCol = new TableColumn<>("Product ID");
		productCol.setCellValueFactory(new PropertyValueFactory<>("productId"));

		TableColumn<CartItem, String> nameCol = new TableColumn<>("Product");
		nameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));

		TableColumn<CartItem, Integer> quantityCol = new TableColumn<>("Quantity");
		quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));

		TableColumn<CartItem, Double> priceCol = new TableColumn<>("Price");
		priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

		table.getColumns().addAll(productCol, nameCol, quantityCol, priceCol);
		table.setPrefHeight(500);
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
	}

	private void loadCart() {
		try {
			CartDAO dao = new CartDAO();
			int session = dao.getSession(account.getAccountId());
			table.setItems(FXCollections.observableArrayList(dao.getCart(session)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void checkout() {
		try {
			CartDAO cartDAO = new CartDAO();
			OrderDAO orderDAO = new OrderDAO();
			OrderItemDAO itemDAO = new OrderItemDAO();

			int session = cartDAO.getSession(account.getAccountId());
			ArrayList<CartItem> items = cartDAO.getCart(session);

			if (items.isEmpty()) {
				new Alert(Alert.AlertType.WARNING, "Your cart is empty!").showAndWait();
				return;
			}

			// 1. احسب المجموع الإجمالي للطلب كامل أولاً
			double totalOrderPrice = 0.0;
			for (CartItem c : items) {
				totalOrderPrice += c.getQuantity() * c.getPrice(); // الكمية × السعر
			}

			// 2. تم الإصلاح: مرر المجموع الحقيقي المحسوب (totalOrderPrice) بدلاً من الصفر
			// الثابت
			int orderId = orderDAO.createOrder(totalOrderPrice, account.getAccountId());

			for (CartItem c : items) {
				OrderItem item = new OrderItem();
				item.setOrderId(orderId);
				item.setProductId(c.getProductId());
				item.setQuantity(c.getQuantity());

				// إرسال السعر الفعلي للمنتج عند الشراء
				item.setPriceAtPurchase(c.getPrice());

				itemDAO.insert(item);
			}

			// تفريغ عربة التسوق بعد الشراء بنجاح
			cartDAO.clearCart(session);

			new Alert(Alert.AlertType.INFORMATION, "Order Created Successfully!").showAndWait();

			// تشغيل التحديث التلقائي فوراً لتنعكس التغييرات في الواجهة الرئيسية للزبون
			if (onOrderPlaced != null) {
				onOrderPlaced.run();
			}

			loadCart();

		} catch (Exception e) {
			e.printStackTrace();
			new Alert(Alert.AlertType.ERROR, "Checkout Failed\n" + e.getMessage()).showAndWait();
		}
	}
}