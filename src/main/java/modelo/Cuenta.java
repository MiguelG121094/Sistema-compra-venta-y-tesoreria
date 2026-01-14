/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Miguel
 */
public class Cuenta {

    private Long idCuenta;
    private TipoCuenta tipoCuenta;
    private EntidadFinanciera entidadFinanciera;
    private Long numero;
    private Moneda moneda;

    public Cuenta() {
    }

    public Cuenta(Long idCuenta) {
        this.idCuenta = idCuenta;
    }

    public Cuenta(Long idCuenta, TipoCuenta tipoCuenta, EntidadFinanciera entidadFinanciera,
            Long numero, Moneda moneda) {
        this.idCuenta = idCuenta;
        this.tipoCuenta = tipoCuenta;
        this.entidadFinanciera = entidadFinanciera;
        this.numero = numero;
        this.moneda = moneda;
    }

    public Long getIdCuenta() {
        return idCuenta;
    }

    public void setIdCuenta(Long idCuenta) {
        this.idCuenta = idCuenta;
    }

    public TipoCuenta getTipoCuenta() {
        return tipoCuenta;
    }

    public void setTipoCuenta(TipoCuenta tipoCuenta) {
        this.tipoCuenta = tipoCuenta;
    }

    public EntidadFinanciera getEntidadFinanciera() {
        return entidadFinanciera;
    }

    public void setEntidadFinanciera(EntidadFinanciera entidadFinanciera) {
        this.entidadFinanciera = entidadFinanciera;
    }

    public Long getNumero() {
        return numero;
    }

    public void setNumero(Long numero) {
        this.numero = numero;
    }

    public Moneda getMoneda() {
        return moneda;
    }

    public void setMoneda(Moneda moneda) {
        this.moneda = moneda;
    }
}
