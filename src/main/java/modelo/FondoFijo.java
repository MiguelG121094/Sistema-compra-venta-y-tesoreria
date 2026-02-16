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
public class FondoFijo implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private Long idFondoFijo;
    private String responsable;
    private Long montoAsignado;
    private Date fechaAsignacion;
    private Proveedor proveedor;

    public FondoFijo() {
    }

    public FondoFijo(Long idFondoFijo) {
        this.idFondoFijo = idFondoFijo;
    }

    public FondoFijo(Long idFondoFijo, String responsable, Long montoAsignado,
            Date fechaAsignacion, Proveedor proveedor) {
        this.idFondoFijo = idFondoFijo;
        this.responsable = responsable;
        this.montoAsignado = montoAsignado;
        this.fechaAsignacion = fechaAsignacion;
        this.proveedor = proveedor;
    }

    public Long getIdFondoFijo() {
        return idFondoFijo;
    }

    public void setIdFondoFijo(Long idFondoFijo) {
        this.idFondoFijo = idFondoFijo;
    }

    public String getResponsable() {
        return responsable;
    }

    public void setResponsable(String responsable) {
        this.responsable = responsable;
    }

    public Long getMontoAsignado() {
        return montoAsignado;
    }

    public void setMontoAsignado(Long montoAsignado) {
        this.montoAsignado = montoAsignado;
    }

    public Date getFechaAsignacion() {
        return fechaAsignacion;
    }

    public void setFechaAsignacion(Date fechaAsignacion) {
        this.fechaAsignacion = fechaAsignacion;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }
}
