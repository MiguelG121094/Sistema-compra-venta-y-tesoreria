/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Miguel
 */
public class ClienteDAO {

    private Connection conn;
    private PersonaDAO personaDAO;
    private TipoEntidadDAO tipoEntidadDAO;
    private static final Logger LOGGER = Logger.getLogger(ClienteDAO.class.getName());

    public ClienteDAO(Connection conn) {
        this.conn = conn;
    }

    public Cliente getCliente(Long idCliente) throws SQLException {
        if (idCliente == null) {
            LOGGER.log(Level.WARNING, "Error: idCliente es nulo");
            return null;
        }
        Cliente cliente = null;
        String sql = "SELECT id_cliente, cli_razon_social, id_persona, id_tipo_entidad, cli_ruc, " +
                    "cli_nombre_comercial, cli_direccion, cli_telefono FROM cliente WHERE id_cliente = ?";

        personaDAO = new PersonaDAO(conn);
        tipoEntidadDAO = new TipoEntidadDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idCliente);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    cliente = new Cliente(
                        rs.getLong("id_cliente"),
                        rs.getString("cli_razon_social"),
                        personaDAO.getPersona(rs.getLong("id_persona")),
                        tipoEntidadDAO.getTipoEntidad(rs.getLong("id_tipo_entidad")),
                        rs.getString("cli_ruc"),
                        rs.getString("cli_nombre_comercial"),
                        rs.getString("cli_direccion"),
                        rs.getString("cli_telefono")
                    );
                }
            }
        }
        return cliente;
    }

    public List<Cliente> listarClientes() throws SQLException {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT id_cliente, cli_razon_social, id_persona, id_tipo_entidad, cli_ruc, " +
                    "cli_nombre_comercial, cli_direccion, cli_telefono FROM cliente";

        personaDAO = new PersonaDAO(conn);
        tipoEntidadDAO = new TipoEntidadDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Cliente cliente = new Cliente(
                    rs.getLong("id_cliente"),
                    rs.getString("cli_razon_social"),
                    personaDAO.getPersona(rs.getLong("id_persona")),
                    tipoEntidadDAO.getTipoEntidad(rs.getLong("id_tipo_entidad")),
                    rs.getString("cli_ruc"),
                    rs.getString("cli_nombre_comercial"),
                    rs.getString("cli_direccion"),
                    rs.getString("cli_telefono")
                );
                clientes.add(cliente);
            }
        }
        return clientes;
    }

    public Long insertarCliente(Cliente cliente) throws SQLException {
        if (cliente == null) {
            LOGGER.log(Level.SEVERE, "Error: El cliente es nulo");
            return null;
        }

        String sql = "INSERT INTO cliente (cli_razon_social, id_persona, id_tipo_entidad, cli_ruc, " +
                    "cli_nombre_comercial, cli_direccion, cli_telefono) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, cliente.getRazonSocial());
            stmt.setLong(2, cliente.getPersona().getIdPersona());
            stmt.setLong(3, cliente.getTipoEntidad().getIdTipoEntidad());
            stmt.setString(4, cliente.getRuc());
            stmt.setString(5, cliente.getNombreComercial());
            stmt.setString(6, cliente.getDireccion());
            stmt.setString(7, cliente.getTelefono());

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas == 0) {
                throw new SQLException("No se insertó el cliente, ninguna fila afectada");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long idGenerado = generatedKeys.getLong(1);
                    cliente.setIdCliente(idGenerado);
                    return idGenerado;
                } else {
                    throw new SQLException("Error: No se generó ningún ID para el cliente.");
                }
            }
        }
    }

    public void actualizarCliente(Cliente cliente) throws SQLException {
        if (cliente == null || cliente.getIdCliente() == null) {
            LOGGER.log(Level.WARNING, "Error: cliente es nulo");
            return;
        }

        String sql = "UPDATE cliente SET cli_razon_social = ?, id_persona = ?, id_tipo_entidad = ?, " +
                    "cli_ruc = ?, cli_nombre_comercial = ?, cli_direccion = ?, cli_telefono = ? " +
                    "WHERE id_cliente = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cliente.getRazonSocial());
            stmt.setLong(2, cliente.getPersona().getIdPersona());
            stmt.setLong(3, cliente.getTipoEntidad().getIdTipoEntidad());
            stmt.setString(4, cliente.getRuc());
            stmt.setString(5, cliente.getNombreComercial());
            stmt.setString(6, cliente.getDireccion());
            stmt.setString(7, cliente.getTelefono());
            stmt.setLong(8, cliente.getIdCliente());

            stmt.executeUpdate();
        }
    }

    public void eliminarCliente(Long idCliente) throws SQLException {
        if (idCliente == null) {
            LOGGER.log(Level.WARNING, "Error: idCliente es nulo");
            return;
        }

        String sql = "DELETE FROM cliente WHERE id_cliente = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idCliente);
            stmt.executeUpdate();
        }
    }
}
