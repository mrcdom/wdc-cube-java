package br.com.wedocode.shopping.view.react.stub.util;

import java.util.Map;

import br.com.wedocode.framework.commons.serialization.ExtensibleObjectOutput;
import br.com.wedocode.framework.commons.util.Promise;
import br.com.wedocode.framework.webflow.WebFlowView;
import br.com.wedocode.shopping.presentation.ShoppingApplication;
import br.com.wedocode.shopping.view.react.stub.viewimpl.ApplicationReactImpl;

public abstract class GenericViewImpl implements WebFlowView {

    protected final ApplicationReactImpl app;
    protected final String stateId;

    protected int alertId;

    public GenericViewImpl(ShoppingApplication app, String vid) {
        this(app, vid, ((ApplicationReactImpl) app).nextInstanceId());
    }

    public GenericViewImpl(ShoppingApplication app, String vid, int instanceId) {
        this.app = (ApplicationReactImpl) app;
        this.stateId = vid + ":" + instanceId;
        this.app.putView(this);
        this.app.markDirty(this);
    }

    public final String getStateId() {
        return this.stateId;
    }

    @Override
    public void release() {
        this.app.removeView(this.stateId);
    }

    @Override
    public void update() {
        this.app.markDirty(this);
    }

    public void syncClientToServer(Map<String, Object> formData) {
        // NOOP
    }

    public Promise<Boolean> submit(int eventCode, int eventQtde, Map<String, Object> formData) {
        return Promise.resolve(Boolean.TRUE);
    }

    public abstract void writeState(ExtensibleObjectOutput json);

}
