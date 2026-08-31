package modelo;

import java.util.Date;

/**
 * Lo que un debito y un credito tienen en comun. Existe para que una sola vista pueda mostrar los
 * dos: son el mismo formulario y la unica diferencia es el signo del movimiento y la tabla donde
 * cae. Evita que la JSP tenga que preguntar si esta mirando un Debito o un Credito para saber que
 * getter llamar.
 *
 * @author Miguel
 */
public interface MovimientoBancario extends java.io.Serializable {

    Long getId();

    Long getNumeroComprobante();

    Date getFecha();

    String getDetalle();

    Cuenta getCuenta();

    Long getMonto();

    Double getTipoCambio();

    String getEstado();
}
