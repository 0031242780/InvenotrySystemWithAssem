package dao;

import java.sql.*;
import java.util.ArrayList;

import model.Order;

public class OrderDAO {

	public ArrayList<Order> getAll() throws Exception {
		ArrayList<Order> list = new ArrayList<>();
		// استعلام فخم بدمج الاسم الأول والثاني وبجيب اسم الحالة والشركة
		String sql = """
				SELECT o.*, c.company_name,
				       CONCAT(a.first_name, ' ', a.last_name) AS customer_name,
				       s.status_name
				FROM orders o
				LEFT JOIN delivery_company c ON o.company_id = c.company_id
				INNER JOIN accounts a ON o.account_id = a.account_id
				INNER JOIN order_statuses s ON o.status_id = s.status_id
				""";

		try (Connection con = DBConnection.getConnection();
				PreparedStatement ps = con.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				Order o = new Order();
				o.setOrderId(rs.getInt("order_id"));
				o.setTotalPrice(rs.getDouble("total_price"));
				o.setStatusId(rs.getInt("status_id"));
				o.setAccountId(rs.getInt("account_id"));

				int companyId = rs.getInt("company_id");
				if (!rs.wasNull()) {
					o.setCompanyId(companyId);
				}

				o.setCompanyName(rs.getString("company_name"));

				// 🔥 تعبئة الأسماء الجديدة المجلوبة من الـ JOIN
				o.setStatusName(rs.getString("status_name"));
				o.setCustomerName(rs.getString("customer_name"));
				list.add(o);
			}
		}
		return list;
	}

	public ArrayList<Order> getByAccount(int accountId) throws Exception {

		ArrayList<Order> list = new ArrayList<>();

		String sql = "SELECT order_id, total_price, status_id, company_id, account_id FROM orders WHERE account_id=?";

		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setInt(1, accountId);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {

				Order o = new Order();

				o.setOrderId(rs.getInt("order_id"));
				o.setTotalPrice(rs.getDouble("total_price"));
				o.setStatusId(rs.getInt("status_id"));

				int companyId = rs.getInt("company_id");
				if (!rs.wasNull()) {
					o.setCompanyId(companyId);
				}

				o.setAccountId(rs.getInt("account_id"));

				list.add(o);
			}
		}
		return list;
	}

	public void updateStatus(int orderId, int status) throws Exception {

		String sql = "UPDATE orders SET status_id=? WHERE order_id=?";

		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setInt(1, status);

			ps.setInt(2, orderId);

			ps.executeUpdate();

		}

	}

	public int countOrders() throws Exception {

		String sql = "SELECT COUNT(*) FROM orders";

		try (Connection con = DBConnection.getConnection();
				PreparedStatement ps = con.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			if (rs.next())
				return rs.getInt(1);

		}

		return 0;
	}

	public int countByAccount(int accountId) throws Exception {

		String sql = "SELECT COUNT(*) FROM orders WHERE account_id=?";

		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setInt(1, accountId);

			ResultSet rs = ps.executeQuery();

			if (rs.next())
				return rs.getInt(1);

		}

		return 0;

	}

	public int createOrder(double totalPrice, int accountId) throws Exception {
		// ركز في جملة الـ INSERT هان 👇
		String sql = "INSERT INTO orders(total_price, status_id, account_id) VALUES(?,?,?)";

		try (Connection con = DBConnection.getConnection();
				PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			ps.setDouble(1, totalPrice); // <--- أنت بتخزن الـ totalPrice هان
			ps.setInt(2, 1); // status_id = 1 (Pending)
			ps.setInt(3, accountId); // account_id

			ps.executeUpdate();

			ResultSet rs = ps.getGeneratedKeys();

			if (rs.next())
				return rs.getInt(1);

		}

		return -1;

	}

	public double getTotalSpentByAccount(int accountId) throws Exception {
		String sql = "SELECT SUM(total_price) FROM orders WHERE account_id = ?";

		// تم تصحيح السطر هنا وإزالة الـ .con الزائدة
		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setInt(1, accountId);

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getDouble(1);
				}
			}
		}
		return 0.0;
	}

	public void assignDeliveryCompany(int orderId, int companyId) throws Exception {
		String sql = "UPDATE orders SET company_id = ? WHERE order_id = ?";
		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setInt(1, companyId);
			ps.setInt(2, orderId);
			ps.executeUpdate();
		}
	}
}