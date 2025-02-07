package br.com.wedocode.shopping.view.react.stub.viewimpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.LoggerFactory;

import br.com.wedocode.framework.commons.function.ThrowingRunnable;
import br.com.wedocode.framework.commons.serialization.ExtensibleObjectOutput;
import br.com.wedocode.framework.commons.util.CoerceUtils;
import br.com.wedocode.framework.commons.util.Promise;
import br.com.wedocode.shopping.view.react.stub.util.GenericViewImpl;

public class BrowserReactViewImpl extends GenericViewImpl {

    public static String VID = "7b32e816a191";
    public static String VSID = VID + ":0";

    private int alertId;
    private List<String> alertArgs = Collections.emptyList();
    private ThrowingRunnable alertAction = ThrowingRunnable.noop();

    public BrowserReactViewImpl(ApplicationReactImpl app) {
        super(app, VID, 0);
    }

    public void alertUnexpectedError(String msg, Throwable cause) {
        int alertCode = -1;
        if (msg == null) {
            if (cause != null) {
                msg = cause.getMessage();
            } else {
                msg = "Ocorreu um erro não previsto";
            }
        }

        if (cause != null) {
            var detail = ExceptionUtils.getStackTrace(cause);
            this.alert(ThrowingRunnable.noop(), alertCode, msg, detail);
        } else {
            this.alert(ThrowingRunnable.noop(), alertCode, msg);
        }
    }

    public void alert(ThrowingRunnable action, int code, Object... args) {
        var oldAlertAction = this.alertAction;
        this.alertAction = () -> {
            oldAlertAction.runThrows();
            action.runThrows();
        };

        this.alertId = code;
        this.alertArgs = Collections.emptyList();

        if (args != null && args.length > 0) {
            this.alertArgs = new ArrayList<>(args.length);
            for (Object arg : args) {
                this.alertArgs.add(String.valueOf(arg));
            }
        }
        this.update();
    }

    private Promise<Boolean> onStart(String path) throws Exception {
        return app.safeGo(path)
                .then(value -> {
                    app.updateAllViews();
                    return Promise.resolve(value);
                });
    }

    private Promise<Boolean> onHistoryChanged(String path) {
        try {
            return app.safeGo(path);
        } catch (Exception e) {
            var logger = LoggerFactory.getLogger(this.getClass());
            logger.warn("onHistoryChanged", e);
            return Promise.resolve(Boolean.FALSE);
        }
    }

    private Promise<Boolean> onAlertOk() {
        try {
            this.alertId = 0;
            this.alertArgs = Collections.emptyList();
            this.alertAction.run();
        } finally {
            this.alertAction = ThrowingRunnable.noop();
            this.update();
        }
        return Promise.resolve(Boolean.TRUE);
    }

    private Promise<Boolean> onKeepAlive() {
        app.extendLife();
        return Promise.resolve(Boolean.TRUE);
    }

    @Override
    public Promise<Boolean> submit(int eventCode, int eventQtde, Map<String, Object> formData) {
        if (eventCode == 1) {
            return this.onAlertOk();
        }

        if (eventCode == 2) {
            return this.onKeepAlive();
        }

        if (eventCode == -1) {
            try {
                var path = CoerceUtils.toString(formData.get("p.path"));
                return this.onStart(path);
            } catch (Exception e) {
                var logger = LoggerFactory.getLogger(this.getClass());
                logger.warn("onStart", e);
                return Promise.resolve(Boolean.FALSE);
            }
        }

        if (eventCode == -2) {
            var path = CoerceUtils.toString(formData.get("p.path"));
            return this.onHistoryChanged(path);
        }

        return Promise.resolve(Boolean.TRUE);
    }

    @Override
    public void writeState(ExtensibleObjectOutput json) {
        json.beginObject();
        {
            json.name("id").value(this.stateId);

            if (alertId != 0) {
                json.name("alertMessage").beginObject();
                {
                    json.name("id").value(alertId);
                    json.name("args").beginArray();
                    {
                        alertArgs.forEach(msg -> json.value(msg));
                    }
                    json.endArray();
                }
                json.endObject();
            }

            if (this.app.getRootPresenter() != null
                    && this.app.getRootPresenter().getView() instanceof GenericViewImpl view) {
                json.name("contentViewId").value(view.getStateId());
            }
        }
        json.endObject();
    }

}
