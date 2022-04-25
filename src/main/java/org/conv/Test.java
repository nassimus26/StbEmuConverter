package org.conv;

/**
 * @author Nassim MOUALEK
 * @since 23/04/2022
 */
public class Test {
    public static void main(String[] args) {
        DecodeMac decodeMac = new DecodeMac();
        Info info = decodeMac.getData("http://tvalb.xyz:8080/c", "00:1A:79:55:9E:09");
        if (info!=null)
            System.out.println(info);
    }
}
