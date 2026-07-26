package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de la cabecera de forma de pago (el catálogo: cheque / transferencia). Se usa para el
 * combo "Tipo" del carrito de formas de pago de la Orden de Pago. En la OP no se paga con
 * efectivo (para eso está el fondo fijo), por eso el catálogo solo trae cheque y transferencia.
 *
 * @author Miguel
 */
public class FormaPagoCabeceraDAO {

    private Connection conn;

    public FormaPagoCabeceraDAO(Connection conn) {
        this.conn = conn;
    }

    public List<FormaPagoCabecera> listarFormaPago() throws SQLException {
        List<FormaPagoCabecera> lista = new ArrayList<>();
        String sql = "SELECT id_forma_pago_cab, forma_pago_descripcion FROM public.forma_pago_cabecera "
                   + "ORDER BY id_forma_pago_cab";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(new FormaPagoCabecera(rs.getLong("id_forma_pago_cab"),
                        rs.getString("forma_pago_descripcion")));
            }
        }
        return lista;
    }

    public FormaPagoCabecera getFormaPago(Long idFormaPagoCabecera) throws SQLException {
        if (idFormaPagoCabecera == null) {
            return null;
        }
        String sql = "SELECT id_forma_pago_cab, forma_pago_descripcion FROM public.forma_pago_cabecera "
                   + "WHERE id_forma_pago_cab = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idFormaPagoCabecera);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new FormaPagoCabecera(rs.getLong("id_forma_pago_cab"),
                            rs.getString("forma_pago_descripcion"));
                }
            }
        }
        return null;
    }
}
