/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Miguel
 */
public class ConciliacionBancariaDetalle {

    private ConciliacionBancaria conciliacionBancaria;
    private Long numeroItem;
    private Credito credito;
    private Debito debito;
    private OrdenPago ordenPago;
    private String descripcion;
    private Long monto;
    private String tipo;
    private Boolean conciliado;
    private FormaPagoDetalle formaPagoDetalle;

    public ConciliacionBancariaDetalle() {
    }

    public ConciliacionBancariaDetalle(ConciliacionBancaria conciliacionBancaria, Long numeroItem) {
        this.conciliacionBancaria = conciliacionBancaria;
        this.numeroItem = numeroItem;
    }

    public ConciliacionBancariaDetalle(ConciliacionBancaria conciliacionBancaria, Long numeroItem,
            Credito credito, Debito debito, OrdenPago ordenPago, String descripcion, Long monto,
            String tipo, Boolean conciliado, FormaPagoDetalle formaPagoDetalle) {
        this.conciliacionBancaria = conciliacionBancaria;
        this.numeroItem = numeroItem;
        this.credito = credito;
        this.debito = debito;
        this.ordenPago = ordenPago;
        this.descripcion = descripcion;
        this.monto = monto;
        this.tipo = tipo;
        this.conciliado = conciliado;
        this.formaPagoDetalle = formaPagoDetalle;
    }

    public ConciliacionBancaria getConciliacionBancaria() {
        return conciliacionBancaria;
    }

    public void setConciliacionBancaria(ConciliacionBancaria conciliacionBancaria) {
        this.conciliacionBancaria = conciliacionBancaria;
    }

    public Long getNumeroItem() {
        return numeroItem;
    }

    public void setNumeroItem(Long numeroItem) {
        this.numeroItem = numeroItem;
    }

    public Credito getCredito() {
        return credito;
    }

    public void setCredito(Credito credito) {
        this.credito = credito;
    }

    public Debito getDebito() {
        return debito;
    }

    public void setDebito(Debito debito) {
        this.debito = debito;
    }

    public OrdenPago getOrdenPago() {
        return ordenPago;
    }

    public void setOrdenPago(OrdenPago ordenPago) {
        this.ordenPago = ordenPago;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Long getMonto() {
        return monto;
    }

    public void setMonto(Long monto) {
        this.monto = monto;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Boolean getConciliado() {
        return conciliado;
    }

    public void setConciliado(Boolean conciliado) {
        this.conciliado = conciliado;
    }

    public FormaPagoDetalle getFormaPagoDetalle() {
        return formaPagoDetalle;
    }

    public void setFormaPagoDetalle(FormaPagoDetalle formaPagoDetalle) {
        this.formaPagoDetalle = formaPagoDetalle;
    }
}
