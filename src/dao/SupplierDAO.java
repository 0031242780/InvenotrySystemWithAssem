package dao;

import java.sql.*;
import java.util.ArrayList;

import model.Supplier;

public class SupplierDAO {

	public ArrayList<Supplier> getAll() throws Exception {

		ArrayList<Supplier> list = new ArrayList<>();

		String sql = "SELECT * FROM supplier";

		try (Connection con = DBConnection.getConnection();
				PreparedStatement ps = con.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {

				Supplier s = new Supplier();

				s.setSupplierId(rs.getInt("supplier_id"));

				s.setCompanyName(rs.getString("company_name"));

				s.setContactPerson(rs.getString("contact_person"));

				s.setEmail(rs.getString("email"));

				s.setActive(rs.getBoolean("is_active"));

				list.add(s);

			}

		}

		return list;

	}

	public void insert(Supplier s) throws Exception {

		String sql = """
				INSERT INTO supplier
				(company_name, contact_person, email, is_active)
				VALUES (?,?,?,?)
				""";

		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setString(1, s.getCompanyName());

			ps.setString(2, s.getContactPerson());

			ps.setString(3, s.getEmail());

			ps.setBoolean(4, s.isActive());

			ps.executeUpdate();

		}

	}

	public void update(Supplier s) throws Exception {

		String sql = "UPDATE supplier SET company_name=?,contact_person=?,email=?,is_active=? WHERE supplier_id=?";

		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setString(1, s.getCompanyName());
			ps.setString(2, s.getContactPerson());
			ps.setString(3, s.getEmail());
			ps.setBoolean(4, s.isActive());
			ps.setInt(5, s.getSupplierId());

			ps.executeUpdate();

		}

	}

	public void delete(int id) throws Exception {

		String sql = "DELETE FROM supplier WHERE supplier_id=?";

		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setInt(1, id);

			ps.executeUpdate();

		}

	}

	public int countSuppliers() throws Exception {

		String sql = "SELECT COUNT(*) FROM supplier";

		try (Connection con = DBConnection.getConnection();
				PreparedStatement ps = con.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			if (rs.next())
				return rs.getInt(1);

		}

		return 0;
	}

}