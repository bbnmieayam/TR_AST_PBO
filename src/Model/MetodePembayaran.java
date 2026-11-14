package Model;

public enum MetodePembayaran {
    CASH,
    QRIS;

    public static MetodePembayaran fromString(String value) {
        return MetodePembayaran.valueOf(value.trim().toUpperCase());
    }
}