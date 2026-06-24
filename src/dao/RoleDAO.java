package dao;

import java.sql.*;
import java.util.ArrayList;

import model.Role;

public class RoleDAO {

	public ArrayList<Role> getAll() throws Exception {

		ArrayList<Role> list = new ArrayList<>();

		String sql = "SELECT * FROM roles";

		try (Connection con = DBConnection.getConnection();
				PreparedStatement ps = con.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {

				Role r = new Role();

				r.setRoleId(rs.getInt("role_id"));

				r.setRoleName(rs.getString("role_name"));

				r.setPermissionLevel(rs.getString("permission_level"));

				list.add(r);

			}

		}

		return list;
	}

}