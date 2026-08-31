package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de chequera. Ademas del ABM, calcula el proximo numero de cheque disponible
 * dentro del rango de la chequera. Corre sobre la Connection compartida (la transaccion
 * la controla el Service). Ver MODULO_TESORERIA_PLAN.md §C y §G1.
 *
 * @author Miguel
 */
public class ChequeraDAO {

    private Connection conn;

    public ChequeraDAO(Connection conn) {
        this.conn = conn;
    }

    private static final String COLUMNAS =
            "id_chequera, id_cuenta, chequera_serie, chequera_desde_nro, chequera_hasta_nro";

    private Chequera mapear(ResultSet rs) throws SQLException {
        Chequera ch = new Chequera();
        ch.setIdChequera(rs.getLong("id_chequera"));
        // Se hidrata la cuenta entera: la chequera se identifica por banco y numero de cuenta,
        // tanto en la grilla del ABM como en el combo de la orden de pago.
        ch.setCuenta(new CuentaDAO(conn).getCuenta(rs.getLong("id_cuenta")));
        ch.setSerie(rs.getLong("chequera_serie"));
        ch.setDesdeNumero(rs.getLong("chequera_desde_nro"));
        ch.setHastaNumero(rs.getLong("chequera_hasta_nro"));
        return ch;
    }

    public Chequera getChequera(Long idChequera) throws SQLException {
        if (idChequera == null) {
            return null;
        }
        String sql = "SELECT " + COLUMNAS + " FROM chequera WHERE id_chequera = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idChequera);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    /**
     * Listado del ABM. Trae ademas el consumo del rango: cuantos cheques se emitieron, cual es el
     * proximo numero libre y cuantos quedan. El ultimo numero se toma de MAX(chq_numero) y no del
     * conteo, porque un numero anulado no se reutiliza y el rango igual queda consumido.
     */
    public List<Chequera> listarChequeras() throws SQLException {
        List<Chequera> lista = new ArrayList<>();
        String sql = "SELECT ch.id_chequera, ch.id_cuenta, ch.chequera_serie, "
                   + "ch.chequera_desde_nro, ch.chequera_hasta_nro, "
                   + "COUNT(c.id_cheque) AS emitidos, "
                   + "COALESCE(MAX(c.chq_numero), ch.chequera_desde_nro - 1) AS ultimo_numero "
                   + "FROM chequera ch LEFT JOIN cheque c ON c.id_chequera = ch.id_chequera "
                   + "GROUP BY ch.id_chequera, ch.id_cuenta, ch.chequera_serie, "
                   + "ch.chequera_desde_nro, ch.chequera_hasta_nro "
                   + "ORDER BY ch.id_chequera";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Chequera ch = mapear(rs);
                long ultimo = rs.getLong("ultimo_numero");
                long disponibles = ch.getHastaNumero() - ultimo;
                ch.setEmitidos(rs.getLong("emitidos"));
                ch.setDisponibles(disponibles > 0 ? disponibles : 0L);
                ch.setProximoNumero(disponibles > 0 ? ultimo + 1 : null);
                lista.add(ch);
            }
        }
        return lista;
    }

    public void insertarChequera(Chequera chequera) throws SQLException {
        String sql = "INSERT INTO chequera (id_cuenta, chequera_serie, chequera_desde_nro, chequera_hasta_nro) "
                   + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, chequera.getCuenta().getIdCuenta());
            stmt.setLong(2, chequera.getSerie());
            stmt.setLong(3, chequera.getDesdeNumero());
            stmt.setLong(4, chequera.getHastaNumero());
            stmt.executeUpdate();
        }
    }

    public void actualizarChequera(Chequera chequera) throws SQLException {
        String sql = "UPDATE chequera SET id_cuenta = ?, chequera_serie = ?, "
                   + "chequera_desde_nro = ?, chequera_hasta_nro = ? WHERE id_chequera = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, chequera.getCuenta().getIdCuenta());
            stmt.setLong(2, chequera.getSerie());
            stmt.setLong(3, chequera.getDesdeNumero());
            stmt.setLong(4, chequera.getHastaNumero());
            stmt.setLong(5, chequera.getIdChequera());
            stmt.executeUpdate();
        }
    }

    public void eliminarChequera(Long idChequera) throws SQLException {
        String sql = "DELETE FROM chequera WHERE id_chequera = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idChequera);
            stmt.executeUpdate();
        }
    }

    public long contarCheques(Long idChequera) throws SQLException {
        String sql = "SELECT COUNT(*) FROM cheque WHERE id_chequera = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idChequera);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    /** Menor y mayor numero ya emitido de la chequera, o null si todavia no emitio ninguno. */
    public long[] rangoEmitido(Long idChequera) throws SQLException {
        String sql = "SELECT MIN(chq_numero), MAX(chq_numero) FROM cheque WHERE id_chequera = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idChequera);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                long minimo = rs.getLong(1);
                if (rs.wasNull()) {
                    return null;
                }
                return new long[]{minimo, rs.getLong(2)};
            }
        }
    }

    /**
     * Hay otra chequera de la misma cuenta cuyo rango se pisa con el que se quiere guardar.
     * Dos rangos solapados sobre la misma cuenta emiten dos cheques con el mismo numero, porque
     * el proximo numero se calcula por chequera y no por cuenta.
     */
    public boolean haySolapamiento(Long idCuenta, Long desde, Long hasta, Long idChequeraExcluir) throws SQLException {
        String sql = "SELECT COUNT(*) FROM chequera WHERE id_cuenta = ? "
                   + "AND chequera_desde_nro <= ? AND chequera_hasta_nro >= ?";
        if (idChequeraExcluir != null) {
            sql += " AND id_chequera <> ?";
        }
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idCuenta);
            stmt.setLong(2, hasta);
            stmt.setLong(3, desde);
            if (idChequeraExcluir != null) {
                stmt.setLong(4, idChequeraExcluir);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getLong(1) > 0;
            }
        }
    }

    /**
     * Proximo numero de cheque disponible de la chequera: MAX(chq_numero) ya emitido + 1,
     * arrancando en chequera_desde_nro. Valida que no supere chequera_hasta_nro (chequera agotada).
     */
    public long proximoNumeroCheque(Long idChequera) throws SQLException {
        Chequera ch = getChequera(idChequera);
        if (ch == null) {
            throw new SQLException("proximoNumeroCheque: la chequera " + idChequera + " no existe.");
        }
        long base = ch.getDesdeNumero() - 1;
        String sql = "SELECT COALESCE(MAX(chq_numero), ?) FROM cheque WHERE id_chequera = ?";
        long maxUsado;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, base);
            stmt.setLong(2, idChequera);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                maxUsado = rs.getLong(1);
            }
        }
        long proximo = maxUsado + 1;
        if (proximo > ch.getHastaNumero()) {
            throw new SQLException("Chequera " + idChequera + " agotada (rango "
                    + ch.getDesdeNumero() + "-" + ch.getHastaNumero() + "). Cargá una nueva chequera.");
        }
        return proximo;
    }
}
