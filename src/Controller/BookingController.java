package Controller;

import Model.Booking;
import Utility.Koneksi;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import javax.swing.table.DefaultTableModel;

public class BookingController {

    public Statement stm;
    public ResultSet res;
    public String sql;

    public String pesan = "";

    // model tabel / tabel virtual
    DefaultTableModel dtm = new DefaultTableModel();

    // konstruktor
    public BookingController() {
        // objek koneksi
        Koneksi db = new Koneksi();
        db.config();
        this.stm = db.stm;
    }

    // ==================== Method 1: Buat model/desain tabel ====================
    public DefaultTableModel createTable() {
        this.dtm = new DefaultTableModel();
        this.dtm.addColumn("ID Booking");
        this.dtm.addColumn("Pelanggan");
        this.dtm.addColumn("Pegawai");
        this.dtm.addColumn("Layanan");
        this.dtm.addColumn("Waktu Mulai");
        this.dtm.addColumn("Waktu Selesai");
        this.dtm.addColumn("Status");
        this.dtm.addColumn("Total Biaya");
        return this.dtm;
    }

    // ==================== Method 2: SELECT semua booking (isi JTable) ====================
    public void tampilkanBooking() {
        try {
            // bersihkan isi tabel virtual
            this.dtm.getDataVector().removeAllElements();
            this.dtm.fireTableDataChanged();

            // satu baris per booking, nama layanan digabung dengan koma
            this.sql = "SELECT b.id, pl.nama AS pelanggan, pg.nama AS pegawai, "
                    + "GROUP_CONCAT(l.nama SEPARATOR ', ') AS layanan, "
                    + "b.waktu_mulai, b.waktu_selesai, b.status, b.total_biaya "
                    + "FROM booking b "
                    + "JOIN pelanggan pl ON pl.id = b.id_pelanggan "
                    + "JOIN pegawai pg ON pg.id = b.id_pegawai "
                    + "JOIN booking_layanan bl ON bl.id_booking = b.id "
                    + "JOIN layanan l ON l.id = bl.id_layanan "
                    + "GROUP BY b.id, pl.nama, pg.nama, b.waktu_mulai, b.waktu_selesai, b.status, b.total_biaya "
                    + "ORDER BY b.waktu_mulai DESC";
            this.res = this.stm.executeQuery(sql);

            // masukkan hasil query ke tabel virtual
            while (res.next()) {
                Object[] obj = new Object[8];
                obj[0] = res.getInt("id");
                obj[1] = res.getString("pelanggan");
                obj[2] = res.getString("pegawai");
                obj[3] = res.getString("layanan");
                obj[4] = res.getTimestamp("waktu_mulai");
                obj[5] = res.getTimestamp("waktu_selesai");
                obj[6] = res.getString("status");
                obj[7] = res.getDouble("total_biaya");
                this.dtm.addRow(obj);
            }
        } catch (Exception e) {
            System.out.println("Query tampil booking gagal : " + e.getMessage());
        }
    }

    // ==================== Method 3: INSERT booking ====================
    // idLayanan boleh lebih dari satu, mis. new int[]{1, 3}
    public boolean tambahBooking(int idPelanggan, int[] idLayanan, int idPegawai, Timestamp mulai) {
        // 1. validasi input
        if (idLayanan == null || idLayanan.length == 0) {
            this.pesan = "Pilih minimal satu layanan.";
            return false;
        }
        if (mulai == null || mulai.before(new Timestamp(System.currentTimeMillis()))) {
            this.pesan = "Waktu booking tidak boleh di masa lalu.";
            return false;
        }
        if (!ada("pelanggan", idPelanggan)) {
            this.pesan = "Pelanggan tidak ditemukan.";
            return false;
        }
        if (!ada("pegawai", idPegawai)) {
            this.pesan = "Pegawai tidak ditemukan.";
            return false;
        }

        // 2. total biaya dan durasi dari layanan yang dipilih
        String daftar = daftarId(idLayanan);
        double[] r = ringkas(daftar); // {jumlah layanan ditemukan, total harga, total menit}
        if (r == null) {
            this.pesan = "Gagal membaca data layanan.";
            return false;
        }
        if (r[0] != daftar.split(",").length) {
            this.pesan = "Ada layanan yang tidak ditemukan.";
            return false;
        }

        // 3. Hubungkan dengan model Booking
        Booking bk = new Booking();
        bk.setId_pelanggan(idPelanggan);
        bk.setId_pegawai(idPegawai);
        bk.setWaktu_mulai(mulai);
        bk.setWaktu_selesai(new Timestamp(mulai.getTime() + (long) r[2] * 60000L));
        bk.setStatus("MENUNGGU");
        bk.setTotal_biaya(r[1]);

        try {
            // 4. cek jadwal bentrok: dua jadwal bertabrakan jika mulaiBaru < selesaiLama DAN selesaiBaru > mulaiLama
            this.sql = "SELECT COUNT(*) FROM booking WHERE id_pegawai = " + bk.getId_pegawai()
                    + " AND status <> 'SELESAI'"
                    + " AND '" + bk.getWaktu_mulai() + "' < waktu_selesai"
                    + " AND '" + bk.getWaktu_selesai() + "' > waktu_mulai";
            this.res = this.stm.executeQuery(sql);
            if (res.next() && res.getInt(1) > 0) {
                this.pesan = "Pegawai sudah punya jadwal pada jam tersebut.";
                return false;
            }

            // 5. simpan booking, ambil id barunya
            this.sql = "INSERT INTO booking (id_pelanggan, id_pegawai, waktu_mulai, waktu_selesai, status, total_biaya) VALUES ("
                    + bk.getId_pelanggan() + ", "
                    + bk.getId_pegawai() + ", '"
                    + bk.getWaktu_mulai() + "', '"
                    + bk.getWaktu_selesai() + "', '"
                    + bk.getStatus() + "', "
                    + bk.getTotal_biaya() + ")";
            this.stm.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            this.res = this.stm.getGeneratedKeys();
            res.next();
            int idBaru = res.getInt(1);

            // 6. simpan layanannya beserta harga saat ini; jika gagal, booking tadi dihapus lagi
            try {
                this.sql = "INSERT INTO booking_layanan (id_booking, id_layanan, harga_saat_booking) "
                        + "SELECT " + idBaru + ", id, harga FROM layanan WHERE id IN (" + daftar + ")";
                this.stm.executeUpdate(sql);
            } catch (Exception e) {
                this.stm.executeUpdate("DELETE FROM booking WHERE id = " + idBaru);
                throw e;
            }
            return true;
        } catch (Exception e) {
            System.out.println("Query tambah booking gagal : " + e.getMessage());
            this.pesan = "Gagal menyimpan booking.";
            return false;
        }
    }

