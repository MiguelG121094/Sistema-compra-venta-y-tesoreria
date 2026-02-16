/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.util.Date;

/**
 *
 * @author Miguel
 */
public class FondoFijoRendicion implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private Long idFondoFijoRendicion;
    private FondoFijo fondoFijo;
    private Date fechaEmisionRendicion;
    private Date fechaReposicion;
    private Long numeroRendicion;

    public FondoFijoRendicion() {
    }

    public FondoFijoRendicion(Long idFondoFijoRendicion) {
        this.idFondoFijoRendicion = idFondoFijoRendicion;
    }

    public FondoFijoRendicion(Long idFondoFijoRendicion, FondoFijo fondoFijo,
            Date fechaEmisionRendicion, Date fechaReposicion, Long numeroRendicion) {
        this.idFondoFijoRendicion = idFondoFijoRendicion;
        this.fondoFijo = fondoFijo;
        this.fechaEmisionRendicion = fechaEmisionRendicion;
        this.fechaReposicion = fechaReposicion;
        this.numeroRendicion = numeroRendicion;
    }

    public Long getIdFondoFijoRendicion() {
        return idFondoFijoRendicion;
    }

    public void setIdFondoFijoRendicion(Long idFondoFijoRendicion) {
        this.idFondoFijoRendicion = idFondoFijoRendicion;
    }

    public FondoFijo getFondoFijo() {
        return fondoFijo;
    }

    public void setFondoFijo(FondoFijo fondoFijo) {
        this.fondoFijo = fondoFijo;
    }

    public Date getFechaEmisionRendicion() {
        return fechaEmisionRendicion;
    }

    public void setFechaEmisionRendicion(Date fechaEmisionRendicion) {
        this.fechaEmisionRendicion = fechaEmisionRendicion;
    }

    public Date getFechaReposicion() {
        return fechaReposicion;
    }

    public void setFechaReposicion(Date fechaReposicion) {
        this.fechaReposicion = fechaReposicion;
    }

    public Long getNumeroRendicion() {
        return numeroRendicion;
    }

    public void setNumeroRendicion(Long numeroRendicion) {
        this.numeroRendicion = numeroRendicion;
    }
}
