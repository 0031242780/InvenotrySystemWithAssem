package dao;

import model.Account;
import java.sql.*;
import java.util.ArrayList;

public class AccountDAO {

	// ➕ 1. دالة الإضافة الديناميكية
	public void insert(Account a) throws Exception {
		String sql = "INSERT INTO accounts(role_id, email, password_, first_name, last_name, phone_number, city, street) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setInt(1, a.getRoleId());
			ps.setString(2, a.getEmail());
			ps.setString(3, a.getPassword());
			ps.setString(4, a.getFirstName());
			ps.setString(5, a.getLastName());
			ps.setString(6, a.getPhoneNumber());
			ps.setString(7, a.getCity());
			ps.setString(8, a.getStreet());

			ps.executeUpdate();
		}
	}

	// 🔄 2. دالة التعديل لشاشة إدارة الحسابات
	public void update(Account a) throws Exception {
		String sql = "UPDATE accounts SET role_id = ?, email = ?, first_name = ?, last_name = ?, phone_number = ? WHERE account_id = ?";

		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setInt(1, a.getRoleId());
			ps.setString(2, a.getEmail());
			ps.setString(3, a.getFirstName());
			ps.setString(4, a.getLastName());
			ps.setString(5, a.getPhoneNumber());
			ps.setInt(6, a.getAccountId());

			ps.executeUpdate();
		}
	}

	// 🔄 3. تحويل الحذف الفعلي إلى تعطيل حساب (Soft Delete) لحماية ترابط البيانات
	public void deactivate(int accountId) throws Exception {
		String sql = "UPDATE accounts SET is_active = FALSE WHERE account_id = ?";

		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setInt(1, accountId);
			ps.executeUpdate();
		}
	}

	// 🔑 4. دالة تسجيل الدخول المحدثة (تمنع دخول الحسابات المعطلة)
	public Account login(String email, String password) throws Exception {
		// 🔥 أضفنا شرط حماية أمني: AND a.is_active = TRUE
		String sql = "SELECT a.*, r.role_name, r.permission_level " + "FROM accounts a "
				+ "JOIN roles r ON a.role_id = r.role_id " + "WHERE a.email=? AND a.password_=? AND a.is_active = TRUE";

		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setString(1, email);
			ps.setString(2, password);

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					Account a = new Account();
					a.setAccountId(rs.getInt("account_id"));
					a.setRoleId(rs.getInt("role_id"));
					a.setEmail(rs.getString("email"));
					a.setFirstName(rs.getString("first_name"));
					a.setLastName(rs.getString("last_name"));
					return a;
				}
			}
		}
		return null;
	}

	// 📊 5. دالة جلب كافة الحسابات (محدثة لتقرأ الحالة وتمررها للـ UI)
	public ArrayList<Account> getAllAccounts() throws Exception {
		ArrayList<Account> list = new ArrayList<>();
		String sql = "SELECT a.*, r.role_name FROM accounts a JOIN roles r ON a.role_id = r.role_id";

		try (Connection con = DBConnection.getConnection();
				PreparedStatement ps = con.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				Account a = new Account();
				a.setAccountId(rs.getInt("account_id"));
				a.setRoleId(rs.getInt("role_id"));
				a.setEmail(rs.getString("email"));
				a.setFirstName(rs.getString("first_name"));
				a.setLastName(rs.getString("last_name"));
				a.setPhoneNumber(rs.getString("phone_number"));
				a.setCity(rs.getString("city"));
				a.setStreet(rs.getString("street"));
				a.setRoleName(rs.getString("role_name"));

				// 🔥 هان السطر السحري اللي كان ناقص عشان يغذي الـ UI بالحالة الصح
				a.setActive(rs.getBoolean("is_active"));

				list.add(a);
			}
		}
		return list;
	}

	// 🛒 6. دالة جلب الزبائن فقط (رقم 3)
	public ArrayList<Account> getCustomers() throws Exception {
		ArrayList<Account> list = new ArrayList<>();
		String sql = "SELECT * FROM accounts WHERE role_id = 3";

		try (Connection con = DBConnection.getConnection();
				PreparedStatement ps = con.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				Account a = new Account();
				a.setAccountId(rs.getInt("account_id"));
				a.setRoleId(rs.getInt("role_id"));
				a.setEmail(rs.getString("email"));
				a.setFirstName(rs.getString("first_name"));
				a.setLastName(rs.getString("last_name"));
				a.setPhoneNumber(rs.getString("phone_number"));

				list.add(a);
			}
		}
		return list;
	}

	// 🧮 7. دالة عدّ الزبائن للداشبورد
	public int countCustomers() throws Exception {
		String sql = "SELECT COUNT(*) FROM accounts WHERE role_id = 3";

		try (Connection con = DBConnection.getConnection();
				PreparedStatement ps = con.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			if (rs.next())
				return rs.getInt(1);
		}
		return 0;
	}
}