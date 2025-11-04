package Banker.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Account  extends Remote {

    public Boolean deposit(double ammount) throws RemoteException;

    public Boolean withdraw(double ammount) throws RemoteException;

    public int getNumber() throws RemoteException;

    public Customer getCustomer() throws RemoteException;

    public double getBalance() throws RemoteException;
}
