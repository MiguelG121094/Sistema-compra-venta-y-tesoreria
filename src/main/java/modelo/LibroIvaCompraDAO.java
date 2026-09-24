package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO para la tabla libro_iva_compra.
 * Registra los totales de IVA 5% e IVA 10% por cada factura de compra.
 *
 * @author Miguel
 */
public class LibroIvaCompraDAO {

    /** Valores de libro_iva_comp_origen. Las filas viejas de factura lo tienen en null. */
    public static final String ORIGEN_FACTURA = "FACTURA";
    public static final String ORIGEN_NOTA_CREDITO = "NOTA_CRED";
    public static final String ORIGEN_NOTA_DEBITO = "NOTA_DEBI";

    private static final String ESTADO_ANULADO = "Anulado";
    private static final String ESTADO_ACTIVO = "Activo";

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
                    "libro_iva_comp_estado, libro_iva_comp_origen) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, libroIva.getFacturaCompra().getIdFacturaCompra());
            stmt.setDate(2, new java.sql.Date(libroIva.getFecha().getTime()));
            setNullableLong(stmt, 3, libroIva.getIva5());
            setNullableLong(stmt, 4, libroIva.getIva10());
            setNullableLong(stmt, 5, libroIva.getGravada5());
            setNullableLong(stmt, 6, libroIva.getGravada10());
            setNullableLong(stmt, 7, libroIva.getExenta());
            setNullableLong(stmt, 8, libroIva.getTotal());
            stmt.setString(9, libroIva.getEstado() != null ? libroIva.getEstado() : ESTADO_ACTIVO);
            // Las notas ya grababan su origen; la factura no, y sin eso el informe no la puede
            // separar de las notas. Las filas viejas quedan en null y valen como FACTURA.
            stmt.setString(10, ORIGEN_FACTURA);

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

    // ==================== INFORME LIBRO DE COMPRAS ====================

