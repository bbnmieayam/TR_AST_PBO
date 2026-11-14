package Model;

import java.time.LocalDateTime;

public class Pembayaran {

    private int id;
    private int idBooking;
    private MetodePembayaran metode;
    private double jumlahBayar;
    private double kembalian;
    private LocalDateTime waktuBayar;

    public Pembayaran() {
    }

    public Pembayaran(int id, int idBooking, MetodePembayaran metode,
            double jumlahBayar, double kembalian, LocalDateTime waktuBayar) {
        this.id = id;
        this.idBooking = idBooking;
        this.metode = metode;
        this.jumlahBayar = jumlahBayar;
        this.kembalian = kembalian;
        this.waktuBayar = waktuBayar;
    }

    public static Pembayaran buat(int idBooking, double totalBiaya,
            MetodePembayaran metode, double uangDiberikan) {
        Pembayaran p = new Pembayaran();
        p.idBooking = idBooking;
        p.metode = metode;
        p.waktuBayar = LocalDateTime.now();

        if (metode == MetodePembayaran.CASH) {
            if (uangDiberikan < totalBiaya) {
                throw new IllegalArgumentException("Uang yang diberikan kurang dari total biaya.");
            }
            p.jumlahBayar = uangDiberikan;
            p.kembalian = uangDiberikan - totalBiaya;
        } else { 
            p.jumlahBayar = totalBiaya;
            p.kembalian = 0;
        }
        return p;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdBooking() {
        return idBooking;
    }

    public void setIdBooking(int idBooking) {
        this.idBooking = idBooking;
    }

    public MetodePembayaran getMetode() {
        return metode;
    }

    public void setMetode(MetodePembayaran metode) {
        this.metode = metode;
    }

    public double getJumlahBayar() {
        return jumlahBayar;
    }

    public void setJumlahBayar(double jumlahBayar) {
        this.jumlahBayar = jumlahBayar;
    }

    public double getKembalian() {
        return kembalian;
    }

    public void setKembalian(double kembalian) {
        this.kembalian = kembalian;
    }

    public LocalDateTime getWaktuBayar() {
        return waktuBayar;
    }

    public void setWaktuBayar(LocalDateTime waktuBayar) {
        this.waktuBayar = waktuBayar;
    }
}
