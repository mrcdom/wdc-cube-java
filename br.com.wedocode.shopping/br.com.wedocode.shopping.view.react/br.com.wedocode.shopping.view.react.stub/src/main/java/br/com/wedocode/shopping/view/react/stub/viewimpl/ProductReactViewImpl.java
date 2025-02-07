package br.com.wedocode.shopping.view.react.stub.viewimpl;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import br.com.wedocode.framework.commons.serialization.ExtensibleObjectOutput;
import br.com.wedocode.framework.commons.util.CoerceUtils;
import br.com.wedocode.framework.commons.util.Promise;
import br.com.wedocode.shopping.presentation.presenter.restricted.ProductPresenter;
import br.com.wedocode.shopping.view.react.stub.util.GenericViewImpl;

public class ProductReactViewImpl extends GenericViewImpl {

    protected ProductPresenter presenter;

    public ProductReactViewImpl(ProductPresenter presenter) {
        super(presenter.getApp(), "48b693f67410");
        this.presenter = presenter;
    }

    @Override
    public Promise<Boolean> submit(int eventCode, int eventQtde, Map<String, Object> formData) {
        if (eventCode == 1) {
            return presenter.onOpenProducts().finally_(presenter::update);
        }

        if (eventCode == 2) {
            var quantity = CoerceUtils.toInteger(formData.get("p.quantity"));
            return presenter.onAddToCart(quantity);
        }

        return Promise.resolve(Boolean.TRUE);
    }

    @Override
    public void writeState(ExtensibleObjectOutput json) {
        var state = this.presenter.getState();

        json.beginObject();
        {
            json.name("id").value(this.stateId);

            if (state.product != null) {
                json.name("product").beginObject();
                {
                    json.name("id").value(state.product.id);
                    json.name("name").value(state.product.name);
                    json.name("description").value(state.product.description);
                    json.name("price").value(state.product.price);
                }
                json.endObject();

                json.name("id").value(this.stateId);
            }

            if (StringUtils.isNotBlank(state.errorMessage)) {
                json.name("errorMessage").value(state.errorMessage);
            }
        }
        json.endObject();
    }

}
