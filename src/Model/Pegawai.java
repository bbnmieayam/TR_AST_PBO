package Model;

public class Pegawai {
    private int id;
    private String nama;
    private String spesialisasi;

    public Pegawai() {
    }

    public Pegawai(String nama, String spesialisasi) {
        this.nama = nama;
        this.spesialisasi = spesialisasi;
    }

    public Pegawai(int id, String nama, String spesialisasi) {
        this(nama, spesialisasi);
        this.id = id;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getSpesialisasi() { return spesialisasi; }
    public void setSpesialisasi(String spesialisasi) { this.spesialisasi = spesialisasi; }

    @Override
    public String toString() {
        return nama;
    }
}
