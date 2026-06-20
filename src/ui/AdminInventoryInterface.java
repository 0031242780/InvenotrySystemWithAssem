package ui;

import dao.ProductDAO;

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

		createTable();

		loadData();

		buildLayout();

	}

	private void createTable() {

		table = new TableView<>();

		idCol = new TableColumn<>("Product ID");

		idCol.setCellValueFactory(new PropertyValueFactory<>("productId"));

		nameCol = new TableColumn<>("Product Name");

		nameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));

		qtyCol = new TableColumn<>("Quantity");

		qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));

		table.getColumns().addAll(idCol, nameCol, qtyCol);

		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		table.setItems(products);

		table.setPrefHeight(600);
		table.setPrefWidth(800);

	}

	private void loadData() {

		try {

			products.clear();

			products.addAll(dao.getInventory());

		} catch (Exception e) {

			showAlert(Alert.AlertType.ERROR, "Error", null, "Failed to load inventory");

		}

	}

	private void buildLayout() {

		container = new VBox(10);

		container.setPadding(new Insets(10));

		container.getChildren().add(table);

		setCenter(container);

	}

	private void showAlert(Alert.AlertType type, String title, String header, String content) {

		Alert alert = new Alert(type);

		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);

		alert.showAndWait();

	}

}