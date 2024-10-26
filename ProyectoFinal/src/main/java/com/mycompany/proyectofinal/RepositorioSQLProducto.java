package com.mycompany.proyectofinal;

import java.io.IOException;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class RepositorioSQLProducto implements IRepositorioProducto {
    private List<Producto> productos = new ArrayList<>();

    @Override
    public void cargarProductos(JTable jtable) throws IOException, SQLException {
        var con = Mysql.getConnection();
        var statement = con.prepareStatement("SELECT * FROM producto");
        var result = statement.executeQuery();     
        
        String[] columnNames = new String[]{"ID", "Nombre", "Stock", "Estado"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        productos = new ArrayList<>();

        while (result.next()) {
            Producto producto = new Producto();
            producto.setIdProducto(result.getInt("productoId"));
            producto.setCantidad(result.getInt("stock"));
            producto.setEstado(result.getString("estado"));
            producto.setNombre(result.getString("nombre"));
           
            Object[] rowData = {producto.getIdProducto(), producto.getNombre(), producto.getCantidad(), producto.getEstado()};
            model.addRow(rowData);
            
            productos.add(producto);
        }
        jtable.setModel(model);
    }

    @Override
    public void guardarProductos() throws IOException {
        System.out.println("Guardando productos en MySQL");
    }

    @Override
    public void agregarProducto(Producto producto) throws SQLException, IOException {
        var con = Mysql.getConnection();
        var statement = con.prepareStatement("INSERT INTO producto (nombre, stock, estado) VALUES (?, ?, ?)");
        statement.setString(1, producto.getNombre());
        statement.setInt(2, producto.getCantidad());
        statement.setString(3, producto.getEstado());
        statement.executeUpdate();
        productos.add(producto);
    }

    @Override
    public Producto despacharProducto(int idProducto, int cantidadRequerida) throws SQLException, IOException {
        var producto = this.getProducto(idProducto);
        var cantidad = producto.getCantidad();

        if (cantidad < cantidadRequerida) {
            throw new IllegalArgumentException("No hay suficiente cantidad de producto id: " + idProducto + " nombre: " + producto.getNombre());
        }

        producto.setCantidad(cantidad - cantidadRequerida);

        if (producto.getCantidad() == 0) {
            producto.setEstado("Agotado");
        }
        var con = Mysql.getConnection();
        var statement = con.prepareStatement("UPDATE producto SET stock = ?, estado = ? WHERE productoId = ?");
        statement.setInt(1, producto.getCantidad());
        statement.setString(2, producto.getEstado());
        statement.setInt(3, idProducto);
        statement.executeUpdate();

        return new Producto(producto.getIdProducto(), producto.getNombre(), cantidadRequerida, producto.getEstado());
    }

    @Override
    public List<Producto> getProductos() {
        return productos;
    }

    @Override
    public Producto getProducto(int idProducto) throws SQLException, IOException {
        var con = Mysql.getConnection();
        var statement = con.prepareStatement("SELECT * FROM producto where productoId = ?");
        statement.setInt(1, idProducto);
        var result = statement.executeQuery();
        var producto = new Producto();
        if (!result.next()) {
            throw new RuntimeException("Producto no encontrado");
        }
        producto.setIdProducto(result.getInt("productoId"));
        producto.setCantidad(result.getInt("stock"));
        producto.setEstado(result.getString("estado"));
        producto.setNombre(result.getString("nombre"));
        return producto;
    }
}
