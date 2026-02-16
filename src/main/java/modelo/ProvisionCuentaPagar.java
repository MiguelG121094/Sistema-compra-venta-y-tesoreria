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
public class ProvisionCuentaPagar implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private Long idProvisionCuentaPagar;
    private String estado;
    private Date fecha;
    private Proveedor proveedor;

    public ProvisionCuentaPagar() {
    }

    public ProvisionCuentaPagar(Long idProvisionCuentaPagar) {
        this.idProvisionCuentaPagar = idProvisionCuentaPagar;
    }

    public ProvisionCuentaPagar(Long idProvisionCuentaPagar, String estado, Date fecha,
            Proveedor proveedor) {
        this.idProvisionCuentaPagar = idProvisionCuentaPagar;
        this.estado = estado;
        this.fecha = fecha;
        this.proveedor = proveedor;
    }

    public Long getIdProvisionCuentaPagar() {
        return idProvisionCuentaPagar;
    }

    public void setIdProvisionCuentaPagar(Long idProvisionCuentaPagar) {
        this.idProvisionCuentaPagar = idProvisionCuentaPagar;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }
}
