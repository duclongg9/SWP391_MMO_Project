/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package units;
import java.io.InputStream;
import java.util.Properties;
/**
 *
 * @author D E L L
 */
public class EmailConfig {
    private static final Properties props = new Properties();

    static {
        try (InputStream input = EmailConfig.class.getClassLoader().getResourceAsStream("email.properties")) {
            props.load(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getEmail() {
        return props.getProperty("email");
    }

    public static String getPassword() {
        return props.getProperty("password");
    }
}
