/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Miguel
 */
public class FondoFijoRendicionDetalle implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private Long idFondoFijoRendicionDetalle;
    private FondoFijoRendicion fondoFijoRendicion;
    private FacturaCompra facturaCompra;

    public FondoFijoRendicionDetalle() {
    }

    public FondoFijoRendicionDetalle(Long idFondoFijoRendicionDetalle) {
        this.idFondoFijoRendicionDetalle = idFondoFijoRendicionDetalle;
    }

    public FondoFijoRendicionDetalle(Long idFondoFijoRendicionDetalle,
            FondoFijoRendicion fondoFijoRendicion, FacturaCompra facturaCompra) {
        this.idFondoFijoRendicionDetalle = idFondoFijoRendicionDetalle;
        this.fondoFijoRendicion = fondoFijoRendicion;
        this.facturaCompra = facturaCompra;
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

    public FacturaCompra getFacturaCompra() {
        return facturaCompra;
    }

    public void setFacturaCompra(FacturaCompra facturaCompra) {
        this.facturaCompra = facturaCompra;
    }
}
