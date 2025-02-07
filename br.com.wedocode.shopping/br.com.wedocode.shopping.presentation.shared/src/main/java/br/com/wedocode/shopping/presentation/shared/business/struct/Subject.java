package br.com.wedocode.shopping.presentation.shared.business.struct;

import java.io.Serializable;

public class Subject implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    String ninkName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNickName() {
        return ninkName;
    }

    public void setNinkName(String ninkName) {
        this.ninkName = ninkName;
    }

}
