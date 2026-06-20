package ui;

import java.util.ArrayList;
import dao.StockMovementDAO;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.StockMovement;

public class AdminStockMovementInterface extends VBox {

	private TextField prodIdField;
	private TextField qtyField;
	private TextField notesField;

	private ComboBox<String> typeComboBox;

	private Button addBtn;
	private Button refreshBtn;
	private Button clearBtn;

	private TableView<StockMovement> table;
	private TableColumn<StockMovement, Integer> idCol;
	private TableColumn<StockMovement, Integer> prodCol;
	private TableColumn<StockMovement, Integer> qtyCol;
	private TableColumn<StockMovement, Integer> typeCol;
	private TableColumn<StockMovement, Integer> accCol;

	private StockMovementDAO dao;
	private ObservableList<StockMovement> movements = FXCollections.observableArrayList();

	public AdminStockMovementInterface() {

		dao = new StockMovementDAO();

		// ضبط التباعد والحواف لراحة العين وتناسق الشاشات
		setSpacing(15);
		setPadding(new Insets(20));

		// 1. 🔥 إضافة وتنسيق العنوان العلوي للشاشة ليتطابق مع هوية النظام
		Label mainTitle = new Label("Stock Movements Management");
		mainTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #0B1E3A;");

		// بناء النموذج (Form)
		GridPane form = new GridPane();
		form.setHgap(10);
		form.setVgap(10);

		form.add(new Label("Product ID"), 0, 0);
		prodIdField = new TextField();
		form.add(prodIdField, 1, 0);

		form.add(new Label("Quantity"), 0, 1);
		qtyField = new TextField();
		form.add(qtyField, 1, 1);

		form.add(new Label("Movement Type"), 0, 2);
		typeComboBox = new ComboBox<>();
		typeComboBox.getItems().addAll("Restock (Incoming)", "Damaged (Outgoing)");
		typeComboBox.setValue("Restock (Incoming)");
		typeComboBox.setPrefWidth(150);
		form.add(typeComboBox, 1, 2);

		form.add(new Label("Notes"), 0, 3);
		notesField = new TextField();
		form.add(notesField, 1, 3);

		// شريط الأزرار (Buttons)
		HBox buttonsBar = new HBox(10);
		buttonsBar.setAlignment(Pos.CENTER_LEFT);

		addBtn = new Button("Add");
		refreshBtn = new Button("Refresh");
		clearBtn = new Button("Clear");

		addBtn.setPrefWidth(70);
		refreshBtn.setPrefWidth(80);
		clearBtn.setPrefWidth(70);

		buttonsBar.getChildren().addAll(addBtn, refreshBtn, clearBtn);

		// بناء الجدول وتوزيع مساحاته
		createTable();

		// تجميع المكونات بالترتيب الهندسي الصحيح (العنوان أولاً)
		getChildren().addAll(mainTitle, form, buttonsBar, table);

		setupActions();
		loadData();
	}

	private void createTable() {
		table = new TableView<>();

		// إلغاء السياسة الافتراضية للتحكم بالملّي ومنع ظهور العمود الزائد
		table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

		idCol = new TableColumn<>("ID");
		idCol.setCellValueFactory(new PropertyValueFactory<>("movementId"));

		prodCol = new TableColumn<>("Product ID");
		prodCol.setCellValueFactory(new PropertyValueFactory<>("productId"));

		qtyCol = new TableColumn<>("Quantity Changed");
		qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantityChanged"));

		typeCol = new TableColumn<>("Type");
		typeCol.setCellValueFactory(new PropertyValueFactory<>("typeId"));

		accCol = new TableColumn<>("Logged By (Account ID)");
		accCol.setCellValueFactory(new PropertyValueFactory<>("accountId"));

		// 🔥 حركة UX فخمة: تحويل الـ Type ID لنصوص ملونة مفهومة للدكتور (أخضر للداخل
		// وأحمر للتالف)
		typeCol.setCellFactory(column -> new TableCell<StockMovement, Integer>() {
			@Override
			protected void updateItem(Integer item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setText(null);
					setStyle("");
				} else {
					if (item == 1) {
						setText("Restock (+)");
						setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
					} else if (item == 2) {
						setText("Damaged (-)");
						setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
					} else {
						setText("Unknown (" + item + ")");
						setStyle("");
					}
				}
			}
		});

		// 🔥 توزيع مساحات العرض هندسياً بالملّي (المجموع 100%) مع خصم الـ 2 بكسل للحواف
		idCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.10)); // 10%
		prodCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.20)); // 20%
		qtyCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.25)); // 25%
		typeCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.20)); // 20%
		accCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.25)); // 25%

		table.getColumns().addAll(idCol, prodCol, qtyCol, typeCol, accCol);
		table.setItems(movements);
		table.setPrefHeight(380);
	}

	private void setupActions() {

		addBtn.setOnAction(e -> {
			try {
				int prodId = Integer.parseInt(prodIdField.getText());
				int qty = Integer.parseInt(qtyField.getText());
				String notes = notesField.getText();

				int typeId = typeComboBox.getValue().equals("Restock (Incoming)") ? 1 : 2;
				int accountId = 1; // تثبيت رقم حساب الآدمن الافتراضي في نظامك

				StockMovement sm = new StockMovement();
				sm.setProductId(prodId);
				sm.setQuantityChanged(qty);
				sm.setTypeId(typeId);
				sm.setAccountId(accountId);
				sm.setNotes(notes);

				dao.save(sm);

				showAlert(Alert.AlertType.INFORMATION, "Success", "Movement logged successfully!");
				loadData();
				clearFields();

			} catch (NumberFormatException nfe) {
				showAlert(Alert.AlertType.ERROR, "Input Error",
						"Please fill Product ID and Quantity with valid numbers!");
			} catch (Exception ex) {
				showAlert(Alert.AlertType.ERROR, "Database Error", ex.getMessage());
			}
		});

		refreshBtn.setOnAction(e -> loadData());
		clearBtn.setOnAction(e -> clearFields());
	}

	private void loadData() {
		try {
			movements.clear();
			ArrayList<StockMovement> list = dao.getAll();
			movements.addAll(list);
			table.refresh();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void clearFields() {
		prodIdField.clear();
		qtyField.clear();
		notesField.clear();
		typeComboBox.setValue("Restock (Incoming)");
	}

	private void showAlert(Alert.AlertType type, String title, String content) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(content);
		alert.showAndWait();
	}
}