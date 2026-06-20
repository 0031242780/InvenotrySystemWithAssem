package ui;

import java.util.ArrayList;

import dao.CartDAO;
import dao.CategoryDAO;
import dao.ProductDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
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

	public UserCategoryInterface(Account account) {
		this.account = account;
		categoryDAO = new CategoryDAO();
		productDAO = new ProductDAO();

		// 1. إنشاء جدول الفئات
		categoryTable = new TableView<>();
		TableColumn<Category, Integer> catIdCol = new TableColumn<>("Category ID");
		catIdCol.setCellValueFactory(new PropertyValueFactory<>("categoryId"));
		TableColumn<Category, String> catNameCol = new TableColumn<>("Category Name");
		catNameCol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
		categoryTable.getColumns().addAll(catIdCol, catNameCol);
		categoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		categoryTable.setItems(categories);
		categoryTable.setPrefHeight(200);

		// 2. إنشاء جدول المنتجات التابعة للفئة
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

		// 3. زر إضافة المنتج المحدد إلى السلة
		Button addToCartBtn = new Button("Add Selected Product To Cart");
		addToCartBtn.setOnAction(e -> {
			Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
			if (selectedProduct == null) {
				new Alert(Alert.AlertType.WARNING, "Please select a product from the list!").showAndWait();
				return;
			}
			try {
				CartDAO cartDAO = new CartDAO();
				int session = cartDAO.getSession(account.getAccountId());
				cartDAO.addToCart(session, selectedProduct.getProductId(), 1);
				new Alert(Alert.AlertType.INFORMATION, "Product added to cart!").showAndWait();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});

		// 4. مراقبة الضغط على الفئة: عند اختيار فئة، يتم تحميل بضاعتها فوراً
		categoryTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal != null) {
				loadCategoryProducts(newVal.getCategoryId());
			}
		});

		// ترتيب الواجهة داخل صندوق عمودي بسيط
		VBox layout = new VBox(10);
		layout.setPadding(new Insets(10));
		layout.getChildren().addAll(new Label("Select a Category:"), categoryTable,
				new Label("Products in this Category:"), productTable, addToCartBtn);

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
			// 1. تنظيف الجدول أولاً
			products.clear();

			// 2. جلب القائمة البسيطة من الـ DAO
			ArrayList<Product> list = productDAO.getProductsByCategory(categoryId);

			// 3. إضافتها للجدول مباشرة بدون أي تعارض في الأنواع
			products.addAll(list);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}