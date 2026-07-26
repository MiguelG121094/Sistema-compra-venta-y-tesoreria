package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.TipoCheque;
import modelo.TipoChequeDAO;

/**
 * Service del catálogo de tipos de cheque — combo de la línea de cheque en la Orden de Pago.
 *
 * @author Miguel
 */
public class TipoChequeService {

    public List<TipoCheque> listarTipoCheque() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return new TipoChequeDAO(conn).listarTipoCheque();
        } catch (SQLException e) {
            System.out.println("Error en TipoChequeService: " + e);
            return null;
        }
    }

    public TipoCheque getTipoCheque(Long idTipoCheque) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return new TipoChequeDAO(conn).getTipoCheque(idTipoCheque);
        } catch (SQLException e) {
            System.out.println("Error en TipoChequeService: " + e);
            return null;
        }
    }
}
