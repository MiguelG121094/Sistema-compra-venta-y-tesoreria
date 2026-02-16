/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Miguel
 */
public class Modulo implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private Long idModulo;
    private String descripcion;

    public Modulo() {
    }

    public Modulo(Long idModulo) {
        this.idModulo = idModulo;
    }

    public Modulo(Long idModulo, String descripcion) {
        this.idModulo = idModulo;
        this.descripcion = descripcion;
    }

    public Long getIdModulo() {
        return idModulo;
    }

    public void setIdModulo(Long idModulo) {
        this.idModulo = idModulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