    /**
     * Filas del libro de compras de un periodo, para el informe (Ley 125/91).
     *
     * <p>El numero, el timbrado y el proveedor NO estan en libro_iva_compra: viven en la cabecera
     * que origino la fila, que es la factura, la nota de credito o la nota de debito. Por eso se
     * traen las tres y es el Java el que elige cual corresponde segun el origen; una nota lleva su
     * propio numero y timbrado, no los de la factura que corrige.
     *
     * <p>Las anuladas quedan afuera: la fila se conserva para la trazabilidad, pero no es parte del
     * libro. Las notas de credito ya vienen con los importes en negativo, asi que el total del
     * periodo sale neto sin hacer nada.
     *
     * @param origen filtra por tipo de comprobante; null o vacio trae todos
     */
    public List<LibroIvaCompra> listarParaInforme(Date desde, Date hasta, String origen)
            throws SQLException {

        List<LibroIvaCompra> lista = new ArrayList<>();
        if (desde == null || hasta == null) {
            return lista;
        }

        boolean filtraOrigen = origen != null && !origen.trim().isEmpty();
        String sql = "SELECT l.id_libro_iva_compra, l.libro_iva_comp_fecha, l.libro_iva_comp_5, "
                   + "l.libro_iva_comp_10, l.libro_iva_comp_gravada_5, l.libro_iva_comp_gravada_10, "
                   + "l.libro_iva_comp_exenta, l.libro_iva_comp_total, l.libro_iva_comp_estado, "
                   + "l.libro_iva_comp_origen, l.id_fact_comp_cab, "
                   + "f.fact_comp_numero, f.fact_comp_timbrado, "
                   + "nc.nota_cred_comp_numero, nc.nota_cred_comp_timbrado, "
                   + "nd.nota_debi_comp_numero, nd.nota_debi_comp_timbrado, "
                   + "pf.id_proveedor AS id_prov_fact, pf.prov_razon_social AS razon_fact, pf.prov_ruc AS ruc_fact, "
                   + "pc.id_proveedor AS id_prov_nc, pc.prov_razon_social AS razon_nc, pc.prov_ruc AS ruc_nc, "
                   + "pd.id_proveedor AS id_prov_nd, pd.prov_razon_social AS razon_nd, pd.prov_ruc AS ruc_nd "
                   + "FROM libro_iva_compra l "
                   + "LEFT JOIN factura_compra_cabecera f ON l.id_fact_comp_cab = f.id_fact_comp_cab "
                   + "LEFT JOIN nota_credito_compra_cabecera nc ON l.id_nota_cred_comp_cab = nc.id_nota_cred_comp_cab "
                   + "LEFT JOIN nota_debito_compra_cabecera nd ON l.id_nota_debi_comp_cab = nd.id_nota_debi_comp_cab "
                   + "LEFT JOIN proveedor pf ON f.id_proveedor = pf.id_proveedor "
                   + "LEFT JOIN proveedor pc ON nc.id_proveedor = pc.id_proveedor "
                   + "LEFT JOIN proveedor pd ON nd.id_proveedor = pd.id_proveedor "
                   + "WHERE l.libro_iva_comp_fecha BETWEEN ? AND ? "
                   + "AND COALESCE(l.libro_iva_comp_estado, '" + ESTADO_ACTIVO + "') <> '" + ESTADO_ANULADO + "' "
                   // El origen de las filas de factura viejas quedo en null: valen como FACTURA.
                   + (filtraOrigen ? "AND COALESCE(l.libro_iva_comp_origen, '" + ORIGEN_FACTURA + "') = ? " : "")
                   + "ORDER BY l.libro_iva_comp_fecha, l.id_libro_iva_compra";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, new java.sql.Date(desde.getTime()));
            stmt.setDate(2, new java.sql.Date(hasta.getTime()));
            if (filtraOrigen) {
                stmt.setString(3, origen.trim());
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearFilaInforme(rs));
                }
            }
        }
        return lista;
    }

    /** Arma la fila del informe y le pega los datos del comprobante que le corresponde. */
    private LibroIvaCompra mapearFilaInforme(ResultSet rs) throws SQLException {
        LibroIvaCompra fila = new LibroIvaCompra();
        fila.setIdLibroIvaCompra(rs.getLong("id_libro_iva_compra"));
        fila.setFecha(rs.getDate("libro_iva_comp_fecha"));
        fila.setIva5(leerLong(rs, "libro_iva_comp_5"));
        fila.setIva10(leerLong(rs, "libro_iva_comp_10"));
        fila.setGravada5(leerLong(rs, "libro_iva_comp_gravada_5"));
        fila.setGravada10(leerLong(rs, "libro_iva_comp_gravada_10"));
        fila.setExenta(leerLong(rs, "libro_iva_comp_exenta"));
        fila.setTotal(leerLong(rs, "libro_iva_comp_total"));
        fila.setEstado(rs.getString("libro_iva_comp_estado"));

        String origen = rs.getString("libro_iva_comp_origen");
        if (origen == null || origen.trim().isEmpty()) {
            origen = ORIGEN_FACTURA;
        }
        fila.setOrigen(origen);

        long idFactura = rs.getLong("id_fact_comp_cab");
        if (!rs.wasNull()) {
            fila.setFacturaCompra(new FacturaCompra(idFactura));
        }

        if (ORIGEN_NOTA_CREDITO.equals(origen)) {
            fila.setNumeroComprobante(rs.getString("nota_cred_comp_numero"));
            fila.setTimbrado(leerLong(rs, "nota_cred_comp_timbrado"));
            fila.setProveedor(leerProveedor(rs, "id_prov_nc", "razon_nc", "ruc_nc"));
        } else if (ORIGEN_NOTA_DEBITO.equals(origen)) {
            fila.setNumeroComprobante(rs.getString("nota_debi_comp_numero"));
            fila.setTimbrado(leerLong(rs, "nota_debi_comp_timbrado"));
            fila.setProveedor(leerProveedor(rs, "id_prov_nd", "razon_nd", "ruc_nd"));
        } else {
            fila.setNumeroComprobante(rs.getString("fact_comp_numero"));
            fila.setTimbrado(leerLong(rs, "fact_comp_timbrado"));
            fila.setProveedor(leerProveedor(rs, "id_prov_fact", "razon_fact", "ruc_fact"));
        }
        return fila;
    }

    private Proveedor leerProveedor(ResultSet rs, String columnaId, String columnaRazon,
            String columnaRuc) throws SQLException {
        long id = rs.getLong(columnaId);
        if (rs.wasNull()) {
            return null;
        }
        Proveedor proveedor = new Proveedor();
        proveedor.setIdProveedor(id);
        proveedor.setRazonSocial(rs.getString(columnaRazon));
        proveedor.setRuc(rs.getString(columnaRuc));
        return proveedor;
    }

    private Long leerLong(ResultSet rs, String columna) throws SQLException {
        long valor = rs.getLong(columna);
        return rs.wasNull() ? null : valor;
    }
}
