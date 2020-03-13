package cn.wy.mybatis1;

import cn.wy.beans.Test;

import java.sql.*;

/**
 * Created by 胡歌的小迷弟 on 2020/3/13 15:15
 */
public class MyExecutor {


    public <T> T selectOne(String sql,String parameters) {
        try {
            Connection connection=getConnection();
            PreparedStatement preparedStatement=connection.prepareStatement(sql);

            ResultSet rs=preparedStatement.executeQuery();
            Test test = new Test();
            while (rs.next()) {
                test.setId(rs.getInt(1));
                test.setName(rs.getString(2));
            }
            return (T) test;
        } catch (SQLException e) {
            e.printStackTrace();
        }

return null;
    }



    public Connection getConnection() throws SQLException {
        String driver = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=utf-8&useSSL=false&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
        String username = "root";
        String password = "root";
        Connection conn = null;
        try {
            Class.forName(driver); //classLoader,加载对应驱动
            conn = DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }
}
