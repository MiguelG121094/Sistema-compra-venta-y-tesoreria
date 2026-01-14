/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Miguel
 */
public class TipoCheque {

    private Long idTipoCheque;
    private String descripcion;

    public TipoCheque() {
    }

    public TipoCheque(Long idTipoCheque) {
        this.idTipoCheque = idTipoCheque;
    }

    public TipoCheque(Long idTipoCheque, String descripcion) {
        this.idTipoCheque = idTipoCheque;
        this.descripcion = descripcion;
    }

    public Long getIdTipoCheque() {
        return idTipoCheque;
    }

    public void setIdTipoCheque(Long idTipoCheque) {
        this.idTipoCheque = idTipoCheque;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
