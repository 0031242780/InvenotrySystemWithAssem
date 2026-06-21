package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import model.StockMovement;

public class StockMovementDAO {

	// 📊 1. دالة جلب السجل الكامل بالأسماء والتاريخ المصلحة هندسياً
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

				// تعبئة الأسماء المستخرجة من الـ JOIN
				sm.setProductName(rs.getString("product_name"));
				sm.setTypeName(rs.getString("type_name"));

				// 🔥 التعديل الصح هان: نقرأ العمود كـ Timestamp متوافق بالملّي مع كلاس الموديل
				// عندك
				sm.setCreatedAt(rs.getTimestamp("create_at"));

				list.add(sm);
			}
		}
		return list;
	}

	// 🔥 2. الدالة المركزية الأوتوماتيكية: تسجل الحركة بالجدول وتعدل كمية الـ
	// inventory فوراً!
	public static void logMovementAndUpdateStock(int productId, int qtyChanged, int typeId, String notes, int accountId,
			Connection con) throws Exception {
		// أ. إدخال السطر في جدول حركات المستودع تلقائياً
		String sqlMovement = "INSERT INTO stock_movement (quantity_changed, notes, create_at, type_id, account_id, product_id) VALUES (?, ?, NOW(), ?, ?, ?)";
		try (PreparedStatement psMove = con.prepareStatement(sqlMovement)) {
			psMove.setInt(1, qtyChanged);
			psMove.setString(2, notes);
			psMove.setInt(3, typeId);
			psMove.setInt(4, accountId);
			psMove.setInt(5, productId);
			psMove.executeUpdate();
		}

		// ب. تحديث جدول الـ inventory الفعلي بحساب الكميات (إضافة لو نوع 1، وطرح لو أي
		// نوع تاني)
		String sqlInventory = """
				INSERT INTO inventory (product_id, quantity_in_stock) VALUES (?, ?)
				ON DUPLICATE KEY UPDATE quantity_in_stock = quantity_in_stock + ?
				""";
		try (PreparedStatement psInv = con.prepareStatement(sqlInventory)) {
			int changeEffect = (typeId == 1) ? qtyChanged : -qtyChanged; // إذا دخول بضاعة يزيد، لو تالف أو مبيعات ينقص

			psInv.setInt(1, productId);
			psInv.setInt(2, qtyChanged); // لو أول مرة ينزل المنتج بتنزل الكمية الممررة كبداية
			psInv.setInt(3, changeEffect); // لو المنتج موجود مسبقاً، بجمع التأثير الرياضي للكمية (+ أو -)
			psInv.executeUpdate();
		}
	}
}