/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.sql.Date;

/**
 *
 * @author Miguel
 */
public class Grupo {

    private Long idGrupo;
    private String descripcion;

    public Grupo() {
    }

    public Grupo(Long idGrupo) {
        this.idGrupo = idGrupo;
    }

    public Grupo(Long idGrupo, String descripcion) {
        this.idGrupo = idGrupo;
        this.descripcion = descripcion;
    }

    public Long getIdGrupo() {
        return idGrupo;
    }

    public void setIdGrupo(Long idGrupo) {
        this.idGrupo = idGrupo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    
}
