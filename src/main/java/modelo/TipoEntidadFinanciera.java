/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Miguel
 */
public class TipoEntidadFinanciera {

    private Long idTipoEntidadFinanciera;
    private String descripcion;

    public TipoEntidadFinanciera() {
    }

    public TipoEntidadFinanciera(Long idTipoEntidadFinanciera) {
        this.idTipoEntidadFinanciera = idTipoEntidadFinanciera;
    }

    public TipoEntidadFinanciera(Long idTipoEntidadFinanciera, String descripcion) {
        this.idTipoEntidadFinanciera = idTipoEntidadFinanciera;
        this.descripcion = descripcion;
    }

    public Long getIdTipoEntidadFinanciera() {
        return idTipoEntidadFinanciera;
    }

    public void setIdTipoEntidadFinanciera(Long idTipoEntidadFinanciera) {
        this.idTipoEntidadFinanciera = idTipoEntidadFinanciera;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
