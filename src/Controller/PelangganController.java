package Controller;

import Model.Pelanggan;
import Utility.Koneksi;
import java.sql.Statement;
import java.sql.ResultSet;
import javax.swing.table.DefaultTableModel;

public class PelangganController {

    public Statement stm;
    public ResultSet res;
    public String sql;

    // model tabel / tabel virtual
    DefaultTableModel dtm = new DefaultTableModel();

    // konstruktor
    public PelangganController() {
        // objek koneksi
        Koneksi db = new Koneksi();
        db.config();
        this.stm = db.stm;
    }

    // Method 1: Buat model/desain tabel
    public DefaultTableModel createTable() {
        this.dtm = new DefaultTableModel();
        this.dtm.addColumn("ID Pelanggan");
        this.dtm.addColumn("Nama Pelanggan");
        this.dtm.addColumn("No. HP");
        return this.dtm;
    }

    // Method 2: SELECT semua pelanggan (isi JTable)
    public void tampilkanPelanggan() {
        try {
            // bersihkan isi tabel virtual
            this.dtm.getDataVector().removeAllElements();
            this.dtm.fireTableDataChanged();

            // ambil semua data pelanggan
            this.sql = "SELECT * FROM pelanggan ORDER BY nama";
            this.res = this.stm.executeQuery(sql);

            // masukkan hasil query ke tabel virtual
            while (res.next()) {
                Object[] obj = new Object[3];
                obj[0] = res.getInt("id");
                obj[1] = res.getString("nama");
                obj[2] = res.getString("no_hp");
                this.dtm.addRow(obj);
            }
        } catch (Exception e) {
            System.out.println("Query tampil pelanggan gagal : " + e.getMessage());
        }
    }

    // Method 3: INSERT pelanggan
    public boolean tambahPelanggan(String nama, String noHp) {
        // Hubungkan dengan model Pelanggan
        Pelanggan pl = new Pelanggan();
        pl.setNama_pelanggan(nama);
        pl.setNo_hp(noHp);

        try {
            this.sql = "INSERT INTO pelanggan (nama, no_hp) VALUES ('"
                    + esc(pl.getNama_pelanggan()) + "', '"
                    + esc(pl.getNo_hp()) + "')";
            this.stm.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            System.out.println("Query tambah pelanggan gagal : " + e.getMessage());
            return false;
        }
    }

    //  Method 4: UPDATE pelanggan
    public boolean ubahPelanggan(int id, String nama, String noHp) {
        Pelanggan pl = new Pelanggan();
        pl.setId_pelanggan(id);
        pl.setNama_pelanggan(nama);
        pl.setNo_hp(noHp);

        try {
            this.sql = "UPDATE pelanggan SET nama = '" + esc(pl.getNama_pelanggan())
                    + "', no_hp = '" + esc(pl.getNo_hp())
                    + "' WHERE id = " + pl.getId_pelanggan();
            this.stm.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            System.out.println("Query ubah pelanggan gagal : " + e.getMessage());
        }
        return false;
    }

    //  Method 5: DELETE pelanggan
    public boolean hapusPelanggan(int id) {
        Pelanggan pl = new Pelanggan();
        pl.setId_pelanggan(id);

        try {
            this.sql = "DELETE FROM pelanggan WHERE id = " + pl.getId_pelanggan();
            this.stm.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            System.out.println("Query hapus pelanggan gagal : " + e.getMessage());
        }
        return false;
    }

    // Method 6: SELECT satu pelanggan (null jika tidak ada)
    public Pelanggan cariPelanggan(int id) {
        try {
            this.sql = "SELECT * FROM pelanggan WHERE id = " + id;
            this.res = this.stm.executeQuery(sql);
            if (res.next()) {
                Pelanggan pl = new Pelanggan();
                pl.setId_pelanggan(res.getInt("id"));
                pl.setNama_pelanggan(res.getString("nama"));
                pl.setNo_hp(res.getString("no_hp"));
                return pl;
            }
        } catch (Exception e) {
            System.out.println("Query cari pelanggan gagal : " + e.getMessage());
        }
        return null;
    }

    // ' menjadi '' dan \ menjadi \\ supaya nama seperti D'Angelo tidak merusak query
    private String esc(String teks) {
        return teks == null ? "" : teks.replace("\\", "\\\\").replace("'", "''");
    }
}