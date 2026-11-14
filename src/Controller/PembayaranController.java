package Controller;

import Model.Pembayaran;
import Model.PembayaranCash;
import Model.PembayaranQris;
import Utility.Koneksi;
import java.sql.Statement;
import java.sql.ResultSet;
import javax.swing.table.DefaultTableModel;

public class PembayaranController {

    public Statement stm;
    public ResultSet res;
    public String sql;

    // alasan gagal (untuk JOptionPane) dan kembalian pembayaran terakhir (0 untuk QRIS)
    public String pesan = "";
    public double kembalianTerakhir = 0;

    // model tabel / tabel virtual
    DefaultTableModel dtm = new DefaultTableModel();

    // konstruktor
    public PembayaranController() {
        // objek koneksi
        Koneksi db = new Koneksi();
        db.config();
        this.stm = db.stm;
    }

    //  Method 1: Buat model/desain tabel 
    public DefaultTableModel createTable() {
        this.dtm = new DefaultTableModel();
        this.dtm.addColumn("ID Pembayaran");
        this.dtm.addColumn("ID Booking");
        this.dtm.addColumn("Metode");
        this.dtm.addColumn("Total Biaya");
        this.dtm.addColumn("Dibayar");
        this.dtm.addColumn("Kembalian");
        this.dtm.addColumn("Waktu Bayar");
        return this.dtm;
    }

    // Method 2: SELECT semua pembayaran (isi JTable)
    public void tampilkanPembayaran() {
        try {
            // bersihkan isi tabel virtual
            this.dtm.getDataVector().removeAllElements();
            this.dtm.fireTableDataChanged();

            // ambil semua data pembayaran beserta total biaya booking
            this.sql = "SELECT p.id, p.id_booking, p.metode, b.total_biaya, "
                    + "p.jumlah_bayar, p.kembalian, p.waktu_bayar "
                    + "FROM pembayaran p JOIN booking b ON b.id = p.id_booking "
                    + "ORDER BY p.waktu_bayar DESC";
            this.res = this.stm.executeQuery(sql);

            // masukkan hasil query ke tabel virtual
            while (res.next()) {
                Object[] obj = new Object[7];
                obj[0] = res.getInt("id");
                obj[1] = res.getInt("id_booking");
                obj[2] = res.getString("metode");
                obj[3] = res.getDouble("total_biaya");
                obj[4] = res.getDouble("jumlah_bayar");
                obj[5] = res.getDouble("kembalian");
                obj[6] = res.getTimestamp("waktu_bayar");
                this.dtm.addRow(obj);
            }
        } catch (Exception e) {
            System.out.println("Query tampil pembayaran gagal : " + e.getMessage());
        }
    }

    // Method 3: Bayar CASH (menghitung kembalian)
    public boolean bayarCash(int idBooking, double uangDiterima) {
        double total = ambilTotal(idBooking);
        if (total < 0) {
            return false;
        }
        if (uangDiterima < total) {
            this.pesan = "Uang yang diterima kurang dari total biaya.";
            return false;
        }
        return simpan(new PembayaranCash(idBooking, total, uangDiterima));
    }

    // Method 4: Bayar QRIS (tanpa kembalian) 
    public boolean bayarQris(int idBooking) {
        double total = ambilTotal(idBooking);
        if (total < 0) {
            return false;
        }
        return simpan(new PembayaranQris(idBooking, total));
    }

    // Return total biaya, atau -1 jika booking tidak boleh dibayar (alasannya di this.pesan)
    private double ambilTotal(int idBooking) {
        try {
            this.sql = "SELECT total_biaya, status FROM booking WHERE id = " + idBooking;
            this.res = this.stm.executeQuery(sql);
            if (!res.next()) {
                this.pesan = "Booking tidak ditemukan.";
                return -1;
            }
            double total = res.getDouble("total_biaya");
            if (!"SELESAI".equals(res.getString("status"))) { // hapus if ini jika boleh bayar sebelum selesai
                this.pesan = "Booking belum selesai dilayani.";
                return -1;
            }

            this.sql = "SELECT COUNT(*) FROM pembayaran WHERE id_booking = " + idBooking;
            this.res = this.stm.executeQuery(sql);
            if (res.next() && res.getInt(1) > 0) {
                this.pesan = "Booking ini sudah dibayar.";
                return -1;
            }
            return total;
        } catch (Exception e) {
            System.out.println("Query cek booking gagal : " + e.getMessage());
            this.pesan = "Gagal memeriksa data booking.";
            return -1;
        }
    }

    // Simpan pembayaran CASH maupun QRIS lewat satu method
    private boolean simpan(Pembayaran pb) {
        try {
            double kembalian = pb.hitungKembalian();
            this.sql = "INSERT INTO pembayaran (id_booking, metode, jumlah_bayar, kembalian) VALUES ("
                    + pb.getId_booking() + ", '"
                    + pb.getMetode() + "', "
                    + pb.getJumlah_bayar() + ", "
                    + kembalian + ")";
            this.stm.executeUpdate(sql);
            this.kembalianTerakhir = kembalian;
            return true;
        } catch (Exception e) {
            System.out.println("Query simpan pembayaran gagal : " + e.getMessage());
            this.pesan = "Gagal menyimpan pembayaran.";
            return false;
        }
    }
}