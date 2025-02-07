package br.com.wedocode.shopping.view.react.stub.viewimpl;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import br.com.wedocode.framework.commons.serialization.ExtensibleObjectOutput;
import br.com.wedocode.framework.commons.util.CoerceUtils;
import br.com.wedocode.framework.commons.util.Promise;
import br.com.wedocode.shopping.presentation.presenter.nonrestricted.LoginPresenter;
import br.com.wedocode.shopping.view.react.stub.util.GenericViewImpl;

public class LoginReactViewImpl extends GenericViewImpl {

    protected LoginPresenter presenter;

    public LoginReactViewImpl(LoginPresenter presenter) {
        super(presenter.getApp(), "c677cda52d14");
        this.presenter = presenter;
    }

    @Override
    public void syncClientToServer(Map<String, Object> formData) {
        var state = this.presenter.getState();

        //@formatter:off
        var fn = "userName";
        if (formData.containsKey(fn)) state.userName = CoerceUtils.toString(formData.get(fn));

        fn = "password";
        if (formData.containsKey(fn)) {
            state.password = this.app.getDataSecurity().b64Decipher(CoerceUtils.toString(formData.get(fn)));
        }
        //@formatter:on
    }

    @Override
    public Promise<Boolean> submit(int eventCode, int eventQtde, Map<String, Object> formData) {
        if (eventCode == 1) {
            return presenter.onEnter();
        }
        return Promise.resolve(Boolean.TRUE);
    }

    @Override
    public void writeState(ExtensibleObjectOutput json) {
        var state = this.presenter.getState();

        json.beginObject();
        {
            json.name("id").value(this.stateId);

            if (StringUtils.isNotBlank(state.userName)) {
                json.name("userName").value(state.userName);
            }

            if (StringUtils.isNotBlank(state.errorMessage)) {
                json.name("errorMessage").value(state.errorMessage);
            }
        }
        json.endObject();
    }

}
