package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import model.StockMovement;

public class StockMovementDAO {

	public ArrayList<StockMovement> getAllMovementsWithNames() throws Exception {
		ArrayList<StockMovement> list = new ArrayList<>();
		String sql = """
				SELECT sm.*, p.product_name, mt.type_name
				FROM stock_movement sm
				INNER JOIN product p ON sm.product_id = p.product_id
				INNER JOIN movement_type mt ON sm.type_id = mt.type_id
				ORDER BY sm.movement_id DESC
				""";
		try (Connection con = DBConnection.getConnection();
				PreparedStatement ps = con.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				StockMovement sm = new StockMovement();
				sm.setMovementId(rs.getInt("movement_id"));
				sm.setProductId(rs.getInt("product_id"));
				sm.setQuantityChanged(rs.getInt("quantity_changed"));
				sm.setTypeId(rs.getInt("type_id"));
				sm.setAccountId(rs.getInt("account_id"));
				sm.setNotes(rs.getString("notes"));

				sm.setProductName(rs.getString("product_name"));
				sm.setTypeName(rs.getString("type_name"));
				sm.setCreatedAt(rs.getTimestamp("create_at"));

				list.add(sm);
			}
		}
		return list;
	}

	public static void logMovementAndUpdateStock(int productId, int qtyChanged, int typeId, String notes, int accountId,
	                                             Connection con) throws Exception {
		String sqlMovement = "INSERT INTO stock_movement (quantity_changed, notes, create_at, type_id, account_id, product_id) VALUES (?, ?, NOW(), ?, ?, ?)";
		try (PreparedStatement psMove = con.prepareStatement(sqlMovement)) {
			psMove.setInt(1, qtyChanged);
			psMove.setString(2, notes);
			psMove.setInt(3, typeId);
			psMove.setInt(4, accountId);
			psMove.setInt(5, productId);
			psMove.executeUpdate();
		}

		String sqlInventory = """
				INSERT INTO inventory (product_id, quantity_in_stock, last_restock) 
				VALUES (?, 0, ?)
				ON DUPLICATE KEY UPDATE 
				    quantity_in_stock = quantity_in_stock + ?,
				    last_restock = CASE WHEN ? = 1 THEN CURDATE() ELSE last_restock END
				""";

		try (PreparedStatement psInv = con.prepareStatement(sqlInventory)) {
			int changeEffect = (typeId == 1) ? qtyChanged : -qtyChanged;

			java.sql.Date initialRestockDate = (typeId == 1) ? new java.sql.Date(System.currentTimeMillis()) : null;

			psInv.setInt(1, productId);
			psInv.setDate(2, initialRestockDate);
			psInv.setInt(3, changeEffect);
			psInv.setInt(4, typeId);

			psInv.executeUpdate();
		}
	}
}