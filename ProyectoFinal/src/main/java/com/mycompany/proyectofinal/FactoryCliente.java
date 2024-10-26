package com.mycompany.proyectofinal;

public class FactoryCliente {
    public static IRepositorioCliente getFabricaCliente(Integer tipoBaseDatos) {
        return switch (tipoBaseDatos) {
            case 1 -> new RepositorioJSONCliente();
            case 2 -> new RepositorioSQLCliente();
            default -> null;
        };
    }
}
