package dao;

import java.sql.*;
import java.util.ArrayList;

import model.DeliveryCompany;

public class DeliveryCompanyDAO {

	public ArrayList<DeliveryCompany> getAll() throws Exception {
		ArrayList<DeliveryCompany> list = new ArrayList<>();
		String sql = "SELECT * FROM delivery_company";

		try (Connection con = DBConnection.getConnection();
				PreparedStatement ps = con.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				DeliveryCompany d = new DeliveryCompany();
				d.setCompanyId(rs.getInt("company_id"));
				d.setCompanyName(rs.getString("company_name"));
				d.setContactPerson(rs.getString("contact_person"));
				d.setPhone(rs.getString("support_phone_number"));
				d.setActive(rs.getBoolean("is_active"));
				list.add(d);
			}
		}
		return list;
	}

	public void insert(DeliveryCompany d) throws Exception {
		String sql = """
				INSERT INTO delivery_company (company_name, contact_person, support_phone_number, is_active)
				VALUES (?, ?, ?, ?)
				""";

		try (Connection con = DBConnection.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setString(1, d.getCompanyName());
			ps.setString(2, d.getContactPerson());
			ps.setString(3, d.getPhone());
			ps.setBoolean(4, d.isActive());
			ps.executeUpdate();
		}
	}

	public void update(DeliveryCompany d) throws Exception {
		String sql = """
				UPDATE delivery_company 
				SET company_name = ?,
				    contact_person = ?,
				    support_phone_number = ?,
				    is_active = ?
				WHERE company_id = ?
				""";

		try (Connection con = DBConnection.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setString(1, d.getCompanyName());
			ps.setString(2, d.getContactPerson());
			ps.setString(3, d.getPhone());
			ps.setBoolean(4, d.isActive());
			ps.setInt(5, d.getCompanyId());
			ps.executeUpdate();
		}
	}

	public void delete(int id) throws Exception {
		String sql = "DELETE FROM delivery_company WHERE company_id = ?";

		try (Connection con = DBConnection.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setInt(1, id);
			ps.executeUpdate();
		}
	}
}