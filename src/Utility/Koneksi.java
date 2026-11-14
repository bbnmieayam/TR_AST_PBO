package Utility;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Koneksi {
    public static Connection con;
    public static Statement stm;


    public void config() {
        try {
         
            if (con == null || con.isClosed()) {
                String url = "jdbc:mysql://127.0.0.1/db_salon";
                String user = "root";
                String pass = "";
                Class.forName("com.mysql.cj.jdbc.Driver");

                
                con = DriverManager.getConnection(url, user, pass);
                System.out.println("Koneksi berhasil...");
            }

            
            stm = con.createStatement();
        } catch (Exception e) {
            System.out.println("Koneksi gagal...");
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        Koneksi kn = new Koneksi();
        kn.config();
    }
}