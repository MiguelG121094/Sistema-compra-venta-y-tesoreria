/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Miguel
 */
public class FormaPagoCabecera implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private Long idFormaPagoCabecera;
    private String descripcion;

    public FormaPagoCabecera() {
    }

    public FormaPagoCabecera(Long idFormaPagoCabecera) {
        this.idFormaPagoCabecera = idFormaPagoCabecera;
    }

    public FormaPagoCabecera(Long idFormaPagoCabecera, String descripcion) {
        this.idFormaPagoCabecera = idFormaPagoCabecera;
        this.descripcion = descripcion;
    }

    public Long getIdFormaPagoCabecera() {
        return idFormaPagoCabecera;
    }

    public void setIdFormaPagoCabecera(Long idFormaPagoCabecera) {
        this.idFormaPagoCabecera = idFormaPagoCabecera;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
