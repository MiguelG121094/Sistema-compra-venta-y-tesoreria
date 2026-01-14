/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Miguel
 */
public class Deposito {

    private Long idDeposito;
    private String descripcion;
    private String estado;
    private Sucursal sucursal;

    public Deposito() {
    }

    public Deposito(Long idDeposito) {
        this.idDeposito = idDeposito;
    }

    public Deposito(Long idDeposito, String descripcion, String estado, Sucursal sucursal) {
        this.idDeposito = idDeposito;
        this.descripcion = descripcion;
        this.estado = estado;
        this.sucursal = sucursal;
    }

    public Long getIdDeposito() {
        return idDeposito;
    }

    public void setIdDeposito(Long idDeposito) {
        this.idDeposito = idDeposito;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
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
