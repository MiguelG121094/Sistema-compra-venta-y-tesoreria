/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Miguel
 */
public class FormaCobroDetalle {

    private FormaCobro formaCobro;
    private Cobro cobro;
    private Long efectivo;
    private Long cheque;
    private Long tarjeta;
    private Long total;

    public FormaCobroDetalle() {
    }

    public FormaCobroDetalle(FormaCobro formaCobro, Cobro cobro) {
        this.formaCobro = formaCobro;
        this.cobro = cobro;
    }

    public FormaCobroDetalle(FormaCobro formaCobro, Cobro cobro, Long efectivo,
            Long cheque, Long tarjeta, Long total) {
        this.formaCobro = formaCobro;
        this.cobro = cobro;
        this.efectivo = efectivo;
        this.cheque = cheque;
        this.tarjeta = tarjeta;
        this.total = total;
    }

    public FormaCobro getFormaCobro() {
        return formaCobro;
    }

    public void setFormaCobro(FormaCobro formaCobro) {
        this.formaCobro = formaCobro;
    }

    public Cobro getCobro() {
        return cobro;
    }

    public void setCobro(Cobro cobro) {
        this.cobro = cobro;
    }

    public Long getEfectivo() {
        return efectivo;
    }

    public void setEfectivo(Long efectivo) {
        this.efectivo = efectivo;
    }

    public Long getCheque() {
        return cheque;
    }

    public void setCheque(Long cheque) {
        this.cheque = cheque;
    }

    public Long getTarjeta() {
        return tarjeta;
    }

    public void setTarjeta(Long tarjeta) {
        this.tarjeta = tarjeta;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }
}
