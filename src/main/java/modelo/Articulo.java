/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Miguel
 */
public class Articulo {

    private Long idArticulo;
    private TipoArticulo tipoArticulo;
    private Marca marca;
    private TipoImpuesto tipoImpuesto;
    private Presentacion presentacion;
    private String descripcion;
    private Long precioCompra;
    private Long precioVenta;
    private String estado;

    public Articulo() {
    }

    public Articulo(Long idArticulo) {
        this.idArticulo = idArticulo;
    }

    public Articulo(Long idArticulo, TipoArticulo tipoArticulo, Marca marca, TipoImpuesto tipoImpuesto, Presentacion presentacion, String descripcion, Long precioCompra, Long precioVenta, String estado) {
        this.idArticulo = idArticulo;
        this.tipoArticulo = tipoArticulo;
        this.marca = marca;
        this.tipoImpuesto = tipoImpuesto;
        this.presentacion = presentacion;
        this.descripcion = descripcion;
        this.precioCompra = precioCompra;
        this.precioVenta = precioVenta;
        this.estado = estado;
    }

    public Long getIdArticulo() {
        return idArticulo;
    }

    public void setIdArticulo(Long idArticulo) {
        this.idArticulo = idArticulo;
    }

    public TipoArticulo getTipoArticulo() {
        return tipoArticulo;
    }

    public void setTipoArticulo(TipoArticulo tipoArticulo) {
        this.tipoArticulo = tipoArticulo;
    }

    public Marca getMarca() {
        return marca;
    }

    public void setMarca(Marca marca) {
        this.marca = marca;
    }

    public TipoImpuesto getTipoImpuesto() {
        return tipoImpuesto;
    }

    public void setTipoImpuesto(TipoImpuesto tipoImpuesto) {
        this.tipoImpuesto = tipoImpuesto;
    }

    public Presentacion getPresentacion() {
        return presentacion;
    }

    public void setPresentacion(Presentacion presentacion) {
        this.presentacion = presentacion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Long getPrecioCompra() {
        return precioCompra;
    }

    public void setPrecioCompra(Long precioCompra) {
        this.precioCompra = precioCompra;
    }

    public Long getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(Long precioVenta) {
        this.precioVenta = precioVenta;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    

}
