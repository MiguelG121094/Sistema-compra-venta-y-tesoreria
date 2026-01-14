/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Miguel
 */
public class ProvisionCuentaPagarDetalle {

    private Long idProvisionCuentaPagarDetalle;
    private ProvisionCuentaPagar provisionCuentaPagar;
    private CuentaPagar cuentaPagar;
    private Long monto;

    public ProvisionCuentaPagarDetalle() {
    }

    public ProvisionCuentaPagarDetalle(Long idProvisionCuentaPagarDetalle) {
        this.idProvisionCuentaPagarDetalle = idProvisionCuentaPagarDetalle;
    }

    public ProvisionCuentaPagarDetalle(Long idProvisionCuentaPagarDetalle,
            ProvisionCuentaPagar provisionCuentaPagar, CuentaPagar cuentaPagar, Long monto) {
        this.idProvisionCuentaPagarDetalle = idProvisionCuentaPagarDetalle;
        this.provisionCuentaPagar = provisionCuentaPagar;
        this.cuentaPagar = cuentaPagar;
        this.monto = monto;
    }

    public Long getIdProvisionCuentaPagarDetalle() {
        return idProvisionCuentaPagarDetalle;
    }

    public void setIdProvisionCuentaPagarDetalle(Long idProvisionCuentaPagarDetalle) {
        this.idProvisionCuentaPagarDetalle = idProvisionCuentaPagarDetalle;
    }

    public ProvisionCuentaPagar getProvisionCuentaPagar() {
        return provisionCuentaPagar;
    }

    public void setProvisionCuentaPagar(ProvisionCuentaPagar provisionCuentaPagar) {
        this.provisionCuentaPagar = provisionCuentaPagar;
    }

    public CuentaPagar getCuentaPagar() {
        return cuentaPagar;
    }

    public void setCuentaPagar(CuentaPagar cuentaPagar) {
        this.cuentaPagar = cuentaPagar;
    }

    public Long getMonto() {
        return monto;
    }

    public void setMonto(Long monto) {
        this.monto = monto;
    }
}
