/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Miguel
 */
public class Permiso {

    private Grupo grupo;
    private Modulo modulo;
    private Boolean leer;
    private Boolean insertar;
    private Boolean borrar;
    private Boolean editar;

    public Permiso() {
    }

    public Permiso(Grupo grupo, Modulo modulo) {
        this.grupo = grupo;
        this.modulo = modulo;
    }

    public Permiso(Grupo grupo, Modulo modulo, Boolean leer, Boolean insertar,
            Boolean borrar, Boolean editar) {
        this.grupo = grupo;
        this.modulo = modulo;
        this.leer = leer;
        this.insertar = insertar;
        this.borrar = borrar;
        this.editar = editar;
    }

    public Grupo getGrupo() {
        return grupo;
    }

    public void setGrupo(Grupo grupo) {
        this.grupo = grupo;
    }

    public Modulo getModulo() {
        return modulo;
    }

    public void setModulo(Modulo modulo) {
        this.modulo = modulo;
    }

    public Boolean getLeer() {
        return leer;
    }

    public void setLeer(Boolean leer) {
        this.leer = leer;
    }

    public Boolean getInsertar() {
        return insertar;
    }

    public void setInsertar(Boolean insertar) {
        this.insertar = insertar;
    }

    public Boolean getBorrar() {
        return borrar;
    }

    public void setBorrar(Boolean borrar) {
        this.borrar = borrar;
    }

    public Boolean getEditar() {
        return editar;
    }

    public void setEditar(Boolean editar) {
        this.editar = editar;
    }
}
