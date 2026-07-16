package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO para la tabla libro_iva_compra.
 * Registra los totales de IVA 5% e IVA 10% por cada factura de compra.
 *
 * @author Miguel
 */
public class LibroIvaCompraDAO {

    private Connection conn;
    private static final Logger LOGGER = Logger.getLogger(LibroIvaCompraDAO.class.getName());

    public LibroIvaCompraDAO(Connection conn) {
        this.conn = conn;
    }

    /**
     * Inserta un registro en el libro IVA compra.
     *
     * @param libroIva el registro a insertar
     * @return el ID generado, o null si hubo error
     * @throws SQLException si ocurre un error de base de datos
     */
    public Long insertarLibroIvaCompra(LibroIvaCompra libroIva) throws SQLException {
        if (libroIva == null || libroIva.getFacturaCompra() == null) {
            LOGGER.log(Level.SEVERE, "Error: El registro de libro IVA compra o la factura es nula");
            return null;
        }

        String sql = "INSERT INTO libro_iva_compra (id_fact_comp_cab, libro_iva_comp_fecha, " +
                    "libro_iva_comp_5, libro_iva_comp_10, libro_iva_comp_gravada_5, " +
                    "libro_iva_comp_gravada_10, libro_iva_comp_exenta, libro_iva_comp_total, " +
                    "libro_iva_comp_estado) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, libroIva.getFacturaCompra().getIdFacturaCompra());
            stmt.setDate(2, new java.sql.Date(libroIva.getFecha().getTime()));
            setNullableLong(stmt, 3, libroIva.getIva5());
            setNullableLong(stmt, 4, libroIva.getIva10());
            setNullableLong(stmt, 5, libroIva.getGravada5());
            setNullableLong(stmt, 6, libroIva.getGravada10());
            setNullableLong(stmt, 7, libroIva.getExenta());
            setNullableLong(stmt, 8, libroIva.getTotal());
            stmt.setString(9, libroIva.getEstado() != null ? libroIva.getEstado() : "Activo");

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas == 0) {
                throw new SQLException("No se insertó el registro de libro IVA compra, ninguna fila afectada");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long idGenerado = generatedKeys.getLong(1);
                    libroIva.setIdLibroIvaCompra(idGenerado);
                    return idGenerado;
                } else {
                    throw new SQLException("Error: No se generó ningún ID para el libro IVA compra.");
                }
            }
        }
    }

    /**
     * Elimina los registros del libro IVA compra asociados a una factura.
     *
     * @param idFacturaCompra ID de la factura de compra
     * @throws SQLException si ocurre un error de base de datos
     */
    private void setNullableLong(PreparedStatement stmt, int index, Long value) throws SQLException {
        if (value != null) {
            stmt.setLong(index, value);
        } else {
            stmt.setNull(index, Types.INTEGER);
        }
    }

    public void eliminarPorFactura(Long idFacturaCompra) throws SQLException {
        if (idFacturaCompra == null) {
            LOGGER.log(Level.WARNING, "Error: idFacturaCompra es nulo");
            return;
        }

        String sql = "DELETE FROM libro_iva_compra WHERE id_fact_comp_cab = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idFacturaCompra);
            stmt.executeUpdate();
        }
    }

    /**
     * Marca como 'Anulado' los registros del libro IVA asociados a una factura.
     * Preserva el registro para trazabilidad fiscal (no se elimina físicamente).
     */
    public void anularPorFactura(Long idFacturaCompra) throws SQLException {
        if (idFacturaCompra == null) {
            LOGGER.log(Level.WARNING, "Error: idFacturaCompra es nulo");
            return;
        }

        String sql = "UPDATE libro_iva_compra SET libro_iva_comp_estado = 'Anulado' "
                   + "WHERE id_fact_comp_cab = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idFacturaCompra);
            stmt.executeUpdate();
        }
    }

    // ==================== FILAS DE ORIGEN NOTA (CRÉDITO / DÉBITO) ====================
    // Ver NOTA_CREDITO_DEBITO_PLAN.md §5.1.

    /**
     * Inserta una fila de libro IVA con origen NOTA (crédito o débito), conservando el
     * id_fact_comp_cab de la factura referenciada. Los montos deben venir YA CON SIGNO
     * (negativos para NC, positivos para ND).
     *
     * @param libroIva        montos (con signo), fecha y estado a registrar
     * @param idFacturaCompra factura referenciada por la nota (se conserva en la fila)
     * @param origen          'NOTA_CRED' o 'NOTA_DEBI'
     * @param idNotaCredito   FK de la nota de crédito (null si es débito)
     * @param idNotaDebito    FK de la nota de débito (null si es crédito)
     * @return id generado
     * @throws SQLException si ocurre un error de base de datos
     */
    public Long insertarLibroIvaNota(LibroIvaCompra libroIva, Long idFacturaCompra, String origen,
            Long idNotaCredito, Long idNotaDebito) throws SQLException {
        if (libroIva == null || idFacturaCompra == null || origen == null) {
            LOGGER.log(Level.SEVERE, "Error: parámetros nulos en insertarLibroIvaNota");
            return null;
        }

        String sql = "INSERT INTO libro_iva_compra (id_fact_comp_cab, id_nota_cred_comp_cab, " +
                    "id_nota_debi_comp_cab, libro_iva_comp_fecha, libro_iva_comp_5, libro_iva_comp_10, " +
                    "libro_iva_comp_gravada_5, libro_iva_comp_gravada_10, libro_iva_comp_exenta, " +
                    "libro_iva_comp_total, libro_iva_comp_estado, libro_iva_comp_origen) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, idFacturaCompra);
            setNullableLong(stmt, 2, idNotaCredito);
            setNullableLong(stmt, 3, idNotaDebito);
            stmt.setDate(4, new java.sql.Date(libroIva.getFecha().getTime()));
            setNullableLong(stmt, 5, libroIva.getIva5());
            setNullableLong(stmt, 6, libroIva.getIva10());
            setNullableLong(stmt, 7, libroIva.getGravada5());
            setNullableLong(stmt, 8, libroIva.getGravada10());
            setNullableLong(stmt, 9, libroIva.getExenta());
            setNullableLong(stmt, 10, libroIva.getTotal());
            stmt.setString(11, libroIva.getEstado() != null ? libroIva.getEstado() : "Activo");
            stmt.setString(12, origen);

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas == 0) {
                throw new SQLException("No se insertó la fila de libro IVA (nota), ninguna fila afectada");
            }
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long idGenerado = generatedKeys.getLong(1);
                    libroIva.setIdLibroIvaCompra(idGenerado);
                    return idGenerado;
                } else {
                    throw new SQLException("Error: No se generó ID para la fila de libro IVA (nota).");
                }
            }
        }
    }

    /**
     * Marca como 'Anulado' las filas de libro IVA generadas por una nota de crédito.
     * Filtra por la FK de la nota (NO por id_fact_comp_cab, que también llevan la fila de la
     * factura y las de otras notas). Preserva para trazabilidad fiscal.
     */
    public void anularPorNotaCredito(Long idNotaCredito) throws SQLException {
        if (idNotaCredito == null) {
            LOGGER.log(Level.WARNING, "Error: idNotaCredito es nulo");
            return;
        }
        String sql = "UPDATE libro_iva_compra SET libro_iva_comp_estado = 'Anulado' "
                   + "WHERE id_nota_cred_comp_cab = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idNotaCredito);
            stmt.executeUpdate();
        }
    }

    /**
     * Marca como 'Anulado' las filas de libro IVA generadas por una nota de débito.
     */
    public void anularPorNotaDebito(Long idNotaDebito) throws SQLException {
        if (idNotaDebito == null) {
            LOGGER.log(Level.WARNING, "Error: idNotaDebito es nulo");
            return;
        }
        String sql = "UPDATE libro_iva_compra SET libro_iva_comp_estado = 'Anulado' "
                   + "WHERE id_nota_debi_comp_cab = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idNotaDebito);
            stmt.executeUpdate();
        }
    }
}
