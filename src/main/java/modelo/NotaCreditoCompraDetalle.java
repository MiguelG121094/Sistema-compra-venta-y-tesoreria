/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Miguel
 */
public class NotaCreditoCompraDetalle implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;                     // id_nota_credito_det
    private NotaCreditoCompra notaCreditoCompra;
    private Articulo articulo;           // nullable: línea sin artículo = NC financiera (descuento)
    private Long cantidad;
    private Long monto;                  // nota_cred_monto: importe UNITARIO de la línea
    private String descripcion;          // nota_credito_descripcion
    private TipoImpuesto tipoImpuesto;   // id_impuesto
    private Deposito deposito;           // id_deposito: seteado (con artículo) => mueve stock por trigger

    // Campos calculados (no persistidos en BD)
    private Long subtotal;
    private Long gravada10;
    private Long iva10;
    private Long gravada5;
    private Long iva5;
    private Long exenta;

    public NotaCreditoCompraDetalle() {
    }

    public NotaCreditoCompraDetalle(NotaCreditoCompra notaCreditoCompra, Articulo articulo, Long cantidad, Long monto) {
        this.notaCreditoCompra = notaCreditoCompra;
        this.articulo = articulo;
        this.cantidad = cantidad;
        this.monto = monto;
    }

    public NotaCreditoCompraDetalle(NotaCreditoCompra notaCreditoCompra, Articulo articulo, Long cantidad,
            Long monto, String descripcion, TipoImpuesto tipoImpuesto, Deposito deposito) {
        this.notaCreditoCompra = notaCreditoCompra;
        this.articulo = articulo;
        this.cantidad = cantidad;
        this.monto = monto;
        this.descripcion = descripcion;
        this.tipoImpuesto = tipoImpuesto;
        this.deposito = deposito;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public NotaCreditoCompra getNotaCreditoCompra() {
        return notaCreditoCompra;
    }

    public void setNotaCreditoCompra(NotaCreditoCompra notaCreditoCompra) {
        this.notaCreditoCompra = notaCreditoCompra;
    }

    public Articulo getArticulo() {
        return articulo;
    }

    public void setArticulo(Articulo articulo) {
        this.articulo = articulo;
    }

    public Long getCantidad() {
        return cantidad;
    }

    public void setCantidad(Long cantidad) {
        this.cantidad = cantidad;
    }

    public Long getMonto() {
        return monto;
    }

    public void setMonto(Long monto) {
        this.monto = monto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public TipoImpuesto getTipoImpuesto() {
        return tipoImpuesto;
    }

    public void setTipoImpuesto(TipoImpuesto tipoImpuesto) {
        this.tipoImpuesto = tipoImpuesto;
    }

    public Deposito getDeposito() {
        return deposito;
    }

    public void setDeposito(Deposito deposito) {
        this.deposito = deposito;
    }

    /**
     * Devuelve la descripción del artículo si existe, o la descripción directa del detalle.
     */
    public String getDescripcionDisplay() {
        if (articulo != null && articulo.getDescripcion() != null) {
            return articulo.getDescripcion();
        }
        return descripcion;
    }

    public Long getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Long subtotal) {
        this.subtotal = subtotal;
    }

    public Long getGravada10() {
        return gravada10;
    }

    public void setGravada10(Long gravada10) {
        this.gravada10 = gravada10;
    }

    public Long getIva10() {
        return iva10;
    }

    public void setIva10(Long iva10) {
        this.iva10 = iva10;
    }

    public Long getGravada5() {
        return gravada5;
    }

    public void setGravada5(Long gravada5) {
        this.gravada5 = gravada5;
    }

    public Long getIva5() {
        return iva5;
    }

    public void setIva5(Long iva5) {
        this.iva5 = iva5;
    }

    public Long getExenta() {
        return exenta;
    }

    public void setExenta(Long exenta) {
        this.exenta = exenta;
    }
}
