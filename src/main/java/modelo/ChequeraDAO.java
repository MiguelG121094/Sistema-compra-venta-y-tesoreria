package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de chequera. Además de leerla, calcula el próximo número de cheque disponible
 * dentro del rango de la chequera. Corre sobre la Connection compartida (la transacción
 * la controla el Service). Ver MODULO_TESORERIA_PLAN.md §C.
 *
 * @author Miguel
 */
public class ChequeraDAO {

    private Connection conn;

    public ChequeraDAO(Connection conn) {
        this.conn = conn;
    }

    private Chequera mapear(ResultSet rs) throws SQLException {
        Chequera ch = new Chequera();
        ch.setIdChequera(rs.getLong("id_chequera"));
        ch.setCuenta(new Cuenta(rs.getLong("id_cuenta")));
        ch.setSerie(rs.getLong("chequera_serie"));
        ch.setDesdeNumero(rs.getLong("chequera_desde_nro"));
        ch.setHastaNumero(rs.getLong("chequera_hasta_nro"));
        return ch;
    }

    public Chequera getChequera(Long idChequera) throws SQLException {
        if (idChequera == null) {
            return null;
        }
        String sql = "SELECT id_chequera, id_cuenta, chequera_serie, chequera_desde_nro, chequera_hasta_nro "
                   + "FROM chequera WHERE id_chequera = ?";
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

    public List<Chequera> listarChequeras() throws SQLException {
        List<Chequera> lista = new ArrayList<>();
        String sql = "SELECT id_chequera, id_cuenta, chequera_serie, chequera_desde_nro, chequera_hasta_nro "
                   + "FROM chequera ORDER BY id_chequera";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    /**
     * Próximo número de cheque disponible de la chequera: MAX(chq_numero) ya emitido + 1,
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
