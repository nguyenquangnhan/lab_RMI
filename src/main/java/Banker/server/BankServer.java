package Banker.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BankServer {

    public static void main(String[] args) {

        System.out.println("Đang khởi động BankServer (kết nối CSDL)...");

        // Lấy danh sách tài khoản từ CSDL
        List<Integer> accountNumbers;
        try {
            accountNumbers = getAccountNumbersFromDB();
        } catch (SQLException e) {
            System.err.println("Không thể lấy danh sách tài khoản từ CSDL. Server tắt.");
            e.printStackTrace();
            return;
        }

        // Khởi tạo RMI Registry
        Registry registry;
        try {
            registry = LocateRegistry.createRegistry(1099);
            System.out.println("Đã tạo RMI Registry trên cổng 1099.");
        } catch (RemoteException e1) {
            System.out.println("Không thể tạo RMI Registry: " + e1.getMessage());
            return;
        }

        // --- THAY ĐỔI MỚI ---
        // 1. Đăng ký NotificationService TRƯỚC
        // AccountImp sẽ cần tìm nó khi được khởi tạo
        try {
            // Khai báo và tạo đối tượng thật
            NotificationServiceImp notificationService = new NotificationServiceImp();

            registry.rebind("notification", notificationService); // Đăng ký dịch vụ
            System.out.println("Đã đăng ký 'notification' (Notification Service).");

            // DÒNG MỚI: "Bơm" tham chiếu service THẬT vào lớp AccountImp
            AccountImp.setNotificationService(notificationService);

        } catch (Exception ex) {
            System.out.println("Lỗi khi đăng ký 'notification': " + ex.getMessage());
            ex.printStackTrace();
            return;
        }
        // --- KẾT THÚC THAY ĐỔI ---

        // 2. Đăng ký AccountList (danh sách tài khoản)
        try {
            AccountListImp list = new AccountListImp();
            registry.rebind("accounts", list);
            System.out.println("Đã đăng ký 'accounts' (danh sách tài khoản).");
        } catch (Exception ex) {
            System.out.println("Lỗi khi đăng ký 'accounts': " + ex.getMessage());
            ex.printStackTrace();
            return;
        }

        // 3. Đăng ký từng đối tượng Account
        int boundCount = 0;
        for (int accNum : accountNumbers) {
            try {
                // Constructor của AccountImp giờ sẽ an toàn (không còn lỗi)
                AccountImp account = new AccountImp(accNum);
                registry.rebind("act" + accNum, account);
                boundCount++;
            } catch (Exception e) {
                System.err.println("Lỗi khi tạo hoặc đăng ký tài khoản " + accNum);
                e.printStackTrace();
            }
        }

        System.out.println("==================================================");
        System.out.println("ĐÃ ĐĂNG KÝ THÀNH CÔNG " + boundCount + " TÀI KHOẢN.");
        System.out.println("BankServer đã sẵn sàng!");
        System.out.println("==================================================");
    }

    private static List<Integer> getAccountNumbersFromDB() throws SQLException {
        // ... (phương thức này giữ nguyên như cũ) ...
        List<Integer> numbers = new ArrayList<>();
        String sql = "SELECT account_number FROM Account";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                numbers.add(rs.getInt("account_number"));
            }
        }
        return numbers;
    }
}