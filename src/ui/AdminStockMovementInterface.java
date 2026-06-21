package ui;

import java.util.ArrayList;
import dao.StockMovementDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.StockMovement;

public class AdminStockMovementInterface extends VBox {

	private TableView<StockMovement> table;
	private TableColumn<StockMovement, Integer> idCol;
	private TableColumn<StockMovement, String> prodCol; // قلبناه String لعرض اسم المنتج
	private TableColumn<StockMovement, Integer> qtyCol;
	private TableColumn<StockMovement, String> typeCol; // قلبناه String لعرض نوع الحركة نصياً
	private TableColumn<StockMovement, String> notesCol;
	private TableColumn<StockMovement, String> dateCol; // عمود التاريخ والوقت الجديد

	private Button refreshBtn;
	private StockMovementDAO dao;
	private ObservableList<StockMovement> movements = FXCollections.observableArrayList();

	public AdminStockMovementInterface() {
		dao = new StockMovementDAO();

		setSpacing(15);
		setPadding(new Insets(20));

		// العنوان الرئيسي الفخم للشاشة
		Label mainTitle = new Label("Warehouse Stock Movements Log");
		mainTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #0B1E3A;");

		// شريط التحكم العلوي ويحتوي فقط على زر التحديث العام
		HBox topBar = new HBox(10);
		topBar.setAlignment(Pos.CENTER_LEFT);
		refreshBtn = new Button("Refresh Log History");
		refreshBtn.setStyle("-fx-font-weight: bold;");
		topBar.getChildren().add(refreshBtn);

		// بناء جدول المراقبة الواسع
		createTable();

		// تجميع الشاشة بشكل نظيف واحترافي
		VBox.setVgrow(table, Priority.ALWAYS);
		getChildren().addAll(mainTitle, topBar, table);

		// تشغيل الأحداث
		refreshBtn.setOnAction(e -> loadData());
		loadData();
	}

	private void createTable() {
		table = new TableView<>();
		table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

		idCol = new TableColumn<>("Log ID");
		idCol.setCellValueFactory(new PropertyValueFactory<>("movementId"));

		prodCol = new TableColumn<>("Product Name");
		prodCol.setCellValueFactory(new PropertyValueFactory<>("productName")); // ربط مع الحقل النصي المطور

		qtyCol = new TableColumn<>("Quantity Changed");
		qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantityChanged"));

		typeCol = new TableColumn<>("Movement Type");
		typeCol.setCellValueFactory(new PropertyValueFactory<>("typeName")); // ربط مع اسم النوع القادم من الـ JOIN

		notesCol = new TableColumn<>("Notes / System Trigger");
		notesCol.setCellValueFactory(new PropertyValueFactory<>("notes"));

		dateCol = new TableColumn<>("Timestamp");
		dateCol.setCellValueFactory(new PropertyValueFactory<>("createdAt")); // ربط مع تاريخ ووقت الحركة

		// 🔥 حركة UX: تلوين كميات الدخول باللون الأخضر والتالف/المبيعات باللون الأحمر
		// تلقائياً
		qtyCol.setCellFactory(column -> new TableCell<StockMovement, Integer>() {
			@Override
			protected void updateItem(Integer item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setText(null);
					setStyle("");
				} else {
					StockMovement sm = (StockMovement) getTableRow().getItem();
					if (sm != null && sm.getTypeId() == 1) { // 1 تعني Restock (دخول بضاعة)
						setText("+" + item);
						setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
					} else { // أي حركة سحب أو مبيعات أو تالف تظهر بالسالب باللون الأحمر
						setText("-" + item);
						setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
					}
				}
			}
		});

		// توزيع المساحات الهندسية بشكل متناسق ومريح للعين
		idCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.08)); // 8%
		prodCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.22)); // 22%
		qtyCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.15)); // 15%
		typeCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.15)); // 15%
		notesCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.22)); // 22%
		dateCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.18)); // 18%

		table.getColumns().addAll(idCol, prodCol, qtyCol, typeCol, notesCol, dateCol);
		table.setItems(movements);
		table.setPrefHeight(500);
	}

	private void loadData() {
		try {
			movements.clear();
			ArrayList<StockMovement> list = dao.getAllMovementsWithNames();
			movements.addAll(list);
			table.refresh();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}