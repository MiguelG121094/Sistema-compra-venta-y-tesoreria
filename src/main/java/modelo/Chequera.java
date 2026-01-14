/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Miguel
 */
public class Chequera {

    private Long idChequera;
    private Cuenta cuenta;
    private Long serie;
    private Long desdeNumero;
    private Long hastaNumero;

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
}
