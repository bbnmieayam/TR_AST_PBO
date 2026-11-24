package Model;

import java.sql.Timestamp;

public class Booking {

    private int id_booking;
    private int id_pelanggan;
    private int id_pegawai;
    private Timestamp waktu_mulai;
    private Timestamp waktu_selesai;
    private String status;
    private double total_biaya;

    public int getId_booking() {
        return id_booking;
    }

    public void setId_booking(int id_booking) {
        this.id_booking = id_booking;
    }

    public int getId_pelanggan() {
        return id_pelanggan;
    }

    public void setId_pelanggan(int id_pelanggan) {
        this.id_pelanggan = id_pelanggan;
    }

    public int getId_pegawai() {
        return id_pegawai;
    }

    public void setId_pegawai(int id_pegawai) {
        this.id_pegawai = id_pegawai;
    }

    public Timestamp getWaktu_mulai() {
        return waktu_mulai;
    }

    public void setWaktu_mulai(Timestamp waktu_mulai) {
        this.waktu_mulai = waktu_mulai;
    }

    public Timestamp getWaktu_selesai() {
        return waktu_selesai;
    }

    public void setWaktu_selesai(Timestamp waktu_selesai) {
        this.waktu_selesai = waktu_selesai;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getTotal_biaya() {
        return total_biaya;
    }

    public void setTotal_biaya(double total_biaya) {
        this.total_biaya = total_biaya;
    }
}