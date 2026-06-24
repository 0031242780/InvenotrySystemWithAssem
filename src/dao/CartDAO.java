package dao;

import java.sql.*;
import java.util.ArrayList;

import model.CartItem;

public class CartDAO {

	public int getSession(int accountId) throws Exception {

		String sql = "SELECT session_id FROM shopping_session WHERE account_id=?";

		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setInt(1, accountId);

			ResultSet rs = ps.executeQuery();

			if (rs.next())
				return rs.getInt("session_id");

		}


		String insert = "INSERT INTO shopping_session(create_at,updated_at,account_id) VALUES(NOW(),NOW(),?)";

		try (Connection con = DBConnection.getConnection();
				PreparedStatement ps = con.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {

			ps.setInt(1, accountId);

			ps.executeUpdate();

			ResultSet rs = ps.getGeneratedKeys();

			if (rs.next())
				return rs.getInt(1);

		}

		return -1;

	}

	public void addToCart(int sessionId, int productId, int quantity) throws Exception {

		String sql = """
				INSERT INTO cart_item
				(session_id,product_id,quantity)
				VALUES(?,?,?)

				ON DUPLICATE KEY UPDATE
				quantity = quantity + ?
				""";

		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setInt(1, sessionId);

			ps.setInt(2, productId);

			ps.setInt(3, quantity);

			ps.setInt(4, quantity);

			ps.executeUpdate();

		}

	}

	public ArrayList<CartItem> getCart(int sessionId) throws Exception {
		ArrayList<CartItem> list = new ArrayList<>();

		String sql = """
            SELECT 
                ci.session_id, ci.product_id, ci.quantity, p.product_name, 
                pp.retail_price, d.discounted_price
            FROM cart_item ci
            LEFT JOIN product p ON ci.product_id = p.product_id
            LEFT JOIN product_pricing pp ON p.product_id = pp.product_id
            LEFT JOIN discount d ON d.product_id = p.product_id 
                AND NOW() BETWEEN d.start_date AND d.end_date
            WHERE ci.session_id = ?
            """;

		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setInt(1, sessionId);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				CartItem c = new CartItem();
				c.setSessionId(rs.getInt("session_id"));
				c.setProductId(rs.getInt("product_id"));
				c.setQuantity(rs.getInt("quantity"));
				c.setProductName(rs.getString("product_name"));

				double retailPrice = rs.getDouble("retail_price");
				double discountedPrice = rs.getDouble("discounted_price");

				double finalUnitPrice = retailPrice;
				if (!rs.wasNull() && discountedPrice > 0) {
					finalUnitPrice = discountedPrice;
				}

				c.setPrice(finalUnitPrice * rs.getInt("quantity"));

				list.add(c);
			}
		}
		return list;
	}
	public void clearCart(int sessionId) throws Exception {

		String sql = "DELETE FROM cart_item WHERE session_id=?";

		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setInt(1, sessionId);

			ps.executeUpdate();

		}

	}

	public int countCartItems(int sessionId) throws Exception {

		String sql = "SELECT COUNT(*) FROM cart_item WHERE session_id=?";

		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setInt(1, sessionId);

			ResultSet rs = ps.executeQuery();

			if (rs.next())
				return rs.getInt(1);

		}

		return 0;
	}

}