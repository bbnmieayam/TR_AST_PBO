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

    public DefaultTableModel createTable() {
        if (this.dtm.getColumnCount() == 0) {
            this.dtm.addColumn("ID Layanan");
            this.dtm.addColumn("Nama Layanan");
            this.dtm.addColumn("Harga");
            this.dtm.addColumn("Estimasi (menit)");
        }
        return this.dtm;
    }

    public void tampilkanLayanan() {
        try {
            this.dtm.setRowCount(0);
            this.sql = "SELECT * FROM layanan ORDER BY nama";
            this.res = this.stm.executeQuery(this.sql);
            while (res.next()) {
                this.dtm.addRow(new Object[]{
                    res.getString("id"), res.getString("nama"),
                    res.getDouble("harga"), res.getInt("estimasi_menit")
                });
            }
        } catch (Exception e) {
            System.out.println("Gagal Query Layanan..");
            System.out.println(e.getMessage());
        }
    }

    public Layanan cariLayanan(int id) {
        try {
            this.sql = "SELECT * FROM layanan WHERE id = ?";
            try (PreparedStatement ps = this.cn.prepareStatement(this.sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new Layanan(rs.getInt("id"), rs.getString("nama"),
                                rs.getDouble("harga"), rs.getInt("estimasi_menit"));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Gagal cari Layanan..");
            System.out.println(e.getMessage());
        }
        return null;
    }

    public boolean tambahLayanan(String nama, double harga, int estimasiMenit) {
        Layanan l = new Layanan(rapikan(nama), harga, estimasiMenit);
        if (!valid(l)) {
            return false;
        }
        try {
            this.sql = "INSERT INTO layanan (nama, harga, estimasi_menit) VALUES (?, ?, ?)";
            try (PreparedStatement ps = this.cn.prepareStatement(this.sql)) {
                ps.setString(1, l.getNama());
                ps.setDouble(2, l.getHarga());
                ps.setInt(3, l.getEstimasiMenit());
                ps.executeUpdate();
            }
            return true;
        } catch (Exception e) {
            this.pesan = "Gagal menyimpan data layanan.";
            System.out.println("tambahLayanan failed: " + e.getMessage());
            return false;
        }
    }

    public boolean ubahLayanan(int id, String nama, double harga, int estimasiMenit) {
        Layanan l = new Layanan(id, rapikan(nama), harga, estimasiMenit);
        if (!valid(l)) {
            return false;
        }
        try {
            this.sql = "UPDATE layanan SET nama = ?, harga = ?, estimasi_menit = ? WHERE id = ?";
            try (PreparedStatement ps = this.cn.prepareStatement(this.sql)) {
                ps.setString(1, l.getNama());
                ps.setDouble(2, l.getHarga());
                ps.setInt(3, l.getEstimasiMenit());
                ps.setInt(4, l.getId());
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

    public boolean hapusLayanan(int id) {
        try {
            this.sql = "SELECT COUNT(*) FROM booking_layanan WHERE id_layanan = ?";
            try (PreparedStatement ps = this.cn.prepareStatement(this.sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        this.pesan = "Layanan tidak bisa dihapus karena sudah pernah dipesan.";
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

    private boolean valid(Layanan l) {
        if (l.getNama().isEmpty()) {
            this.pesan = "Nama layanan tidak boleh kosong.";
            return false;
        }
        if (l.getNama().length() > 100) {
            this.pesan = "Nama layanan maksimal 100 karakter.";
            return false;
        }
        if (l.getHarga() <= 0) {
            this.pesan = "Harga harus lebih dari 0.";
            return false;
        }
        if (l.getEstimasiMenit() <= 0) {
            this.pesan = "Estimasi waktu harus lebih dari 0 menit.";
            return false;
        }
        return true;
    }
} 