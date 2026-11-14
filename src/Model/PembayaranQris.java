package Model;

public class PembayaranQris extends Pembayaran {

    public PembayaranQris(int id_booking, double total_biaya) {
        super(id_booking, total_biaya);
        this.jumlah_bayar = total_biaya; // QRIS dibayar pas
    }

    @Override
    public String getMetode() {
        return "QRIS";
    }

    @Override
    public double hitungKembalian() {
        return 0; // QRIS tidak menghitung kembalian
    }
}