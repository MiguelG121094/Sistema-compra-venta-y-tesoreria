/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Miguel
 */
public class FormaCobro {

    private Long idFormaCobro;
    private String descripcion;
    private String estado;

    public FormaCobro() {
    }

    public FormaCobro(Long idFormaCobro) {
        this.idFormaCobro = idFormaCobro;
    }

    public FormaCobro(Long idFormaCobro, String descripcion, String estado) {
        this.idFormaCobro = idFormaCobro;
        this.descripcion = descripcion;
        this.estado = estado;
    }

    public Long getIdFormaCobro() {
        return idFormaCobro;
    }

    public void setIdFormaCobro(Long idFormaCobro) {
        this.idFormaCobro = idFormaCobro;
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
}
