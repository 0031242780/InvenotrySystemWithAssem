package ui;

import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import model.DeliveryCompany;
import dao.DeliveryCompanyDAO;

public class AdminDeliveryCompanyInterface extends VBox {

	private TableView<DeliveryCompany> table;
	private TextField companyField, contactField, phoneField;
	private CheckBox activeCheckBox;
	private DeliveryCompanyDAO dao;

	public AdminDeliveryCompanyInterface() {
		dao = new DeliveryCompanyDAO();

		// إعدادات التباعد للشاشة الرئيسية
		setSpacing(15);
		setPadding(new Insets(20));

		// 1. العنوان العلوي للشاشة بتنسيق فخم
		Label mainTitle = new Label("Delivery Companies Management");
		mainTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #0B1E3A;");

		// إنشاء سطر المحتوى (الجدول يسار، والفورم يمين)
		HBox contentRow = new HBox(20);
		VBox.setVgrow(contentRow, Priority.ALWAYS);

		// 2. ----------------- إعداد الجدول وتمديده هندسياً -----------------
		table = new TableView<>();
		HBox.setHgrow(table, Priority.ALWAYS);
		table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

		// تعريف الأعمدة الخمسة وربطها بالموديل بالملّي
		TableColumn<DeliveryCompany, Integer> idCol = new TableColumn<>("ID");
		idCol.setCellValueFactory(new PropertyValueFactory<>("companyId"));

		TableColumn<DeliveryCompany, String> companyCol = new TableColumn<>("Company");
		companyCol.setCellValueFactory(new PropertyValueFactory<>("companyName"));

		TableColumn<DeliveryCompany, String> contactCol = new TableColumn<>("Contact Person");
		contactCol.setCellValueFactory(new PropertyValueFactory<>("contactPerson"));

		TableColumn<DeliveryCompany, String> phoneCol = new TableColumn<>("Phone");
		phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));

		TableColumn<DeliveryCompany, Boolean> activeCol = new TableColumn<>("Status");
		activeCol.setCellValueFactory(new PropertyValueFactory<>("active"));

		// تلوين وتنسيق عرض حالة الشركة (Active / Inactive) بشكل احترافي
		activeCol.setCellFactory(column -> new TableCell<DeliveryCompany, Boolean>() {
			@Override
			protected void updateItem(Boolean item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setText(null);
					setStyle("");
				} else {
					if (item) {
						setText("Active");
					} else {
						setText("Inactive");
					}
				}
			}
		});

		// توزيع العرض هندسياً بالتساوي (المجموع 100%) وطرح الـ 2 بكسل السحرية لمنع
		// العمود الزائد
		idCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.10)); // 10%
		companyCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.25)); // 25%
		contactCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.25)); // 25%
		phoneCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.25)); // 25%
		activeCol.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.15)); // 15%

		table.getColumns().addAll(idCol, companyCol, contactCol, phoneCol, activeCol);

		// 3. ----------------- إعداد الفورم الجانبي (اليمين) -----------------
		VBox formBox = new VBox(10);
		formBox.setPrefWidth(200);
		formBox.setMinWidth(200);

		Label lblCompany = new Label("Company Name");
		companyField = new TextField();

		Label lblContact = new Label("Contact Person");
		contactField = new TextField();

		Label lblPhone = new Label("Phone");
		phoneField = new TextField();

		activeCheckBox = new CheckBox("Active Status");

		// أزرار التحكم السفلى بالفورم
		HBox actionsBox = new HBox(8);
		actionsBox.setAlignment(Pos.CENTER_LEFT);
		Button addBtn = new Button("Add");
		Button deleteBtn = new Button("Delete");
		Button refreshBtn = new Button("Refresh");
		actionsBox.getChildren().addAll(addBtn, deleteBtn, refreshBtn);

		formBox.getChildren().addAll(lblCompany, companyField, lblContact, contactField, lblPhone, phoneField,
				activeCheckBox, actionsBox);

		// 4. ----------------- تشغيل الأحداث والربط الذكي -----------------

		// حدث جلب البيانات عند الضغط على زر ريفريش
		refreshBtn.setOnAction(e -> loadData());

		// 🔥 ميزة الـ UX: عند الضغط على أي سطر بالجدول، تتعبأ البيانات بالفورم فوراً
		table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			if (newSelection != null) {
				companyField.setText(newSelection.getCompanyName());
				contactField.setText(newSelection.getContactPerson());
				phoneField.setText(newSelection.getPhone());
				activeCheckBox.setSelected(newSelection.isActive());
			}
		});

		// تجميع الجدول والفورم داخل السطر الأفقي
		contentRow.getChildren().addAll(table, formBox);

		// تجميع الشاشة بالكامل داخل الـ VBox الرئيسي
		getChildren().addAll(mainTitle, contentRow);

		// تحميل البيانات تلقائياً عند تشغيل الواجهة
		loadData();
	}

	// دالة جلب البيانات من الـ DAO وتحديث الجدول
	private void loadData() {
		try {
			ArrayList<DeliveryCompany> list = dao.getAll();
			table.setItems(FXCollections.observableArrayList(list));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}