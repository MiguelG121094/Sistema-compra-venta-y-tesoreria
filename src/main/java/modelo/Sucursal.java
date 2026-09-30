/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Miguel
 */
public class Sucursal implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private Long idSucursal;
    private String descripcion;
    private String direccion;
    private String estado;
    // Primer tramo del numero de comprobante paraguayo (001-002-0000123): identifica a la sucursal.
    // El segundo tramo es el punto de expedicion, que es de la caja (caja.caja_nro_expedicion).
    // Va como texto porque son 3 digitos con ceros a la izquierda.
    private String establecimiento;

    public Sucursal() {
    }

    public Sucursal(Long idSucursal) {
        this.idSucursal = idSucursal;
    }

    public Sucursal(Long idSucursal, String descripcion, String direccion, String estado) {
        this.idSucursal = idSucursal;
        this.descripcion = descripcion;
        this.direccion = direccion;
        this.estado = estado;
    }

    public Long getIdSucursal() {
        return idSucursal;
    }

    public void setIdSucursal(Long idSucursal) {
        this.idSucursal = idSucursal;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getEstablecimiento() {
        return establecimiento;
    }

    public void setEstablecimiento(String establecimiento) {
        this.establecimiento = establecimiento;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    

}
