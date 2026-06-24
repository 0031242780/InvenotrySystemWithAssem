package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Product;

public class StatisticsDAO {

    public double getTotalRevenue() throws Exception {
        String sql = "SELECT SUM(quantity * price_at_purchase) FROM order_item";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    public int getTotalOrdersCount() throws Exception {
        String sql = "SELECT COUNT(*) FROM orders";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public int getTotalProductsCount() throws Exception {
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

    public ObservableList<Product> getLowStockAlerts() throws Exception {
        ObservableList<Product> list = FXCollections.observableArrayList();
        String sql = """
				SELECT p.product_id, p.product_name, p.barcode, i.quantity_in_stock
				FROM inventory i
				INNER JOIN product p ON i.product_id = p.product_id
				WHERE i.quantity_in_stock <= 10
				ORDER BY i.quantity_in_stock ASC
				""";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Product p = new Product();
                p.setProductId(rs.getInt("product_id"));
                p.setProductName(rs.getString("product_name"));
                p.setBarcode(rs.getString("barcode"));
                p.setCost(rs.getInt("quantity_in_stock"));
                list.add(p);
            }
        }
        return list;
    }
}