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
public class NotaRemisionVenta {

    private Long idNotaRemisionVenta;
    private Date fechaEmision;
    private String descripcion;
    private String estado;
    private Cliente cliente;
    private Usuario usuario;
    private FacturaVenta facturaVenta;
    private Timbrado timbrado;
    private String listaArticulos;

    public NotaRemisionVenta() {
    }

    public NotaRemisionVenta(Long idNotaRemisionVenta) {
        this.idNotaRemisionVenta = idNotaRemisionVenta;
    }

    public NotaRemisionVenta(Long idNotaRemisionVenta, Date fechaEmision, String descripcion,
            String estado, Cliente cliente, Usuario usuario, FacturaVenta facturaVenta,
            Timbrado timbrado, String listaArticulos) {
        this.idNotaRemisionVenta = idNotaRemisionVenta;
        this.fechaEmision = fechaEmision;
        this.descripcion = descripcion;
        this.estado = estado;
        this.cliente = cliente;
        this.usuario = usuario;
        this.facturaVenta = facturaVenta;
        this.timbrado = timbrado;
        this.listaArticulos = listaArticulos;
    }

    public Long getIdNotaRemisionVenta() {
        return idNotaRemisionVenta;
    }

    public void setIdNotaRemisionVenta(Long idNotaRemisionVenta) {
        this.idNotaRemisionVenta = idNotaRemisionVenta;
    }

    public Date getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(Date fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public FacturaVenta getFacturaVenta() {
        return facturaVenta;
    }

    public void setFacturaVenta(FacturaVenta facturaVenta) {
        this.facturaVenta = facturaVenta;
    }

    public Timbrado getTimbrado() {
        return timbrado;
    }

    public void setTimbrado(Timbrado timbrado) {
        this.timbrado = timbrado;
    }

    public String getListaArticulos() {
        return listaArticulos;
    }

    public void setListaArticulos(String listaArticulos) {
        this.listaArticulos = listaArticulos;
    }
}
