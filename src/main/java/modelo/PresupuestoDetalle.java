/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Miguel
 */
public class PresupuestoDetalle {
    private Presupuesto presupuesto;
    private Articulo articulo;
    private Long cantidad;
    private Long precioCompra;

    public PresupuestoDetalle() {
    }

    public PresupuestoDetalle(Presupuesto presupuesto, Articulo articulo, Long cantidad, Long precioCompra) {
        this.presupuesto = presupuesto;
        this.articulo = articulo;
        this.cantidad = cantidad;
        this.precioCompra = precioCompra;
    }

    public Presupuesto getPresupuesto() {
        return presupuesto;
    }

    public void setPresupuesto(Presupuesto presupuesto) {
        this.presupuesto = presupuesto;
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

    public Long getPrecioCompra() {
        return precioCompra;
    }

    public void setPrecioCompra(Long precioCompra) {
        this.precioCompra = precioCompra;
    }
}
