package Controller;

import Model.Layanan;
import Utility.Koneksi;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.swing.table.DefaultTableModel;

public class LayananController {

    public Connection cn;
    public Statement stm;
    public ResultSet res;
    public String sql;

    public String pesan = "";
    DefaultTableModel dtm = new DefaultTableModel();

    public LayananController() {
        Koneksi db = new Koneksi();
        db.config();
        this.cn = db.cn;
        this.stm = db.stm;
    }

    // Method 1: Membuat desain tabel virtual
    public DefaultTableModel createTable() {
        if (this.dtm.getColumnCount() == 0) {
            this.dtm.addColumn("ID Layanan");
            this.dtm.addColumn("Nama Layanan");
            this.dtm.addColumn("Harga (Rp)");
            this.dtm.addColumn("Estimasi (Menit)");
        }
        return this.dtm;
    }

    // Method 2: SELECT semua
    public void tampilkanLayanan() {
        try {
            this.dtm.setRowCount(0);
            this.sql = "SELECT * FROM layanan ORDER BY nama";
            this.res = this.stm.executeQuery(this.sql);

            while (res.next()) {
                Object[] obj = new Object[4];
                obj[0] = res.getInt("id");
                obj[1] = res.getString("nama");
                obj[2] = res.getDouble("harga");
                obj[3] = res.getInt("estimasi_menit");

                this.dtm.addRow(obj);
            }
        } catch (Exception e) {
            System.out.println("Gagal Query Layanan..");
            System.out.println(e.getMessage());
        }
    }

    // Method 3: SELECT satu (dipakai oleh BookingController)
    public Layanan cariLayanan(int id) {
        try {
            this.sql = "SELECT * FROM layanan WHERE id = ?";
            try (PreparedStatement ps = this.cn.prepareStatement(this.sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new Layanan(
                            rs.getInt("id"),
                            rs.getString("nama"),
                            rs.getDouble("harga"),
                            rs.getInt("estimasi_menit")
                        );
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Gagal cari Layanan..");
            System.out.println(e.getMessage());
        }
        return null;
    }

    // Method 4: INSERT
    public boolean tambahLayanan(String nama, double harga, int estimasiMenit) {
        Layanan ly = new Layanan(rapikan(nama), harga, estimasiMenit);

        if (!valid(ly)) {
            return false;
        }

        try {
            this.sql = "INSERT INTO layanan (nama, harga, estimasi_menit) VALUES (?, ?, ?)";
            try (PreparedStatement ps = this.cn.prepareStatement(this.sql)) {
                ps.setString(1, ly.getNama());
                ps.setDouble(2, ly.getHarga());
                ps.setInt(3, ly.getEstimasiMenit());
                ps.executeUpdate();
            }
            return true;
        } catch (Exception e) {
            this.pesan = "Gagal menyimpan data layanan.";
            System.out.println("tambahLayanan failed: " + e.getMessage());
            return false;
        }
    }

    // Method 5: UPDATE
    public boolean ubahLayanan(int id, String nama, double harga, int estimasiMenit) {
        Layanan ly = new Layanan(id, rapikan(nama), harga, estimasiMenit);

        if (!valid(ly)) {
            return false;
        }

        try {
            this.sql = "UPDATE layanan SET nama = ?, harga = ?, estimasi_menit = ? WHERE id = ?";
            try (PreparedStatement ps = this.cn.prepareStatement(this.sql)) {
                ps.setString(1, ly.getNama());
                ps.setDouble(2, ly.getHarga());
                ps.setInt(3, ly.getEstimasiMenit());
                ps.setInt(4, ly.getId());

                if (ps.executeUpdate() == 0) {
                    this.pesan = "Layanan tidak ditemukan.";
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            this.pesan = "Gagal mengubah data layanan.";
            System.out.println("ubahLayanan failed: " + e.getMessage());
            return false;
        }
    }

    // Method 6: DELETE
    public boolean hapusLayanan(int id) {
        try {
            // Cek apakah layanan masih dipakai di detail_booking
            this.sql = "SELECT COUNT(*) FROM detail_booking WHERE id_layanan = ?";
            try (PreparedStatement ps = this.cn.prepareStatement(this.sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        this.pesan = "Layanan tidak bisa dihapus karena pernah digunakan dalam booking.";
                        return false;
                    }
                }
            }

            this.sql = "DELETE FROM layanan WHERE id = ?";
            try (PreparedStatement ps = this.cn.prepareStatement(this.sql)) {
                ps.setInt(1, id);
                if (ps.executeUpdate() == 0) {
                    this.pesan = "Layanan tidak ditemukan.";
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            this.pesan = "Gagal menghapus data layanan.";
            System.out.println("hapusLayanan failed: " + e.getMessage());
            return false;
        }
    }

    private String rapikan(String teks) {
        return teks == null ? "" : teks.trim();
    }

    private boolean valid(Layanan ly) {
        if (ly.getNama().isEmpty()) {
            this.pesan = "Nama layanan tidak boleh kosong.";
            return false;
        }
        if (ly.getNama().length() > 100) {
            this.pesan = "Nama layanan maksimal 100 karakter.";
            return false;
        }
        if (ly.getHarga() <= 0) {
            this.pesan = "Harga layanan harus lebih dari 0.";
            return false;
        }
        if (ly.getEstimasiMenit() <= 0) {
            this.pesan = "Estimasi waktu pengerjaan harus lebih dari 0 menit.";
            return false;
        }
        return true;
    }
}