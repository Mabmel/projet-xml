package cv24.cv24.entities;

public enum TypeContart {
    stage("stage"),
    emploi("emploi");

    private final String label;

    TypeContart(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
