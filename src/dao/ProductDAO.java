package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import model.Product;
import model.Provide;

public class ProductDAO {

    private static final double WHOLESALE_MARKUP = 1.20;
    private static final double RETAIL_MARKUP = 1.50;

    public ArrayList<Product> getAllProducts() throws Exception {
        ArrayList<Product> list = new ArrayList<>();
        String sql = """
				SELECT p.*, c.category_name,
				       (SELECT MIN(cost) FROM provide WHERE product_id = p.product_id) AS lowest_cost,
				       (SELECT d.discounted_price FROM discount d 
				        WHERE d.product_id = p.product_id AND NOW() BETWEEN d.start_date AND d.end_date 
				        LIMIT 1) AS active_discount
				FROM product p
				LEFT JOIN category c ON p.category_id = c.category_id
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
                p.setCategoryName(rs.getString("category_name"));

                double baseCost = rs.getDouble("lowest_cost");
                p.setCost(baseCost);
                p.setWholeSalePrice(baseCost * WHOLESALE_MARKUP);
                p.setPrice(baseCost * RETAIL_MARKUP);

                p.setDiscountPrice(rs.getDouble("active_discount"));

                list.add(p);
            }
        }
        return list;
    }

    public void insertProduct(Product p) throws Exception {
        String sqlProduct = "INSERT INTO product (product_name, barcode, descrption, category_id) VALUES (?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement psProd = con.prepareStatement(sqlProduct, Statement.RETURN_GENERATED_KEYS)) {
                    psProd.setString(1, p.getProductName());
                    psProd.setString(2, p.getBarcode());
                    psProd.setString(3, p.getDescription());
                    psProd.setInt(4, p.getCategoryId());
                    psProd.executeUpdate();

                    try (ResultSet generatedKeys = psProd.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int newProductId = generatedKeys.getInt(1);

                            StockMovementDAO.logMovementAndUpdateStock(newProductId, 0, 1,
                                    "Initial Stock Setup via Product Creation", 1, con);
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

    public void updateProduct(Product p) throws Exception {
        String sqlProduct = "UPDATE product SET product_name = ?, barcode = ?, descrption = ?, category_id = ? WHERE product_id = ?";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement psProd = con.prepareStatement(sqlProduct)) {
                    psProd.setString(1, p.getProductName());
                    psProd.setString(2, p.getBarcode());
                    psProd.setString(3, p.getDescription());
                    psProd.setInt(4, p.getCategoryId());
                    psProd.setInt(5, p.getProductId());
                    psProd.executeUpdate();
                }
                con.commit();
            } catch (Exception ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    public void deleteProduct(int id) throws Exception {
        String sql = "DELETE FROM product WHERE product_id = ?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void addDiscount(int productId, Timestamp startDate, Timestamp endDate, double discountedPrice) throws Exception {
        String sql = "INSERT INTO discount (product_id, start_date, end_date, discounted_price) VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.setTimestamp(2, startDate);
            ps.setTimestamp(3, endDate);
            ps.setDouble(4, discountedPrice);
            ps.executeUpdate();
        }
    }

    public ArrayList<Product> getProductsByCategory(int categoryId) throws Exception {
        ArrayList<Product> list = new ArrayList<>();
        String sql = """
				SELECT p.*,
				       (SELECT MIN(cost) FROM provide WHERE product_id = p.product_id) AS lowest_cost,
				       (SELECT d.discounted_price FROM discount d 
				        WHERE d.product_id = p.product_id AND NOW() BETWEEN d.start_date AND d.end_date 
				        LIMIT 1) AS active_discount
				FROM product p
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

                    double baseCost = rs.getDouble("lowest_cost");
                    p.setCost(baseCost);
                    p.setWholeSalePrice(baseCost * WHOLESALE_MARKUP);
                    p.setPrice(baseCost * RETAIL_MARKUP);

                    p.setDiscountPrice(rs.getDouble("active_discount"));

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
    }

    public ArrayList<Product> getInventory() throws Exception {
        ArrayList<Product> list = new ArrayList<>();
        String sql = """
				SELECT p.product_id, p.product_name, i.quantity_in_stock AS stock_qty
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
                p.setQuantity(rs.getInt("stock_qty"));
                list.add(p);
            }
        }
        return list;
    }

    public ArrayList<Product> getProductsBySupplierId(int supplierId) throws Exception {
        ArrayList<Product> list = new ArrayList<>();
        String sql = """
				SELECT p.product_id, p.product_name, p.barcode, p.price,
				       c.category_name, pr.cost 
				FROM provide pr
				INNER JOIN product p ON pr.product_id = p.product_id
				INNER JOIN category c ON p.category_id = c.category_id
				WHERE pr.supplier_id = ?
				ORDER BY p.product_name ASC
				""";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, supplierId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Product p = new Product();
                    p.setProductId(rs.getInt("product_id"));
                    p.setProductName(rs.getString("product_name"));
                    p.setBarcode(rs.getString("barcode"));
                    p.setPrice(rs.getDouble("price"));
                    p.setCategoryName(rs.getString("category_name"));

                    p.setCost(rs.getDouble("cost"));

                    list.add(p);
                }
            }
        }
        return list;
    }
}