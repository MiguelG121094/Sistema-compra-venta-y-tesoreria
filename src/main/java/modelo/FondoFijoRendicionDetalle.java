/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 * Linea de una rendicion de fondo fijo: una factura de fondo fijo rendida.
 *
 * <p>Apunta a la cuenta a pagar y no a la factura, porque la tabla referencia la clave compuesta
 * {@code (id_cta_pagar, id_fact_comp_cab)}, igual que el detalle de la provision.
 *
 * @author Miguel
 */
public class FondoFijoRendicionDetalle implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private Long idFondoFijoRendicionDetalle;
    private FondoFijoRendicion fondoFijoRendicion;
    private CuentaPagar cuentaPagar;
    private Long montoRendido;

    public FondoFijoRendicionDetalle() {
    }

    public FondoFijoRendicionDetalle(Long idFondoFijoRendicionDetalle) {
        this.idFondoFijoRendicionDetalle = idFondoFijoRendicionDetalle;
    }

    public FondoFijoRendicionDetalle(Long idFondoFijoRendicionDetalle,
            FondoFijoRendicion fondoFijoRendicion, CuentaPagar cuentaPagar, Long montoRendido) {
        this.idFondoFijoRendicionDetalle = idFondoFijoRendicionDetalle;
        this.fondoFijoRendicion = fondoFijoRendicion;
        this.cuentaPagar = cuentaPagar;
        this.montoRendido = montoRendido;
    }

    public Long getIdFondoFijoRendicionDetalle() {
        return idFondoFijoRendicionDetalle;
    }

    public void setIdFondoFijoRendicionDetalle(Long idFondoFijoRendicionDetalle) {
        this.idFondoFijoRendicionDetalle = idFondoFijoRendicionDetalle;
    }

    public FondoFijoRendicion getFondoFijoRendicion() {
        return fondoFijoRendicion;
    }

    public void setFondoFijoRendicion(FondoFijoRendicion fondoFijoRendicion) {
        this.fondoFijoRendicion = fondoFijoRendicion;
    }

    public CuentaPagar getCuentaPagar() {
        return cuentaPagar;
    }

    public void setCuentaPagar(CuentaPagar cuentaPagar) {
        this.cuentaPagar = cuentaPagar;
    }

    public Long getMontoRendido() {
        return montoRendido;
    }

    public void setMontoRendido(Long montoRendido) {
        this.montoRendido = montoRendido;
    }
}
