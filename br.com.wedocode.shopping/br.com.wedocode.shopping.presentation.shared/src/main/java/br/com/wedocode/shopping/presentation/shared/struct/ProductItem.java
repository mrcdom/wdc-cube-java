package br.com.wedocode.shopping.presentation.shared.struct;

import java.io.Serializable;

public class ProductItem implements Serializable {

    private static final long serialVersionUID = 1L;

    public long id;

    public String image;

    public String name;

    public String description;

    public double price;

}
