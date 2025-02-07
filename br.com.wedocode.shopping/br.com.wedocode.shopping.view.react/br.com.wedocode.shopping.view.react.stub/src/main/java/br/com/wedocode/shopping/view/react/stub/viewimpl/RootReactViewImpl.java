package br.com.wedocode.shopping.view.react.stub.viewimpl;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import br.com.wedocode.framework.commons.serialization.ExtensibleObjectOutput;
import br.com.wedocode.framework.commons.util.Promise;
import br.com.wedocode.shopping.presentation.presenter.RootPresenter;
import br.com.wedocode.shopping.view.react.stub.util.GenericViewImpl;

public class RootReactViewImpl extends GenericViewImpl {

    protected RootPresenter presenter;

    public RootReactViewImpl(RootPresenter presenter) {
        super(presenter.getApp(), "f2d345c4a610");
        this.presenter = presenter;
        this.app.setRootPresenter(presenter);
    }

    @Override
    public void release() {
        this.app.setRootPresenter(null);
        super.release();
    }

    @Override
    public Promise<Boolean> submit(int eventCode, int eventQtde, Map<String, Object> formData) {
        return Promise.resolve(Boolean.TRUE);
    }

    @Override
    public void writeState(ExtensibleObjectOutput json) {
        var state = this.presenter.getState();

        json.beginObject();
        {
            json.name("id").value(this.stateId);

            if (state.contentView instanceof GenericViewImpl view) {
                json.name("contentViewId").value(view.getStateId());
            }

            if (StringUtils.isNotBlank(state.errorMessage)) {
                json.name("errorMessage").value(state.errorMessage);
            }
        }
        json.endObject();
    }

}
