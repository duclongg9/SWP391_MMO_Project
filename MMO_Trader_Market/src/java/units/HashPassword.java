/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package units;

import java.security.MessageDigest;

/**
 *
 * @author admin
 */
public class HashPassword {

    // md5
    // sha-1 => thường được sử dụng
    public static String toSHA1(String str) {
        String salt = "asjrlkmcoewj@tjle;oxqskjhdjksjf1jurVn";// Làm cho mật khẩu phức tap
        String result = null;

        str = str + salt;
        try {
            byte[] dataBytes = str.getBytes("UTF-8"); //huyển chuỗi sau khi đã nối muối thành mảng byte theo mã hoá UTF-8, 20 byte
            MessageDigest md = MessageDigest.getInstance("SHA-1"); //ấy một đối tượng băm theo thuật toán SHA-1 từ Java Crypto.
            result = java.util.Base64.getEncoder().encodeToString(md.digest(dataBytes)); //chạy thuật toán băm trên dữ liệu byte
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void main(String[] args) {
        String test = "kino1234";
        System.out.println(toSHA1(test));
    }
}

