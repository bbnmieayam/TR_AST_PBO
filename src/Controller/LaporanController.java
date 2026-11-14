package Controller;

import Utility.Koneksi;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.swing.table.DefaultTableModel;

public class LaporanController {

    public Connection cn;
    public Statement stm;
    public ResultSet res;
    public String sql;

    public String pesan = "";

    DefaultTableModel dtmPendapatan = new DefaultTableModel();
    DefaultTableModel dtmRiwayat = new DefaultTableModel();

    public LaporanController() {
        Koneksi db = new Koneksi();
        db.config();
        this.cn = db.cn;
        this.stm = db.stm;
    }

    // Method 1: Desain tabel virtual Laporan Pendapatan
    public DefaultTableModel createTablePendapatan() {
        if (this.dtmPendapatan.getColumnCount() == 0) {
            this.dtmPendapatan.addColumn("ID Transaksi");
            this.dtmPendapatan.addColumn("Tanggal Bayar");
            this.dtmPendapatan.addColumn("Pelanggan");
            this.dtmPendapatan.addColumn("Layanan");
            this.dtmPendapatan.addColumn("Terapis");
            this.dtmPendapatan.addColumn("Metode");
            this.dtmPendapatan.addColumn("Nominal (Rp)");
        }
        return this.dtmPendapatan;
    }

    // Method 2: Desain tabel virtual Riwayat Layanan Pelanggan
    public DefaultTableModel createTableRiwayat() {
        if (this.dtmRiwayat.getColumnCount() == 0) {
            this.dtmRiwayat.addColumn("Tanggal");
            this.dtmRiwayat.addColumn("Jam");
            this.dtmRiwayat.addColumn("Nama Pelanggan");
            this.dtmRiwayat.addColumn("Layanan");
            this.dtmRiwayat.addColumn("Terapis");
            this.dtmRiwayat.addColumn("Biaya (Rp)");
            this.dtmRiwayat.addColumn("Status");
        }
        return this.dtmRiwayat;
    }

    // Method 3: Tampilkan Laporan Pendapatan berdasarkan rentang tanggal (YYYY-MM-DD)
    public double tampilkanLaporanPendapatan(String tanggalMulai, String tanggalSelesai) {
        double totalPendapatan = 0;
        try {
            this.dtmPendapatan.setRowCount(0);
            this.sql = "SELECT pb.id, pb.waktu_bayar, p.nama AS nama_pelanggan, l.nama_layanan, "
                     + "pg.nama AS nama_pegawai, pb.metode, pb.total_bayar "
                     + "FROM pembayaran pb "
                     + "JOIN booking b ON pb.id_booking = b.id "
                     + "JOIN pelanggan p ON b.id_pelanggan = p.id "
                     + "JOIN layanan l ON b.id_layanan = l.id "
                     + "JOIN pegawai pg ON b.id_pegawai = pg.id "
                     + "WHERE DATE(pb.waktu_bayar) BETWEEN ? AND ? "
                     + "ORDER BY pb.waktu_bayar DESC";

            try (PreparedStatement ps = this.cn.prepareStatement(this.sql)) {
                ps.setString(1, tanggalMulai.trim());
                ps.setString(2, tanggalSelesai.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        double nominal = rs.getDouble("total_bayar");
                        totalPendapatan += nominal;

                        Object[] obj = new Object[7];
                        obj[0] = rs.getInt("id");
                        obj[1] = rs.getString("waktu_bayar");
                        obj[2] = rs.getString("nama_pelanggan");
                        obj[3] = rs.getString("nama_layanan");
                        obj[4] = rs.getString("nama_pegawai");
                        obj[5] = rs.getString("metode");
                        obj[6] = nominal;

                        this.dtmPendapatan.addRow(obj);
                    }
                }
            }
        } catch (Exception e) {
            this.pesan = "Gagal memuat laporan pendapatan.";
            System.out.println("tampilkanLaporanPendapatan failed: " + e.getMessage());
        }
        return totalPendapatan;
    }

    // Method 4: Tampilkan seluruh pendapatan tanpa filter tanggal
    public double tampilkanSemuaPendapatan() {
        double totalPendapatan = 0;
        try {
            this.dtmPendapatan.setRowCount(0);
            this.sql = "SELECT pb.id, pb.waktu_bayar, p.nama AS nama_pelanggan, l.nama_layanan, "
                     + "pg.nama AS nama_pegawai, pb.metode, pb.total_bayar "
                     + "FROM pembayaran pb "
                     + "JOIN booking b ON pb.id_booking = b.id "
                     + "JOIN pelanggan p ON b.id_pelanggan = p.id "
                     + "JOIN layanan l ON b.id_layanan = l.id "
                     + "JOIN pegawai pg ON b.id_pegawai = pg.id "
                     + "ORDER BY pb.waktu_bayar DESC";

            this.res = this.stm.executeQuery(this.sql);
            while (res.next()) {
                double nominal = res.getDouble("total_bayar");
                totalPendapatan += nominal;

                Object[] obj = new Object[7];
                obj[0] = res.getInt("id");
                obj[1] = res.getString("waktu_bayar");
                obj[2] = res.getString("nama_pelanggan");
                obj[3] = res.getString("nama_layanan");
                obj[4] = res.getString("nama_pegawai");
                obj[5] = res.getString("metode");
                obj[6] = nominal;

                this.dtmPendapatan.addRow(obj);
            }
        } catch (Exception e) {
            this.pesan = "Gagal memuat seluruh laporan pendapatan.";
            System.out.println("tampilkanSemuaPendapatan failed: " + e.getMessage());
        }
        return totalPendapatan;
    }

    // Method 5: Tampilkan Riwayat Layanan Pelanggan tertentu berdasarkan ID Pelanggan
    public void tampilkanRiwayatPelanggan(int idPelanggan) {
        try {
            this.dtmRiwayat.setRowCount(0);
            this.sql = "SELECT b.tanggal, b.jam, p.nama AS nama_pelanggan, l.nama_layanan, "
                     + "pg.nama AS nama_pegawai, b.total_biaya, b.status "
                     + "FROM booking b "
                     + "JOIN pelanggan p ON b.id_pelanggan = p.id "
                     + "JOIN layanan l ON b.id_layanan = l.id "
                     + "JOIN pegawai pg ON b.id_pegawai = pg.id "
                     + "WHERE b.id_pelanggan = ? "
                     + "ORDER BY b.tanggal DESC, b.jam DESC";

            try (PreparedStatement ps = this.cn.prepareStatement(this.sql)) {
                ps.setInt(1, idPelanggan);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Object[] obj = new Object[7];
                        obj[0] = rs.getString("tanggal");
                        obj[1] = rs.getString("jam");
                        obj[2] = rs.getString("nama_pelanggan");
                        obj[3] = rs.getString("nama_layanan");
                        obj[4] = rs.getString("nama_pegawai");
                        obj[5] = rs.getDouble("total_biaya");
                        obj[6] = rs.getString("status");

                        this.dtmRiwayat.addRow(obj);
                    }
                }
            }
        } catch (Exception e) {
            this.pesan = "Gagal memuat riwayat layanan pelanggan.";
            System.out.println("tampilkanRiwayatPelanggan failed: " + e.getMessage());
        }
    }
}