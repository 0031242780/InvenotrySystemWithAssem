package dao;

import model.Account;
import java.sql.*;
import java.util.ArrayList;

import javafx.util.Callback;

public class AccountDAO {

	public void insert(Account a) throws Exception {

		String sql = "INSERT INTO accounts(role_id,email,password_,first_name,last_name,phone_number,city,street) VALUES (3,?,?,?,?,?,?,?)";
		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setString(1, a.getEmail());
			ps.setString(2, a.getPassword());
			ps.setString(3, a.getFirstName());
			ps.setString(4, a.getLastName());
			ps.setString(5, a.getPhoneNumber());
			ps.setString(6, a.getCity());
			ps.setString(7, a.getStreet());

			ps.executeUpdate();
		}
	}

	public Account login(String email, String password) throws Exception {

		String sql = "SELECT a.*, r.role_name, r.permission_level " + "FROM accounts a "
				+ "JOIN roles r ON a.role_id = r.role_id " + "WHERE a.email=? AND a.password_=?";

		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setString(1, email);
			ps.setString(2, password);

			ResultSet rs = ps.executeQuery();

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

		return null;
	}

	public ArrayList<Account> getAllAccounts() throws Exception {
		ArrayList<Account> list = new ArrayList<>();
		// الربط الصحيح لجلب اسم الدور
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
				// جلب الاسم الفعلي للدور
				a.setRoleName(rs.getString("role_name"));

				list.add(a);
			}
		}
		return list;
	}

	public ArrayList<Account> getCustomers() throws Exception {

		ArrayList<Account> list = new ArrayList<>();

		String sql = """
				SELECT *
				FROM accounts
				WHERE role_id = 3
				""";

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

	public int countCustomers() throws Exception {

		String sql = "SELECT COUNT(*) FROM accounts WHERE role_id=3";

		try (Connection con = DBConnection.getConnection();
				PreparedStatement ps = con.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			if (rs.next())
				return rs.getInt(1);

		}

		return 0;
	}
}