package Banker.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface cho dịch vụ đăng ký nhận thông báo.
 * Client sẽ gọi các phương thức này.
 */
public interface NotificationService extends Remote {

    /**
     * Client gọi để đăng ký nhận thông báo cho một tài khoản.
     * @param accountNumber Số tài khoản muốn theo dõi.
     * @param clientCallback Đối tượng callback của chính Client.
     * @throws RemoteException
     */
    void registerCallback(int accountNumber, ClientCallback clientCallback) throws RemoteException;

    /**
     * Client gọi để hủy đăng ký.
     * @param accountNumber Số tài khoản.
     * @param clientCallback Đối tượng callback của Client.
     * @throws RemoteException
     */
    void unregisterCallback(int accountNumber, ClientCallback clientCallback) throws RemoteException;
}