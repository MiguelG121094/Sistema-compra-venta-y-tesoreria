package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de cuentas bancarias. Referencia entidad financiera, tipo de cuenta y moneda.
 *
 * @author Miguel
 */
public class CuentaDAO {

    private Connection conn;

    public CuentaDAO(Connection conn) {
        this.conn = conn;
    }

    public List<Cuenta> listarCuenta() throws SQLException {
        List<Cuenta> lista = new ArrayList<>();
        String sql = "SELECT id_cuenta, id_tipo_cuenta, id_enti_finan, cuenta_numero, id_moneda "
                   + "FROM public.cuenta ORDER BY id_cuenta";

        TipoCuentaDAO tipoCuentaDAO = new TipoCuentaDAO(conn);
        EntidadFinancieraDAO entidadDAO = new EntidadFinancieraDAO(conn);
        MonedaDAO monedaDAO = new MonedaDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Cuenta cuenta = new Cuenta();
                cuenta.setIdCuenta(rs.getLong("id_cuenta"));
                cuenta.setTipoCuenta(tipoCuentaDAO.getTipoCuenta(rs.getLong("id_tipo_cuenta")));
                cuenta.setEntidadFinanciera(entidadDAO.getEntidadFinanciera(rs.getLong("id_enti_finan")));
                cuenta.setNumero(rs.getLong("cuenta_numero"));
                cuenta.setMoneda(monedaDAO.getMoneda(rs.getLong("id_moneda")));
                lista.add(cuenta);
            }
        }
        return lista;
    }

    public Cuenta getCuenta(Long idCuenta) throws SQLException {
        if (idCuenta == null) {
            return null;
        }
        String sql = "SELECT id_cuenta, id_tipo_cuenta, id_enti_finan, cuenta_numero, id_moneda "
                   + "FROM public.cuenta WHERE id_cuenta = ?";

        TipoCuentaDAO tipoCuentaDAO = new TipoCuentaDAO(conn);
        EntidadFinancieraDAO entidadDAO = new EntidadFinancieraDAO(conn);
        MonedaDAO monedaDAO = new MonedaDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idCuenta);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Cuenta cuenta = new Cuenta();
                    cuenta.setIdCuenta(rs.getLong("id_cuenta"));
                    cuenta.setTipoCuenta(tipoCuentaDAO.getTipoCuenta(rs.getLong("id_tipo_cuenta")));
                    cuenta.setEntidadFinanciera(entidadDAO.getEntidadFinanciera(rs.getLong("id_enti_finan")));
                    cuenta.setNumero(rs.getLong("cuenta_numero"));
                    cuenta.setMoneda(monedaDAO.getMoneda(rs.getLong("id_moneda")));
                    return cuenta;
                }
            }
        }
        return null;
    }

    public void insertarCuenta(Cuenta cuenta) throws SQLException {
        String sql = "INSERT INTO public.cuenta (id_tipo_cuenta, id_enti_finan, cuenta_numero, id_moneda) "
                   + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, cuenta.getTipoCuenta().getIdTipoCuenta());
            stmt.setLong(2, cuenta.getEntidadFinanciera().getIdEntidadFinanciera());
            stmt.setLong(3, cuenta.getNumero());
            stmt.setLong(4, cuenta.getMoneda().getIdMoneda());
            stmt.executeUpdate();
        }
    }

    public void actualizarCuenta(Cuenta cuenta) throws SQLException {
        String sql = "UPDATE public.cuenta SET id_tipo_cuenta = ?, id_enti_finan = ?, "
                   + "cuenta_numero = ?, id_moneda = ? WHERE id_cuenta = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, cuenta.getTipoCuenta().getIdTipoCuenta());
            stmt.setLong(2, cuenta.getEntidadFinanciera().getIdEntidadFinanciera());
            stmt.setLong(3, cuenta.getNumero());
            stmt.setLong(4, cuenta.getMoneda().getIdMoneda());
            stmt.setLong(5, cuenta.getIdCuenta());
            stmt.executeUpdate();
        }
    }

    public void eliminarCuenta(Long idCuenta) throws SQLException {
        String sql = "DELETE FROM public.cuenta WHERE id_cuenta = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idCuenta);
            stmt.executeUpdate();
        }
    }
}
