package ui;

import dao.CategoryDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.Category;

public class AdminCategoryInterface extends BorderPane {

	private TableView<Category> table;

	private TextField nameField;
	private TextField descField;

	private CategoryDAO dao;

	private ObservableList<Category> list;

	public AdminCategoryInterface() {

		dao = new CategoryDAO();

		// إضافة حواف داخلية مريحة للعين حول الشاشة بأكملها
		setPadding(new Insets(20));

		createTable();
		createForm();

		loadCategories();
	}

	private void createTable() {

		table = new TableView<>();

		// استخدام السياسة الحرّة لتوزيع الأعمدة يدوياً بالملّي ومنع العمود الزائد
		table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

		TableColumn<Category, Integer> id = new TableColumn<>("ID");
		id.setCellValueFactory(new PropertyValueFactory<>("categoryId"));

		TableColumn<Category, String> name = new TableColumn<>("Name");
		name.setCellValueFactory(new PropertyValueFactory<>("categoryName"));

		TableColumn<Category, String> desc = new TableColumn<>("Description");
		desc.setCellValueFactory(new PropertyValueFactory<>("description"));

		// 🔥 توزيع المساحات هندسياً ليعطى الوصف مساحة أكبر (المجموع 100%)
		id.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.15)); // 15%
		name.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.35)); // 35%
		desc.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(0.50)); // 50%

		table.getColumns().addAll(id, name, desc);

		setCenter(table);
	}

	private void createForm() {

		// 1. 🔥 إضافة العنوان العلوي للشاشة وتنسيقه ليتطابق مع باقي النظام
		Label mainTitle = new Label("Categories Management");
		mainTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #0B1E3A;");

		nameField = new TextField();
		descField = new TextField();

		Button add = new Button("Add");
		Button update = new Button("Update");
		Button delete = new Button("Delete");
		Button refresh = new Button("Refresh");
		Button clear = new Button("Clear");

		GridPane gp = new GridPane();

		// ترتيب التباعدات للفورم
		gp.setPadding(new Insets(10, 0, 15, 0));
		gp.setHgap(10);
		gp.setVgap(10);

		gp.add(new Label("Name"), 0, 0);
		gp.add(nameField, 1, 0);

		gp.add(new Label("Description"), 0, 1);
		gp.add(descField, 1, 1);

		HBox buttons = new HBox(10, add, update, delete, refresh, clear);

		gp.add(buttons, 1, 2);

		// 2. 🔥 تجميع العنوان مع الفورم في حاوية VBox ووضعها في أعلى الـ BorderPane
		VBox topContainer = new VBox(5);
		topContainer.getChildren().addAll(mainTitle, gp);

		setTop(topContainer);

		// 3. ----------------- إعداد الأحداث والعمليات -----------------
		add.setOnAction(e -> {
			try {
				Category c = new Category();
				c.setCategoryName(nameField.getText());
				c.setDescription(descField.getText());

				dao.insert(c);
				loadCategories();
				clear();
			} catch (Exception ex) {
				showError(ex);
			}
		});

		update.setOnAction(e -> {
			Category c = table.getSelectionModel().getSelectedItem();
			if (c == null)
				return;

			try {
				c.setCategoryName(nameField.getText());
				c.setDescription(descField.getText());

				dao.update(c);
				loadCategories();
			} catch (Exception ex) {
				showError(ex);
			}
		});

		delete.setOnAction(e -> {
			Category c = table.getSelectionModel().getSelectedItem();
			if (c == null)
				return;

			try {
				dao.delete(c.getCategoryId());
				loadCategories();
				clear();
			} catch (Exception ex) {
				showError(ex);
			}
		});

		refresh.setOnAction(e -> loadCategories());

		clear.setOnAction(e -> clear());

		// مستمع الأحداث لتعبئة الحقول فور الضغط على أي سطر بالجدول
		table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal != null) {
				nameField.setText(newVal.getCategoryName());
				descField.setText(newVal.getDescription());
			}
		});
	}

	private void loadCategories() {
		try {
			list = FXCollections.observableArrayList(dao.getAll());
			table.setItems(list);
			table.refresh();
		} catch (Exception e) {
			showError(e);
		}
	}

	private void clear() {
		nameField.clear();
		descField.clear();
		table.getSelectionModel().clearSelection();
	}

	private void showError(Exception e) {
		Alert a = new Alert(Alert.AlertType.ERROR);
		a.setTitle("Error");
		a.setHeaderText(null);
		a.setContentText(e.getMessage());
		a.showAndWait();
	}
}