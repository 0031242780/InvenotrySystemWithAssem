package ui;

import java.util.ArrayList;

import dao.CartDAO;
import dao.CategoryDAO;
import dao.ProductDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.Account;
import model.Category;
import model.Product;

public class UserCategoryInterface extends BorderPane {

	private TableView<Category> categoryTable;
	private TableView<Product> productTable;

	private CategoryDAO categoryDAO;
	private ProductDAO productDAO;
	private Account account;

	private ObservableList<Category> categories = FXCollections.observableArrayList();
	private ObservableList<Product> products = FXCollections.observableArrayList();

	private Spinner<Integer> quantitySpinner;

	public UserCategoryInterface(Account account) {
		this.account = account;
		categoryDAO = new CategoryDAO();
		productDAO = new ProductDAO();

		categoryTable = new TableView<>();

		TableColumn<Category, String> catNameCol = new TableColumn<>("Category Name");
		catNameCol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
		categoryTable.getColumns().addAll(catNameCol);
		categoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		categoryTable.setItems(categories);
		categoryTable.setPrefHeight(200);

		productTable = new TableView<>();
		TableColumn<Product, String> pNameCol = new TableColumn<>("Product Name");
		pNameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
		TableColumn<Product, Double> pPriceCol = new TableColumn<>("Price");
		pPriceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
		TableColumn<Product, String> pDescCol = new TableColumn<>("Description");
		pDescCol.setCellValueFactory(new PropertyValueFactory<>("description"));
		productTable.getColumns().addAll(pNameCol, pPriceCol, pDescCol);
		productTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		productTable.setItems(products);
		productTable.setPrefHeight(300);

		Button addToCartBtn = new Button("Add Selected Product To Cart");

		Label qtyLabel = new Label("Qty:");
		quantitySpinner = new Spinner<>(1, 100, 1);
		quantitySpinner.setPrefWidth(80);
		quantitySpinner.setEditable(true);

		addToCartBtn.setOnAction(e -> {
			Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
			if (selectedProduct == null) {
				new Alert(Alert.AlertType.WARNING, "Please select a product from the list!").showAndWait();
				return;
			}
			try {
				int quantity = quantitySpinner.getValue();

				CartDAO cartDAO = new CartDAO();
				int session = cartDAO.getSession(account.getAccountId());

				cartDAO.addToCart(session, selectedProduct.getProductId(), quantity);

				new Alert(Alert.AlertType.INFORMATION, "Successfully added " + quantity + " item(s) to cart.").showAndWait();

				quantitySpinner.getValueFactory().setValue(1);
			} catch (Exception ex) {
				ex.printStackTrace();
				new Alert(Alert.AlertType.ERROR, "Could not add item to cart: " + ex.getMessage()).showAndWait();
			}
		});

		categoryTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal != null) {
				loadCategoryProducts(newVal.getCategoryId());
			}
		});

		HBox actionBox = new HBox(10);
		actionBox.setAlignment(Pos.CENTER_LEFT);
		actionBox.getChildren().addAll(qtyLabel, quantitySpinner, addToCartBtn);

		VBox layout = new VBox(10);
		layout.setPadding(new Insets(10));
		layout.getChildren().addAll(
				new Label("Select a Category:"), categoryTable,
				new Label("Products in this Category:"), productTable,
				actionBox
		);

		setCenter(layout);
		loadCategories();
	}

	private void loadCategories() {
		try {
			categories.clear();
			categories.addAll(categoryDAO.getAll());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void loadCategoryProducts(int categoryId) {
		try {
			products.clear();
			ArrayList<Product> list = productDAO.getProductsByCategory(categoryId);
			products.addAll(list);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}