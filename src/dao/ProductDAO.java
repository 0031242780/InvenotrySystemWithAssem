package dao;

import java.sql.*;
import java.util.ArrayList;
import model.Product;

public class ProductDAO {

	public ArrayList<Product> getAllProducts() throws Exception {
		ArrayList<Product> list = new ArrayList<>();
		String query = "SELECT product_id, product_name, barcode, descrption, category_id, supplier_id, price, discount_price FROM product";

		try (Connection conn = DBConnection.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(query)) {

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

				list.add(p);
			}
		}
		return list;
	}

	public ArrayList<Product> getInventory() throws Exception {
		return getAllProducts();
	}

	public int countProducts() {
		String query = "SELECT COUNT(*) FROM product";
		int count = 0;
		try (Connection conn = DBConnection.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(query)) {

			if (rs.next()) {
				count = rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return count;
	}

	public void insertProduct(Product p) throws Exception {
		String query = "INSERT INTO product (product_name, barcode, descrption, category_id, supplier_id, price, discount_price) VALUES (?, ?, ?, ?, ?, ?, 0.0)";
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
			ps.setString(1, p.getProductName());
			ps.setString(2, p.getBarcode());
			ps.setString(3, p.getDescription());
			ps.setInt(4, p.getCategoryId());
			ps.setInt(5, p.getSupplierId());
			ps.setDouble(6, p.getPrice());
			ps.executeUpdate();
		}
	}

	public void updateProduct(Product p) throws Exception {
		String query = "UPDATE product SET product_name = ?, barcode = ?, descrption = ?, category_id = ?, supplier_id = ?, price = ? WHERE product_id = ?";
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
			ps.setString(1, p.getProductName());
			ps.setString(2, p.getBarcode());
			ps.setString(3, p.getDescription());
			ps.setInt(4, p.getCategoryId());
			ps.setInt(5, p.getSupplierId());
			ps.setDouble(6, p.getPrice());
			ps.setInt(7, p.getProductId());
			ps.executeUpdate();
		}
	}

	public ArrayList<Product> getProductsByCategory(int categoryId) throws Exception {
		ArrayList<Product> list = new ArrayList<>();
		String query = "SELECT product_id, product_name, barcode, descrption, category_id, supplier_id, price, discount_price FROM product WHERE category_id = ?";

		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {

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
					list.add(p);
				}
			}
		}
		return list;
	}

	public void addDiscount(int productId, double discountPrice) throws Exception {
		String query = "UPDATE product SET discount_price = ? WHERE product_id = ?";
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
			ps.setDouble(1, discountPrice);
			ps.setInt(2, productId);
			ps.executeUpdate();
		}
	}

	public void deleteProduct(int productId) throws Exception {
		String query = "DELETE FROM product WHERE product_id = ?";
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
			ps.setInt(1, productId);
			ps.executeUpdate();
		}
	}
}