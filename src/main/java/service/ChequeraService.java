package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.Chequera;
import modelo.ChequeraDAO;

/**
 * Service de chequeras — combo de la línea de cheque en la Orden de Pago. La emisión del cheque
 * real (que consume el próximo número del rango) la hace OrdenPagoService dentro de su transacción.
 *
 * @author Miguel
 */
public class ChequeraService {

    public List<Chequera> listarChequeras() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return new ChequeraDAO(conn).listarChequeras();
        } catch (SQLException e) {
            System.out.println("Error en ChequeraService: " + e);
            return null;
        }
    }

    public Chequera getChequera(Long idChequera) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return new ChequeraDAO(conn).getChequera(idChequera);
        } catch (SQLException e) {
            System.out.println("Error en ChequeraService: " + e);
            return null;
        }
    }
}
