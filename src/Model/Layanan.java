package Model;

public class Layanan {
    private int id;
    private String nama;
    private double harga;
    private int estimasiMenit;   

    public Layanan() {
    }

    public Layanan(String nama, double harga, int estimasiMenit) {
        this.nama = nama;
        this.harga = harga;
        this.estimasiMenit = estimasiMenit;
    }

    public Layanan(int id, String nama, double harga, int estimasiMenit) {
        this(nama, harga, estimasiMenit);
        this.id = id;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public double getHarga() { return harga; }
    public void setHarga(double harga) { this.harga = harga; }

    public int getEstimasiMenit() { return estimasiMenit; }
    public void setEstimasiMenit(int estimasiMenit) { this.estimasiMenit = estimasiMenit; }

    @Override
    public String toString() {
        return nama;
    }
}