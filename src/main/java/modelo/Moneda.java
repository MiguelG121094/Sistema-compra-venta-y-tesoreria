/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Miguel
 */
public class Moneda {

    private Long idMoneda;
    private String descripcion;

    public Moneda() {
    }

    public Moneda(Long idMoneda) {
        this.idMoneda = idMoneda;
    }

    public Moneda(Long idMoneda, String descripcion) {
        this.idMoneda = idMoneda;
        this.descripcion = descripcion;
    }

    public Long getIdMoneda() {
        return idMoneda;
    }

    public void setIdMoneda(Long idMoneda) {
        this.idMoneda = idMoneda;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
