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
public class AjusteStockCabecera {

    private Long idAjusteStockCabecera;
    private Date fecha;
    private String estado;

    public AjusteStockCabecera() {
    }

    public AjusteStockCabecera(Long idAjusteStockCabecera) {
        this.idAjusteStockCabecera = idAjusteStockCabecera;
    }

    public AjusteStockCabecera(Long idAjusteStockCabecera, Date fecha, String estado) {
        this.idAjusteStockCabecera = idAjusteStockCabecera;
        this.fecha = fecha;
        this.estado = estado;
    }

    public Long getIdAjusteStockCabecera() {
        return idAjusteStockCabecera;
    }

    public void setIdAjusteStockCabecera(Long idAjusteStockCabecera) {
        this.idAjusteStockCabecera = idAjusteStockCabecera;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
