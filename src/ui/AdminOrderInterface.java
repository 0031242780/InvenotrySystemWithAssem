package ui;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import dao.DBConnection;
import dao.OrderDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.Order;

public class AdminOrderInterface extends VBox {

	private TableView<Order> orderTable;
	private ObservableList<Order> ordersList = FXCollections.observableArrayList();

	private TableView<OrderDetail> detailsTable;
	private ObservableList<OrderDetail> detailsList = FXCollections.observableArrayList();

	private ComboBox<String> statusComboBox;
	private ComboBox<CompanyHelper> companyComboBox;

	private Button refreshBtn;
	private Button updateStatusBtn;
	private Button assignCompanyBtn;

	private OrderDAO orderDAO;

	public AdminOrderInterface() {
		orderDAO = new OrderDAO();

		setSpacing(15);
		setPadding(new Insets(20));

		Label mainTitle = new Label("Orders Management");
		mainTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #0B1E3A;");

		Label tableTitle = new Label("All System Orders List");
		tableTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0B1E3A;");

		createOrderTable();

		Label subTitle = new Label("Selected Order Included Items (Details)");
		subTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0B1E3A;");

		createDetailsTable();

		HBox bottomBox = new HBox(12);
		bottomBox.setAlignment(Pos.CENTER_LEFT);

		refreshBtn = new Button("Refresh Data");

		statusComboBox = new ComboBox<>();
		statusComboBox.setPromptText("Select Status");
		statusComboBox.getItems().addAll("Pending", "Processing", "Shipped", "Completed", "Cancelled");

		updateStatusBtn = new Button("Update Status");

		companyComboBox = new ComboBox<>();
		companyComboBox.setPromptText("Select Company");
		companyComboBox.setPrefWidth(150);

		assignCompanyBtn = new Button("Assign Company");

		bottomBox.getChildren().addAll(refreshBtn, statusComboBox, updateStatusBtn, companyComboBox, assignCompanyBtn);

		getChildren().addAll(mainTitle, tableTitle, orderTable, subTitle, detailsTable, bottomBox);

		setupActions();

		loadOrders();
		loadDeliveryCompanies();
	}

