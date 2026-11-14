package Controller;

import Model.Booking;
import Model.MetodePembayaran;
import Model.Pembayaran;
import Model.StatusLayanan;
import Utility.Koneksi;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import javax.swing.table.DefaultTableModel;

public class PembayaranController {

    public Connection cn;
    public Statement stm;
    public ResultSet res;
    public String sql;
    public String pesan = "";

    // Diisi setelah bayar() sukses, supaya view bisa menampilkan kembalian / struk
    public Pembayaran pembayaranTerakhir;

    DefaultTableModel dtm = new DefaultTableModel();

    public PembayaranController() {
        Koneksi db = new Koneksi();
        db.config();
        this.cn = db.cn;
        this.stm = db.stm;
    }

    public DefaultTableModel createTable() {
        if (this.dtm.getColumnCount() == 0) {
            this.dtm.addColumn("ID Pembayaran");
            this.dtm.addColumn("ID Booking");
            this.dtm.addColumn("Pelanggan");
            this.dtm.addColumn("Metode");
            this.dtm.addColumn("Jumlah Bayar");
            this.dtm.addColumn("Kembalian");
            this.dtm.addColumn("Waktu Bayar");
        }
        return this.dtm;
    }

    public void tampilkanPembayaran() {
        try {
            this.dtm.setRowCount(0);
            this.sql = "SELECT p.id, p.id_booking, pl.nama AS nama_pelanggan, p.metode, "
                    + "p.jumlah_bayar, p.kembalian, p.waktu_bayar "
                    + "FROM pembayaran p "
                    + "JOIN booking b ON b.id = p.id_booking "
                    + "JOIN pelanggan pl ON pl.id = b.id_pelanggan "
                    + "ORDER BY p.waktu_bayar DESC";
            this.res = this.stm.executeQuery(this.sql);
            while (res.next()) {
                this.dtm.addRow(new Object[]{
                    res.getString("id"),
                    res.getString("id_booking"),
                    res.getString("nama_pelanggan"),
                    res.getString("metode"),
                    res.getDouble("jumlah_bayar"),
                    res.getDouble("kembalian"),
                    res.getString("waktu_bayar")
                });
            }
        } catch (Exception e) {
            System.out.println("Gagal Query Pembayaran..");
            System.out.println(e.getMessage());
        }
    }

    /** Booking harus SELESAI dan belum pernah dibayar. */
    public boolean bayar(int idBooking, MetodePembayaran metode, double uangDiberikan) {
        this.pembayaranTerakhir = null;

        if (metode == null) {
            this.pesan = "Metode pembayaran belum dipilih.";
            return false;
        }

        Booking b = new BookingController().cariBooking(idBooking);
        if (b == null) {
            this.pesan = "Booking tidak ditemukan.";
            return false;
        }
        if (b.getStatus() != StatusLayanan.SELESAI) {
            this.pesan = "Booking belum selesai, belum bisa dibayar.";
            return false;
        }
        if (sudahDibayar(idBooking)) {
            this.pesan = "Booking ini sudah dibayar.";
            return false;
        }

        Pembayaran p;
        try {
            p = Pembayaran.buat(idBooking, b.getTotalBiaya(), metode, uangDiberikan);
        } catch (IllegalArgumentException e) {
            this.pesan = e.getMessage();
            return false;
        }

        try {
            this.sql = "INSERT INTO pembayaran (id_booking, metode, jumlah_bayar, kembalian, waktu_bayar) "
                    + "VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = this.cn.prepareStatement(this.sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, p.getIdBooking());
                ps.setString(2, p.getMetode().name());
                ps.setDouble(3, p.getJumlahBayar());
                ps.setDouble(4, p.getKembalian());
                ps.setTimestamp(5, Timestamp.valueOf(p.getWaktuBayar()));
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        p.setId(keys.getInt(1));
                    }
                }
            }
            this.pembayaranTerakhir = p;
            return true;
        } catch (Exception e) {
            this.pesan = "Gagal menyimpan pembayaran.";
            System.out.println("bayar failed: " + e.getMessage());
            return false;
        }
    }

    public boolean sudahDibayar(int idBooking) {
        try {
            this.sql = "SELECT COUNT(*) FROM pembayaran WHERE id_booking = ?";
            try (PreparedStatement ps = this.cn.prepareStatement(this.sql)) {
                ps.setInt(1, idBooking);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() && rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            System.out.println("sudahDibayar failed: " + e.getMessage());
            return false;
        }
    }
}