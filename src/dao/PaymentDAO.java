package dao;

import java.sql.*;
import java.util.ArrayList;
import model.Payment;

public class PaymentDAO {

    public void insert(Payment p) throws Exception {
        String sql = """
				INSERT INTO payment (order_id, payment_date, amount, payment_method, transaction_status)
				VALUES (?, NOW(), ?, ?, ?)
				""";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, p.getOrderId());
            ps.setDouble(2, p.getAmount());
            ps.setString(3, p.getPaymentMethod());
            ps.setString(4, p.getTransactionStatus());
            ps.executeUpdate();
        }
    }

    public ArrayList<Payment> getPaymentsByOrder(int orderId) throws Exception {
        ArrayList<Payment> list = new ArrayList<>();
        String sql = "SELECT * FROM payment WHERE order_id = ? ORDER BY payment_id DESC";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Payment p = new Payment();
                    p.setPaymentId(rs.getInt("payment_id"));
                    p.setOrderId(rs.getInt("order_id"));
                    p.setPaymentDate(rs.getTimestamp("payment_date"));
                    p.setAmount(rs.getDouble("amount"));
                    p.setPaymentMethod(rs.getString("payment_method"));
                    p.setTransactionStatus(rs.getString("transaction_status"));
                    list.add(p);
                }
            }
        }
        return list;
    }

    public void updateStatus(int paymentId, String status) throws Exception {
        String sql = "UPDATE payment SET transaction_status = ? WHERE payment_id = ?";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, paymentId);
            ps.executeUpdate();
        }
    }

    public double getTotalRevenue() throws Exception {
        String sql = "SELECT SUM(amount) FROM payment WHERE transaction_status = 'COMPLETED'";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                double total = rs.getDouble(1);
                if (!rs.wasNull()) {
                    return total;
                }
            }
        }
        return 0.0;
    }
}