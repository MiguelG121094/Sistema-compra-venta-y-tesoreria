/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Miguel
 */
public class CobroCheque {

    private Long idCobroCheque;
    private Cobro cobro;
    private ChequeRecibido chequeRecibido;

    public CobroCheque() {
    }

    public CobroCheque(Long idCobroCheque, Cobro cobro) {
        this.idCobroCheque = idCobroCheque;
        this.cobro = cobro;
    }

    public CobroCheque(Long idCobroCheque, Cobro cobro, ChequeRecibido chequeRecibido) {
        this.idCobroCheque = idCobroCheque;
        this.cobro = cobro;
        this.chequeRecibido = chequeRecibido;
    }

    public Long getIdCobroCheque() {
        return idCobroCheque;
    }

    public void setIdCobroCheque(Long idCobroCheque) {
        this.idCobroCheque = idCobroCheque;
    }

    public Cobro getCobro() {
        return cobro;
    }

    public void setCobro(Cobro cobro) {
        this.cobro = cobro;
    }

    public ChequeRecibido getChequeRecibido() {
        return chequeRecibido;
    }

    public void setChequeRecibido(ChequeRecibido chequeRecibido) {
        this.chequeRecibido = chequeRecibido;
    }
}
