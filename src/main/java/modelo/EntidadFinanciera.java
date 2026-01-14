/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Miguel
 */
public class EntidadFinanciera {

    private Long idEntidadFinanciera;
    private String nombre;
    private TipoEntidadFinanciera tipoEntidadFinanciera;

    public EntidadFinanciera() {
    }

    public EntidadFinanciera(Long idEntidadFinanciera) {
        this.idEntidadFinanciera = idEntidadFinanciera;
    }

    public EntidadFinanciera(Long idEntidadFinanciera, String nombre, TipoEntidadFinanciera tipoEntidadFinanciera) {
        this.idEntidadFinanciera = idEntidadFinanciera;
        this.nombre = nombre;
        this.tipoEntidadFinanciera = tipoEntidadFinanciera;
    }

    public Long getIdEntidadFinanciera() {
        return idEntidadFinanciera;
    }

    public void setIdEntidadFinanciera(Long idEntidadFinanciera) {
        this.idEntidadFinanciera = idEntidadFinanciera;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TipoEntidadFinanciera getTipoEntidadFinanciera() {
        return tipoEntidadFinanciera;
    }

    public void setTipoEntidadFinanciera(TipoEntidadFinanciera tipoEntidadFinanciera) {
        this.tipoEntidadFinanciera = tipoEntidadFinanciera;
    }
}
