/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.util.Date;

/**
 *
 * @author Miguel
 */
public class LibroIvaCompra implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private Long idLibroIvaCompra;
    private FacturaCompra facturaCompra;
    private Date fecha;
    private Long iva5;
    private Long iva10;
    private Long gravada5;
    private Long gravada10;
    private Long exenta;
    private Long total;
    private String estado;

    // Datos del comprobante que origino la fila, para el informe de Libro de Compras. La fila solo
    // guarda las FKs: el numero, el timbrado y el proveedor viven en la cabecera de la factura o de
    // la nota, por eso se hidratan al listar y no se persisten aca.
    private String origen;
    private String numeroComprobante;
    private Long timbrado;
    private Proveedor proveedor;

    public LibroIvaCompra() {
    }

    public LibroIvaCompra(Long idLibroIvaCompra, FacturaCompra facturaCompra) {
        this.idLibroIvaCompra = idLibroIvaCompra;
        this.facturaCompra = facturaCompra;
    }

    public LibroIvaCompra(Long idLibroIvaCompra, FacturaCompra facturaCompra, Date fecha,
            Long iva5, Long iva10, Long gravada5, Long gravada10, Long exenta, Long total) {
        this.idLibroIvaCompra = idLibroIvaCompra;
        this.facturaCompra = facturaCompra;
        this.fecha = fecha;
        this.iva5 = iva5;
        this.iva10 = iva10;
        this.gravada5 = gravada5;
        this.gravada10 = gravada10;
        this.exenta = exenta;
        this.total = total;
    }

    public Long getIdLibroIvaCompra() {
        return idLibroIvaCompra;
    }

    public void setIdLibroIvaCompra(Long idLibroIvaCompra) {
        this.idLibroIvaCompra = idLibroIvaCompra;
    }

    public FacturaCompra getFacturaCompra() {
        return facturaCompra;
    }

    public void setFacturaCompra(FacturaCompra facturaCompra) {
        this.facturaCompra = facturaCompra;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Long getIva5() {
        return iva5;
    }

    public void setIva5(Long iva5) {
        this.iva5 = iva5;
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

    public Long getGravada10() {
        return gravada10;
    }

    public void setGravada10(Long gravada10) {
        this.gravada10 = gravada10;
    }

    public Long getExenta() {
        return exenta;
    }

    public void setExenta(Long exenta) {
        this.exenta = exenta;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public String getNumeroComprobante() {
        return numeroComprobante;
    }

    public void setNumeroComprobante(String numeroComprobante) {
        this.numeroComprobante = numeroComprobante;
    }

    public Long getTimbrado() {
        return timbrado;
    }

    public void setTimbrado(Long timbrado) {
        this.timbrado = timbrado;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
