package com.mycompany.proyectofinal;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class RepositorioSQLCliente implements IRepositorioCliente {
    private static List<Cliente> clientes = new ArrayList<>();
    @Override
    public void cargarClientes(JTable jtable) throws IOException, SQLException {
        var con = Mysql.getConnection();
        var statement = con.prepareStatement("SELECT * FROM cliente");
        var result = statement.executeQuery();

        clientes = new ArrayList<>();
     
        String[] columnNames = new String[]{"Nit", "Nombre", "Dirección", "Teléfono"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        while (result.next()) {
            var cliente = new Cliente();
            cliente.setNit(result.getString("nitCliente"));
            cliente.setTelefono(result.getString("celular"));
            cliente.setNombre(result.getString("nombreCliente"));
            cliente.setDireccion(result.getString("direccion"));
            Object[] rowData = {cliente.getNit(), cliente.getNombre(), cliente.getDireccion(), cliente.getTelefono()};
            model.addRow(rowData);

            var statementPedido = con.prepareStatement("SELECT pedido.* FROM pedido INNER JOIN cliente ON cliente.nitCliente = pedido.nitCliente where pedido.nitCliente = ?");
            statementPedido.setString(1, cliente.getNit());
            var resultPedido = statementPedido.executeQuery();
            var pedidos = new ArrayList<Pedido>();
            while (resultPedido.next()) {
                var pedido = new Pedido();
                pedido.setFecha(resultPedido.getDate("fechaOrden"));
                pedido.setIdPedido(resultPedido.getInt("pedidoId"));
                pedido.modificarEstado(resultPedido.getString("estado"));

                var stmtProducto = con.prepareStatement("SELECT producto.* FROM producto INNER JOIN pedido_productos ON pedido_productos.productoId = producto.productoId where pedido_productos.pedidoId = ?");
                stmtProducto.setInt(1, pedido.getIdPedido());
                var resultProducto = stmtProducto.executeQuery();

                var productos = new ArrayList<Producto>();
                while (resultProducto.next()) {
                    Producto producto = new Producto();
                    producto.setEstado(resultProducto.getString("estado"));
                    producto.setNombre(resultProducto.getString("nombre"));
                    producto.setIdProducto(resultProducto.getInt("productoId"));
                    productos.add(producto);
                }
                pedido.setProductos(productos);

                pedidos.add(pedido);
            }

            cliente.setPedidos(pedidos);
            clientes.add(cliente);
        }
        jtable.setModel(model);
    }

    @Override
    public void guardarClientes() throws IOException {
        System.out.println("Guardando clientes en MySQL");
    }

    public List<Cliente> getClientes() {
        return clientes;
    }

    @Override
    public void agregarCliente(Cliente cliente, Integer productoId) throws SQLException, IOException {
        var con = Mysql.getConnection();
        PreparedStatement statement;

        statement = con.prepareStatement("SELECT * FROM cliente where nitCliente = ?");
        statement.setString(1, cliente.getNit());
        var result = statement.executeQuery();
        if (result.next()) {
            statement = con.prepareStatement("UPDATE cliente SET nombreCliente = ?, direccion = ?, celular = ? where nitCliente = ?");
            statement.setString(1, cliente.getNombre());
            statement.setString(2, cliente.getDireccion());
            statement.setString(3, cliente.getTelefono());
            statement.setString(4, cliente.getNit());
            statement.executeUpdate();
            this.createPedido(cliente, productoId);
            return;
        }

        statement = con.prepareStatement("INSERT INTO cliente (nitCliente, nombreCliente, direccion, celular) VALUES (?, ?, ?, ?)");

        statement.setString(1, cliente.getNit());
        statement.setString(2, cliente.getNombre());
        statement.setString(3, cliente.getDireccion());
        statement.setString(4, cliente.getTelefono());
        statement.executeUpdate();
        this.createPedido(cliente, productoId);
    }

    private void createPedido(Cliente cliente, Integer productoId) throws SQLException, IOException {
        var con = Mysql.getConnection();
        var statement = con.prepareStatement("INSERT INTO pedido (nitCliente, fechaOrden, estado) VALUES (?, ?, 'GENERADO')", PreparedStatement.RETURN_GENERATED_KEYS);
        statement.setString(1, cliente.getNit());
        statement.setDate(2, new Date(new java.util.Date().getTime()));
        statement.executeUpdate();

        var affectedRows = statement.executeUpdate();

        if (affectedRows == 0) {
            throw new SQLException("Error creando pedido.");
        }

        ResultSet generatedKeys = statement.getGeneratedKeys();
        if (generatedKeys.next()) {
            var pedidoId = generatedKeys.getInt(1);
            statement = con.prepareStatement("INSERT INTO pedido_productos (pedidoId, productoId) VALUES (?, ?)");
            statement.setInt(1, pedidoId);
            statement.setInt(2, productoId);
            statement.executeUpdate();
        }
    }
}
