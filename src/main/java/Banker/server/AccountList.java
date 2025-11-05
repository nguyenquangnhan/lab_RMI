package Banker.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AccountList extends Remote {
    public int[] getAccounts() throws RemoteException;
}
