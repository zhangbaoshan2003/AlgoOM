package com.csc108.infrastructure.pooledActiveMQ;

/**
 * Created by LEGEN on 2016/6/18.
 */
public class ConnectionKey {
    private String userName;
    private String password;
    private int hash;

    public ConnectionKey(String password, String userName) {
        this.password = password;
        this.userName = userName;
        hash = 31;
        if (userName != null)  {
            hash += userName.hashCode();
        }
        hash *= 31;
        if (password != null) {
            hash += password.hashCode();
        }
    }

    public int hashCode() {
        return hash;
    }

    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that instanceof ConnectionKey) {
            return equals((ConnectionKey) that);
        }
        return false;
    }

    public boolean equals(ConnectionKey that) {
        return isEqual(this.userName, that.userName) && isEqual(this.password, that.password);
    }

    public String getPassword() {
        return password;
    }

    public String getUserName() {
        return userName;
    }

    public static boolean isEqual(Object o1, Object o2) {
        if (o1 == o2) {
            return true;
        }
        return (o1 != null && o2 != null && o1.equals(o2));
    }


}
