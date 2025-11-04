package Banker.server;

import Banker.server.Customer;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;


public class AccountImp extends UnicastRemoteObject implements Account {

    /**
     *
     */
    private static final long serialVersionUID = -5677093365471646101L;
    private Customer customer;
    private int accountNumber;
    private double balance;

    /**
     * Creates a new AccountImp object with full parameter information.
     * @param c The Customer who owns the account.  The values are copied so there is no problem of references.
     * @param num The account number.
     * @param amount The initial balance of the account.
     * @throws RemoteException
     */
    public AccountImp(Customer c, int num, double amount) throws RemoteException
    {
        this.customer = new Customer(c.getTZ(), c.getName(), c.getAddress(), c.getCity());
        this.accountNumber = num;
        this.balance = amount;
    }
    @Override
    public Boolean deposit(double amount) throws RemoteException {
        if (amount <= 0)
        {
            return false;
        }
        else
        {
            this.balance += amount;
            return true;
        }
    }

    @Override
    public Boolean withdraw(double amount) throws RemoteException {
        if ( balance - amount < 0)
        {
            return false;
        }
        else
        {
            this.balance -= amount;
            return true;
        }
    }

    @Override
    public int getNumber() throws RemoteException {
        return this.accountNumber;
    }

    @Override
    public Customer getCustomer() throws RemoteException {
        return this.customer;
    }

    @Override
    public double getBalance() throws RemoteException {
        return this.balance;
    }

    @Override
    public String toString()
    {
        return customer.getTZ() + ";" + customer.getName() + ";" + customer.getAddress() + ";" + customer.getCity() + ";" + this.accountNumber + ";" + this.balance;
    }

}
