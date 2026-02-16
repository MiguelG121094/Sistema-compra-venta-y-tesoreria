/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Miguel
 */
public class TipoTarjeta implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private Long idTipoTarjeta;
    private String descripcion;

    public TipoTarjeta() {
    }

    public TipoTarjeta(Long idTipoTarjeta) {
        this.idTipoTarjeta = idTipoTarjeta;
    }

    public TipoTarjeta(Long idTipoTarjeta, String descripcion) {
        this.idTipoTarjeta = idTipoTarjeta;
        this.descripcion = descripcion;
    }

    public Long getIdTipoTarjeta() {
        return idTipoTarjeta;
    }

    public void setIdTipoTarjeta(Long idTipoTarjeta) {
        this.idTipoTarjeta = idTipoTarjeta;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
