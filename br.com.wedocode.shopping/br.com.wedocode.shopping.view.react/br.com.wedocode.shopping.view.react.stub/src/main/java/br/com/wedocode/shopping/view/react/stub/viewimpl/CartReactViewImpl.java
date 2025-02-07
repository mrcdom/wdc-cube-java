package br.com.wedocode.shopping.view.react.stub.viewimpl;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import br.com.wedocode.framework.commons.serialization.ExtensibleObjectOutput;
import br.com.wedocode.framework.commons.util.CoerceUtils;
import br.com.wedocode.framework.commons.util.Promise;
import br.com.wedocode.shopping.presentation.presenter.restricted.CartPresenter;
import br.com.wedocode.shopping.view.react.stub.util.GenericViewImpl;

public class CartReactViewImpl extends GenericViewImpl {

    protected CartPresenter presenter;

    public CartReactViewImpl(CartPresenter presenter) {
        super(presenter.getApp(), "7eb485e5f843");
        this.presenter = presenter;
    }

    @Override
    public Promise<Boolean> submit(int eventCode, int eventQtde, Map<String, Object> formData) {
        if (eventCode == 1) {
            return presenter.onBuy();
        }

        if (eventCode == 2) {
            var productId = CoerceUtils.toLong(formData.get("p.productId"));
            return presenter.onRemoveProduct(productId);
        }

        if (eventCode == 3) {
            return presenter.onOpenProducts();
        }

        return Promise.resolve(true);
    }

    @Override
    public void writeState(ExtensibleObjectOutput json) {
        var state = this.presenter.getState();

        json.beginObject();
        {
            json.name("id").value(this.stateId);

            json.name("items").beginArray();
            if (state.items != null) {
                state.items.forEach(item -> {
                    json.beginObject();
                    {
                        json.name("id").value(item.id);
                        json.name("name").value(item.name);
                        json.name("price").value(item.price);
                        json.name("quantity").value(item.quantity);
                    }
                    json.endObject();
                });
            }
            json.endArray();

            if (StringUtils.isNotBlank(state.errorMessage)) {
                json.name("errorMessage").value(state.errorMessage);
            }
        }
        json.endObject();
    }

}
