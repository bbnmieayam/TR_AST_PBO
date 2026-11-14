package Model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Booking {

    private int id;
    private int idPelanggan;
    private int idPegawai;
    private LocalDateTime waktuMulai;
    private LocalDateTime waktuSelesai;
    private StatusLayanan status = StatusLayanan.MENUNGGU;
    private double totalBiaya;

    private String namaPelanggan;
    private String namaPegawai;
    private String ringkasanLayanan;
    private List<String> daftarLayanan = new ArrayList<>();

    public Booking() {
    }

    public Booking(int idPelanggan, int idPegawai, LocalDateTime waktuMulai) {
        this.idPelanggan = idPelanggan;
        this.idPegawai = idPegawai;
        this.waktuMulai = waktuMulai;
        this.status = StatusLayanan.MENUNGGU;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdPelanggan() {
        return idPelanggan;
    }

    public void setIdPelanggan(int idPelanggan) {
        this.idPelanggan = idPelanggan;
    }

    public int getIdPegawai() {
        return idPegawai;
    }

    public void setIdPegawai(int idPegawai) {
        this.idPegawai = idPegawai;
    }

    public LocalDateTime getWaktuMulai() {
        return waktuMulai;
    }

    public void setWaktuMulai(LocalDateTime waktuMulai) {
        this.waktuMulai = waktuMulai;
    }

    public LocalDateTime getWaktuSelesai() {
        return waktuSelesai;
    }

    public void setWaktuSelesai(LocalDateTime waktuSelesai) {
        this.waktuSelesai = waktuSelesai;
    }

    public StatusLayanan getStatus() {
        return status;
    }

    public void setStatus(StatusLayanan status) {
        this.status = status;
    }

    public double getTotalBiaya() {
        return totalBiaya;
    }

    public void setTotalBiaya(double totalBiaya) {
        this.totalBiaya = totalBiaya;
    }

    public String getNamaPelanggan() {
        return namaPelanggan;
    }

    public void setNamaPelanggan(String namaPelanggan) {
        this.namaPelanggan = namaPelanggan;
    }

    public String getNamaPegawai() {
        return namaPegawai;
    }

    public void setNamaPegawai(String namaPegawai) {
        this.namaPegawai = namaPegawai;
    }

    public String getRingkasanLayanan() {
        return ringkasanLayanan;
    }

    public void setRingkasanLayanan(String ringkasanLayanan) {
        this.ringkasanLayanan = ringkasanLayanan;
    }

    public List<String> getDaftarLayanan() {
        return daftarLayanan;
    }

    public void setDaftarLayanan(List<String> daftarLayanan) {
        this.daftarLayanan = daftarLayanan;
    }
}