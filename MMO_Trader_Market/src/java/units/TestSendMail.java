/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package units;

/**
 *
 * @author D E L L
 */
public class TestSendMail {
     public static void main(String[] args) {
        try {
            String toEmail = "aanhtuankiet@gmail.com"; // Email bạn muốn nhận để test
            String subject = "KIETNGUYENCEO";
            String messageText = "Xin chào, đây là email test gửi từ Java";

            SendMail.sendMail(toEmail, subject, messageText);

            System.out.println("Đã gọi SendMail xong.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
