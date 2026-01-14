/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Miguel
 */
public class TipoArticulo {

    private Long idTipoArticulo;
    private String descripcion;

    public TipoArticulo() {
    }

    public TipoArticulo(Long idTipoArticulo) {
        this.idTipoArticulo = idTipoArticulo;
    }
    
    public TipoArticulo(Long idTipoArticulo, String descripcion) {
        this.idTipoArticulo = idTipoArticulo;
        this.descripcion = descripcion;
    }

    public Long getIdTipoArticulo() {
        return idTipoArticulo;
    }

    public void setIdTipoArticulo(Long idTipoArticulo) {
        this.idTipoArticulo = idTipoArticulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

}
