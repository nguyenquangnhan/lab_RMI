package Banker.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {

    // THAY ĐỔI CÁC THÔNG SỐ NÀY CHO PHÙ HỢP VỚI BẠN
    // 'BankDB' là tên CSDL ta đã tạo
    // 'localhost:3306' là địa chỉ IP và cổng của MySQL Server
    private static final String DB_URL = "jdbc:mysql://localhost:3306/BankDB";
    private static final String DB_USER = "root";       // Tên user MySQL
    private static final String DB_PASSWORD = "1234";  // Mật khẩu MySQL của bạn

    /**
     * Tải driver MySQL một lần khi lớp được nạp.
     */
    static {
        try {
            // Đảm bảo driver MySQL đã được đăng ký
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Không tìm thấy MySQL JDBC Driver! Bạn đã thêm file .jar chưa?", e);
        }
    }

    /**
     * Lấy một kết nối mới đến CSDL.
     * @return Một đối tượng Connection
     * @throws SQLException
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}