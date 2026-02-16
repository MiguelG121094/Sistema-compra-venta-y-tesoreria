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
public class ArqueoCaja implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private Long idArqueoCaja;
    private Date fechaHora;
    private Cobro cobro;
    private AperturaCierreCaja aperturaCierreCaja;
    private Date fecha;
    private Long efectivo;
    private Long cheque;
    private Long tarjeta;
    private Long facturaInicial;
    private Long facturaFinal;
    private String observacion;
    private Usuario usuario;
    private Caja caja;

    public ArqueoCaja() {
    }

    public ArqueoCaja(Long idArqueoCaja) {
        this.idArqueoCaja = idArqueoCaja;
    }

    public ArqueoCaja(Long idArqueoCaja, Date fechaHora, Cobro cobro,
            AperturaCierreCaja aperturaCierreCaja, Date fecha, Long efectivo, Long cheque,
            Long tarjeta, Long facturaInicial, Long facturaFinal, String observacion,
            Usuario usuario, Caja caja) {
        this.idArqueoCaja = idArqueoCaja;
        this.fechaHora = fechaHora;
        this.cobro = cobro;
        this.aperturaCierreCaja = aperturaCierreCaja;
        this.fecha = fecha;
        this.efectivo = efectivo;
        this.cheque = cheque;
        this.tarjeta = tarjeta;
        this.facturaInicial = facturaInicial;
        this.facturaFinal = facturaFinal;
        this.observacion = observacion;
        this.usuario = usuario;
        this.caja = caja;
    }

    public Long getIdArqueoCaja() {
        return idArqueoCaja;
    }

    public void setIdArqueoCaja(Long idArqueoCaja) {
        this.idArqueoCaja = idArqueoCaja;
    }

    public Date getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(Date fechaHora) {
        this.fechaHora = fechaHora;
    }

    public Cobro getCobro() {
        return cobro;
    }

    public void setCobro(Cobro cobro) {
        this.cobro = cobro;
    }

    public AperturaCierreCaja getAperturaCierreCaja() {
        return aperturaCierreCaja;
    }

    public void setAperturaCierreCaja(AperturaCierreCaja aperturaCierreCaja) {
        this.aperturaCierreCaja = aperturaCierreCaja;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
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

    public Long getFacturaInicial() {
        return facturaInicial;
    }

    public void setFacturaInicial(Long facturaInicial) {
        this.facturaInicial = facturaInicial;
    }

    public Long getFacturaFinal() {
        return facturaFinal;
    }

    public void setFacturaFinal(Long facturaFinal) {
        this.facturaFinal = facturaFinal;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Caja getCaja() {
        return caja;
    }

    public void setCaja(Caja caja) {
        this.caja = caja;
    }
}
