/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Miguel
 */
public class TipoEntidad {

    private Long idTipoEntidad;
    private String descripcion;

    public TipoEntidad() {
    }

    public TipoEntidad(Long idTipoEntidad) {
        this.idTipoEntidad = idTipoEntidad;
    }

    public TipoEntidad(Long idTipoEntidad, String descripcion) {
        this.idTipoEntidad = idTipoEntidad;
        this.descripcion = descripcion;
    }

    public Long getIdTipoEntidad() {
        return idTipoEntidad;
    }

    public void setIdTipoEntidad(Long idTipoEntidad) {
        this.idTipoEntidad = idTipoEntidad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

}
