/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Miguel
 */
public class MotivoAjuste {

    private Long idMotivoAjuste;
    private String descripcion;

    public MotivoAjuste() {
    }

    public MotivoAjuste(Long idMotivoAjuste) {
        this.idMotivoAjuste = idMotivoAjuste;
    }

    public MotivoAjuste(Long idMotivoAjuste, String descripcion) {
        this.idMotivoAjuste = idMotivoAjuste;
        this.descripcion = descripcion;
    }

    public Long getIdMotivoAjuste() {
        return idMotivoAjuste;
    }

    public void setIdMotivoAjuste(Long idMotivoAjuste) {
        this.idMotivoAjuste = idMotivoAjuste;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
