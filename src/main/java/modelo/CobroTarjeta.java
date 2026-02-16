/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Miguel
 */
public class CobroTarjeta implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private Long idCobroTarjeta;
    private Cobro cobro;
    private Tarjeta tarjeta;
    private Long numeroBoletaPost;

    public CobroTarjeta() {
    }

    public CobroTarjeta(Long idCobroTarjeta, Cobro cobro) {
        this.idCobroTarjeta = idCobroTarjeta;
        this.cobro = cobro;
    }

    public CobroTarjeta(Long idCobroTarjeta, Cobro cobro, Tarjeta tarjeta, Long numeroBoletaPost) {
        this.idCobroTarjeta = idCobroTarjeta;
        this.cobro = cobro;
        this.tarjeta = tarjeta;
        this.numeroBoletaPost = numeroBoletaPost;
    }

    public Long getIdCobroTarjeta() {
        return idCobroTarjeta;
    }

    public void setIdCobroTarjeta(Long idCobroTarjeta) {
        this.idCobroTarjeta = idCobroTarjeta;
    }

    public Cobro getCobro() {
        return cobro;
    }

    public void setCobro(Cobro cobro) {
        this.cobro = cobro;
    }

    public Tarjeta getTarjeta() {
        return tarjeta;
    }

    public void setTarjeta(Tarjeta tarjeta) {
        this.tarjeta = tarjeta;
    }

    public Long getNumeroBoletaPost() {
        return numeroBoletaPost;
    }

    public void setNumeroBoletaPost(Long numeroBoletaPost) {
        this.numeroBoletaPost = numeroBoletaPost;
    }
}
