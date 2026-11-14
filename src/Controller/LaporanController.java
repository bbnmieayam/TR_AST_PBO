package Controller;

import Model.LaporanPendapatan;
import Utility.Koneksi;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public class LaporanController {

    public Connection cn;
    public Statement stm;
    public ResultSet res;
    public String sql;
    public String pesan = "";

    // Ringkasan periode terakhir yang ditampilkan
    public int totalTransaksi;
    public double totalPendapatan;

    DefaultTableModel dtm = new DefaultTableModel();

    public LaporanController() {
        Koneksi db = new Koneksi();
        db.config();
        this.cn = db.cn;
        this.stm = db.stm;
    }

    public DefaultTableModel createTable() {
        if (this.dtm.getColumnCount() == 0) {
            this.dtm.addColumn("Tanggal");
            this.dtm.addColumn("Jumlah Transaksi");
            this.dtm.addColumn("Total Pendapatan");
        }
        return this.dtm;
    }

    /** Pendapatan = jumlah_bayar - kembalian (uang yang benar-benar diterima). */
    public List<LaporanPendapatan> ambilLaporan(LocalDate dari, LocalDate sampai) {
        List<LaporanPendapatan> hasil = new ArrayList<>();

        if (dari == null || sampai == null) {
            this.pesan = "Tanggal awal dan akhir harus diisi.";
            return hasil;
        }
        if (sampai.isBefore(dari)) {
            this.pesan = "Tanggal akhir tidak boleh sebelum tanggal awal.";
            return hasil;
        }

        try {
            this.sql = "SELECT DATE(waktu_bayar) AS tanggal, COUNT(*) AS jumlah, "
                    + "SUM(jumlah_bayar - kembalian) AS total "
                    + "FROM pembayaran "
                    + "WHERE DATE(waktu_bayar) BETWEEN ? AND ? "
                    + "GROUP BY DATE(waktu_bayar) ORDER BY tanggal";
            try (PreparedStatement ps = this.cn.prepareStatement(this.sql)) {
                ps.setDate(1, Date.valueOf(dari));
                ps.setDate(2, Date.valueOf(sampai));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        hasil.add(new LaporanPendapatan(
                                rs.getDate("tanggal").toLocalDate(),
                                rs.getInt("jumlah"),
                                rs.getDouble("total")));
                    }
                }
            }
        } catch (Exception e) {
            this.pesan = "Gagal mengambil laporan.";
            System.out.println("ambilLaporan failed: " + e.getMessage());
        }
        return hasil;
    }

    public void tampilkanLaporan(LocalDate dari, LocalDate sampai) {
        this.dtm.setRowCount(0);
        this.totalTransaksi = 0;
        this.totalPendapatan = 0;

        for (LaporanPendapatan l : ambilLaporan(dari, sampai)) {
            this.dtm.addRow(new Object[]{
                l.getTanggal().toString(), l.getJumlahTransaksi(), l.getTotalPendapatan()
            });
            this.totalTransaksi += l.getJumlahTransaksi();
            this.totalPendapatan += l.getTotalPendapatan();
        }
    }
}