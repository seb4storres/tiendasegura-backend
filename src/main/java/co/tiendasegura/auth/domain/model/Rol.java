package co.tiendasegura.auth.domain.model;

public enum Rol {
    ADMIN,
    CAJERO;

    public boolean esAdmin() {
        return this == ADMIN;
    }
}