    // ==================== Method 4: UPDATE status ====================
    // Status hanya boleh maju satu langkah: MENUNGGU -> SEDANG_DILAYANI -> SELESAI
    public boolean ubahStatus(int idBooking, String statusBaru) {
        try {
            this.sql = "SELECT status FROM booking WHERE id = " + idBooking;
            this.res = this.stm.executeQuery(sql);
            if (!res.next()) {
                this.pesan = "Booking tidak ditemukan.";
                return false;
            }
            String sekarang = res.getString("status");

            boolean boleh = ("MENUNGGU".equals(sekarang) && "SEDANG_DILAYANI".equals(statusBaru))
                    || ("SEDANG_DILAYANI".equals(sekarang) && "SELESAI".equals(statusBaru));
            if (!boleh) {
                this.pesan = "Status tidak bisa diubah dari " + sekarang + " ke " + statusBaru + ".";
                return false;
            }

            this.sql = "UPDATE booking SET status = '" + statusBaru + "' WHERE id = " + idBooking;
            this.stm.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            System.out.println("Query ubah status gagal : " + e.getMessage());
            this.pesan = "Gagal mengubah status.";
            return false;
        }
    }

    // pintasan untuk tombol di view
    public boolean mulaiDilayani(int idBooking) {
        return ubahStatus(idBooking, "SEDANG_DILAYANI");
    }

    public boolean selesaikan(int idBooking) {
        return ubahStatus(idBooking, "SELESAI");
    }

    // ==================== Method 5 & 6: hitung (untuk label di form sebelum disimpan) ====================
    public double hitungTotalBiaya(int[] idLayanan) {
        double[] r = ringkas(daftarId(idLayanan));
        return r == null ? 0 : r[1];
    }

    public int hitungDurasiMenit(int[] idLayanan) {
        double[] r = ringkas(daftarId(idLayanan));
        return r == null ? 0 : (int) r[2];
    }

    // ==================== helper ====================

    // cek apakah baris dengan id tersebut ada di tabel (nama tabel selalu dari kode, bukan input)
    private boolean ada(String tabel, int id) {
        try {
            this.res = this.stm.executeQuery("SELECT COUNT(*) FROM " + tabel + " WHERE id = " + id);
            return res.next() && res.getInt(1) > 0;
        } catch (Exception e) {
            System.out.println("Query cek " + tabel + " gagal : " + e.getMessage());
            return false;
        }
    }

    // {1, 3, 3} -> "1,3" (id kembar dibuang, hasil siap dipakai di IN (...))
    private String daftarId(int[] ids) {
        String hasil = "";
        if (ids == null) {
            return hasil;
        }
        for (int i = 0; i < ids.length; i++) {
            boolean kembar = false;
            for (int j = 0; j < i; j++) {
                if (ids[j] == ids[i]) {
                    kembar = true;
                }
            }
            if (!kembar) {
                hasil += (hasil.isEmpty() ? "" : ",") + ids[i];
            }
        }
        return hasil;
    }

    // return {jumlah layanan ditemukan, total harga, total menit}, atau null jika query gagal
    private double[] ringkas(String daftar) {
        if (daftar.isEmpty()) {
            return new double[]{0, 0, 0};
        }
        try {
            this.sql = "SELECT COUNT(*) AS jumlah, COALESCE(SUM(harga), 0) AS total, "
                    + "COALESCE(SUM(estimasi_menit), 0) AS durasi FROM layanan WHERE id IN (" + daftar + ")";
            this.res = this.stm.executeQuery(sql);
            res.next();
            return new double[]{res.getInt("jumlah"), res.getDouble("total"), res.getInt("durasi")};
        } catch (Exception e) {
            System.out.println("Query ringkas layanan gagal : " + e.getMessage());
            return null;
        }
    }
}