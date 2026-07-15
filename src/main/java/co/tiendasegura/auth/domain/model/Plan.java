package co.tiendasegura.auth.domain.model;

public enum Plan {
    BASICO(1),
    PRO(3);

    private final int maxUsuarios;

    Plan(int maxUsuarios) {
        this.maxUsuarios = maxUsuarios;
    }

    public int getMaxUsuarios() {
        return maxUsuarios;
    }
}