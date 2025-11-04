package Banker.server;

import java.io.Serializable;

public class Customer implements Serializable {

    /**
     * The serial id for the class
     */
    private static final long serialVersionUID = -4385080825232129934L;
    /**
     * The id for the customer
     */
    private String tz;
    /**
     * The customer's name
     */
    private String name;
    /**
     * The customer's address
     */
    private String address;
    /**
     * The customer's city
     */
    private String city;

    /**
     * Builds a new customer instance
     * @param tz Customer's id number
     * @param n Customer's name
     * @param add Customer's address
     * @param c Customer's city
     */

    public Customer(String tz,String n,String add,String c)
    {
        this.name=n;
        this.address=add;
        this.city=c;
        this.tz=tz;
    }

    public String getTZ(){return this.tz;}

    public String getName(){return this.name;}

    public String getCity(){return this.city;}

    public String getAddress(){return this.address;}

    public void setName(String n)
    {
        this.name = n;
    }

    public void setAddress(String a, String c)
    {
        this.address = a;
        this.city = c;
    }

    @Override
    public String toString()
    {
        return name + " (" + tz + ") " + address + ", " + city;
    }

}
