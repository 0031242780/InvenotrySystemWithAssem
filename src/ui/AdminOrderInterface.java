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

	// 🔥 جدول تفاصيل عناصر الطلب السفلي المطور
	private TableView<OrderDetail> detailsTable;
	private ObservableList<OrderDetail> detailsList = FXCollections.observableArrayList();

	// 🔥 عناصر التحكم السفلية الذكية (ComboBox بدل الـ TextField)
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

		// 1. بناء الجدول العلوي للطلبات
		createOrderTable();

		Label subTitle = new Label("Selected Order Included Items (Details)");
		subTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0B1E3A;");

		// 2. بناء الجدول السفلي لتفاصيل المنتجات
		createDetailsTable();

		// 3. بناء شريط التحكم السفلي المتناسق
		HBox bottomBox = new HBox(12);
		bottomBox.setAlignment(Pos.CENTER_LEFT);

		refreshBtn = new Button("Refresh Data");

		// 🔥 القائمة المنسدلة لحالات الطلب المضمونة هندسياً
		statusComboBox = new ComboBox<>();
		statusComboBox.setPromptText("Select Status");
		statusComboBox.getItems().addAll("Pending", "Processing", "Shipped", "Completed", "Cancelled");

		updateStatusBtn = new Button("Update Status");

		// 🔥 القائمة المنسدلة لشركات التوصيل الحية من الداتابيز
		companyComboBox = new ComboBox<>();
		companyComboBox.setPromptText("Select Company");
		companyComboBox.setPrefWidth(150);

		assignCompanyBtn = new Button("Assign Company");

		bottomBox.getChildren().addAll(refreshBtn, statusComboBox, updateStatusBtn, companyComboBox, assignCompanyBtn);

		// تجميع الشاشة كاملة مكملة عمودياً
		getChildren().addAll(mainTitle, tableTitle, orderTable, subTitle, detailsTable, bottomBox);

		setupActions();

		// تحميل البيانات فوراً عند فتح الشاشة
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

		// 🔥 تم قلبها لـ Status Name وتعرض كلمات صريحة
		TableColumn<Order, String> statusCol = new TableColumn<>("Status Name");
		statusCol.setCellValueFactory(new PropertyValueFactory<>("statusName"));

		// 🔥 تم قلبها لـ Customer Name وتعرض اسم الزبون الكامل
		TableColumn<Order, String> userCol = new TableColumn<>("Customer Name");
		userCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));

		// اكتفينا باسم شركة التوصيل صراحةً وطيرنا الـ ID الرقمي المشوه
		TableColumn<Order, String> compNameCol = new TableColumn<>("Company Name");
		compNameCol.setCellValueFactory(new PropertyValueFactory<>("companyName"));

		// تقسيم الأبعاد بالتساوي الهندي الفخم
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

		// 🔥 العمود السحري الجديد: يعرض اسم المنتج بالكلمات
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

		// مراقب الجدول العلوي: عند الضغط على طلب، بروح يسحب عناصره بالأسماء فوراً
		// للجدول التحتاني
		orderTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			if (newSelection != null) {
				loadOrderItems(newSelection.getOrderId());

				// تحديد الحالة تلقائياً في الـ ComboBox بناءً على حالة الطلب المكبوس
				statusComboBox.setValue(newSelection.getStatusName());
			} else {
				detailsList.clear();
			}
		});

		// 🔄 أكشن كبسة التحديث
		refreshBtn.setOnAction(e -> {
			loadOrders();
			loadDeliveryCompanies();
			statusComboBox.setValue(null);
			companyComboBox.setValue(null);
		});

		// ⚡ أكشن تحديث الحالة المطور (ترجمة النص لرقم أوتوماتيكياً)
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

			// ترجمة النص الصافي إلى رقم الـ ID المقابل له بالداتابيز
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

		// 🚚 أكشن تعيين شركة التوصيل بالماوس
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

	// 🔥 دالة سحب عناصر الفاتورة مع عمل JOIN ذكي لجلب أسماء المنتجات صراحةً
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

	// دالة جلب شركات التوصيل الحية لتعبئة الـ ComboBox
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

	// 🔥 كائن داخلي (Inner Class) يمثل تفاصيل عناصر الفاتورة بالأسماء، عشان يشتغل
	// معك فوراً وبأمان
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

	// كائن داخلي مساعد لربط اسم شركة التوصيل بـ الـ ID تبعها جوّا الـ ComboBox
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
		} // يجبر الجافا تعرض الاسم بالماوس للآدمن
	}
}