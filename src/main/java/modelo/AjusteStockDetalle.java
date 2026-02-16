/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Miguel
 */
public class AjusteStockDetalle implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private AjusteStockCabecera ajusteStockCabecera;
    private Articulo articulo;
    private Long cantidad;
    private MotivoAjuste motivoAjuste;
    private Deposito deposito;

    public AjusteStockDetalle() {
    }

    public AjusteStockDetalle(AjusteStockCabecera ajusteStockCabecera, Articulo articulo) {
        this.ajusteStockCabecera = ajusteStockCabecera;
        this.articulo = articulo;
    }

    public AjusteStockDetalle(AjusteStockCabecera ajusteStockCabecera, Articulo articulo,
            Long cantidad, MotivoAjuste motivoAjuste, Deposito deposito) {
        this.ajusteStockCabecera = ajusteStockCabecera;
        this.articulo = articulo;
        this.cantidad = cantidad;
        this.motivoAjuste = motivoAjuste;
        this.deposito = deposito;
    }

    public AjusteStockCabecera getAjusteStockCabecera() {
        return ajusteStockCabecera;
    }

    public void setAjusteStockCabecera(AjusteStockCabecera ajusteStockCabecera) {
        this.ajusteStockCabecera = ajusteStockCabecera;
    }

    public Articulo getArticulo() {
        return articulo;
    }

    public void setArticulo(Articulo articulo) {
        this.articulo = articulo;
    }

    public Long getCantidad() {
        return cantidad;
    }

    public void setCantidad(Long cantidad) {
        this.cantidad = cantidad;
    }

    public MotivoAjuste getMotivoAjuste() {
        return motivoAjuste;
    }

    public void setMotivoAjuste(MotivoAjuste motivoAjuste) {
        this.motivoAjuste = motivoAjuste;
    }

    public Deposito getDeposito() {
        return deposito;
    }

    public void setDeposito(Deposito deposito) {
        this.deposito = deposito;
    }
}
