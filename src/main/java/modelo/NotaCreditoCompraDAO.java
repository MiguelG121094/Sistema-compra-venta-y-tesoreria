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
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Miguel
 */
public class NotaCreditoCompraDAO {

    private Connection conn;
    private UsuarioDAO usuarioDAO;
    private ProveedorDAO proveedorDAO;
    private FacturaCompraDAO facturaCompraDAO;
    private static final Logger LOGGER = Logger.getLogger(NotaCreditoCompraDAO.class.getName());

    public NotaCreditoCompraDAO(Connection conn) {
        this.conn = conn;
    }

    public NotaCreditoCompra getNotaCreditoCompra(Long idNotaCreditoCompra) throws SQLException {
        if (idNotaCreditoCompra == null) {
            LOGGER.log(Level.WARNING, "Error: el parámetro idNotaCreditoCompra es nulo");
            return null;
        }
        NotaCreditoCompra notaCredito = null;
        String sql = "SELECT id_nota_cred_comp_cab, nota_cred_comp_numero, nota_cred_comp_timbrado, " +
                    "nota_cred_comp_fecha_venci_timb, nota_cred_comp_fecha_emision, nota_cred_comp_fecha_carga, " +
                    "nota_cred_comp_estado, nota_cred_comp_observacion, id_usuario, id_proveedor, " +
                    "id_fact_comp_cab, nota_cred_motivo FROM nota_credito_compra_cabecera WHERE id_nota_cred_comp_cab = ?";

        usuarioDAO = new UsuarioDAO(conn);
        proveedorDAO = new ProveedorDAO(conn);
        facturaCompraDAO = new FacturaCompraDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idNotaCreditoCompra);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    notaCredito = new NotaCreditoCompra(
                        rs.getLong("id_nota_cred_comp_cab"),
                        rs.getString("nota_cred_comp_numero"),
                        rs.getInt("nota_cred_comp_timbrado"),
                        rs.getDate("nota_cred_comp_fecha_venci_timb"),
                        rs.getDate("nota_cred_comp_fecha_emision"),
                        rs.getDate("nota_cred_comp_fecha_carga"),
                        rs.getString("nota_cred_comp_estado"),
                        rs.getString("nota_cred_comp_observacion"),
                        usuarioDAO.getUsuario(rs.getLong("id_usuario")),
                        proveedorDAO.getProveedor(rs.getLong("id_proveedor")),
                        facturaCompraDAO.getFacturaCompra(rs.getLong("id_fact_comp_cab")),
                        rs.getString("nota_cred_motivo")
                    );
                }
            }
        }
        return notaCredito;
    }

    public List<NotaCreditoCompra> listarNotasCreditoCompra() throws SQLException {
        List<NotaCreditoCompra> notas = new ArrayList<>();
        String sql = "SELECT id_nota_cred_comp_cab, nota_cred_comp_numero, nota_cred_comp_timbrado, " +
                    "nota_cred_comp_fecha_venci_timb, nota_cred_comp_fecha_emision, nota_cred_comp_fecha_carga, " +
                    "nota_cred_comp_estado, nota_cred_comp_observacion, id_usuario, id_proveedor, " +
                    "id_fact_comp_cab, nota_cred_motivo FROM nota_credito_compra_cabecera";

        usuarioDAO = new UsuarioDAO(conn);
        proveedorDAO = new ProveedorDAO(conn);
        facturaCompraDAO = new FacturaCompraDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                NotaCreditoCompra notaCredito = new NotaCreditoCompra(
                    rs.getLong("id_nota_cred_comp_cab"),
                    rs.getString("nota_cred_comp_numero"),
                    rs.getInt("nota_cred_comp_timbrado"),
                    rs.getDate("nota_cred_comp_fecha_venci_timb"),
                    rs.getDate("nota_cred_comp_fecha_emision"),
                    rs.getDate("nota_cred_comp_fecha_carga"),
                    rs.getString("nota_cred_comp_estado"),
                    rs.getString("nota_cred_comp_observacion"),
                    usuarioDAO.getUsuario(rs.getLong("id_usuario")),
                    proveedorDAO.getProveedor(rs.getLong("id_proveedor")),
                    facturaCompraDAO.getFacturaCompra(rs.getLong("id_fact_comp_cab")),
                    rs.getString("nota_cred_motivo")
                );
                notas.add(notaCredito);
            }
        }
        return notas;
    }

    public Long insertarNotaCreditoCompra(NotaCreditoCompra notaCredito) throws SQLException {
        if (notaCredito == null) {
            LOGGER.log(Level.SEVERE, "Error: La nota de crédito de compra es nula");
            return null;
        }

        String sql = "INSERT INTO nota_credito_compra_cabecera (nota_cred_comp_numero, nota_cred_comp_timbrado, " +
                    "nota_cred_comp_fecha_venci_timb, nota_cred_comp_fecha_emision, nota_cred_comp_fecha_carga, " +
                    "nota_cred_comp_estado, nota_cred_comp_observacion, id_usuario, id_proveedor, " +
                    "id_fact_comp_cab, nota_cred_motivo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, notaCredito.getNumero());
            stmt.setInt(2, notaCredito.getTimbrado());
            stmt.setDate(3, new java.sql.Date(notaCredito.getFechaVenciTimbrado().getTime()));
            stmt.setDate(4, new java.sql.Date(notaCredito.getFechaEmision().getTime()));
            stmt.setDate(5, new java.sql.Date(notaCredito.getFechaCarga().getTime()));
            stmt.setString(6, notaCredito.getEstado());
            stmt.setString(7, notaCredito.getObservacion());
            stmt.setLong(8, notaCredito.getUsuario().getIdUsuario());
            stmt.setLong(9, notaCredito.getProveedor().getIdProveedor());
            stmt.setLong(10, notaCredito.getFacturaCompra().getIdFacturaCompra());
            stmt.setString(11, notaCredito.getMotivo());

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas == 0) {
                throw new SQLException("No se insertó la nota de crédito, ninguna fila afectada");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long idGenerado = generatedKeys.getLong(1);
                    notaCredito.setIdNotaCreditoCompra(idGenerado);
                    return idGenerado;
                } else {
                    throw new SQLException("Error: No se generó ningún ID para la nota de crédito.");
                }
            }
        }
    }

    public void actualizarNotaCreditoCompra(NotaCreditoCompra notaCredito) throws SQLException {
        if (notaCredito == null || notaCredito.getIdNotaCreditoCompra() == null) {
            LOGGER.log(Level.WARNING, "Error: nota de crédito de compra es nula");
            return;
        }

        String sql = "UPDATE nota_credito_compra_cabecera SET nota_cred_comp_numero = ?, nota_cred_comp_timbrado = ?, " +
                    "nota_cred_comp_fecha_venci_timb = ?, nota_cred_comp_fecha_emision = ?, nota_cred_comp_fecha_carga = ?, " +
                    "nota_cred_comp_estado = ?, nota_cred_comp_observacion = ?, id_usuario = ?, id_proveedor = ?, " +
                    "id_fact_comp_cab = ?, nota_cred_motivo = ? WHERE id_nota_cred_comp_cab = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, notaCredito.getNumero());
            stmt.setInt(2, notaCredito.getTimbrado());
            stmt.setDate(3, new java.sql.Date(notaCredito.getFechaVenciTimbrado().getTime()));
            stmt.setDate(4, new java.sql.Date(notaCredito.getFechaEmision().getTime()));
            stmt.setDate(5, new java.sql.Date(notaCredito.getFechaCarga().getTime()));
            stmt.setString(6, notaCredito.getEstado());
            stmt.setString(7, notaCredito.getObservacion());
            stmt.setLong(8, notaCredito.getUsuario().getIdUsuario());
            stmt.setLong(9, notaCredito.getProveedor().getIdProveedor());
            stmt.setLong(10, notaCredito.getFacturaCompra().getIdFacturaCompra());
            stmt.setString(11, notaCredito.getMotivo());
            stmt.setLong(12, notaCredito.getIdNotaCreditoCompra());

            stmt.executeUpdate();
        }
    }

    public void eliminarNotaCreditoCompra(Long idNotaCreditoCompra) throws SQLException {
        if (idNotaCreditoCompra == null) {
            LOGGER.log(Level.WARNING, "Error: id de la nota de crédito es nulo");
            return;
        }

        String sql = "DELETE FROM nota_credito_compra_cabecera WHERE id_nota_cred_comp_cab = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idNotaCreditoCompra);
            stmt.executeUpdate();
        }
    }

    // ==================== DETALLE ====================

    /**
     * Inserta una línea de la nota de crédito. La inserción de una línea con id_articulo + id_deposito
     * dispara el trigger de stock (resta la devolución). Las líneas sin depósito no tocan stock.
     *
     * @param detalle       la línea a insertar
     * @param idNotaCredito id de la cabecera a la que pertenece
     * @return true si insertó
     * @throws SQLException si ocurre un error de base de datos
     */
    public boolean insertarDetalle(NotaCreditoCompraDetalle detalle, Long idNotaCredito) throws SQLException {
        if (detalle == null || idNotaCredito == null) {
            LOGGER.log(Level.SEVERE, "Error: detalle o idNotaCredito nulo en insertarDetalle");
            return false;
        }

        String sql = "INSERT INTO nota_credito_compra_detalle (id_articulo, nota_cred_comp_cantidad, " +
                    "nota_cred_monto, id_impuesto, nota_credito_descripcion, id_deposito, id_nota_cred_comp_cab) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (detalle.getArticulo() != null) {
                stmt.setLong(1, detalle.getArticulo().getIdArticulo());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            if (detalle.getCantidad() != null) {
                stmt.setLong(2, detalle.getCantidad());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            if (detalle.getMonto() != null) {
                stmt.setLong(3, detalle.getMonto());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            if (detalle.getTipoImpuesto() != null) {
                stmt.setLong(4, detalle.getTipoImpuesto().getIdTipoImpuesto());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }
            stmt.setString(5, detalle.getDescripcion());
            if (detalle.getDeposito() != null && detalle.getDeposito().getIdDeposito() != null) {
                stmt.setLong(6, detalle.getDeposito().getIdDeposito());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }
            stmt.setLong(7, idNotaCredito);

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;
        }
    }

    public List<NotaCreditoCompraDetalle> listarDetallesPorNota(Long idNotaCredito) throws SQLException {
        if (idNotaCredito == null) {
            LOGGER.log(Level.WARNING, "Error: idNotaCredito es nulo en listarDetallesPorNota");
            return null;
        }
        List<NotaCreditoCompraDetalle> detalles = new ArrayList<>();
        String sql = "SELECT * FROM nota_credito_compra_detalle WHERE id_nota_cred_comp_cab = ?";

        ArticuloDAO articuloDAO = new ArticuloDAO(conn);
        TipoImpuestoDAO tipoImpuestoDAO = new TipoImpuestoDAO(conn);
        DepositoDAO depositoDAO = new DepositoDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idNotaCredito);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    NotaCreditoCompraDetalle detalle = new NotaCreditoCompraDetalle();
                    detalle.setId(rs.getLong("id_nota_credito_det"));

                    Long idArticulo = rs.getLong("id_articulo");
                    detalle.setArticulo(!rs.wasNull() && idArticulo != 0 ? articuloDAO.getArticulo(idArticulo) : null);

                    detalle.setCantidad(rs.getLong("nota_cred_comp_cantidad"));
                    detalle.setMonto(rs.getLong("nota_cred_monto"));
                    detalle.setDescripcion(rs.getString("nota_credito_descripcion"));

                    Long idImpuesto = rs.getLong("id_impuesto");
                    detalle.setTipoImpuesto(!rs.wasNull() && idImpuesto != 0 ? tipoImpuestoDAO.getTipoImpuesto(idImpuesto) : null);

                    Long idDeposito = rs.getLong("id_deposito");
                    detalle.setDeposito(!rs.wasNull() && idDeposito != 0 ? depositoDAO.getDepostio(idDeposito) : null);

                    detalles.add(detalle);
                }
            }
        }
        return detalles;
    }

    /**
     * Cambia el estado de la nota a 'Anulado' (dispara el trigger de reposición de stock).
     * Preserva la fila para trazabilidad, no la elimina.
     */
    public void anularNotaCredito(Long idNotaCredito) throws SQLException {
        if (idNotaCredito == null) {
            LOGGER.log(Level.WARNING, "Error: idNotaCredito es nulo");
            return;
        }
        String sql = "UPDATE nota_credito_compra_cabecera SET nota_cred_comp_estado = 'Anulado' "
                   + "WHERE id_nota_cred_comp_cab = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idNotaCredito);
            stmt.executeUpdate();
        }
    }
}
