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
public class LibroIvaCompra {

    private Long idLibroIvaCompra;
    private FacturaCompra facturaCompra;
    private Date fecha;
    private Long iva5;
    private Long iva10;

    public LibroIvaCompra() {
    }

    public LibroIvaCompra(Long idLibroIvaCompra, FacturaCompra facturaCompra) {
        this.idLibroIvaCompra = idLibroIvaCompra;
        this.facturaCompra = facturaCompra;
    }

    public LibroIvaCompra(Long idLibroIvaCompra, FacturaCompra facturaCompra, Date fecha,
            Long iva5, Long iva10) {
        this.idLibroIvaCompra = idLibroIvaCompra;
        this.facturaCompra = facturaCompra;
        this.fecha = fecha;
        this.iva5 = iva5;
        this.iva10 = iva10;
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
}
