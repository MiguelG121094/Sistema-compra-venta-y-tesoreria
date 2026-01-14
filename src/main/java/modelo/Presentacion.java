/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Miguel
 */
public class Presentacion {

    private Long idPresentacion;
    private String descripcion;

    public Presentacion() {
    }

    public Presentacion(Long idPresentacion) {
        this.idPresentacion = idPresentacion;
    }

    public Presentacion(Long idPresentacion, String descripcion) {
        this.idPresentacion = idPresentacion;
        this.descripcion = descripcion;
    }
    
    public Long getIdPresentacion() {
        return idPresentacion;
    }

    public void setIdPresentacion(Long idMarca) {
        this.idPresentacion = idMarca;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    
    

}
