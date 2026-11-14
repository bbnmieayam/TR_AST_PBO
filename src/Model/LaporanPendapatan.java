package Model;

import java.time.LocalDate;

/** Satu baris laporan pendapatan (per hari). */
public class LaporanPendapatan {
    private LocalDate tanggal;
    private int jumlahTransaksi;
    private double totalPendapatan;

    public LaporanPendapatan(LocalDate tanggal, int jumlahTransaksi, double totalPendapatan) {
        this.tanggal = tanggal;
        this.jumlahTransaksi = jumlahTransaksi;
        this.totalPendapatan = totalPendapatan;
    }

    public LocalDate getTanggal() { return tanggal; }
    public int getJumlahTransaksi() { return jumlahTransaksi; }
    public double getTotalPendapatan() { return totalPendapatan; }
}