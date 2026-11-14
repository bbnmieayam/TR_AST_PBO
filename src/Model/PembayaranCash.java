package Model;

public class PembayaranCash extends Pembayaran {

    public PembayaranCash(int id_booking, double total_biaya, double uang_diterima) {
        super(id_booking, total_biaya);
        this.jumlah_bayar = uang_diterima;
    }

    @Override
    public String getMetode() {
        return "CASH";
    }

    @Override
    public double hitungKembalian() {
        return this.jumlah_bayar - this.total_biaya;
    }
}