package Controller;

import Model.Booking;
import Model.Layanan;
import Model.StatusLayanan;
import Utility.Koneksi;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public class BookingController {

    public Connection cn;
    public Statement stm;
    public ResultSet res;
    public String sql;
    public String pesan = "";

    DefaultTableModel dtm = new DefaultTableModel();

    public BookingController() {
        Koneksi db = new Koneksi();
        db.config();
        this.cn = db.cn;
        this.stm = db.stm;
    }

    public DefaultTableModel createTable() {
        if (this.dtm.getColumnCount() == 0) {
            this.dtm.addColumn("ID Booking");
            this.dtm.addColumn("Pelanggan");
            this.dtm.addColumn("Pegawai");
            this.dtm.addColumn("Layanan");
            this.dtm.addColumn("Waktu Mulai");
            this.dtm.addColumn("Waktu Selesai");
            this.dtm.addColumn("Status");
            this.dtm.addColumn("Total Biaya");
        }
        return this.dtm;
    }

    public void tampilkanBooking() {
        try {
            this.dtm.setRowCount(0);
            this.sql = "SELECT b.id, pl.nama AS nama_pelanggan, pg.nama AS nama_pegawai, "
                    + "GROUP_CONCAT(l.nama SEPARATOR ', ') AS layanan, "
                    + "b.waktu_mulai, b.waktu_selesai, b.status, b.total_biaya "
                    + "FROM booking b "
                    + "JOIN pelanggan pl ON pl.id = b.id_pelanggan "
                    + "JOIN pegawai pg ON pg.id = b.id_pegawai "
                    + "LEFT JOIN booking_layanan bl ON bl.id_booking = b.id "
                    + "LEFT JOIN layanan l ON l.id = bl.id_layanan "
                    + "GROUP BY b.id, pl.nama, pg.nama, b.waktu_mulai, b.waktu_selesai, b.status, b.total_biaya "
                    + "ORDER BY b.waktu_mulai DESC";
            this.res = this.stm.executeQuery(this.sql);
            while (res.next()) {
                this.dtm.addRow(new Object[]{
                    res.getString("id"),
                    res.getString("nama_pelanggan"),
                    res.getString("nama_pegawai"),
                    res.getString("layanan"),
                    res.getString("waktu_mulai"),
                    res.getString("waktu_selesai"),
                    StatusLayanan.fromString(res.getString("status")).getLabel(),
                    res.getDouble("total_biaya")
                });
            }
        } catch (Exception e) {
            System.out.println("Gagal Query Booking..");
            System.out.println(e.getMessage());
        }
    }

    /**
     * Return null jika tidak ada. Dipakai juga oleh PembayaranController.
     */
    public Booking cariBooking(int id) {
        try {
            this.sql = "SELECT b.*, pl.nama AS nama_pelanggan, pg.nama AS nama_pegawai "
                    + "FROM booking b "
                    + "JOIN pelanggan pl ON pl.id = b.id_pelanggan "
                    + "JOIN pegawai pg ON pg.id = b.id_pegawai WHERE b.id = ?";
            try (PreparedStatement ps = this.cn.prepareStatement(this.sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Booking b = new Booking();
                        b.setId(rs.getInt("id"));
                        b.setIdPelanggan(rs.getInt("id_pelanggan"));
                        b.setIdPegawai(rs.getInt("id_pegawai"));
                        Timestamp mulai = rs.getTimestamp("waktu_mulai");
                        Timestamp selesai = rs.getTimestamp("waktu_selesai");
                        b.setWaktuMulai(mulai == null ? null : mulai.toLocalDateTime());
                        b.setWaktuSelesai(selesai == null ? null : selesai.toLocalDateTime());
                        b.setStatus(StatusLayanan.fromString(rs.getString("status")));
                        b.setTotalBiaya(rs.getDouble("total_biaya"));
                        b.setNamaPelanggan(rs.getString("nama_pelanggan"));
                        b.setNamaPegawai(rs.getString("nama_pegawai"));
                        return b;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Gagal cari Booking..");
            System.out.println(e.getMessage());
        }
        return null;
    }

    /**
     * Buat booking baru + detail layanannya dalam satu transaksi. Total biaya &
     * waktu selesai dihitung otomatis dari layanan yang dipilih.
     */
    public boolean tambahBooking(int idPelanggan, int idPegawai,
            LocalDateTime waktuMulai, List<Integer> idLayanan) {
        if (idPelanggan <= 0) {
            this.pesan = "Pelanggan belum dipilih.";
            return false;
        }
        if (idPegawai <= 0) {
            this.pesan = "Pegawai belum dipilih.";
            return false;
        }
        if (waktuMulai == null) {
            this.pesan = "Waktu mulai belum diisi.";
            return false;
        }
        if (idLayanan == null || idLayanan.isEmpty()) {
            this.pesan = "Pilih minimal satu layanan.";
            return false;
        }

        LayananController lc = new LayananController();
        List<Layanan> daftar = new ArrayList<>();
        double total = 0;
        int menit = 0;
        for (int idL : idLayanan) {
            Layanan l = lc.cariLayanan(idL);
            if (l == null) {
                this.pesan = "Layanan dengan ID " + idL + " tidak ditemukan.";
                return false;
            }
            daftar.add(l);
            total += l.getHarga();
            menit += l.getEstimasiMenit();
        }
        LocalDateTime waktuSelesai = waktuMulai.plusMinutes(menit);

        if (pegawaiBentrok(idPegawai, waktuMulai, waktuSelesai)) {
            this.pesan = "Pegawai sudah memiliki booking di rentang waktu tersebut.";
            return false;
        }

        try {
            this.cn.setAutoCommit(false);

            this.sql = "INSERT INTO booking (id_pelanggan, id_pegawai, waktu_mulai, waktu_selesai, status, total_biaya) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";
            int idBooking;
            try (PreparedStatement ps = this.cn.prepareStatement(this.sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, idPelanggan);
                ps.setInt(2, idPegawai);
                ps.setTimestamp(3, Timestamp.valueOf(waktuMulai));
                ps.setTimestamp(4, Timestamp.valueOf(waktuSelesai));
                ps.setString(5, StatusLayanan.MENUNGGU.name());
                ps.setDouble(6, total);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new Exception("ID booking tidak terbentuk");
                    }
                    idBooking = keys.getInt(1);
                }
            }

            this.sql = "INSERT INTO booking_layanan (id_booking, id_layanan, harga_saat_booking) VALUES (?, ?, ?)";
            try (PreparedStatement ps = this.cn.prepareStatement(this.sql)) {
                for (Layanan l : daftar) {
                    ps.setInt(1, idBooking);
                    ps.setInt(2, l.getId());
                    ps.setDouble(3, l.getHarga());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            this.cn.commit();
            return true;
        } catch (Exception e) {
            try {
                this.cn.rollback();
            } catch (Exception ignore) {
            }
            this.pesan = "Gagal menyimpan booking.";
            System.out.println("tambahBooking failed: " + e.getMessage());
            return false;
        } finally {
            try {
                this.cn.setAutoCommit(true);
            } catch (Exception ignore) {
            }
        }
    }

    /**
     * Status hanya boleh maju: MENUNGGU -> SEDANG_DILAYANI -> SELESAI.
     */
    public boolean ubahStatus(int idBooking, StatusLayanan statusBaru) {
        Booking b = cariBooking(idBooking);
        if (b == null) {
            this.pesan = "Booking tidak ditemukan.";
            return false;
        }
        if (statusBaru.ordinal() <= b.getStatus().ordinal()) {
            this.pesan = "Status tidak bisa dimundurkan dari " + b.getStatus().getLabel() + ".";
            return false;
        }
        try {
            this.sql = "UPDATE booking SET status = ? WHERE id = ?";
            try (PreparedStatement ps = this.cn.prepareStatement(this.sql)) {
                ps.setString(1, statusBaru.name());
                ps.setInt(2, idBooking);
                ps.executeUpdate();
            }
            return true;
        } catch (Exception e) {
            this.pesan = "Gagal mengubah status booking.";
            System.out.println("ubahStatus failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Booking yang sudah dibayar tidak boleh dihapus.
     */
    public boolean hapusBooking(int idBooking) {
        try {
            this.sql = "SELECT COUNT(*) FROM pembayaran WHERE id_booking = ?";
            try (PreparedStatement ps = this.cn.prepareStatement(this.sql)) {
                ps.setInt(1, idBooking);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        this.pesan = "Booking tidak bisa dihapus karena sudah dibayar.";
                        return false;
                    }
                }
            }

            this.cn.setAutoCommit(false);
            try (PreparedStatement ps = this.cn.prepareStatement("DELETE FROM booking_layanan WHERE id_booking = ?")) {
                ps.setInt(1, idBooking);
                ps.executeUpdate();
            }
            int terhapus;
            try (PreparedStatement ps = this.cn.prepareStatement("DELETE FROM booking WHERE id = ?")) {
                ps.setInt(1, idBooking);
                terhapus = ps.executeUpdate();
            }
            if (terhapus == 0) {
                this.cn.rollback();
                this.pesan = "Booking tidak ditemukan.";
                return false;
            }
            this.cn.commit();
            return true;
        } catch (Exception e) {
            try {
                this.cn.rollback();
            } catch (Exception ignore) {
            }
            this.pesan = "Gagal menghapus booking.";
            System.out.println("hapusBooking failed: " + e.getMessage());
            return false;
        } finally {
            try {
                this.cn.setAutoCommit(true);
            } catch (Exception ignore) {
            }
        }
    }

    // ---------- helper ----------
    private boolean pegawaiBentrok(int idPegawai, LocalDateTime mulai, LocalDateTime selesai) {
        try {
            this.sql = "SELECT COUNT(*) FROM booking WHERE id_pegawai = ? "
                    + "AND waktu_mulai < ? AND waktu_selesai > ?";
            try (PreparedStatement ps = this.cn.prepareStatement(this.sql)) {
                ps.setInt(1, idPegawai);
                ps.setTimestamp(2, Timestamp.valueOf(selesai));
                ps.setTimestamp(3, Timestamp.valueOf(mulai));
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() && rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            System.out.println("pegawaiBentrok failed: " + e.getMessage());
            return false;
        }
    }
}
