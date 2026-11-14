package Model;

public class BookingLayanan {

    private int idBooking;
    private int idLayanan;
    private String namaLayanan;
    private double hargaSaatBooking;

    public BookingLayanan() {
    }

    public BookingLayanan(int idBooking, int idLayanan, String namaLayanan, double hargaSaatBooking) {
        this.idBooking = idBooking;
        this.idLayanan = idLayanan;
        this.namaLayanan = namaLayanan;
        this.hargaSaatBooking = hargaSaatBooking;
    }

    public int getIdBooking() {
        return idBooking;
    }

    public void setIdBooking(int idBooking) {
        this.idBooking = idBooking;
    }

    public int getIdLayanan() {
        return idLayanan;
    }

    public void setIdLayanan(int idLayanan) {
        this.idLayanan = idLayanan;
    }

    public String getNamaLayanan() {
        return namaLayanan;
    }

    public void setNamaLayanan(String namaLayanan) {
        this.namaLayanan = namaLayanan;
    }

    public double getHargaSaatBooking() {
        return hargaSaatBooking;
    }

    public void setHargaSaatBooking(double hargaSaatBooking) {
        this.hargaSaatBooking = hargaSaatBooking;
    }
}
