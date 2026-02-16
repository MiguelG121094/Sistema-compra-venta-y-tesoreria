/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Miguel
 */
public class Stock implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private Deposito deposito;
    private Articulo articulo;
    private Long cantidadMinima;
    private Long cantidadMaxima;
    private Long stockActual;

    public Stock() {
    }

    public Stock(Deposito deposito, Articulo articulo) {
        this.deposito = deposito;
        this.articulo = articulo;
    }

    public Stock(Deposito deposito, Articulo articulo, Long cantidadMinima,
            Long cantidadMaxima, Long stockActual) {
        this.deposito = deposito;
        this.articulo = articulo;
        this.cantidadMinima = cantidadMinima;
        this.cantidadMaxima = cantidadMaxima;
        this.stockActual = stockActual;
    }

    public Deposito getDeposito() {
        return deposito;
    }

    public void setDeposito(Deposito deposito) {
        this.deposito = deposito;
    }

    public Articulo getArticulo() {
        return articulo;
    }

    public void setArticulo(Articulo articulo) {
        this.articulo = articulo;
    }

    public Long getCantidadMinima() {
        return cantidadMinima;
    }

    public void setCantidadMinima(Long cantidadMinima) {
        this.cantidadMinima = cantidadMinima;
    }

    public Long getCantidadMaxima() {
        return cantidadMaxima;
    }

    public void setCantidadMaxima(Long cantidadMaxima) {
        this.cantidadMaxima = cantidadMaxima;
    }

    public Long getStockActual() {
        return stockActual;
    }

    public void setStockActual(Long stockActual) {
        this.stockActual = stockActual;
    }
}
