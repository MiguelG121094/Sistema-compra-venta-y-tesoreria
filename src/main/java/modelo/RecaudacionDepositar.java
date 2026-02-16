/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.util.Date;

/**
 *
 * @author Miguel
 */
public class RecaudacionDepositar implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private Long idRecaudacionDepositar;
    private Date fecha;
    private String estado;
    private String referencia;
    private Cuenta cuentaDestino;

    public RecaudacionDepositar() {
    }

    public RecaudacionDepositar(Long idRecaudacionDepositar) {
        this.idRecaudacionDepositar = idRecaudacionDepositar;
    }

    public RecaudacionDepositar(Long idRecaudacionDepositar, Date fecha, String estado,
            String referencia, Cuenta cuentaDestino) {
        this.idRecaudacionDepositar = idRecaudacionDepositar;
        this.fecha = fecha;
        this.estado = estado;
        this.referencia = referencia;
        this.cuentaDestino = cuentaDestino;
    }

    public Long getIdRecaudacionDepositar() {
        return idRecaudacionDepositar;
    }

    public void setIdRecaudacionDepositar(Long idRecaudacionDepositar) {
        this.idRecaudacionDepositar = idRecaudacionDepositar;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public Cuenta getCuentaDestino() {
        return cuentaDestino;
    }

    public void setCuentaDestino(Cuenta cuentaDestino) {
        this.cuentaDestino = cuentaDestino;
    }
}
