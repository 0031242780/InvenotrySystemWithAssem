package ui;

import java.sql.Connection;
import java.util.ArrayList;
import dao.CartDAO;
import dao.DBConnection;
import dao.OrderDAO;
import dao.OrderItemDAO;
import dao.PaymentDAO;
import dao.StockMovementDAO;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import model.Account;
import model.CartItem;
import model.OrderItem;
import model.Payment;

public class UserShoppingCartInterface extends BorderPane {

	private Account account;
	private TableView<CartItem> table;
	private Button checkoutBtn;
	private Runnable onOrderPlaced;

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

		TableColumn<CartItem, String> nameCol = new TableColumn<>("Product");
		nameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));

		TableColumn<CartItem, Integer> quantityCol = new TableColumn<>("Quantity");
		quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));

		TableColumn<CartItem, Double> priceCol = new TableColumn<>("Price");
		priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

		table.getColumns().addAll(nameCol, quantityCol, priceCol);
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
		CartDAO cartDAO = new CartDAO();
		OrderDAO orderDAO = new OrderDAO();
		OrderItemDAO itemDAO = new OrderItemDAO();
		PaymentDAO paymentDAO = new PaymentDAO();

		try {
			int session = cartDAO.getSession(account.getAccountId());
			ArrayList<CartItem> items = cartDAO.getCart(session);

			if (items.isEmpty()) {
				new Alert(Alert.AlertType.WARNING, "Your cart is empty!").showAndWait();
				return;
			}

			double totalAmount = 0.0;
			for (CartItem c : items) {
				totalAmount += (c.getPrice() * c.getQuantity());
			}

			try (Connection con = DBConnection.getConnection()) {
				con.setAutoCommit(false);
				try {
					int initialStatusId = 1;
					int orderId = orderDAO.createOrder(initialStatusId, account.getAccountId());

					if (orderId == -1) {
						throw new Exception("Failed to generate order reference ID.");
					}

					for (CartItem c : items) {
						OrderItem item = new OrderItem();
						item.setOrderId(orderId);
						item.setProductId(c.getProductId());
						item.setQuantity(c.getQuantity());
						item.setPriceAtPurchase(c.getPrice());

						itemDAO.insert(item);

						int deductionType = 2;
						StockMovementDAO.logMovementAndUpdateStock(
								c.getProductId(),
								c.getQuantity(),
								deductionType,
								"Stock deducted for Order #" + orderId,
								account.getAccountId(),
								con
						);
					}

					Payment payment = new Payment();
					payment.setOrderId(orderId);
					payment.setPaymentMethod("Credit Card");
					payment.setAmount(totalAmount);
					payment.setStatus("SUCCESS");
					paymentDAO.insertPayment(payment);

					cartDAO.clearCart(session);
					con.commit();

				} catch (Exception ex) {
					con.rollback();
					throw ex;
				}
			}

			new Alert(Alert.AlertType.INFORMATION, "Order created! Total charged: $" + String.format("%.2f", totalAmount)).showAndWait();

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