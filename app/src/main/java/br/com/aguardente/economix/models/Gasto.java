package br.com.aguardente.economix.models;

import java.util.Date;

/**
 * Created by joao on 25/04/17.
 */

public class Gasto {
    private double preco;
    private String sobre;
    private Date data;

    public Gasto() {

    }

    public Gasto(double preco, String sobre, Date data) {
        this.preco = preco;
        this.sobre = sobre;
        this.data = data;
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public String getSobre() {
        return sobre;
    }

    public void setSobre(String sobre) {
        this.sobre = sobre;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }
}
