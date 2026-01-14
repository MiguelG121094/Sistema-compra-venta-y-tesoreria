/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Miguel
 */
public class Marca {

    private Long idMarca;
    private String descripcion;

    public Marca() {
    }

    public Marca(Long idMarca) {
        this.idMarca = idMarca;
    }

    public Marca(Long idMarca, String descripcion) {
        this.idMarca = idMarca;
        this.descripcion = descripcion;
    }
    
    public Long getIdMarca() {
        return idMarca;
    }

    public void setIdMarca(Long idMarca) {
        this.idMarca = idMarca;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    
    

}
