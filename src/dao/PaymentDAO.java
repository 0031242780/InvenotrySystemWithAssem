package dao;

import java.sql.*;
import java.util.ArrayList;
import model.Payment;

public class PaymentDAO {

    public ArrayList<Payment> getAllPayments() throws Exception {
        ArrayList<Payment> list = new ArrayList<>();
        String sql = """
				SELECT p.*, CONCAT(a.first_name, ' ', a.last_name) AS customer_name
				FROM payment p
				INNER JOIN orders o ON p.order_id = o.order_id
				INNER JOIN accounts a ON o.account_id = a.account_id
				ORDER BY p.payment_date DESC
				""";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Payment p = new Payment();
                p.setPaymentId(rs.getInt("payment_id"));
                p.setOrderId(rs.getInt("order_id"));
                p.setPaymentMethod(rs.getString("payment_method"));
                p.setAmount(rs.getDouble("amount"));
                p.setPaymentDate(rs.getTimestamp("payment_date"));
                p.setStatus(rs.getString("transaction_status"));
                p.setCustomerName(rs.getString("customer_name"));
                list.add(p);
            }
        }
        return list;
    }
    public void addProductSupplierLink(int productId, int supplierId, double supplyCost) throws Exception {
        String sql = """
				INSERT INTO provide (product_id, supplier_id, supply_cost) 
				VALUES (?, ?, ?)
				ON DUPLICATE KEY UPDATE supply_cost = ?
				""";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, productId);
            ps.setInt(2, supplierId);
            ps.setDouble(3, supplyCost);
            ps.setDouble(4, supplyCost);

            ps.executeUpdate();
        }
    }
    public void insertWholesaleExpense(int productId, double wholesaleCost, int quantity) throws Exception {
        String sql = "INSERT INTO payment (payment_method, amount, transaction_status) VALUES (?, ?, ?)";
        double totalExpense = -(wholesaleCost * quantity);

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "Supplier Invoice");
            ps.setDouble(2, totalExpense);
            ps.setString(3, "SUCCESS");

            ps.executeUpdate();
        }
    }
    public void insertPayment(Payment p) throws Exception {
        String sql = "INSERT INTO payment (order_id, payment_method, amount, transaction_status) VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, p.getOrderId());
            ps.setString(2, p.getPaymentMethod());
            ps.setDouble(3, p.getAmount());
            ps.setString(4, p.getStatus());
            ps.executeUpdate();
        }
    }
}