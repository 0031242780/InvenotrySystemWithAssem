package ui;

import dao.CartDAO;
import dao.ProductDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import model.Account;
import model.Product;

public class UserProductInterface extends VBox {

	private TableView<Product> table;

	private ProductDAO productDAO;
	private CartDAO cartDAO;

	private Account account;

	// عرفنا الـ Callback البسيط اللي بربط مع الدشبرد لتحديث العدادات
	private Runnable onCartUpdated;

	public UserProductInterface(Account account, Runnable onCartUpdated) {

		this.account = account;
		this.onCartUpdated = onCartUpdated;

		productDAO = new ProductDAO();
		cartDAO = new CartDAO();

		table = new TableView<>();

		TableColumn<Product, Integer> idCol = new TableColumn<>("ID");
		idCol.setCellValueFactory(new PropertyValueFactory<>("productId"));

		TableColumn<Product, String> nameCol = new TableColumn<>("Name");
		nameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));

		TableColumn<Product, String> barcodeCol = new TableColumn<>("Barcode");
		barcodeCol.setCellValueFactory(new PropertyValueFactory<>("barcode"));

		// ⚠️ السطر الجديد: ضفنا عمود السعر لعرضه للزبون بطريقة عادية ومفهومة
		TableColumn<Product, Double> priceCol = new TableColumn<>("Price");
		priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

		TableColumn<Product, String> descCol = new TableColumn<>("Description");
		descCol.setCellValueFactory(new PropertyValueFactory<>("description"));

		// ضفنا الـ priceCol جوا الجدول مع باقي الأعمدة
		table.getColumns().addAll(idCol, nameCol, barcodeCol, priceCol, descCol);
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		Button refresh = new Button("Refresh");
		Button addCart = new Button("Add To Cart");

		addCart.setOnAction(e -> {

			try {

				Product p = table.getSelectionModel().getSelectedItem();

				if (p == null) {
					new Alert(Alert.AlertType.WARNING, "Please select a product!").showAndWait();
					return;
				}

				CartDAO dao = new CartDAO();

				int session = dao.getSession(account.getAccountId());

				dao.addToCart(session, p.getProductId(), 1);

				new Alert(Alert.AlertType.INFORMATION, "Added to cart").showAndWait();

				// تحديث الأرقام في الدشبرد فوراً بعد الإضافة
				if (onCartUpdated != null) {
					onCartUpdated.run();
				}

			} catch (Exception ex) {

				ex.printStackTrace();

			}

		});
		refresh.setOnAction(e -> load());

		setPadding(new Insets(10));
		setSpacing(10);

		getChildren().addAll(table, addCart, refresh);

		VBox.setVgrow(table, javafx.scene.layout.Priority.ALWAYS);

		load();

	}

	private void load() {

		try {

			ObservableList<Product> list = FXCollections.observableArrayList(productDAO.getAllProducts());

			table.setItems(list);

		} catch (Exception e) {

			e.printStackTrace();

		}

	}

}