package Controller;

import Model.Pegawai;
import Utility.Koneksi;
import java.sql.Statement;
import java.sql.ResultSet;
import javax.swing.table.DefaultTableModel;

public class PegawaiController {

    public Statement stm;
    public ResultSet res;
    public String sql;

    // model tabel / tabel virtual
    DefaultTableModel dtm = new DefaultTableModel();

    // konstruktor
    public PegawaiController() {
        // objek koneksi
        Koneksi db = new Koneksi();
        db.config();
        this.stm = db.stm;
    }

    //  Method 1: Buat model/desain tabel
    public DefaultTableModel createTable() {
        this.dtm = new DefaultTableModel();
        this.dtm.addColumn("ID Pegawai");
        this.dtm.addColumn("Nama Pegawai");
        this.dtm.addColumn("Spesialisasi");
        return this.dtm;
    }

    //  Method 2: SELECT semua pegawai 
    public void tampilkanPegawai() {
        try {
            // bersihkan isi tabel virtual
            this.dtm.getDataVector().removeAllElements();
            this.dtm.fireTableDataChanged();

            // ambil semua data pegawai
            this.sql = "SELECT * FROM pegawai ORDER BY nama";
            this.res = this.stm.executeQuery(sql);

            // masukkan hasil query ke tabel virtual
            while (res.next()) {
                Object[] obj = new Object[3];
                obj[0] = res.getInt("id");
                obj[1] = res.getString("nama");
                obj[2] = res.getString("spesialisasi");
                this.dtm.addRow(obj);
            }
        } catch (Exception e) {
            System.out.println("Query tampil pegawai gagal : " + e.getMessage());
        }
    }

    // Method 3: INSERT pegawai
    public boolean tambahPegawai(String nama, String spesialisasi) {
        // Hubungkan dengan model Pegawai
        Pegawai pg = new Pegawai();
        pg.setNama_pegawai(nama);
        pg.setSpesialisasi(spesialisasi);

        try {
            this.sql = "INSERT INTO pegawai (nama, spesialisasi) VALUES ('"
                    + esc(pg.getNama_pegawai()) + "', '"
                    + esc(pg.getSpesialisasi()) + "')";
            this.stm.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            System.out.println("Query tambah pegawai gagal : " + e.getMessage());
            return false;
        }
    }

    //  Method 4: UPDATE pegawai 
    public boolean ubahPegawai(int id, String nama, String spesialisasi) {
        Pegawai pg = new Pegawai();
        pg.setId_pegawai(id);
        pg.setNama_pegawai(nama);
        pg.setSpesialisasi(spesialisasi);

        try {
            this.sql = "UPDATE pegawai SET nama = '" + esc(pg.getNama_pegawai())
                    + "', spesialisasi = '" + esc(pg.getSpesialisasi())
                    + "' WHERE id = " + pg.getId_pegawai();
            this.stm.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            System.out.println("Query ubah pegawai gagal : " + e.getMessage());
        }
        return false;
    }

    // Method 5: DELETE pegawai
    // Pegawai yang sudah punya booking akan ditolak MySQL (foreign key) -> return false
    public boolean hapusPegawai(int id) {
        Pegawai pg = new Pegawai();
        pg.setId_pegawai(id);

        try {
            this.sql = "DELETE FROM pegawai WHERE id = " + pg.getId_pegawai();
            this.stm.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            System.out.println("Query hapus pegawai gagal : " + e.getMessage());
        }
        return false;
    }

    // Method 6: SELECT satu pegawai (null jika tidak ada) 
    public Pegawai cariPegawai(int id) {
        try {
            this.sql = "SELECT * FROM pegawai WHERE id = " + id;
            this.res = this.stm.executeQuery(sql);
            if (res.next()) {
                Pegawai pg = new Pegawai();
                pg.setId_pegawai(res.getInt("id"));
                pg.setNama_pegawai(res.getString("nama"));
                pg.setSpesialisasi(res.getString("spesialisasi"));
                return pg;
            }
        } catch (Exception e) {
            System.out.println("Query cari pegawai gagal : " + e.getMessage());
        }
        return null;
    }

    // ' menjadi '' dan \ menjadi \\ supaya nama seperti D'Angelo tidak merusak query
    private String esc(String teks) {
        return teks == null ? "" : teks.replace("\\", "\\\\").replace("'", "''");
    }
}