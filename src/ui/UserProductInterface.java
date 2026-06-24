package ui;

import dao.CartDAO;
import dao.ProductDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.Account;
import model.Product;

public class UserProductInterface extends VBox {

    private TableView<Product> table;
    private ProductDAO productDAO;
    private CartDAO cartDAO;
    private Account account;
    private Runnable onCartUpdated;

    private Spinner<Integer> quantitySpinner;

    public UserProductInterface(Account account, Runnable onCartUpdated) {
        this.account = account;
        this.onCartUpdated = onCartUpdated;

        productDAO = new ProductDAO();
        cartDAO = new CartDAO();

        table = new TableView<>();

        TableColumn<Product, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));

        TableColumn<Product, String> barcodeCol = new TableColumn<>("Barcode");
        barcodeCol.setCellValueFactory(new PropertyValueFactory<>("barcode"));

        TableColumn<Product, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<Product, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        table.getColumns().addAll(nameCol, barcodeCol, priceCol, descCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button refresh = new Button("Refresh");
        Button addCart = new Button("Add To Cart");

        Label qtyLabel = new Label("Qty:");
        quantitySpinner = new Spinner<>(1, 100, 1);
        quantitySpinner.setPrefWidth(80);
        quantitySpinner.setEditable(true);

        addCart.setOnAction(e -> {
            try {
                Product p = table.getSelectionModel().getSelectedItem();

                if (p == null) {
                    new Alert(Alert.AlertType.WARNING, "Please select a product!").showAndWait();
                    return;
                }

                int quantity = quantitySpinner.getValue();

                CartDAO dao = new CartDAO();
                int session = dao.getSession(account.getAccountId());

                dao.addToCart(session, p.getProductId(), quantity);

                new Alert(Alert.AlertType.INFORMATION, "Successfully added " + quantity + " item(s) to cart.").showAndWait();

                quantitySpinner.getValueFactory().setValue(1);

                if (onCartUpdated != null) {
                    onCartUpdated.run();
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Could not add item to cart: " + ex.getMessage()).showAndWait();
            }
        });

        refresh.setOnAction(e -> load());

        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER_LEFT);
        actionBox.getChildren().addAll(qtyLabel, quantitySpinner, addCart, refresh);

        setPadding(new Insets(10));
        setSpacing(10);

        getChildren().addAll(table, actionBox);
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