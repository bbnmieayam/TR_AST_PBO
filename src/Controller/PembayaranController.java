package Controller;

import Model.Pembayaran;
import Model.PembayaranCash;
import Model.PembayaranQris;
import Utility.Koneksi;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.swing.table.DefaultTableModel;

public class PembayaranController {

    public Connection cn;
    public Statement stm;
    public ResultSet res;
    public String sql;

    // Alasan gagal, untuk ditampilkan oleh view (misalnya lewat JOptionPane)
    public String pesan = "";

    // Kembalian dari pembayaran terakhir yang berhasil (0 untuk QRIS), untuk ditampilkan view
    public double kembalianTerakhir = 0;

    // true  = booking hanya boleh dibayar setelah statusnya SELESAI
    // false = boleh dibayar kapan saja (ubah ke false jika soal tidak mensyaratkan)
    public boolean wajibSelesai = true;

    // Model tabel virtual
    DefaultTableModel dtm = new DefaultTableModel();

    // Konstruktor
    public PembayaranController() {
        Koneksi db = new Koneksi();
        db.config();
        this.cn = db.cn;
        this.stm = db.stm;
    }

    // Method 1: Membuat desain tabel virtual
    public DefaultTableModel createTable() {
        if (this.dtm.getColumnCount() == 0) {
            this.dtm.addColumn("ID Pembayaran");
            this.dtm.addColumn("ID Booking");
            this.dtm.addColumn("Metode");
            this.dtm.addColumn("Total Biaya");
            this.dtm.addColumn("Dibayar");
            this.dtm.addColumn("Kembalian");
            this.dtm.addColumn("Waktu Bayar");
        }
        return this.dtm;
    }

    // Method 2: SELECT semua pembayaran
    public void tampilkanPembayaran() {
        try {
            // Bersihkan tabel virtual
            this.dtm.setRowCount(0);

            // Query
            this.sql = "SELECT p.id, p.id_booking, p.metode, b.total_biaya, "
                    + "p.jumlah_bayar, p.kembalian, p.waktu_bayar "
                    + "FROM pembayaran p JOIN booking b ON b.id = p.id_booking "
                    + "ORDER BY p.waktu_bayar DESC";

            // Jalankan query
            this.res = this.stm.executeQuery(this.sql);

            // Masukkan hasil query ke tabel virtual
            while (res.next()) {
                Object[] obj = new Object[7];

                obj[0] = res.getString("id");
                obj[1] = res.getString("id_booking");
                obj[2] = res.getString("metode");
                obj[3] = res.getString("total_biaya");
                obj[4] = res.getString("jumlah_bayar");
                obj[5] = res.getString("kembalian");
                obj[6] = res.getString("waktu_bayar");

                this.dtm.addRow(obj);
            }

        } catch (Exception e) {
            System.out.println("Gagal Query Pembayaran..");
            System.out.println(e.getMessage());
        }
    }

    // Method 3: Bayar CASH (menghitung kembalian)
    public boolean bayarCash(int idBooking, double uangDiterima) {
        double total = ambilTotalSiapBayar(idBooking);
        if (total < 0) {
            return false; // alasan sudah diisi di this.pesan
        }

        if (uangDiterima < total) {
            this.pesan = String.format("Uang yang diterima kurang. Total biaya: Rp %,.0f", total);
            return false;
        }

        Pembayaran pb = new PembayaranCash(idBooking, total, uangDiterima);
        return simpan(pb);
    }

    // Method 4: Bayar QRIS (tanpa kembalian)
    public boolean bayarQris(int idBooking) {
        double total = ambilTotalSiapBayar(idBooking);
        if (total < 0) {
            return false; 
        }

        Pembayaran pb = new PembayaranQris(idBooking, total);
        return simpan(pb);
    }

    // ---------- helper ----------

    // Pastikan booking ada, belum dibayar, dan (opsional) berstatus SELESAI.
    // Return total biaya, atau -1 jika tidak boleh dibayar (alasannya di this.pesan).
    private double ambilTotalSiapBayar(int idBooking) {
        try {
            this.sql = "SELECT total_biaya, status FROM booking WHERE id = ?";
            double total;
            try (PreparedStatement ps = this.cn.prepareStatement(this.sql)) {
                ps.setInt(1, idBooking);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        this.pesan = "Booking tidak ditemukan.";
                        return -1;
                    }
                    total = rs.getDouble("total_biaya");
                    String status = rs.getString("status");

                    if (this.wajibSelesai && !"SELESAI".equals(status)) {
                        this.pesan = "Booking belum selesai dilayani, belum bisa dibayar.";
                        return -1;
                    }
                }
            }

            this.sql = "SELECT COUNT(*) FROM pembayaran WHERE id_booking = ?";
            try (PreparedStatement ps = this.cn.prepareStatement(this.sql)) {
                ps.setInt(1, idBooking);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        this.pesan = "Booking ini sudah dibayar.";
                        return -1;
                    }
                }
            }
            return total;

        } catch (Exception e) {
            this.pesan = "Gagal memeriksa data booking.";
            System.out.println("ambilTotalSiapBayar failed: " + e.getMessage());
            return -1;
        }
    }

    // Simpan pembayaran apa pun jenisnya (CASH atau QRIS) lewat satu method
    private boolean simpan(Pembayaran pb) {
        try {
            this.sql = "INSERT INTO pembayaran (id_booking, metode, jumlah_bayar, kembalian) "
                    + "VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = this.cn.prepareStatement(this.sql)) {
                ps.setInt(1, pb.getId_booking());
                ps.setString(2, pb.getMetode());
                ps.setDouble(3, pb.getJumlah_bayar());
                ps.setDouble(4, pb.hitungKembalian());
                ps.executeUpdate();
            }
            this.kembalianTerakhir = pb.hitungKembalian();
            return true;
        } catch (Exception e) {
            this.pesan = "Gagal menyimpan pembayaran.";
            System.out.println("simpan pembayaran failed: " + e.getMessage());
            return false;
        }
    }
}
