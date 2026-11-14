package Model;

public enum StatusLayanan {
    MENUNGGU("Menunggu"),
    SEDANG_DILAYANI("Sedang Dilayani"),
    SELESAI("Selesai");

    private final String label;

    StatusLayanan(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static StatusLayanan fromString(String value) {
        if (value == null) {
            return MENUNGGU;
        }
        for (StatusLayanan s : values()) {
            if (s.name().equalsIgnoreCase(value) || s.label.equalsIgnoreCase(value)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Status tidak dikenal: " + value);
    }

    @Override
    public String toString() {
        return label;
    }
}
