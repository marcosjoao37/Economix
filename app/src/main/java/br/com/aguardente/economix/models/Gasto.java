package br.com.aguardente.economix.models;

import com.google.firebase.database.Exclude;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by joao on 25/04/17.
 */

public class Gasto {
    private String uid;
    private double preco;
    private String sobre;
    private Long data;

    public Gasto() {

    }

    public Gasto(double preco, String sobre, Date data) {
        this.preco = preco;
        this.sobre = sobre;
        this.data = data.getTime();
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

    public Long getData() {
        return data;
    }

    public void setData(Long data) {
        this.data = data;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("preco", preco);
        result.put("sobre", sobre);
        result.put("data", data);

        return result;
    }
}
