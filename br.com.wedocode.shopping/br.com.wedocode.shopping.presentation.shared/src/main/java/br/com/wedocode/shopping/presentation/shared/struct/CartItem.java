package br.com.wedocode.shopping.presentation.shared.struct;

import java.io.Serializable;

public class CartItem implements Serializable {

    private static final long serialVersionUID = 1L;

    public long id;

    public String image;

    public String name;

    public double price;

    public int quantity;

}
