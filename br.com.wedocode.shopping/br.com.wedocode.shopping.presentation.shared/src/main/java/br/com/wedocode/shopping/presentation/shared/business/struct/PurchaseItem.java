package br.com.wedocode.shopping.presentation.shared.business.struct;

import java.io.Serializable;

public class PurchaseItem implements Serializable {

    private static final long serialVersionUID = 1L;

    public long productId;

    public int quantity;

    public double price;

}
