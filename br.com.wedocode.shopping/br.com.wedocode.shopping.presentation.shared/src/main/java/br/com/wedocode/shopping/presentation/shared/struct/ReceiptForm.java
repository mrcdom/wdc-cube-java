package br.com.wedocode.shopping.presentation.shared.struct;

import java.io.Serializable;
import java.util.List;

public class ReceiptForm implements Serializable {

    private static final long serialVersionUID = 1L;

    public long date;

    public List<ReceiptItem> items;

    public double total;

}
