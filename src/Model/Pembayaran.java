package Model;

// Induk semua metode pembayaran. Perbedaan CASH dan QRIS ada di hitungKembalian().
public abstract class Pembayaran {

    protected int id_pembayaran;
    protected int id_booking;
    protected double total_biaya;
    protected double jumlah_bayar;

    public Pembayaran(int id_booking, double total_biaya) {
        this.id_booking = id_booking;
        this.total_biaya = total_biaya;
    }

    // "CASH" atau "QRIS" (sesuai ENUM di tabel pembayaran)
    public abstract String getMetode();

    // CASH: uang diterima - total. QRIS: selalu 0.
    public abstract double hitungKembalian();

    public int getId_pembayaran() {
        return id_pembayaran;
    }

    public void setId_pembayaran(int id_pembayaran) {
        this.id_pembayaran = id_pembayaran;
    }

    public int getId_booking() {
        return id_booking;
    }

    public double getTotal_biaya() {
        return total_biaya;
    }

    public double getJumlah_bayar() {
        return jumlah_bayar;
    }
}