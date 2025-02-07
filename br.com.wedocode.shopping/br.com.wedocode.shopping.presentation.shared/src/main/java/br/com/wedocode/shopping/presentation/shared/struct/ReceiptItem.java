package br.com.wedocode.shopping.presentation.shared.struct;

import java.io.Serializable;

public class ReceiptItem implements Serializable {

    private static final long serialVersionUID = 1L;

    public String description;

    public double value;

    public int quantity;

}
