/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Miguel
 */
public class Caja {

    private Long idCaja;
    private String descripcion;
    private Integer numeroExpedicion;
    private String estado;
    private Sucursal sucursal;

    public Caja() {
    }

    public Caja(Long idCaja) {
        this.idCaja = idCaja;
    }

    public Caja(Long idCaja, String descripcion, Integer numeroExpedicion, String estado, Sucursal sucursal) {
        this.idCaja = idCaja;
        this.descripcion = descripcion;
        this.numeroExpedicion = numeroExpedicion;
        this.estado = estado;
        this.sucursal = sucursal;
    }

    public Long getIdCaja() {
        return idCaja;
    }

    public void setIdCaja(Long idCaja) {
        this.idCaja = idCaja;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getNumeroExpedicion() {
        return numeroExpedicion;
    }

    public void setNumeroExpedicion(Integer numeroExpedicion) {
        this.numeroExpedicion = numeroExpedicion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Sucursal getSucursal() {
        return sucursal;
    }

    public void setSucursal(Sucursal sucursal) {
        this.sucursal = sucursal;
    }
}
