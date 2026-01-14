/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Miguel
 */
public class Tarjeta {

    private Long idTarjeta;
    private TipoTarjeta tipoTarjeta;
    private EntidadFinanciera entidadFinanciera;
    private TipoCuenta tipoCuenta;

    public Tarjeta() {
    }

    public Tarjeta(Long idTarjeta) {
        this.idTarjeta = idTarjeta;
    }

    public Tarjeta(Long idTarjeta, TipoTarjeta tipoTarjeta, EntidadFinanciera entidadFinanciera,
            TipoCuenta tipoCuenta) {
        this.idTarjeta = idTarjeta;
        this.tipoTarjeta = tipoTarjeta;
        this.entidadFinanciera = entidadFinanciera;
        this.tipoCuenta = tipoCuenta;
    }

    public Long getIdTarjeta() {
        return idTarjeta;
    }

    public void setIdTarjeta(Long idTarjeta) {
        this.idTarjeta = idTarjeta;
    }

    public TipoTarjeta getTipoTarjeta() {
        return tipoTarjeta;
    }

    public void setTipoTarjeta(TipoTarjeta tipoTarjeta) {
        this.tipoTarjeta = tipoTarjeta;
    }

    public EntidadFinanciera getEntidadFinanciera() {
        return entidadFinanciera;
    }

    public void setEntidadFinanciera(EntidadFinanciera entidadFinanciera) {
        this.entidadFinanciera = entidadFinanciera;
    }

    public TipoCuenta getTipoCuenta() {
        return tipoCuenta;
    }

    public void setTipoCuenta(TipoCuenta tipoCuenta) {
        this.tipoCuenta = tipoCuenta;
    }
}
