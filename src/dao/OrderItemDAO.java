package dao;

import java.sql.*;
import java.util.ArrayList;

import model.OrderItem;

public class OrderItemDAO {

	public void insert(OrderItem item) throws Exception {
		String sql = "INSERT INTO order_item(order_id, product_id, quantity, price_at_purchase) VALUES (?, ?, ?, ?)";

		try (Connection con = DBConnection.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setInt(1, item.getOrderId());
			ps.setInt(2, item.getProductId());
			ps.setInt(3, item.getQuantity());
			ps.setDouble(4, item.getPriceAtPurchase());
			ps.executeUpdate();
		}
	}

	public int createOrder(int statusId, int accountId) throws Exception {
		String sql = "INSERT INTO orders(status_id, account_id) VALUES (?, ?)";

		try (Connection con = DBConnection.getConnection();
				PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			ps.setInt(1, statusId);
			ps.setInt(2, accountId);
			ps.executeUpdate();

			try (ResultSet rs = ps.getGeneratedKeys()) {
				if (rs.next()) {
					return rs.getInt(1);
				}
			}
		}
		return -1;
	}

	public ArrayList<OrderItem> getItemsByOrder(int orderId) throws Exception {
		ArrayList<OrderItem> list = new ArrayList<>();
		String sql = "SELECT * FROM order_item WHERE order_id = ?";

		try (Connection con = DBConnection.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setInt(1, orderId);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					OrderItem item = new OrderItem();
					item.setOrderId(rs.getInt("order_id"));
					item.setProductId(rs.getInt("product_id"));
					item.setQuantity(rs.getInt("quantity"));
					item.setPriceAtPurchase(rs.getDouble("price_at_purchase"));
					list.add(item);
				}
			}
		}
		return list;
	}
}