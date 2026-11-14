package Model;

public class Pelanggan {
    private int id;
    private String nama;
    private String noHp;

    public Pelanggan() {
    }

    public Pelanggan(String nama, String noHp) {
        this.nama = nama;
        this.noHp = noHp;
    }

    public Pelanggan(int id, String nama, String noHp) {
        this(nama, noHp);
        this.id = id;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getNoHp() { return noHp; }
    public void setNoHp(String noHp) { this.noHp = noHp; }

    @Override
    public String toString() {
        return nama;
    }
}