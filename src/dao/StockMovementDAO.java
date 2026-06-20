package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import model.StockMovement;

public class StockMovementDAO {

	public void insert(StockMovement m) throws Exception {

		String sql = """
				INSERT INTO stock_movement
				(quantity_changed,notes,create_at,type_id,account_id,product_id)
				VALUES(?,?,NOW(),?,?,?)
				""";

		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setInt(1, m.getQuantityChanged());
			ps.setString(2, m.getNotes());
			ps.setInt(3, m.getTypeId());
			ps.setInt(4, m.getAccountId());
			ps.setInt(5, m.getProductId());

			ps.executeUpdate();
		}
	}

	public ArrayList<StockMovement> getAll() throws Exception {

		ArrayList<StockMovement> list = new ArrayList<>();

		String sql = "SELECT * FROM stock_movement";

		try (Connection con = DBConnection.getConnection();
				PreparedStatement ps = con.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {

				StockMovement m = new StockMovement();

				m.setMovementId(rs.getInt("movement_id"));
				m.setQuantityChanged(rs.getInt("quantity_changed"));
				m.setNotes(rs.getString("notes"));
				m.setCreatedAt(rs.getTimestamp("create_at"));
				m.setTypeId(rs.getInt("type_id"));
				m.setAccountId(rs.getInt("account_id"));
				m.setProductId(rs.getInt("product_id"));

				list.add(m);
			}
		}

		return list;
	}

	public void save(StockMovement sm) throws Exception {

		// جملة الإدخال لجدول حركة المخزن بناءً على أعمدة الداتابيز عندك
		// وبنخلي التاريخ يتسجل تلقائياً بوقت الحركة الحالي NOW()
		String sql = "INSERT INTO stock_movement (quantity_changed, notes, create_at, type_id, account_id, product_id) VALUES (?, ?, NOW(), ?, ?, ?)";

		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setInt(1, sm.getQuantityChanged());
			ps.setString(2, sm.getNotes());
			ps.setInt(3, sm.getTypeId());
			ps.setInt(4, sm.getAccountId());
			ps.setInt(5, sm.getProductId());

			// تنفيذ عملية الحفظ في قاعدة البيانات
			ps.executeUpdate();
		}
	}
}