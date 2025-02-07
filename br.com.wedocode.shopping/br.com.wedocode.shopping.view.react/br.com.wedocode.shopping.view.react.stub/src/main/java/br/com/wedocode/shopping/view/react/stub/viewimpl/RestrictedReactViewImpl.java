package br.com.wedocode.shopping.view.react.stub.viewimpl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import br.com.wedocode.framework.commons.serialization.ExtensibleObjectOutput;
import br.com.wedocode.framework.commons.util.CoerceUtils;
import br.com.wedocode.framework.commons.util.Promise;
import br.com.wedocode.shopping.presentation.presenter.restricted.RestrictedPresenter;
import br.com.wedocode.shopping.presentation.shared.struct.ProductItem;
import br.com.wedocode.shopping.presentation.shared.struct.PurchaseInfo;
import br.com.wedocode.shopping.view.react.stub.util.GenericViewImpl;

public class RestrictedReactViewImpl extends GenericViewImpl {

    protected RestrictedPresenter presenter;

    public RestrictedReactViewImpl(RestrictedPresenter presenter) {
        super(presenter.getApp(), "473dbdd7a36a");
        this.presenter = presenter;
    }

    @Override
    public Promise<Boolean> submit(int eventCode, int eventQtde, Map<String, Object> formData) {
        if (eventCode == 1) {
            return presenter.onExit();
        }

        if (eventCode == 2) {
            return presenter.onOpenCart();
        }

        if (eventCode == 3) {
            var purchaseId = CoerceUtils.toLong(formData.get("p.purchaseId"));
            return presenter.onOpenReceipt(purchaseId);
        }

        if (eventCode == 4) {
            var productId = CoerceUtils.toLong(formData.get("p.productId"));
            return presenter.onOpenProduct(productId);
        }

        return Promise.resolve(Boolean.TRUE);
    }

    @Override
    public void writeState(ExtensibleObjectOutput json) {
        var state = this.presenter.getState();

        json.beginObject();
        {
            json.name("id").value(this.stateId);

            if (StringUtils.isNotBlank(state.nickName)) {
                json.name("nickName").value(state.nickName);
            }

            json.name("cartItemCount").value(state.cartItemCount);

            if (state.contentView instanceof GenericViewImpl view) {
                json.name("contentViewId").value(view.getStateId());
            } else {
                this.writePurchases(json, "purchases", state.purchases);
                this.writeProducts(json, "products", state.products);
            }

            if (StringUtils.isNotBlank(state.errorMessage)) {
                json.name("errorMessage").value(state.errorMessage);
            }
        }
        json.endObject();
    }

    private void writeProducts(ExtensibleObjectOutput json, String name, List<ProductItem> products) {
        json.name(name).beginArray();
        for (var product : Optional.ofNullable(products).orElse(Collections.emptyList())) {
            json.beginObject();
            {
                json.name("id").value(product.id);
                json.name("image").value(product.image);
                json.name("name").value(product.name);
                json.name("description").value(product.description);
                json.name("price").value(product.price);
            }
            json.endObject();
        }
        json.endArray();

    }

    private void writePurchases(ExtensibleObjectOutput json, String name, List<PurchaseInfo> purchases) {
        json.name(name).beginArray();
        for (var purchase : Optional.ofNullable(purchases).orElse(Collections.emptyList())) {
            json.beginObject();
            {
                json.name("id").value(purchase.id);
                json.name("date").value(purchase.date);
                json.name("total").value(purchase.total);
                json.name("items").beginArray();
                if (purchase.items != null) {
                    purchase.items.forEach(item -> json.value(item));
                }
                json.endArray();
            }
            json.endObject();
        }
        json.endArray();
    }

}
