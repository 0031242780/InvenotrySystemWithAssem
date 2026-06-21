package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import model.Product;

public class ProductDAO {

	// 1️⃣ جلب كل المنتجات: أضفنا الاسم المستعار pp.cost AS actual_cost لمنع التضارب
	public ArrayList<Product> getAllProducts() throws Exception {
		ArrayList<Product> list = new ArrayList<>();
		String sql = """
				SELECT p.*, c.category_name, s.company_name AS supplier_name, pp.cost AS actual_cost
				FROM product p
				LEFT JOIN category c ON p.category_id = c.category_id
				LEFT JOIN supplier s ON p.supplier_id = s.supplier_id
				LEFT JOIN product_pricing pp ON p.product_id = pp.product_id
				""";
		try (Connection con = DBConnection.getConnection();
				PreparedStatement ps = con.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				Product p = new Product();
				p.setProductId(rs.getInt("product_id"));
				p.setProductName(rs.getString("product_name"));
				p.setBarcode(rs.getString("barcode"));
				p.setDescription(rs.getString("descrption"));
				p.setCategoryId(rs.getInt("category_id"));
				p.setSupplierId(rs.getInt("supplier_id"));
				p.setPrice(rs.getDouble("price"));
				p.setDiscountPrice(rs.getDouble("discount_price"));

				// 🔥 القراءة من الاسم المستعار الجديد لمنع قراءة الصفر الافتراضي
				p.setCost(rs.getDouble("actual_cost"));

				p.setCategoryName(rs.getString("category_name"));
				p.setSupplierName(rs.getString("supplier_name"));

				list.add(p);
			}
		}
		return list;
	}

	// ➕ دالة إضافة منتج جديد: بتسجل بالمنتج، والأسعار، وبتخلق سطر بالمخزن والحركات
	// تلقائياً!
	public void insertProduct(Product p) throws Exception {
		String sqlProduct = "INSERT INTO product (product_name, barcode, descrption, category_id, supplier_id, price) VALUES (?, ?, ?, ?, ?, ?)";
		String sqlPricing = "INSERT INTO product_pricing (product_id, cost) VALUES (?, ?)";

		try (Connection con = DBConnection.getConnection()) {
			con.setAutoCommit(false); // نظام العمليات المترابطة لحماية الجداول
			try {
				// 1. حفظ البيانات الأساسية في جدول product
				try (PreparedStatement psProd = con.prepareStatement(sqlProduct,
						java.sql.Statement.RETURN_GENERATED_KEYS)) {
					psProd.setString(1, p.getProductName());
					psProd.setString(2, p.getBarcode());
					psProd.setString(3, p.getDescription());
					psProd.setInt(4, p.getCategoryId());
					psProd.setInt(5, p.getSupplierId());
					psProd.setDouble(6, p.getPrice());
					psProd.executeUpdate();

					// الحصول على الـ ID الجديد اللي تولد أوتوماتيكياً
					try (ResultSet generatedKeys = psProd.getGeneratedKeys()) {
						if (generatedKeys.next()) {
							int newProductId = generatedKeys.getInt(1);

							// 2. حفظ الكوست في جدول الـ product_pricing المربوط
							try (PreparedStatement psPrice = con.prepareStatement(sqlPricing)) {
								psPrice.setInt(1, newProductId);
								psPrice.setDouble(2, p.getCost());
								psPrice.executeUpdate();
							}

							// 3. 🔥 الربط الأوتوماتيكي: استدعاء دالة الحركات لتخلق سطر المخزن والحركة
							// أوتوماتيكياً فوراً!
							StockMovementDAO.logMovementAndUpdateStock(newProductId, 0, 1,
									"Initial Stock Setup via Product Creation", 1, con);
						}
					}
				}
				con.commit(); // اعتماد الحفظ لجميع الجداول معاً بنجاح
			} catch (Exception ex) {
				con.rollback(); // تراجع عن كل شيء لو صار أي خطأ منعاً لتشوه البيانات
				throw ex;
			}
		}
	}

	// 3️⃣ تعديل منتج قائم
	public void updateProduct(Product p) throws Exception {
		String sqlProduct = "UPDATE product SET product_name = ?, barcode = ?, descrption = ?, category_id = ?, supplier_id = ?, price = ? WHERE product_id = ?";
		String sqlPricing = """
				INSERT INTO product_pricing (product_id, cost) VALUES (?, ?)
				ON DUPLICATE KEY UPDATE cost = ?
				""";

		try (Connection con = DBConnection.getConnection()) {
			con.setAutoCommit(false);
			try {
				try (PreparedStatement psProd = con.prepareStatement(sqlProduct)) {
					psProd.setString(1, p.getProductName());
					psProd.setString(2, p.getBarcode());
					psProd.setString(3, p.getDescription());
					psProd.setInt(4, p.getCategoryId());
					psProd.setInt(5, p.getSupplierId());
					psProd.setDouble(6, p.getPrice());
					psProd.setInt(7, p.getProductId());
					psProd.executeUpdate();
				}

				try (PreparedStatement psPrice = con.prepareStatement(sqlPricing)) {
					psPrice.setInt(1, p.getProductId());
					psPrice.setDouble(2, p.getCost());
					psPrice.setDouble(3, p.getCost());
					psPrice.executeUpdate();
				}
				con.commit();
			} catch (Exception ex) {
				con.rollback();
				throw ex;
			}
		}
	}

	// 4️⃣ حذف منتج
	public void deleteProduct(int id) throws Exception {
		String sql = "DELETE FROM product WHERE product_id = ?";
		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setInt(1, id);
			ps.executeUpdate();
		}
	}

	// 5️⃣ حفظ الخصم المئوي
	public void addDiscount(int productId, double discountAmount) throws Exception {
		String sql = "UPDATE product SET discount_price = ? WHERE product_id = ?";
		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setDouble(1, discountAmount);
			ps.setInt(2, productId);
			ps.executeUpdate();
		}
	}

	// 6️⃣ دالة شاشة الكاتيجوري الفخمة: أضفنا أيضاً الاسم المستعار actual_cost هان
	// لمنع التعليق
	public ArrayList<Product> getProductsByCategory(int categoryId) throws Exception {
		ArrayList<Product> list = new ArrayList<>();
		String sql = """
				SELECT p.*, pp.cost AS actual_cost
				FROM product p
				LEFT JOIN product_pricing pp ON p.product_id = pp.product_id
				WHERE p.category_id = ?
				""";
		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setInt(1, categoryId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Product p = new Product();
					p.setProductId(rs.getInt("product_id"));
					p.setProductName(rs.getString("product_name"));
					p.setBarcode(rs.getString("barcode"));
					p.setDescription(rs.getString("descrption"));
					p.setCategoryId(rs.getInt("category_id"));
					p.setSupplierId(rs.getInt("supplier_id"));
					p.setPrice(rs.getDouble("price"));
					p.setDiscountPrice(rs.getDouble("discount_price"));

					p.setCost(rs.getDouble("actual_cost"));
					list.add(p);
				}
			}
		}
		return list;
	}

	public int countProducts() throws Exception {
		String sql = "SELECT COUNT(*) FROM product";
		try (Connection con = DBConnection.getConnection();
				PreparedStatement ps = con.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			if (rs.next()) {
				return rs.getInt(1);
			}
		}
		return 0;
	}// 📦 جلب كمية منتج معين من المستودع بواسطة الـ ID
		// 📦 دالة جلب كل المنتجات مع كمياتها الحالية من جدول المخزن (خاصة بشاشة الـ
		// Stock)

	public ArrayList<Product> getInventory() throws Exception {
		ArrayList<Product> list = new ArrayList<>();
		String sql = """
				SELECT p.product_id, p.product_name, COALESCE(i.quantity_in_stock, 0) AS stock_qty
				FROM product p
				LEFT JOIN inventory i ON p.product_id = i.product_id
				""";
		try (Connection con = DBConnection.getConnection();
				PreparedStatement ps = con.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				Product p = new Product();
				p.setProductId(rs.getInt("product_id"));
				p.setProductName(rs.getString("product_name"));
				p.setQuantity(rs.getInt("stock_qty")); // تخزين الكمية المجلوبة في الحقل الجديد
				list.add(p);
			}
		}
		return list;
	}
}