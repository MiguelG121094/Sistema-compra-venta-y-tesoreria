/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Miguel
 */
public class TipoImpuesto {

    private Long idTipoImpuesto;
    private String descripcion;

    public TipoImpuesto() {
    }

    public TipoImpuesto(Long idTipoImpuesto) {
        this.idTipoImpuesto = idTipoImpuesto;
    }

    public TipoImpuesto(Long idTipoImpuesto, String descripcion) {
        this.idTipoImpuesto = idTipoImpuesto;
        this.descripcion = descripcion;
    }

    public Long getIdTipoImpuesto() {
        return idTipoImpuesto;
    }

    public void setIdTipoImpuesto(Long idTipoImpuesto) {
        this.idTipoImpuesto = idTipoImpuesto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

}
