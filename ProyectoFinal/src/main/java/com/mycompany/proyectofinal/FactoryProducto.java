package com.mycompany.proyectofinal;

public class FactoryProducto {
    public static IRepositorioProducto getFabricaProducto(Integer tipoBaseDatos) {
        return switch (tipoBaseDatos) {
            case 1 -> new RepositorioJSONProducto();
            case 2 -> new RepositorioSQLProducto();
            default -> null;
        };
    }
}
