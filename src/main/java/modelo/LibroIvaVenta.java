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
public class LibroIvaVenta implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private Long idLibroIvaVenta;
    private FacturaVenta facturaVenta;
    private Date fecha;
    private Long iva5;
    private Long iva10;

    public LibroIvaVenta() {
    }

    public LibroIvaVenta(Long idLibroIvaVenta, FacturaVenta facturaVenta) {
        this.idLibroIvaVenta = idLibroIvaVenta;
        this.facturaVenta = facturaVenta;
    }

    public LibroIvaVenta(Long idLibroIvaVenta, FacturaVenta facturaVenta, Date fecha,
            Long iva5, Long iva10) {
        this.idLibroIvaVenta = idLibroIvaVenta;
        this.facturaVenta = facturaVenta;
        this.fecha = fecha;
        this.iva5 = iva5;
        this.iva10 = iva10;
    }

    public Long getIdLibroIvaVenta() {
        return idLibroIvaVenta;
    }

    public void setIdLibroIvaVenta(Long idLibroIvaVenta) {
        this.idLibroIvaVenta = idLibroIvaVenta;
    }

    public FacturaVenta getFacturaVenta() {
        return facturaVenta;
    }

    public void setFacturaVenta(FacturaVenta facturaVenta) {
        this.facturaVenta = facturaVenta;
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
