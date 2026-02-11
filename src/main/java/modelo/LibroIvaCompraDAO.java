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
                    "libro_iva_comp_gravada_10, libro_iva_comp_exenta, libro_iva_comp_total) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, libroIva.getFacturaCompra().getIdFacturaCompra());
            stmt.setDate(2, new java.sql.Date(libroIva.getFecha().getTime()));
            setNullableLong(stmt, 3, libroIva.getIva5());
            setNullableLong(stmt, 4, libroIva.getIva10());
            setNullableLong(stmt, 5, libroIva.getGravada5());
            setNullableLong(stmt, 6, libroIva.getGravada10());
            setNullableLong(stmt, 7, libroIva.getExenta());
            setNullableLong(stmt, 8, libroIva.getTotal());

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
}
