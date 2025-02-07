package br.com.wedocode.shopping.view.react.stub.viewimpl;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import br.com.wedocode.framework.commons.serialization.ExtensibleObjectOutput;
import br.com.wedocode.framework.commons.util.Promise;
import br.com.wedocode.shopping.presentation.presenter.restricted.ReceiptPresenter;
import br.com.wedocode.shopping.view.react.stub.util.GenericViewImpl;

public class ReceiptReactViewImpl extends GenericViewImpl {

    protected ReceiptPresenter presenter;

    public ReceiptReactViewImpl(ReceiptPresenter presenter) {
        super(presenter.getApp(), "e8d0bd8ae3bc");
        this.presenter = presenter;
    }

    @Override
    public Promise<Boolean> submit(int eventCode, int eventQtde, Map<String, Object> formData) {
        if (eventCode == 1) {
            return presenter.onOpenProducts();
        }
        return Promise.resolve(Boolean.TRUE);
    }

    @Override
    public void writeState(ExtensibleObjectOutput json) {
        var state = this.presenter.getState();
        json.beginObject();
        {
            json.name("id").value(this.stateId);

            json.name("notifySuccess").value(state.notifySuccess);
            state.notifySuccess = false;

            if (state.receipt != null) {
                var receipt = state.receipt;
                json.name("receipt").beginObject();
                {
                    json.name("date").value(receipt.date);
                    json.name("total").value(receipt.total);

                    json.name("items").beginArray();
                    for (var receiptItem : Optional.ofNullable(receipt.items).orElse(Collections.emptyList())) {
                        json.beginObject();
                        {
                            json.name("description").value(receiptItem.description);
                            json.name("value").value(receiptItem.value);
                            json.name("quantity").value(receiptItem.quantity);
                        }
                        json.endObject();
                    }
                    json.endArray();
                }
                json.endObject();
            }
        }
        json.endObject();
    }

}
