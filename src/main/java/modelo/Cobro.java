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
public class Cobro implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private Long idCobro;
    private Date fecha;
    private String estado;
    private AperturaCierreCaja aperturaCierreCaja;
    private Usuario usuario;
    private Long monto;

    public Cobro() {
    }

    public Cobro(Long idCobro) {
        this.idCobro = idCobro;
    }

    public Cobro(Long idCobro, Date fecha, String estado, AperturaCierreCaja aperturaCierreCaja,
            Usuario usuario, Long monto) {
        this.idCobro = idCobro;
        this.fecha = fecha;
        this.estado = estado;
        this.aperturaCierreCaja = aperturaCierreCaja;
        this.usuario = usuario;
        this.monto = monto;
    }

    public Long getIdCobro() {
        return idCobro;
    }

    public void setIdCobro(Long idCobro) {
        this.idCobro = idCobro;
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

    public AperturaCierreCaja getAperturaCierreCaja() {
        return aperturaCierreCaja;
    }

    public void setAperturaCierreCaja(AperturaCierreCaja aperturaCierreCaja) {
        this.aperturaCierreCaja = aperturaCierreCaja;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Long getMonto() {
        return monto;
    }

    public void setMonto(Long monto) {
        this.monto = monto;
    }
}
