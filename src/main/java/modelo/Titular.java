/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Miguel
 */
public class Titular {

    private Long idTitular;
    private Persona persona;

    public Titular() {
    }

    public Titular(Long idTitular) {
        this.idTitular = idTitular;
    }

    public Titular(Long idTitular, Persona persona) {
        this.idTitular = idTitular;
        this.persona = persona;
    }

    public Long getIdTitular() {
        return idTitular;
    }

    public void setIdTitular(Long idTitular) {
        this.idTitular = idTitular;
    }

    public Persona getPersona() {
        return persona;
    }

    public void setPersona(Persona persona) {
        this.persona = persona;
    }
}
