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

				s.setPhoneNumbers(getPhoneNumbersForSupplier(s.getSupplierId(), con));

				list.add(s);
			}
		}
		return list;
	}

	public void insert(Supplier s) throws Exception {
		String sqlSupplier = """
				INSERT INTO supplier (company_name, contact_person, email, is_active)
				VALUES (?, ?, ?, ?)
				""";

		try (Connection con = DBConnection.getConnection()) {
			con.setAutoCommit(false); // Enable transaction block
			try {
				try (PreparedStatement psSup = con.prepareStatement(sqlSupplier, Statement.RETURN_GENERATED_KEYS)) {
					psSup.setString(1, s.getCompanyName());
					psSup.setString(2, s.getContactPerson());
					psSup.setString(3, s.getEmail());
					psSup.setBoolean(4, s.isActive());
					psSup.executeUpdate();

					try (ResultSet generatedKeys = psSup.getGeneratedKeys()) {
						if (generatedKeys.next()) {
							int generatedId = generatedKeys.getInt(1);
							s.setSupplierId(generatedId);

							// Insert associated phone numbers if they exist
							if (s.getPhoneNumbers() != null && !s.getPhoneNumbers().isEmpty()) {
								insertPhoneNumbers(generatedId, s.getPhoneNumbers(), con);
							}
						}
					}
				}
				con.commit();
			} catch (Exception ex) {
				con.rollback();
				throw ex;
			}
		}
	}

	public void update(Supplier s) throws Exception {
		String sql = "UPDATE supplier SET company_name = ?, contact_person = ?, email = ?, is_active = ? WHERE supplier_id = ?";

		try (Connection con = DBConnection.getConnection()) {
			con.setAutoCommit(false);
			try {
				try (PreparedStatement ps = con.prepareStatement(sql)) {
					ps.setString(1, s.getCompanyName());
					ps.setString(2, s.getContactPerson());
					ps.setString(3, s.getEmail());
					ps.setBoolean(4, s.isActive());
					ps.setInt(5, s.getSupplierId());
					ps.executeUpdate();
				}

				// Sync phone numbers: remove existing ones and insert the current list
				deletePhoneNumbers(s.getSupplierId(), con);
				if (s.getPhoneNumbers() != null && !s.getPhoneNumbers().isEmpty()) {
					insertPhoneNumbers(s.getSupplierId(), s.getPhoneNumbers(), con);
				}

				con.commit();
			} catch (Exception ex) {
				con.rollback();
				throw ex;
			}
		}
	}

	public void delete(int id) throws Exception {
		String sql = "DELETE FROM supplier WHERE supplier_id = ?";
		try (Connection con = DBConnection.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)) {
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


	private ArrayList<String> getPhoneNumbersForSupplier(int supplierId, Connection con) throws Exception {
		ArrayList<String> phones = new ArrayList<>();
		String sql = "SELECT phone_number FROM supplier_phone_number WHERE supplier_id = ?";
		try (PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setInt(1, supplierId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					phones.add(rs.getString("phone_number"));
				}
			}
		}
		return phones;
	}

	private void insertPhoneNumbers(int supplierId, ArrayList<String> phoneNumbers, Connection con) throws Exception {
		String sql = "INSERT INTO supplier_phone_number (supplier_id, phone_number) VALUES (?, ?)";
		try (PreparedStatement ps = con.prepareStatement(sql)) {
			for (String phone : phoneNumbers) {
				ps.setInt(1, supplierId);
				ps.setString(2, phone);
				ps.addBatch();
			}
			ps.executeBatch();
		}
	}

	private void deletePhoneNumbers(int supplierId, Connection con) throws Exception {
		String sql = "DELETE FROM supplier_phone_number WHERE supplier_id = ?";
		try (PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setInt(1, supplierId);
			ps.executeUpdate();
		}
	}
}