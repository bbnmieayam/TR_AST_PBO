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
import java.time.format.DateTimeFormatter;
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

    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public BookingController() {
        Koneksi db = new Koneksi();
        db.config();
        this.cn = db.cn;
        this.stm = db.stm;
    }

    // Method 1: Membuat desain tabel virtual
    public DefaultTableModel createTable() {
        if (this.dtm.getColumnCount() == 0) {
            this.dtm.addColumn("ID Booking");
            this.dtm.addColumn("Pelanggan");
            this.dtm.addColumn("Pegawai");
            this.dtm.addColumn("Layanan");
            this.dtm.addColumn("Waktu Mulai");
            this.dtm.addColumn("Waktu Selesai");
            this.dtm.addColumn("Total Biaya");
            this.dtm.addColumn("Status");
        }
        return this.dtm;
    }

    // Method 2: SELECT semua dengan JOIN & GROUP_CONCAT untuk ringkasan layanan
    public void tampilkanBooking() {
        try {
            this.dtm.setRowCount(0);
            this.sql = "SELECT b.id, p.nama AS nama_pelanggan, pg.nama AS nama_pegawai, "
                     + "GROUP_CONCAT(l.nama SEPARATOR ', ') AS ringkasan_layanan, "
                     + "b.waktu_mulai, b.waktu_selesai, b.total_biaya, b.status "
                     + "FROM booking b "
                     + "JOIN pelanggan p ON b.id_pelanggan = p.id "
                     + "JOIN pegawai pg ON b.id_pegawai = pg.id "
                     + "LEFT JOIN detail_booking db ON b.id = db.id_booking "
                     + "LEFT JOIN layanan l ON db.id_layanan = l.id "
                     + "GROUP BY b.id "
                     + "ORDER BY b.waktu_mulai DESC";
            this.res = this.stm.executeQuery(this.sql);

            while (res.next()) {
                Timestamp tsMulai = res.getTimestamp("waktu_mulai");
                Timestamp tsSelesai = res.getTimestamp("waktu_selesai");

                Object[] obj = new Object[8];
                obj[0] = res.getInt("id");
                obj[1] = res.getString("nama_pelanggan");
                obj[2] = res.getString("nama_pegawai");
                obj[3] = res.getString("ringkasan_layanan");
                obj[4] = tsMulai != null ? tsMulai.toLocalDateTime().format(fmt) : "-";
                obj[5] = tsSelesai != null ? tsSelesai.toLocalDateTime().format(fmt) : "-";
                obj[6] = res.getDouble("total_biaya");
                obj[7] = res.getString("status");

                this.dtm.addRow(obj);
            }
        } catch (Exception e) {
            System.out.println("Gagal Query Booking..");
            System.out.println(e.getMessage());
        }
    }

    // Method 3: SELECT satu Booking lengkap beserta daftar layanannya
    public Booking cariBooking(int id) {
        try {
            this.sql = "SELECT b.*, p.nama AS nama_pelanggan, pg.nama AS nama_pegawai "
                     + "FROM booking b "
                     + "JOIN pelanggan p ON b.id_pelanggan = p.id "
                     + "JOIN pegawai pg ON b.id_pegawai = pg.id "
                     + "WHERE b.id = ?";
            try (PreparedStatement ps = this.cn.prepareStatement(this.sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Booking bk = new Booking();
                        bk.setId(rs.getInt("id"));
                        bk.setIdPelanggan(rs.getInt("id_pelanggan"));
                        bk.setIdPegawai(rs.getInt("id_pegawai"));
                        bk.setNamaPelanggan(rs.getString("nama_pelanggan"));
                        bk.setNamaPegawai(rs.getString("nama_pegawai"));

                        Timestamp tsMulai = rs.getTimestamp("waktu_mulai");
                        Timestamp tsSelesai = rs.getTimestamp("waktu_selesai");
                        if (tsMulai != null) bk.setWaktuMulai(tsMulai.toLocalDateTime());
                        if (tsSelesai != null) bk.setWaktuSelesai(tsSelesai.toLocalDateTime());

                        bk.setTotalBiaya(rs.getDouble("total_biaya"));

                        String statusDb = rs.getString("status");
                        if (statusDb != null) {
                            bk.setStatus(StatusLayanan.valueOf(statusDb.toUpperCase().replace(" ", "_")));
                        }

                        // Ambil daftar nama layanan milik booking ini
                        List<String> listLayanan = ambilDaftarLayananBooking(id);
                        bk.setDaftarLayanan(listLayanan);
                        bk.setRingkasanLayanan(String.join(", ", listLayanan));

                        return bk;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Gagal cari Booking..");
            System.out.println(e.getMessage());
        }
        return null;
    }

    // Method 4: Cek bentrok jadwal pegawai berdasarkan rentang waktu (waktuMulai s/d waktuSelesai)
    public boolean cekJadwalBentrok(int idPegawai, LocalDateTime waktuMulai, LocalDateTime waktuSelesai) {
        try {
            // Dua rentang waktu [A_start, A_end] dan [B_start, B_end] bentrok jika:
            // A_start < B_end AND A_end > B_start
            this.sql = "SELECT COUNT(*) FROM booking "
                     + "WHERE id_pegawai = ? "
                     + "AND status IN ('MENUNGGU', 'SEDANG_DILAYANI') "
                     + "AND waktu_mulai < ? AND waktu_selesai > ?";
            try (PreparedStatement ps = this.cn.prepareStatement(this.sql)) {
                ps.setInt(1, idPegawai);
                ps.setTimestamp(2, Timestamp.valueOf(waktuSelesai));
                ps.setTimestamp(3, Timestamp.valueOf(waktuMulai));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Gagal cek jadwal bentrok: " + e.getMessage());
        }
        return false;
    }

    // Method 5: INSERT Booking baru dengan satu atau banyak layanan
    public boolean tambahBooking(int idPelanggan, int idPegawai, LocalDateTime waktuMulai, List<Integer> daftarIdLayanan) {
        if (idPelanggan <= 0 || idPegawai <= 0) {
            this.pesan = "Pelanggan dan pegawai wajib dipilih.";
            return false;
        }
        if (waktuMulai == null) {
            this.pesan = "Tanggal dan jam layanan wajib diisi.";
            return false;
        }
        if (daftarIdLayanan == null || daftarIdLayanan.isEmpty()) {
            this.pesan = "Pilih minimal 1 layanan salon.";
            return false;
        }

        // Hitung total biaya dan total estimasi menit dari seluruh layanan yang dipilih
        LayananController lc = new LayananController();
        double totalBiaya = 0;
        int totalMenit = 0;
        List<Layanan> layananValid = new ArrayList<>();

        for (int idLayanan : daftarIdLayanan) {
            Layanan ly = lc.cariLayanan(idLayanan);
            if (ly == null) {
                this.pesan = "Salah satu layanan dengan ID " + idLayanan + " tidak ditemukan.";
                return false;
            }
            totalBiaya += ly.getHarga();
            totalMenit += ly.getEstimasiMenit();
            layananValid.add(ly);
        }

        LocalDateTime waktuSelesai = waktuMulai.plusMinutes(totalMenit);

        // Cek ketersediaan pegawai pada rentang waktu tersebut
        if (cekJadwalBentrok(idPegawai, waktuMulai, waktuSelesai)) {
            this.pesan = "Jadwal bentrok! Pegawai masih melayani pelanggan lain pada rentang waktu "
                       + waktuMulai.format(fmt) + " - " + waktuSelesai.format(fmt) + ".";
            return false;
        }

        try {
            this.cn.setAutoCommit(false);

            // 1. Insert ke tabel utama booking
            this.sql = "INSERT INTO booking (id_pelanggan, id_pegawai, waktu_mulai, waktu_selesai, total_biaya, status) "
                     + "VALUES (?, ?, ?, ?, ?, ?)";
            int idBookingBaru = 0;
            try (PreparedStatement ps = this.cn.prepareStatement(this.sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, idPelanggan);
                ps.setInt(2, idPegawai);
                ps.setTimestamp(3, Timestamp.valueOf(waktuMulai));
                ps.setTimestamp(4, Timestamp.valueOf(waktuSelesai));
                ps.setDouble(5, totalBiaya);
                ps.setString(6, StatusLayanan.MENUNGGU.name());
                ps.executeUpdate();

                try (ResultSet rsKey = ps.getGeneratedKeys()) {
                    if (rsKey.next()) {
                        idBookingBaru = rsKey.getInt(1);
                    }
                }
            }

            // 2. Insert rincian layanan ke tabel detail_booking
            this.sql = "INSERT INTO detail_booking (id_booking, id_layanan, harga_saat_booking) VALUES (?, ?, ?)";
            try (PreparedStatement psDetail = this.cn.prepareStatement(this.sql)) {
                for (Layanan ly : layananValid) {
                    psDetail.setInt(1, idBookingBaru);
                    psDetail.setInt(2, ly.getId());
                    psDetail.setDouble(3, ly.getHarga());
                    psDetail.addBatch();
                }
                psDetail.executeBatch();
            }

            this.cn.commit();
            return true;
        } catch (Exception e) {
            try {
                this.cn.rollback();
            } catch (Exception ex) {
                System.out.println("Rollback gagal: " + ex.getMessage());
            }
            this.pesan = "Gagal menyimpan data booking.";
            System.out.println("tambahBooking failed: " + e.getMessage());
            return false;
        } finally {
            try {
                this.cn.setAutoCommit(true);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    // Method 6: UPDATE Status Layanan menggunakan Enum StatusLayanan
    public boolean ubahStatusBooking(int idBooking, StatusLayanan statusBaru) {
        if (statusBaru == null) {
            this.pesan = "Status layanan tidak valid.";
            return false;
        }

        try {
            this.sql = "UPDATE booking SET status = ? WHERE id = ?";
            try (PreparedStatement ps = this.cn.prepareStatement(this.sql)) {
                ps.setString(1, statusBaru.name());
                ps.setInt(2, idBooking);

                if (ps.executeUpdate() == 0) {
                    this.pesan = "Data booking tidak ditemukan.";
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            this.pesan = "Gagal memperbarui status booking.";
            System.out.println("ubahStatusBooking failed: " + e.getMessage());
            return false;
        }
    }

    // Method 7: DELETE Booking (hapus detail_booking lalu booking jika belum dibayar)
    public boolean hapusBooking(int idBooking) {
        try {
            this.sql = "SELECT COUNT(*) FROM pembayaran WHERE id_booking = ?";
            try (PreparedStatement ps = this.cn.prepareStatement(this.sql)) {
                ps.setInt(1, idBooking);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        this.pesan = "Booking tidak dapat dihapus karena transaksi pembayaran sudah tercatat.";
                        return false;
                    }
                }
            }

            this.cn.setAutoCommit(false);

            this.sql = "DELETE FROM detail_booking WHERE id_booking = ?";
            try (PreparedStatement psDetail = this.cn.prepareStatement(this.sql)) {
                psDetail.setInt(1, idBooking);
                psDetail.executeUpdate();
            }

            this.sql = "DELETE FROM booking WHERE id = ?";
            try (PreparedStatement ps = this.cn.prepareStatement(this.sql)) {
                ps.setInt(1, idBooking);
                if (ps.executeUpdate() == 0) {
                    this.cn.rollback();
                    this.pesan = "Data booking tidak ditemukan.";
                    return false;
                }
            }

            this.cn.commit();
            return true;
        } catch (Exception e) {
            try {
                this.cn.rollback();
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
            this.pesan = "Gagal menghapus data booking.";
            System.out.println("hapusBooking failed: " + e.getMessage());
            return false;
        } finally {
            try {
                this.cn.setAutoCommit(true);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    // ---------- helper ----------

    private List<String> ambilDaftarLayananBooking(int idBooking) {
        List<String> list = new ArrayList<>();
        try {
            String q = "SELECT l.nama FROM detail_booking db "
                     + "JOIN layanan l ON db.id_layanan = l.id "
                     + "WHERE db.id_booking = ?";
            try (PreparedStatement ps = this.cn.prepareStatement(q)) {
                ps.setInt(1, idBooking);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(rs.getString("nama"));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Gagal ambil detail layanan: " + e.getMessage());
        }
        return list;
    }
}