package Utility;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Koneksi {
    public static Connection cn;
    public static Statement stm;

    public void config() {
        try {
            if (cn == null || cn.isClosed()) {
                String url = "jdbc:mysql://127.0.0.1/db_salon";
                String user = "root";
                String pass = "";
                Class.forName("com.mysql.cj.jdbc.Driver");

                cn = DriverManager.getConnection(url, user, pass);
                System.out.println("Koneksi berhasil...");
            }
            stm = cn.createStatement();
        } catch (Exception e) {
            System.out.println("Koneksi gagal...");
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        new Koneksi().config();
    }
}