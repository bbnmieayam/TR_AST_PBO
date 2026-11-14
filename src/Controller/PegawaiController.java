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

    // Alasan gagal, untuk ditampilkan oleh view (misalnya lewat JOptionPane)
    public String pesan = "";

    // Model tabel virtual
    DefaultTableModel dtm = new DefaultTableModel();

    // Konstruktor
    public PegawaiController() {
        Koneksi db = new Koneksi();
        db.config();
        this.cn = db.cn;
        this.stm = db.stm;
    }

    // Method 1: Membuat desain tabel virtual
    public DefaultTableModel createTable() {
        if (this.dtm.getColumnCount() == 0) {
            this.dtm.addColumn("ID Pegawai");
            this.dtm.addColumn("Nama Pegawai");
            this.dtm.addColumn("Spesialisasi");
        }
        return this.dtm;
    }

    // Method 2: SELECT semua
    public void tampilkanPegawai() {
        try {
            // Bersihkan tabel virtual
            this.dtm.setRowCount(0);

            // Query
            this.sql = "SELECT * FROM pegawai ORDER BY nama";

            // Jalankan query
            this.res = this.stm.executeQuery(this.sql);

            // Masukkan hasil query ke tabel virtual
            while (res.next()) {
                Object[] obj = new Object[3];

                obj[0] = res.getString("id");
                obj[1] = res.getString("nama");
                obj[2] = res.getString("spesialisasi");

                this.dtm.addRow(obj);
            }

        } catch (Exception e) {
            System.out.println("Gagal Query Pegawai..");
            System.out.println(e.getMessage());
        }
    }

    // Method 3: SELECT satu (dipakai juga oleh BookingController). Return null jika tidak ada.
    public Pegawai cariPegawai(int id) {
        try {
            this.sql = "SELECT * FROM pegawai WHERE id = ?";
            try (PreparedStatement ps = this.cn.prepareStatement(this.sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Pegawai pg = new Pegawai();
                        pg.setId_pegawai(rs.getInt("id"));
                        pg.setNama_pegawai(rs.getString("nama"));
                        pg.setSpesialisasi(rs.getString("spesialisasi"));
                        return pg;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Gagal cari Pegawai..");
            System.out.println(e.getMessage());
        }
        return null;
    }

    // Method 4: INSERT
    public boolean tambahPegawai(String nama, String spesialisasi) {
        Pegawai pg = new Pegawai();
        pg.setNama_pegawai(rapikan(nama));
        pg.setSpesialisasi(rapikan(spesialisasi));

        if (!valid(pg)) {
            return false;
        }

        try {
            this.sql = "INSERT INTO pegawai (nama, spesialisasi) VALUES (?, ?)";
            try (PreparedStatement ps = this.cn.prepareStatement(this.sql)) {
                ps.setString(1, pg.getNama_pegawai());
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

    // Method 5: UPDATE
    public boolean ubahPegawai(int id, String nama, String spesialisasi) {
        Pegawai pg = new Pegawai();
        pg.setId_pegawai(id);
        pg.setNama_pegawai(rapikan(nama));
        pg.setSpesialisasi(rapikan(spesialisasi));

        if (!valid(pg)) {
            return false;
        }

        try {
            this.sql = "UPDATE pegawai SET nama = ?, spesialisasi = ? WHERE id = ?";
            try (PreparedStatement ps = this.cn.prepareStatement(this.sql)) {
                ps.setString(1, pg.getNama_pegawai());
                ps.setString(2, pg.getSpesialisasi().isEmpty() ? null : pg.getSpesialisasi());
                ps.setInt(3, pg.getId_pegawai());

                // Jika tidak ada baris terupdate => gagal
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

    // Method 6: DELETE
    public boolean hapusPegawai(int id) {
        Pegawai pg = new Pegawai();
        pg.setId_pegawai(id);

        try {
            // Pegawai yang punya riwayat booking tidak boleh dihapus
            this.sql = "SELECT COUNT(*) FROM booking WHERE id_pegawai = ?";
            try (PreparedStatement ps = this.cn.prepareStatement(this.sql)) {
                ps.setInt(1, pg.getId_pegawai());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        this.pesan = "Pegawai tidak bisa dihapus karena masih memiliki data booking.";
                        return false;
                    }
                }
            }

            this.sql = "DELETE FROM pegawai WHERE id = ?";
            try (PreparedStatement ps = this.cn.prepareStatement(this.sql)) {
                ps.setInt(1, pg.getId_pegawai());
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

    // ---------- helper ----------

    private String rapikan(String teks) {
        return teks == null ? "" : teks.trim();
    }

    private boolean valid(Pegawai pg) {
        if (pg.getNama_pegawai().isEmpty()) {
            this.pesan = "Nama pegawai tidak boleh kosong.";
            return false;
        }
        if (pg.getNama_pegawai().length() > 100) {
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