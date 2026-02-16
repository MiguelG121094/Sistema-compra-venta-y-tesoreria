/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Miguel
 */
public class NotaDebitoVentaDetalle implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private NotaDebitoVenta notaDebitoVenta;
    private Articulo articulo;
    private FacturaVenta facturaVenta;
    private Long monto;

    public NotaDebitoVentaDetalle() {
    }

    public NotaDebitoVentaDetalle(NotaDebitoVenta notaDebitoVenta, Articulo articulo,
            FacturaVenta facturaVenta, Long monto) {
        this.notaDebitoVenta = notaDebitoVenta;
        this.articulo = articulo;
        this.facturaVenta = facturaVenta;
        this.monto = monto;
    }

    public NotaDebitoVenta getNotaDebitoVenta() {
        return notaDebitoVenta;
    }

    public void setNotaDebitoVenta(NotaDebitoVenta notaDebitoVenta) {
        this.notaDebitoVenta = notaDebitoVenta;
    }

    public Articulo getArticulo() {
        return articulo;
    }

    public void setArticulo(Articulo articulo) {
        this.articulo = articulo;
    }

    public FacturaVenta getFacturaVenta() {
        return facturaVenta;
    }

    public void setFacturaVenta(FacturaVenta facturaVenta) {
        this.facturaVenta = facturaVenta;
    }

    public Long getMonto() {
        return monto;
    }

    public void setMonto(Long monto) {
        this.monto = monto;
    }
}
