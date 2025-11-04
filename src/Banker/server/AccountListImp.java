package Banker.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author mjmay
 */
public class AccountListImp extends UnicastRemoteObject implements AccountList{

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    int[] accounts;
    int top;
    public AccountListImp(int s) throws RemoteException
    {
        accounts = new int[s];
        top = 0;
    }

    public void addAccount(int i)
    {
        if (top == accounts.length)
        {
            int[] a = new int[accounts.length * 2];
            System.arraycopy(accounts, 0, a, 0, accounts.length);

            accounts = a;
        }

        accounts[top] = i;
        top++;

        return;
    }

    public int[] getAccounts() throws RemoteException
    {
        int[] tightList = new int[top];
        System.arraycopy(accounts, 0, tightList, 0, top);
        return tightList;
    }

}
