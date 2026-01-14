/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Miguel
 */
public class Sucursal {

    private Long idSucursal;
    private String descripcion;
    private String direccion;
    private String estado;

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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    

}
