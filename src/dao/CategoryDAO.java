package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import model.Category;

public class CategoryDAO {

	public ArrayList<Category> getAll() throws Exception {

		ArrayList<Category> list = new ArrayList<>();

		Connection con = DBConnection.getConnection();

		String sql = "SELECT * FROM category";

		PreparedStatement ps = con.prepareStatement(sql);

		ResultSet rs = ps.executeQuery();

		while (rs.next()) {

			Category c = new Category();

			c.setCategoryId(rs.getInt("category_id"));
			c.setCategoryName(rs.getString("category_name"));
			c.setDescription(rs.getString("descrption"));

			list.add(c);
		}

		return list;
	}

	public void insert(Category c) throws Exception {

		Connection con = DBConnection.getConnection();

		String sql = "INSERT INTO category(category_name, descrption) VALUES(?, ?)";

		PreparedStatement ps = con.prepareStatement(sql);

		ps.setString(1, c.getCategoryName());
		ps.setString(2, c.getDescription());

		ps.executeUpdate();
	}

	public void update(Category c) throws Exception {

		Connection con = DBConnection.getConnection();

		String sql = "UPDATE category SET category_name=?, descrption=? WHERE category_id=?";

		PreparedStatement ps = con.prepareStatement(sql);

		ps.setString(1, c.getCategoryName());
		ps.setString(2, c.getDescription());
		ps.setInt(3, c.getCategoryId());

		ps.executeUpdate();
	}

	public void delete(int id) throws Exception {

		Connection con = DBConnection.getConnection();

		String sql = "DELETE FROM category WHERE category_id=?";

		PreparedStatement ps = con.prepareStatement(sql);

		ps.setInt(1, id);

		ps.executeUpdate();
	}
}