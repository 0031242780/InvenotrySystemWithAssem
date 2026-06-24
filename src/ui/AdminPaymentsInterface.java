package ui;

import dao.PaymentDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import model.Payment;
import java.sql.Timestamp;

public class AdminPaymentsInterface extends VBox {

    private TableView<Payment> table;
    private Label totalCollectedLabel;
    private PaymentDAO paymentDAO;
    private ObservableList<Payment> paymentList = FXCollections.observableArrayList();

    public AdminPaymentsInterface() {
        paymentDAO = new PaymentDAO();

        setSpacing(15);
        setPadding(new Insets(20));

        Label title = new Label("Transaction Ledger & Payments");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #0B1E3A;");

        HBox summaryBox = new HBox(10);
        summaryBox.setAlignment(Pos.CENTER_LEFT);
        summaryBox.setPadding(new Insets(10));
        summaryBox.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 5; -fx-border-color: #e5e7eb;");

        Label summaryTitle = new Label("Total Processed Funds:");
        summaryTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        totalCollectedLabel = new Label("$0.00");
        totalCollectedLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2ecc71;");
        summaryBox.getChildren().addAll(summaryTitle, totalCollectedLabel);

        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(450);

        TableColumn<Payment, Integer> idCol = new TableColumn<>("Payment ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("paymentId"));

        TableColumn<Payment, Integer> orderCol = new TableColumn<>("Order ID");
        orderCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));

        TableColumn<Payment, String> clientCol = new TableColumn<>("Customer Name");
        clientCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));

        TableColumn<Payment, String> methodCol = new TableColumn<>("Payment Method");
        methodCol.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));

        TableColumn<Payment, Double> amountCol = new TableColumn<>("Amount ($)");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountCol.setCellFactory(column -> new TableCell<Payment, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", item));
                }
            }
        });

        TableColumn<Payment, Timestamp> dateCol = new TableColumn<>("Transaction Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));

        TableColumn<Payment, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setCellFactory(column -> new TableCell<Payment, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equalsIgnoreCase("SUCCESS")) {
                        setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    }
                }
            }
        });

        table.getColumns().addAll(idCol, orderCol, clientCol, methodCol, amountCol, dateCol, statusCol);

        Button refreshBtn = new Button("Refresh Transactions");
        refreshBtn.setPadding(new Insets(10, 20, 10, 20));
        refreshBtn.setOnAction(e -> loadPaymentData());

        getChildren().addAll(title, summaryBox, table, refreshBtn);
        VBox.setVgrow(table, Priority.ALWAYS);

        loadPaymentData();
    }

    private void loadPaymentData() {
        try {
            paymentList.clear();
            paymentList.addAll(paymentDAO.getAllPayments());
            table.setItems(paymentList);

            double runningSum = 0;
            for (Payment p : paymentList) {
                if ("SUCCESS".equalsIgnoreCase(p.getStatus())) {
                    runningSum += p.getAmount();
                }
            }
            totalCollectedLabel.setText(String.format("$%.2f", runningSum));

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to load transactional ledger records:\n" + e.getMessage());
            alert.showAndWait();
        }
    }
}