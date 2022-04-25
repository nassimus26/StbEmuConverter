package org.conv;

/**
 * @author Nassim MOUALEK
 * @since 23/04/2022
 */
public class Info {
    String url;
    String username;
    String password;

    public Info(String url, String userName, String password) {
        this.url = url;
        this.username = userName;
        this.password = password;
    }

    @Override
    public String toString() {
        return "Info{" +
                "url='" + url + '\'' +
                ", userName='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
