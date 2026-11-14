package Controller;

import Model.Pegawai;
import Utility.Koneksi;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.swing.table.DefaultTableModel;

public class PegawaiController {

    public Connection cn;
    public Statement stm;
    public ResultSet res;
    public String sql;
    public String pesan = "";

    DefaultTableModel dtm = new DefaultTableModel();

    public PegawaiController() {
        Koneksi db = new Koneksi();
        db.config();
        this.cn = db.cn;
        this.stm = db.stm;
    }

    public DefaultTableModel createTable() {
        if (this.dtm.getColumnCount() == 0) {
            this.dtm.addColumn("ID Pegawai");
            this.dtm.addColumn("Nama Pegawai");
            this.dtm.addColumn("Spesialisasi");
        }
        return this.dtm;
    }

    public void tampilkanPegawai() {
        try {
            this.dtm.setRowCount(0);
            this.sql = "SELECT * FROM pegawai ORDER BY nama";
            this.res = this.stm.executeQuery(this.sql);
            while (res.next()) {
                this.dtm.addRow(new Object[]{
                    res.getString("id"), res.getString("nama"), res.getString("spesialisasi")
                });
            }
        } catch (Exception e) {
            System.out.println("Gagal Query Pegawai..");
            System.out.println(e.getMessage());
        }
    }

    public Pegawai cariPegawai(int id) {
        try {
            this.sql = "SELECT * FROM pegawai WHERE id = ?";
            try (PreparedStatement ps = this.cn.prepareStatement(this.sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new Pegawai(rs.getInt("id"), rs.getString("nama"), rs.getString("spesialisasi"));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Gagal cari Pegawai..");
            System.out.println(e.getMessage());
        }
        return null;
    }

    public boolean tambahPegawai(String nama, String spesialisasi) {
        Pegawai pg = new Pegawai(rapikan(nama), rapikan(spesialisasi));
        if (!valid(pg)) {
            return false;
        }
        try {
            this.sql = "INSERT INTO pegawai (nama, spesialisasi) VALUES (?, ?)";
            try (PreparedStatement ps = this.cn.prepareStatement(this.sql)) {
                ps.setString(1, pg.getNama());
                ps.setString(2, pg.getSpesialisasi().isEmpty() ? null : pg.getSpesialisasi());
                ps.executeUpdate();
            }
            return true;
        } catch (Exception e) {
            this.pesan = "Gagal menyimpan data pegawai.";
            System.out.println("tambahPegawai failed: " + e.getMessage());
            return false;
        }
    }

    public boolean ubahPegawai(int id, String nama, String spesialisasi) {
        Pegawai pg = new Pegawai(id, rapikan(nama), rapikan(spesialisasi));
        if (!valid(pg)) {
            return false;
        }
        try {
            this.sql = "UPDATE pegawai SET nama = ?, spesialisasi = ? WHERE id = ?";
            try (PreparedStatement ps = this.cn.prepareStatement(this.sql)) {
                ps.setString(1, pg.getNama());
                ps.setString(2, pg.getSpesialisasi().isEmpty() ? null : pg.getSpesialisasi());
                ps.setInt(3, pg.getId());
                if (ps.executeUpdate() == 0) {
                    this.pesan = "Pegawai tidak ditemukan.";
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            this.pesan = "Gagal mengubah data pegawai.";
            System.out.println("ubahPegawai failed: " + e.getMessage());
            return false;
        }
    }

    public boolean hapusPegawai(int id) {
        try {
            this.sql = "SELECT COUNT(*) FROM booking WHERE id_pegawai = ?";
            try (PreparedStatement ps = this.cn.prepareStatement(this.sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        this.pesan = "Pegawai tidak bisa dihapus karena masih memiliki data booking.";
                        return false;
                    }
                }
            }
            this.sql = "DELETE FROM pegawai WHERE id = ?";
            try (PreparedStatement ps = this.cn.prepareStatement(this.sql)) {
                ps.setInt(1, id);
                if (ps.executeUpdate() == 0) {
                    this.pesan = "Pegawai tidak ditemukan.";
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            this.pesan = "Gagal menghapus data pegawai.";
            System.out.println("hapusPegawai failed: " + e.getMessage());
            return false;
        }
    }

    private String rapikan(String teks) {
        return teks == null ? "" : teks.trim();
    }

    private boolean valid(Pegawai pg) {
        if (pg.getNama().isEmpty()) {
            this.pesan = "Nama pegawai tidak boleh kosong.";
            return false;
        }
        if (pg.getNama().length() > 100) {
            this.pesan = "Nama pegawai maksimal 100 karakter.";
            return false;
        }
        if (pg.getSpesialisasi().length() > 100) {
            this.pesan = "Spesialisasi maksimal 100 karakter.";
            return false;
        }
        return true;
    }
}