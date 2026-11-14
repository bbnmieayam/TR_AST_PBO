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
    public String pesan = "";

    DefaultTableModel dtm = new DefaultTableModel();

    public PelangganController() {
        Koneksi db = new Koneksi();
        db.config();
        this.cn = db.cn;
        this.stm = db.stm;
    }

    public DefaultTableModel createTable() {
        if (this.dtm.getColumnCount() == 0) {
            this.dtm.addColumn("ID Pelanggan");
            this.dtm.addColumn("Nama Pelanggan");
            this.dtm.addColumn("No HP");
        }
        return this.dtm;
    }

    public void tampilkanPelanggan() {
        try {
            this.dtm.setRowCount(0);
            this.sql = "SELECT * FROM pelanggan ORDER BY nama";
            this.res = this.stm.executeQuery(this.sql);
            while (res.next()) {
                this.dtm.addRow(new Object[]{
                    res.getString("id"), res.getString("nama"), res.getString("no_hp")
                });
            }
        } catch (Exception e) {
            System.out.println("Gagal Query Pelanggan..");
            System.out.println(e.getMessage());
        }
    }

    public Pelanggan cariPelanggan(int id) {
        try {
            this.sql = "SELECT * FROM pelanggan WHERE id = ?";
            try (PreparedStatement ps = this.cn.prepareStatement(this.sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new Pelanggan(rs.getInt("id"), rs.getString("nama"), rs.getString("no_hp"));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Gagal cari Pelanggan..");
            System.out.println(e.getMessage());
        }
        return null;
    }

    public boolean tambahPelanggan(String nama, String noHp) {
        Pelanggan p = new Pelanggan(rapikan(nama), rapikan(noHp));
        if (!valid(p)) {
            return false;
        }
        try {
            this.sql = "INSERT INTO pelanggan (nama, no_hp) VALUES (?, ?)";
            try (PreparedStatement ps = this.cn.prepareStatement(this.sql)) {
                ps.setString(1, p.getNama());
                ps.setString(2, p.getNoHp().isEmpty() ? null : p.getNoHp());
                ps.executeUpdate();
            }
            return true;
        } catch (Exception e) {
            this.pesan = "Gagal menyimpan data pelanggan.";
            System.out.println("tambahPelanggan failed: " + e.getMessage());
            return false;
        }
    }

    public boolean ubahPelanggan(int id, String nama, String noHp) {
        Pelanggan p = new Pelanggan(id, rapikan(nama), rapikan(noHp));
        if (!valid(p)) {
            return false;
        }
        try {
            this.sql = "UPDATE pelanggan SET nama = ?, no_hp = ? WHERE id = ?";
            try (PreparedStatement ps = this.cn.prepareStatement(this.sql)) {
                ps.setString(1, p.getNama());
                ps.setString(2, p.getNoHp().isEmpty() ? null : p.getNoHp());
                ps.setInt(3, p.getId());
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

    public boolean hapusPelanggan(int id) {
        try {
            this.sql = "SELECT COUNT(*) FROM booking WHERE id_pelanggan = ?";
            try (PreparedStatement ps = this.cn.prepareStatement(this.sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        this.pesan = "Pelanggan tidak bisa dihapus karena masih memiliki data booking.";
                        return false;
                    }
                }
            }
            this.sql = "DELETE FROM pelanggan WHERE id = ?";
            try (PreparedStatement ps = this.cn.prepareStatement(this.sql)) {
                ps.setInt(1, id);
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

    private String rapikan(String teks) {
        return teks == null ? "" : teks.trim();
    }

    private boolean valid(Pelanggan p) {
        if (p.getNama().isEmpty()) {
            this.pesan = "Nama pelanggan tidak boleh kosong.";
            return false;
        }
        if (p.getNama().length() > 100) {
            this.pesan = "Nama pelanggan maksimal 100 karakter.";
            return false;
        }
        if (p.getNoHp().length() > 20) {
            this.pesan = "No HP maksimal 20 karakter.";
            return false;
        }
        if (!p.getNoHp().isEmpty() && !p.getNoHp().matches("[0-9+]+")) {
            this.pesan = "No HP hanya boleh berisi angka.";
            return false;
        }
        return true;
    }
