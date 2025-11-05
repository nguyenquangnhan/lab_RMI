package Banker.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Lớp này quản lý tất cả các đăng ký callback.
 * Nó là "stateful" (lưu trữ trạng thái).
 */
public class NotificationServiceImp extends UnicastRemoteObject implements NotificationService {

    // Key: Số tài khoản (Integer)
    // Value: Danh sách các Client (ClientCallback) đang theo dõi tài khoản đó.
    private Map<Integer, List<ClientCallback>> subscriptions;

    public NotificationServiceImp() throws RemoteException {
        super();
        // Dùng ConcurrentHashMap và CopyOnWriteArrayList để an toàn khi nhiều Client
        // cùng đăng ký hoặc hủy đăng ký
        subscriptions = new ConcurrentHashMap<>();
    }

    @Override
    public synchronized void registerCallback(int accountNumber, ClientCallback clientCallback) throws RemoteException {
        // Lấy danh sách client cho tài khoản này, nếu chưa có thì tạo mới
        subscriptions.putIfAbsent(accountNumber, new CopyOnWriteArrayList<>());

        // Thêm client này vào danh sách
        List<ClientCallback> clients = subscriptions.get(accountNumber);
        if (!clients.contains(clientCallback)) {
            clients.add(clientCallback);
            System.out.println("[NotificationService] Client mới đăng ký theo dõi tài khoản: " + accountNumber);
        }
    }

    @Override
    public synchronized void unregisterCallback(int accountNumber, ClientCallback clientCallback) throws RemoteException {
        List<ClientCallback> clients = subscriptions.get(accountNumber);
        if (clients != null) {
            clients.remove(clientCallback);
            System.out.println("[NotificationService] Client đã hủy theo dõi tài khoản: " + accountNumber);
        }
    }

    /**
     * Đây KHÔNG phải là phương thức RMI.
     * Đây là phương thức NỘI BỘ mà AccountImp sẽ gọi.
     * @param accountNumber Tài khoản có biến động
     * @param message Nội dung thông báo
     */
    public void notifyClients(int accountNumber, String message) {
        List<ClientCallback> clients = subscriptions.get(accountNumber);

        if (clients != null && !clients.isEmpty()) {
            System.out.println("[NotificationService] Gửi thông báo cho " + clients.size() + " client của tài khoản " + accountNumber);

            // Dùng iterator để an toàn nếu client bị ngắt kết nối
            for (ClientCallback client : clients) {
                try {
                    client.receiveNotification(message);
                } catch (RemoteException e) {
                    // Client này có thể đã bị ngắt kết nối.
                    // Xóa khỏi danh sách.
                    System.err.println("Lỗi khi gửi callback, xóa client: " + e.getMessage());
                    clients.remove(client);
                }
            }
        }
    }
}