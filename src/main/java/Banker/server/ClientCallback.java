package Banker.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface này nằm ở Server, nhưng sẽ được Client TRIỂN KHAI.
 * Nó cho phép Server gọi phương thức 'receiveNotification' trên Client.
 */
public interface ClientCallback extends Remote {
    /**
     * Server sẽ gọi phương thức này để gửi một thông báo (String) cho Client.
     * @param message Nội dung thông báo.
     * @throws RemoteException
     */
    void receiveNotification(String message) throws RemoteException;
}