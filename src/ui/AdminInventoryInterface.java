package ui;

import dao.ProductDAO;
import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import model.Product;

public class AdminInventoryInterface extends BorderPane {

	private TableView<Product> table;

	private TableColumn<Product, Integer> idCol;
	private TableColumn<Product, String> nameCol;
	private TableColumn<Product, Integer> qtyCol;

	private ProductDAO dao;
	private ObservableList<Product> products = FXCollections.observableArrayList();
	private VBox container;

	public AdminInventoryInterface() {
		dao = new ProductDAO();

		Label mainTitle = new Label("Inventory Stock Management");
		mainTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #0B1E3A;");
		mainTitle.setPadding(new Insets(0, 0, 10, 0));

		createTable();
		loadData();

		container = new VBox(10);
		container.setPadding(new Insets(20));
		container.getChildren().addAll(mainTitle, table);

		setCenter(container);
	}

	private void createTable() {
		table = new TableView<>();
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		table.setPrefHeight(600);
		table.setPrefWidth(800);

		idCol = new TableColumn<>("Product ID");
		idCol.setCellValueFactory(new PropertyValueFactory<>("productId"));

		nameCol = new TableColumn<>("Product Name");
		nameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));

		qtyCol = new TableColumn<>("Quantity In Stock");
		qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));

		qtyCol.setCellFactory(column -> new TableCell<Product, Integer>() {
			@Override
			protected void updateItem(Integer item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setText(null);
					setStyle("");
				} else {
					setText(String.valueOf(item));
					if (item <= 5) {
						setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
					} else {
						setStyle("-fx-text-fill: #2c3e50;");
					}
				}
			}
		});

		idCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.15));
		nameCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.60));
		qtyCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.25));

		table.getColumns().addAll(idCol, nameCol, qtyCol);
		table.setItems(products);
	}

	private void loadData() {
		try {
			products.clear();
			ArrayList<Product> inventoryList = dao.getInventory();
			products.addAll(inventoryList);
			table.refresh();
		} catch (Exception e) {
			showAlert(Alert.AlertType.ERROR, "Database Error", null,
					"Failed to load current store inventory from database!");
			e.printStackTrace();
		}
	}

	private void showAlert(Alert.AlertType type, String title, String header, String content) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);

		alert.getDialogPane().setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);

		alert.showAndWait();
	}
}