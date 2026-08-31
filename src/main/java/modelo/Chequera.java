/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Miguel
 */
public class Chequera implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private Long idChequera;
    private Cuenta cuenta;
    private Long serie;
    private Long desdeNumero;
    private Long hastaNumero;

    // Se calculan para la grilla del ABM de chequeras y no se persisten: cuantos cheques
    // se emitieron, cual es el proximo numero libre y cuantos quedan hasta agotar el rango.
    private Long emitidos;
    private Long proximoNumero;
    private Long disponibles;

    public Chequera() {
    }

    public Chequera(Long idChequera) {
        this.idChequera = idChequera;
    }

    public Chequera(Long idChequera, Cuenta cuenta, Long serie, Long desdeNumero, Long hastaNumero) {
        this.idChequera = idChequera;
        this.cuenta = cuenta;
        this.serie = serie;
        this.desdeNumero = desdeNumero;
        this.hastaNumero = hastaNumero;
    }

    public Long getIdChequera() {
        return idChequera;
    }

    public void setIdChequera(Long idChequera) {
        this.idChequera = idChequera;
    }

    public Cuenta getCuenta() {
        return cuenta;
    }

    public void setCuenta(Cuenta cuenta) {
        this.cuenta = cuenta;
    }

    public Long getSerie() {
        return serie;
    }

    public void setSerie(Long serie) {
        this.serie = serie;
    }

    public Long getDesdeNumero() {
        return desdeNumero;
    }

    public void setDesdeNumero(Long desdeNumero) {
        this.desdeNumero = desdeNumero;
    }

    public Long getHastaNumero() {
        return hastaNumero;
    }

    public void setHastaNumero(Long hastaNumero) {
        this.hastaNumero = hastaNumero;
    }

    public Long getEmitidos() {
        return emitidos;
    }

    public void setEmitidos(Long emitidos) {
        this.emitidos = emitidos;
    }

    public Long getProximoNumero() {
        return proximoNumero;
    }

    public void setProximoNumero(Long proximoNumero) {
        this.proximoNumero = proximoNumero;
    }

    public Long getDisponibles() {
        return disponibles;
    }

    public void setDisponibles(Long disponibles) {
        this.disponibles = disponibles;
    }
}
