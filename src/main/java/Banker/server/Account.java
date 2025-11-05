package Banker.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Account extends Remote {

    Boolean deposit(double ammount) throws RemoteException;
    Boolean withdraw(double ammount) throws RemoteException;

    /**
     * Tính năng MỚI: Chuyển khoản
     * @param destinationAccountNumber Số tài khoản nhận
     * @param amount Số tiền
     * @return true nếu thành công
     * @throws RemoteException
     */
    Boolean transfer(int destinationAccountNumber, double amount) throws RemoteException;

    int getNumber() throws RemoteException;
    Customer getCustomer() throws RemoteException;
    double getBalance() throws RemoteException;
}