package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class DiscountDAO {

	public void addDiscount(int productId, double discountedPrice) throws Exception {
		String sql = "INSERT INTO discount (product_id, start_date, end_date, discounted_price) "
				+ "VALUES (?, NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), ?)";

		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setInt(1, productId);
			ps.setDouble(2, discountedPrice);

			ps.executeUpdate();
		}
	}
}