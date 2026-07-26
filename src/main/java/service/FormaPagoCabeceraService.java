package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.FormaPagoCabecera;
import modelo.FormaPagoCabeceraDAO;

/**
 * Service del catálogo de formas de pago (cheque / transferencia) — combos de la Orden de Pago.
 *
 * @author Miguel
 */
public class FormaPagoCabeceraService {

    public List<FormaPagoCabecera> listarFormaPago() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return new FormaPagoCabeceraDAO(conn).listarFormaPago();
        } catch (SQLException e) {
            System.out.println("Error en FormaPagoCabeceraService: " + e);
            return null;
        }
    }

    public FormaPagoCabecera getFormaPago(Long idFormaPagoCabecera) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return new FormaPagoCabeceraDAO(conn).getFormaPago(idFormaPagoCabecera);
        } catch (SQLException e) {
            System.out.println("Error en FormaPagoCabeceraService: " + e);
            return null;
        }
    }
}
