package ui;

import java.util.ArrayList;
import dao.OrderDAO;
import dao.OrderItemDAO;
import dao.DeliveryCompanyDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import model.Order;
import model.OrderItem;
import model.DeliveryCompany;

public class AdminOrderInterface extends VBox {

	private TableView<Order> table;
	private TableColumn<Order, Integer> idCol;
	private TableColumn<Order, Double> priceCol;
	private TableColumn<Order, Integer> statusCol;
	private TableColumn<Order, Integer> accountCol;
	private TableColumn<Order, Integer> companyCol;
	private TableColumn<Order, String> companyNameCol;
	private TableView<OrderItem> detailsTable;
	private Button refreshBtn;
	private OrderDAO dao;
	private OrderItemDAO orderItemDAO;
	private DeliveryCompanyDAO deliveryCompanyDAO;
	private ObservableList<Order> orders = FXCollections.observableArrayList();
	private TextField statusField;
	private Button updateStatusBtn;
	private ComboBox<DeliveryCompany> companyComboBox;
	private Button assignCompanyBtn;

	public AdminOrderInterface() {
		dao = new OrderDAO();
		orderItemDAO = new OrderItemDAO();
		deliveryCompanyDAO = new DeliveryCompanyDAO();

		setSpacing(15);
		setPadding(new Insets(20));

		statusField = new TextField();
		statusField.setPromptText("New Status ID");
		statusField.setPrefWidth(120);

		updateStatusBtn = new Button("Update Status");
		updateStatusBtn.setPadding(new Insets(10));

		companyComboBox = new ComboBox<>();
		companyComboBox.setPromptText("Select Company");
		companyComboBox.setPrefWidth(150);

		assignCompanyBtn = new Button("Assign Company");
		assignCompanyBtn.setPadding(new Insets(10));

		createTable();
		createButtons();
		createLayout();
		loadData();
		loadCompanies();
	}

	private void createTable() {
		table = new TableView<>();
		table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

		idCol = new TableColumn<>("Order ID");
		idCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));

		priceCol = new TableColumn<>("Total Price ($)");
		priceCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

		statusCol = new TableColumn<>("Status ID");
		statusCol.setCellValueFactory(new PropertyValueFactory<>("statusId"));

		accountCol = new TableColumn<>("Customer ID");
		accountCol.setCellValueFactory(new PropertyValueFactory<>("accountId"));

		companyCol = new TableColumn<>("Company ID");
		companyCol.setCellValueFactory(new PropertyValueFactory<>("companyId"));

		companyNameCol = new TableColumn<>("Company Name");
		companyNameCol.setCellValueFactory(new PropertyValueFactory<>("companyName"));

		idCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.10));
		priceCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.15));
		statusCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.10));
		accountCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.15));
		companyCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.15));
		companyNameCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.35));

		table.getColumns().addAll(idCol, priceCol, statusCol, accountCol, companyCol, companyNameCol);
		table.setItems(orders);
		table.setPrefHeight(250);

		table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal != null) {
				try {
					ArrayList<OrderItem> items = orderItemDAO.getItemsByOrder(newVal.getOrderId());
					detailsTable.setItems(FXCollections.observableArrayList(items));
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
	}

	private void createButtons() {
		refreshBtn = new Button("Refresh Data");
		refreshBtn.setPadding(new Insets(10));

		refreshBtn.setOnAction(e -> {
			loadData();
			loadCompanies();
			if (detailsTable != null) {
				detailsTable.getItems().clear();
			}
		});

		updateStatusBtn.setOnAction(e -> {
			Order selectedOrder = table.getSelectionModel().getSelectedItem();
			if (selectedOrder == null) {
				showAlert(Alert.AlertType.WARNING, "Warning", null, "Please select an order from the table first!");
				return;
			}
			try {
				int newStatus = Integer.parseInt(statusField.getText());
				dao.updateStatus(selectedOrder.getOrderId(), newStatus);
				showAlert(Alert.AlertType.INFORMATION, "Success", null, "Order status updated successfully!");
				loadData();
				statusField.clear();
				if (detailsTable != null) {
					detailsTable.getItems().clear();
				}
			} catch (NumberFormatException nfe) {
				showAlert(Alert.AlertType.ERROR, "Input Error", null, "Please enter a valid number for Status ID!");
			} catch (Exception ex) {
				showAlert(Alert.AlertType.ERROR, "Error", null, ex.getMessage());
			}
		});

		assignCompanyBtn.setOnAction(e -> {
			Order selectedOrder = table.getSelectionModel().getSelectedItem();
			DeliveryCompany selectedCompany = companyComboBox.getValue();

			if (selectedOrder == null || selectedCompany == null) {
				showAlert(Alert.AlertType.WARNING, "Warning", null, "Please select both an order and a company!");
				return;
			}
			try {
				dao.assignDeliveryCompany(selectedOrder.getOrderId(), selectedCompany.getCompanyId());
				showAlert(Alert.AlertType.INFORMATION, "Success", null, "Delivery company assigned successfully!");
				loadData();
				if (detailsTable != null) {
					detailsTable.getItems().clear();
				}
			} catch (Exception ex) {
				showAlert(Alert.AlertType.ERROR, "Error", null, ex.getMessage());
			}
		});
	}

	private void createLayout() {
		Label mainTitle = new Label("Orders Management");
		mainTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #0B1E3A;");

		Label lblOrdersTable = new Label("All System Orders List");
		lblOrdersTable.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #555555;");

		Label lblDetailsTable = new Label("Selected Order Included Items (Details)");
		lblDetailsTable.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #555555;");

		detailsTable = new TableView<>();
		detailsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

		TableColumn<OrderItem, Integer> pIdCol = new TableColumn<>("Product ID");
		pIdCol.setCellValueFactory(new PropertyValueFactory<>("productId"));

		TableColumn<OrderItem, Integer> qtyCol = new TableColumn<>("Quantity");
		qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));

		TableColumn<OrderItem, Double> priceCol = new TableColumn<>("Price At Purchase ($)");
		priceCol.setCellValueFactory(new PropertyValueFactory<>("priceAtPurchase"));

		pIdCol.prefWidthProperty().bind(detailsTable.widthProperty().subtract(2).multiply(0.30));
		qtyCol.prefWidthProperty().bind(detailsTable.widthProperty().subtract(2).multiply(0.30));
		priceCol.prefWidthProperty().bind(detailsTable.widthProperty().subtract(2).multiply(0.40));

		detailsTable.getColumns().addAll(pIdCol, qtyCol, priceCol);
		detailsTable.setPrefHeight(180);

		HBox bottomBar = new HBox(10);
		bottomBar.setAlignment(Pos.CENTER_LEFT);
		bottomBar.getChildren().addAll(refreshBtn, statusField, updateStatusBtn, companyComboBox, assignCompanyBtn);

		getChildren().addAll(mainTitle, lblOrdersTable, table, lblDetailsTable, detailsTable, bottomBar);
	}

	private void loadData() {
		try {
			orders.clear();
			orders.addAll(dao.getAll());
			table.refresh();
		} catch (Exception e) {
			showAlert(Alert.AlertType.ERROR, "Error", null, e.getMessage());
		}
	}

	private void loadCompanies() {
		try {
			companyComboBox.getItems().clear();
			companyComboBox.getItems().addAll(deliveryCompanyDAO.getAll());
		} catch (Exception e) {
			e.printStackTrace();
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