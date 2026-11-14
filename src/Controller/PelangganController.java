package Controller;

import Model.Pelanggan;
import Utility.Koneksi;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.swing.table.DefaultTableModel;

public class PelangganController {

    public Connection cn;
    public Statement stm;
    public ResultSet res;
    public String sql;

    // Alasan gagal, untuk ditampilkan oleh view (misalnya lewat JOptionPane)
    public String pesan = "";

    // Model tabel virtual
    DefaultTableModel dtm = new DefaultTableModel();

    // Konstruktor
    public PelangganController() {
        Koneksi db = new Koneksi();
        db.config();
        this.cn = db.con;
        this.stm = db.stm;
    }

    // Method 1: Membuat desain tabel virtual
    public DefaultTableModel createTable() {
        if (this.dtm.getColumnCount() == 0) {
            this.dtm.addColumn("ID Pelanggan");
            this.dtm.addColumn("Nama Pelanggan");
            this.dtm.addColumn("No. HP");
        }
        return this.dtm;
    }

    // Method 2: SELECT semua
    public void tampilkanPelanggan() {
        try {
            // Bersihkan tabel virtual
            this.dtm.setRowCount(0);

            // Query
            this.sql = "SELECT * FROM pelanggan ORDER BY nama";

            // Jalankan query
            this.res = this.stm.executeQuery(this.sql);

            // Masukkan hasil query ke tabel virtual
            while (res.next()) {
                Object[] obj = new Object[3];

                obj[0] = res.getString("id");
                obj[1] = res.getString("nama");
                obj[2] = res.getString("no_hp");

                this.dtm.addRow(obj);
            }

        } catch (Exception e) {
            System.out.println("Gagal Query Pelanggan..");
            System.out.println(e.getMessage());
        }
    }

    // Method 3: SELECT satu (dipakai juga oleh BookingController). Return null jika tidak ada.
    public Pelanggan cariPelanggan(int id) {
        try {
            this.sql = "SELECT * FROM pelanggan WHERE id = ?";
            try (PreparedStatement ps = this.cn.prepareStatement(this.sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Pelanggan pl = new Pelanggan();
                        pl.setId_pelanggan(rs.getInt("id"));
                        pl.setNama_pelanggan(rs.getString("nama"));
                        pl.setNo_hp(rs.getString("no_hp"));
                        return pl;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Gagal cari Pelanggan..");
            System.out.println(e.getMessage());
        }
        return null;
    }

    // Method 4: INSERT
    public boolean tambahPelanggan(String nama, String noHp) {
        Pelanggan pl = new Pelanggan();
        pl.setNama_pelanggan(rapikan(nama));
        pl.setNo_hp(rapikanNoHp(noHp));

        if (!valid(pl)) {
            return false;
        }

        try {
            this.sql = "INSERT INTO pelanggan (nama, no_hp) VALUES (?, ?)";
            try (PreparedStatement ps = this.cn.prepareStatement(this.sql)) {
                ps.setString(1, pl.getNama_pelanggan());
                ps.setString(2, pl.getNo_hp().isEmpty() ? null : pl.getNo_hp());
                ps.executeUpdate();
            }
            return true;
        } catch (Exception e) {
            this.pesan = "Gagal menyimpan data pelanggan.";
            System.out.println("tambahPelanggan failed: " + e.getMessage());
            return false;
        }
    }

    // Method 5: UPDATE
    public boolean ubahPelanggan(int id, String nama, String noHp) {
        Pelanggan pl = new Pelanggan();
        pl.setId_pelanggan(id);
        pl.setNama_pelanggan(rapikan(nama));
        pl.setNo_hp(rapikanNoHp(noHp));

        if (!valid(pl)) {
            return false;
        }

        try {
            this.sql = "UPDATE pelanggan SET nama = ?, no_hp = ? WHERE id = ?";
            try (PreparedStatement ps = this.cn.prepareStatement(this.sql)) {
                ps.setString(1, pl.getNama_pelanggan());
                ps.setString(2, pl.getNo_hp().isEmpty() ? null : pl.getNo_hp());
                ps.setInt(3, pl.getId_pelanggan());

                // Jika tidak ada baris terupdate => gagal
                if (ps.executeUpdate() == 0) {
                    this.pesan = "Pelanggan tidak ditemukan.";
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            this.pesan = "Gagal mengubah data pelanggan.";
            System.out.println("ubahPelanggan failed: " + e.getMessage());
            return false;
        }
    }

    // Method 6: DELETE
    public boolean hapusPelanggan(int id) {
        Pelanggan pl = new Pelanggan();
        pl.setId_pelanggan(id);

        try {
            // Pelanggan yang punya riwayat booking tidak boleh dihapus
            this.sql = "SELECT COUNT(*) FROM booking WHERE id_pelanggan = ?";
            try (PreparedStatement ps = this.cn.prepareStatement(this.sql)) {
                ps.setInt(1, pl.getId_pelanggan());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        this.pesan = "Pelanggan tidak bisa dihapus karena masih memiliki riwayat booking.";
                        return false;
                    }
                }
            }

            this.sql = "DELETE FROM pelanggan WHERE id = ?";
            try (PreparedStatement ps = this.cn.prepareStatement(this.sql)) {
                ps.setInt(1, pl.getId_pelanggan());
                if (ps.executeUpdate() == 0) {
                    this.pesan = "Pelanggan tidak ditemukan.";
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            this.pesan = "Gagal menghapus data pelanggan.";
            System.out.println("hapusPelanggan failed: " + e.getMessage());
            return false;
        }
    }

    // ---------- helper ----------

    private String rapikan(String teks) {
        return teks == null ? "" : teks.trim();
    }

    // Buang spasi dan tanda strip: "0812-3456 7890" -> "081234567890"
    private String rapikanNoHp(String noHp) {
        return noHp == null ? "" : noHp.replaceAll("[\\s-]", "");
    }

    private boolean valid(Pelanggan pl) {
        if (pl.getNama_pelanggan().isEmpty()) {
            this.pesan = "Nama pelanggan tidak boleh kosong.";
            return false;
        }
        if (pl.getNama_pelanggan().length() > 100) {
            this.pesan = "Nama pelanggan maksimal 100 karakter.";
            return false;
        }
        // No. HP boleh kosong; jika diisi harus 8-15 digit (boleh diawali +)
        if (!pl.getNo_hp().isEmpty() && !pl.getNo_hp().matches("\\+?[0-9]{8,15}")) {
            this.pesan = "No. HP harus berupa angka 8-15 digit.";
            return false;
        }
        return true;
    }
}