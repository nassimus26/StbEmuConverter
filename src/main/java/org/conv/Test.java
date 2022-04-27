package org.conv;

/**
 * @author Nassim MOUALEK
 * @since 23/04/2022
 */
public class Test {
    public static void main(String[] args) throws Exception {
        XStreamClient XStreamClient = new XStreamClient();
        XStreamClient.showPlayer();
        Info info = XStreamClient.getData("http://012345x.com:999/c", "00:1A:79:F2:B3:5F");
        if (info!=null)
            System.out.println(info);
    }
}