	private void createOrderTable() {
		orderTable = new TableView<>();
		orderTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
		orderTable.setPrefHeight(250);

		TableColumn<Order, Integer> idCol = new TableColumn<>("Order ID");
		idCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));

		TableColumn<Order, Double> totalCol = new TableColumn<>("Total Price ($)");
		totalCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

		TableColumn<Order, String> statusCol = new TableColumn<>("Status Name");
		statusCol.setCellValueFactory(new PropertyValueFactory<>("statusName"));

		TableColumn<Order, String> userCol = new TableColumn<>("Customer Name");
		userCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));

		TableColumn<Order, String> compNameCol = new TableColumn<>("Company Name");
		compNameCol.setCellValueFactory(new PropertyValueFactory<>("companyName"));

		idCol.prefWidthProperty().bind(orderTable.widthProperty().subtract(2).multiply(0.12));
		totalCol.prefWidthProperty().bind(orderTable.widthProperty().subtract(2).multiply(0.18));
		statusCol.prefWidthProperty().bind(orderTable.widthProperty().subtract(2).multiply(0.20));
		userCol.prefWidthProperty().bind(orderTable.widthProperty().subtract(2).multiply(0.25));
		compNameCol.prefWidthProperty().bind(orderTable.widthProperty().subtract(2).multiply(0.25));

		orderTable.getColumns().addAll(idCol, totalCol, statusCol, userCol, compNameCol);
		orderTable.setItems(ordersList);
	}

	private void createDetailsTable() {
		detailsTable = new TableView<>();
		detailsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		detailsTable.setPrefHeight(180);

		TableColumn<OrderDetail, String> pNameCol = new TableColumn<>("Product Name");
		pNameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));

		TableColumn<OrderDetail, Integer> qtyCol = new TableColumn<>("Quantity");
		qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));

		TableColumn<OrderDetail, Double> priceCol = new TableColumn<>("Price At Purchase ($)");
		priceCol.setCellValueFactory(new PropertyValueFactory<>("priceAtPurchase"));

		detailsTable.getColumns().addAll(pNameCol, qtyCol, priceCol);
		detailsTable.setItems(detailsList);
	}

	private void setupActions() {


		orderTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			if (newSelection != null) {
				loadOrderItems(newSelection.getOrderId());

				statusComboBox.setValue(newSelection.getStatusName());
			} else {
				detailsList.clear();
			}
		});

		refreshBtn.setOnAction(e -> {
			loadOrders();
			loadDeliveryCompanies();
			statusComboBox.setValue(null);
			companyComboBox.setValue(null);
		});

		updateStatusBtn.setOnAction(e -> {
			Order selected = orderTable.getSelectionModel().getSelectedItem();
			String selectedStatus = statusComboBox.getValue();

			if (selected == null) {
				showAlert(Alert.AlertType.WARNING, "Warning", "Please select an order from the table first!");
				return;
			}
			if (selectedStatus == null) {
				showAlert(Alert.AlertType.WARNING, "Warning", "Please select a new status from the list!");
				return;
			}

			int statusId = switch (selectedStatus) {
			case "Pending" -> 1;
			case "Processing" -> 2;
			case "Shipped" -> 3;
			case "Completed" -> 4;
			case "Cancelled" -> 5;
			default -> 1;
			};

			try {
				orderDAO.updateStatus(selected.getOrderId(), statusId);
				showAlert(Alert.AlertType.INFORMATION, "Success",
						"Order status updated to '" + selectedStatus + "' successfully!");
				loadOrders();
			} catch (Exception ex) {
				showAlert(Alert.AlertType.ERROR, "Error", ex.getMessage());
			}
		});

		assignCompanyBtn.setOnAction(e -> {
			Order selected = orderTable.getSelectionModel().getSelectedItem();
			CompanyHelper selectedCompany = companyComboBox.getValue();

			if (selected == null) {
				showAlert(Alert.AlertType.WARNING, "Warning", "Please select an order from the table first!");
				return;
			}
			if (selectedCompany == null) {
				showAlert(Alert.AlertType.WARNING, "Warning", "Please select a delivery company from the list!");
				return;
			}

			try {
				orderDAO.assignDeliveryCompany(selected.getOrderId(), selectedCompany.getCompanyId());
				showAlert(Alert.AlertType.INFORMATION, "Success",
						"Delivery company '" + selectedCompany.getCompanyName() + "' assigned successfully!");
				loadOrders();
			} catch (Exception ex) {
				showAlert(Alert.AlertType.ERROR, "Error", ex.getMessage());
			}
		});
	}

	private void loadOrders() {
		try {
			ordersList.clear();
			ordersList.addAll(orderDAO.getAll());
			orderTable.refresh();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void loadOrderItems(int orderId) {
		detailsList.clear();
		String sql = """
				SELECT oi.*, p.product_name
				FROM order_item oi
				INNER JOIN product p ON oi.product_id = p.product_id
				WHERE oi.order_id = ?
				""";
		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setInt(1, orderId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					OrderDetail item = new OrderDetail(rs.getInt("product_id"), rs.getString("product_name"),
							rs.getInt("quantity"), rs.getDouble("price_at_purchase"));
					detailsList.add(item);
				}
			}
			detailsTable.refresh();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void loadDeliveryCompanies() {
		ObservableList<CompanyHelper> options = FXCollections.observableArrayList();
		String sql = "SELECT company_id, company_name FROM delivery_company WHERE is_active = true";
		try (Connection con = DBConnection.getConnection();
				PreparedStatement ps = con.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				options.add(new CompanyHelper(rs.getInt("company_id"), rs.getString("company_name")));
			}
			companyComboBox.setItems(options);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void showAlert(Alert.AlertType type, String title, String content) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(content);
		alert.showAndWait();
	}


	public static class OrderDetail {
		private int productId;
		private String productName;
		private int quantity;
		private double priceAtPurchase;

		public OrderDetail(int productId, String productName, int quantity, double priceAtPurchase) {
			this.productId = productId;
			this.productName = productName;
			this.quantity = quantity;
			this.priceAtPurchase = priceAtPurchase;
		}

		public int getProductId() {
			return productId;
		}

		public String getProductName() {
			return productName;
		}

		public int getQuantity() {
			return quantity;
		}

		public double getPriceAtPurchase() {
			return priceAtPurchase;
		}
	}

	public static class CompanyHelper {
		private int companyId;
		private String companyName;

		public CompanyHelper(int companyId, String companyName) {
			this.companyId = companyId;
			this.companyName = companyName;
		}

		public int getCompanyId() {
			return companyId;
		}

		public String getCompanyName() {
			return companyName;
		}

		@Override
		public String toString() {
			return companyName;
		}
	}
}