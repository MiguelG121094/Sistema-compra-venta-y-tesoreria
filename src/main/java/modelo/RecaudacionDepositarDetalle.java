/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Miguel
 */
public class RecaudacionDepositarDetalle implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private Long idRecaudacionDepositarDetalle;
    private RecaudacionDepositar recaudacionDepositar;
    private Long monto;

    public RecaudacionDepositarDetalle() {
    }

    public RecaudacionDepositarDetalle(Long idRecaudacionDepositarDetalle) {
        this.idRecaudacionDepositarDetalle = idRecaudacionDepositarDetalle;
    }

    public RecaudacionDepositarDetalle(Long idRecaudacionDepositarDetalle,
            RecaudacionDepositar recaudacionDepositar, Long monto) {
        this.idRecaudacionDepositarDetalle = idRecaudacionDepositarDetalle;
        this.recaudacionDepositar = recaudacionDepositar;
        this.monto = monto;
    }

    public Long getIdRecaudacionDepositarDetalle() {
        return idRecaudacionDepositarDetalle;
    }

    public void setIdRecaudacionDepositarDetalle(Long idRecaudacionDepositarDetalle) {
        this.idRecaudacionDepositarDetalle = idRecaudacionDepositarDetalle;
    }

    public RecaudacionDepositar getRecaudacionDepositar() {
        return recaudacionDepositar;
    }

    public void setRecaudacionDepositar(RecaudacionDepositar recaudacionDepositar) {
        this.recaudacionDepositar = recaudacionDepositar;
    }

    public Long getMonto() {
        return monto;
    }

    public void setMonto(Long monto) {
        this.monto = monto;
    }
}
