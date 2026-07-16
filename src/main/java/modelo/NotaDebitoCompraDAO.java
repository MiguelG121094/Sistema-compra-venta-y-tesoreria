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
public class NotaDebitoCompraDAO {

    private Connection conn;
    private UsuarioDAO usuarioDAO;
    private ProveedorDAO proveedorDAO;
    private FacturaCompraDAO facturaCompraDAO;
    private static final Logger LOGGER = Logger.getLogger(NotaDebitoCompraDAO.class.getName());

    public NotaDebitoCompraDAO(Connection conn) {
        this.conn = conn;
    }

    public NotaDebitoCompra getNotaDebitoCompra(Long idNotaDebitoCompra) throws SQLException {
        if (idNotaDebitoCompra == null) {
            LOGGER.log(Level.WARNING, "Error: el parámetro idNotaDebitoCompra es nulo");
            return null;
        }
        NotaDebitoCompra notaDebito = null;
        String sql = "SELECT id_nota_debi_comp_cab, nota_debi_comp_numero, nota_debi_comp_timbrado, " +
                    "nota_debi_comp_fecha_venci_timb, nota_debi_comp_fecha_emision, nota_debi_comp_fecha_carga, " +
                    "nota_debi_comp_estado, nota_debi_comp_observacion, id_usuario, id_proveedor, " +
                    "id_fact_comp_cab, nota_debito_motivo FROM nota_debito_compra_cabecera WHERE id_nota_debi_comp_cab = ?";

        usuarioDAO = new UsuarioDAO(conn);
        proveedorDAO = new ProveedorDAO(conn);
        facturaCompraDAO = new FacturaCompraDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idNotaDebitoCompra);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    notaDebito = new NotaDebitoCompra(
                        rs.getLong("id_nota_debi_comp_cab"),
                        rs.getString("nota_debi_comp_numero"),
                        rs.getInt("nota_debi_comp_timbrado"),
                        rs.getDate("nota_debi_comp_fecha_venci_timb"),
                        rs.getDate("nota_debi_comp_fecha_emision"),
                        rs.getDate("nota_debi_comp_fecha_carga"),
                        rs.getString("nota_debi_comp_estado"),
                        rs.getString("nota_debi_comp_observacion"),
                        usuarioDAO.getUsuario(rs.getLong("id_usuario")),
                        proveedorDAO.getProveedor(rs.getLong("id_proveedor")),
                        facturaCompraDAO.getFacturaCompra(rs.getLong("id_fact_comp_cab")),
                        rs.getString("nota_debito_motivo")
                    );
                }
            }
        }
        return notaDebito;
    }

    public List<NotaDebitoCompra> listarNotasDebitoCompra() throws SQLException {
        List<NotaDebitoCompra> notas = new ArrayList<>();
        String sql = "SELECT id_nota_debi_comp_cab, nota_debi_comp_numero, nota_debi_comp_timbrado, " +
                    "nota_debi_comp_fecha_venci_timb, nota_debi_comp_fecha_emision, nota_debi_comp_fecha_carga, " +
                    "nota_debi_comp_estado, nota_debi_comp_observacion, id_usuario, id_proveedor, " +
                    "id_fact_comp_cab, nota_debito_motivo FROM nota_debito_compra_cabecera";

        usuarioDAO = new UsuarioDAO(conn);
        proveedorDAO = new ProveedorDAO(conn);
        facturaCompraDAO = new FacturaCompraDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                NotaDebitoCompra notaDebito = new NotaDebitoCompra(
                    rs.getLong("id_nota_debi_comp_cab"),
                    rs.getString("nota_debi_comp_numero"),
                    rs.getInt("nota_debi_comp_timbrado"),
                    rs.getDate("nota_debi_comp_fecha_venci_timb"),
                    rs.getDate("nota_debi_comp_fecha_emision"),
                    rs.getDate("nota_debi_comp_fecha_carga"),
                    rs.getString("nota_debi_comp_estado"),
                    rs.getString("nota_debi_comp_observacion"),
                    usuarioDAO.getUsuario(rs.getLong("id_usuario")),
                    proveedorDAO.getProveedor(rs.getLong("id_proveedor")),
                    facturaCompraDAO.getFacturaCompra(rs.getLong("id_fact_comp_cab")),
                    rs.getString("nota_debito_motivo")
                );
                notas.add(notaDebito);
            }
        }
        return notas;
    }

    public Long insertarNotaDebitoCompra(NotaDebitoCompra notaDebito) throws SQLException {
        if (notaDebito == null) {
            LOGGER.log(Level.SEVERE, "Error: La nota de débito de compra es nula");
            return null;
        }

        String sql = "INSERT INTO nota_debito_compra_cabecera (nota_debi_comp_numero, nota_debi_comp_timbrado, " +
                    "nota_debi_comp_fecha_venci_timb, nota_debi_comp_fecha_emision, nota_debi_comp_fecha_carga, " +
                    "nota_debi_comp_estado, nota_debi_comp_observacion, id_usuario, id_proveedor, " +
                    "id_fact_comp_cab, nota_debito_motivo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, notaDebito.getNumero());
            stmt.setInt(2, notaDebito.getTimbrado());
            stmt.setDate(3, new java.sql.Date(notaDebito.getFechaVenciTimbrado().getTime()));
            stmt.setDate(4, new java.sql.Date(notaDebito.getFechaEmision().getTime()));
            stmt.setDate(5, new java.sql.Date(notaDebito.getFechaCarga().getTime()));
            stmt.setString(6, notaDebito.getEstado());
            stmt.setString(7, notaDebito.getObservacion());
            stmt.setLong(8, notaDebito.getUsuario().getIdUsuario());
            stmt.setLong(9, notaDebito.getProveedor().getIdProveedor());
            stmt.setLong(10, notaDebito.getFacturaCompra().getIdFacturaCompra());
            stmt.setString(11, notaDebito.getMotivo());

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas == 0) {
                throw new SQLException("No se insertó la nota de débito, ninguna fila afectada");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long idGenerado = generatedKeys.getLong(1);
                    notaDebito.setIdNotaDebitoCompra(idGenerado);
                    return idGenerado;
                } else {
                    throw new SQLException("Error: No se generó ningún ID para la nota de débito.");
                }
            }
        }
    }

    public void actualizarNotaDebitoCompra(NotaDebitoCompra notaDebito) throws SQLException {
        if (notaDebito == null || notaDebito.getIdNotaDebitoCompra() == null) {
            LOGGER.log(Level.WARNING, "Error: nota de débito de compra es nula");
            return;
        }

        String sql = "UPDATE nota_debito_compra_cabecera SET nota_debi_comp_numero = ?, nota_debi_comp_timbrado = ?, " +
                    "nota_debi_comp_fecha_venci_timb = ?, nota_debi_comp_fecha_emision = ?, nota_debi_comp_fecha_carga = ?, " +
                    "nota_debi_comp_estado = ?, nota_debi_comp_observacion = ?, id_usuario = ?, id_proveedor = ?, " +
                    "id_fact_comp_cab = ?, nota_debito_motivo = ? WHERE id_nota_debi_comp_cab = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, notaDebito.getNumero());
            stmt.setInt(2, notaDebito.getTimbrado());
            stmt.setDate(3, new java.sql.Date(notaDebito.getFechaVenciTimbrado().getTime()));
            stmt.setDate(4, new java.sql.Date(notaDebito.getFechaEmision().getTime()));
            stmt.setDate(5, new java.sql.Date(notaDebito.getFechaCarga().getTime()));
            stmt.setString(6, notaDebito.getEstado());
            stmt.setString(7, notaDebito.getObservacion());
            stmt.setLong(8, notaDebito.getUsuario().getIdUsuario());
            stmt.setLong(9, notaDebito.getProveedor().getIdProveedor());
            stmt.setLong(10, notaDebito.getFacturaCompra().getIdFacturaCompra());
            stmt.setString(11, notaDebito.getMotivo());
            stmt.setLong(12, notaDebito.getIdNotaDebitoCompra());

            stmt.executeUpdate();
        }
    }

    public void eliminarNotaDebitoCompra(Long idNotaDebitoCompra) throws SQLException {
        if (idNotaDebitoCompra == null) {
            LOGGER.log(Level.WARNING, "Error: id de la nota de débito es nulo");
            return;
        }

        String sql = "DELETE FROM nota_debito_compra_cabecera WHERE id_nota_debi_comp_cab = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idNotaDebitoCompra);
            stmt.executeUpdate();
        }
    }

    // ==================== DETALLE ====================

    /**
     * Inserta una línea de la nota de débito. La ND es financiera (recargo/tarifa) y NO mueve stock,
     * por eso el detalle no lleva depósito ni dispara ningún trigger.
     *
     * @param detalle      la línea a insertar
     * @param idNotaDebito id de la cabecera a la que pertenece
     * @return true si insertó
     * @throws SQLException si ocurre un error de base de datos
     */
    public boolean insertarDetalle(NotaDebitoCompraDetalle detalle, Long idNotaDebito) throws SQLException {
        if (detalle == null || idNotaDebito == null) {
            LOGGER.log(Level.SEVERE, "Error: detalle o idNotaDebito nulo en insertarDetalle");
            return false;
        }

        String sql = "INSERT INTO nota_debito_compra_detalle (id_articulo, nota_debi_comp_cantidad, " +
                    "nota_debi_monto, id_impuesto, nota_debito_descripcion, id_nota_debi_comp_cab) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

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
            stmt.setLong(6, idNotaDebito);

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;
        }
    }

    public List<NotaDebitoCompraDetalle> listarDetallesPorNota(Long idNotaDebito) throws SQLException {
        if (idNotaDebito == null) {
            LOGGER.log(Level.WARNING, "Error: idNotaDebito es nulo en listarDetallesPorNota");
            return null;
        }
        List<NotaDebitoCompraDetalle> detalles = new ArrayList<>();
        String sql = "SELECT * FROM nota_debito_compra_detalle WHERE id_nota_debi_comp_cab = ?";

        ArticuloDAO articuloDAO = new ArticuloDAO(conn);
        TipoImpuestoDAO tipoImpuestoDAO = new TipoImpuestoDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idNotaDebito);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    NotaDebitoCompraDetalle detalle = new NotaDebitoCompraDetalle();
                    detalle.setId(rs.getLong("id_nota_debito_det"));

                    Long idArticulo = rs.getLong("id_articulo");
                    detalle.setArticulo(!rs.wasNull() && idArticulo != 0 ? articuloDAO.getArticulo(idArticulo) : null);

                    detalle.setCantidad(rs.getLong("nota_debi_comp_cantidad"));
                    detalle.setMonto(rs.getLong("nota_debi_monto"));
                    detalle.setDescripcion(rs.getString("nota_debito_descripcion"));

                    Long idImpuesto = rs.getLong("id_impuesto");
                    detalle.setTipoImpuesto(!rs.wasNull() && idImpuesto != 0 ? tipoImpuestoDAO.getTipoImpuesto(idImpuesto) : null);

                    detalles.add(detalle);
                }
            }
        }
        return detalles;
    }

    /**
     * Cambia el estado de la nota a 'Anulado'. Preserva la fila para trazabilidad.
     * (La ND no tiene trigger de stock; solo se revierte el saldo desde el Service.)
     */
    public void anularNotaDebito(Long idNotaDebito) throws SQLException {
        if (idNotaDebito == null) {
            LOGGER.log(Level.WARNING, "Error: idNotaDebito es nulo");
            return;
        }
        String sql = "UPDATE nota_debito_compra_cabecera SET nota_debi_comp_estado = 'Anulado' "
                   + "WHERE id_nota_debi_comp_cab = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idNotaDebito);
            stmt.executeUpdate();
        }
    }

    /**
     * Indica si la factura tiene al menos una nota de debito NO anulada. Se usa para
     * bloquear editar/anular la factura mientras existan notas activas (primero se deben
     * anular las notas). Ver NOTA_CREDITO_DEBITO_PLAN.md §8.4.
     */
    public boolean tieneNotaActivaPorFactura(Long idFacturaCompra) throws SQLException {
        if (idFacturaCompra == null) {
            return false;
        }
        String sql = "SELECT EXISTS (SELECT 1 FROM nota_debito_compra_cabecera "
                   + "WHERE id_fact_comp_cab = ? AND nota_debi_comp_estado <> 'Anulado')";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idFacturaCompra);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        }
    }
}
