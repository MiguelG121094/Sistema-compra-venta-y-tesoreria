/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Miguel
 */
public class TipoComprobante {

    private Long idTipoComprobante;
    private String descripcion;

    public TipoComprobante() {
    }

    public TipoComprobante(Long idTipoComprobante) {
        this.idTipoComprobante = idTipoComprobante;
    }

    public TipoComprobante(Long idTipoComprobante, String descripcion) {
        this.idTipoComprobante = idTipoComprobante;
        this.descripcion = descripcion;
    }

    public Long getIdTipoComprobante() {
        return idTipoComprobante;
    }

    public void setIdTipoComprobante(Long idTipoComprobante) {
        this.idTipoComprobante = idTipoComprobante;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
