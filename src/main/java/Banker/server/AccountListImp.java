package Banker.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AccountListImp extends UnicastRemoteObject implements AccountList {

    private static final long serialVersionUID = 1L;

    // Constructor rỗng, không cần làm gì cả
    public AccountListImp() throws RemoteException {
        super();
    }

    // Phương thức addAccount(int i) không còn cần thiết và bị xóa
    // Phương thức getAccounts() được viết lại hoàn toàn

    @Override
    public int[] getAccounts() throws RemoteException {
        List<Integer> accountList = new ArrayList<>();
        String sql = "SELECT account_number FROM Account ORDER BY account_number ASC";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                accountList.add(rs.getInt("account_number"));
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách tài khoản: " + e.getMessage());
            throw new RemoteException("Lỗi CSDL khi lấy danh sách tài khoản", e);
        }

        // Chuyển List<Integer> thành mảng int[]
        int[] tightList = new int[accountList.size()];
        for (int i = 0; i < accountList.size(); i++) {
            tightList[i] = accountList.get(i);
        }
        return tightList;
    }
}