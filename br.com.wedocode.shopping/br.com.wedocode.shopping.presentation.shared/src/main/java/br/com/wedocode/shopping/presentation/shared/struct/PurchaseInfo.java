package br.com.wedocode.shopping.presentation.shared.struct;

import java.io.Serializable;
import java.util.List;

public class PurchaseInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    public long id;

    public long date;

    public List<String> items;

    public double total;

}
