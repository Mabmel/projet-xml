package cv24.cv24.entities;

public enum Niveaux {
    A1("A1"),
    A2("A2"),
    B1("B1"),
    B2("B2"),
    C1("C1"),
    C2("C2");
    private final String label;

    Niveaux(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
