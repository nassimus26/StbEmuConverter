package org.conv;

/**
 * @author Nassim MOUALEK
 * @since 23/04/2022
 */
public class UserInfo {
    String userName;
    String password;
    String status;
    String expDate;
    String maxConnexion;

    public UserInfo(String userName, String password, String status, String expDate, String maxConnexion) {
        this.userName = userName;
        this.password = password;
        this.status = status;
        this.expDate = expDate;
        this.maxConnexion = maxConnexion;
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", status='" + status + '\'' +
                ", expDate='" + expDate + '\'' +
                ", maxConnexion='" + maxConnexion + '\'' +
                '}';
    }
}
