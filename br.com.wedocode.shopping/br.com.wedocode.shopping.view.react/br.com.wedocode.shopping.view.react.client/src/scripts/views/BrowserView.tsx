import React, { ReactNode } from "react";
import app, { type ViewProps, BROWSER_VID, type BrowserViewState } from "../App";

const onAlertOk = (vsid: string) => app.submit(vsid, 1);
const onRecconectNow = () => app.connect();

export default function BrowserView({ vsid }: ViewProps) {
  const { state } = app.bindView<BrowserViewState>(vsid);
  const handleDismiss = React.useCallback(onAlertOk.bind(app, vsid), [vsid]);
  const handleReconnectNow = onRecconectNow;

  React.useEffect(() => {
    app.onStart();
    return () => {
      app.onStop();
    };
  }, []);

  let connectionAlert: ReactNode;
  if (state.error) {
    connectionAlert = <ConnectionAlert delay={state.error.delay} onReconnectNow={handleReconnectNow} />;
  }

  let rootView: ReactNode;
  if (state.contentViewId) {
    rootView = app.createView(state.contentViewId);
  } else {
    rootView = "Carregando...";
  }

  let alertView: ReactNode;
  if (state.alertMessage) {
    alertView = <Alert code={state.alertMessage.id} args={state.alertMessage.args ?? []} onDismiss={handleDismiss} />;
  }

  return (
    <div>
      {connectionAlert}
      {alertView}
      {rootView}
    </div>
  );
}

BrowserView.register = () => app.registerView(BROWSER_VID, (vsid, props) => <BrowserView vsid={vsid} {...props} />);

// :: Internal - Alert

type AlertProps = {
  code: number;
  args: string[];

  onDismiss?: () => void;
};

function Alert(props: AlertProps) {
  let msgNode: React.ReactNode | null = null;
  let detailMessage: string | null = null;
  switch (props.code) {
    case -1: // UNEXPECTED_ERROR
      msgNode = props.args[0];
      detailMessage = props.args[1];
      break;
    case -2: // UNEXPECTED_URI
      msgNode = "A URI " + props.args[0] + " não está acessível";
      detailMessage = props.args[1];
      break;
    default:
      if (props.args.length > 0) {
        msgNode = props.args[0];
      } else {
        msgNode = "Occorreu um erro não esperado";
      }
  }

  let detalhe: React.ReactNode | null = null;
  if (detailMessage) {
    detalhe = (
      <>
        <br />
        {detailMessage}
      </>
    );
  }

  return (
    <div className="alert alert-warning" style={{ margin: 10 }}>
      <div>
        <b>Aviso!</b>
      </div>
      <p>
        {msgNode}
        {detalhe}
      </p>

      <button onClick={props.onDismiss}>Ok</button>
    </div>
  );
}

// :: Internal ConnectionAlert

type ConnectionAlertProps = {
  delay: number;
  onReconnectNow: () => void;
};

function ConnectionAlert(props: ConnectionAlertProps) {
  let timeElm: React.ReactNode, retryElm: React.ReactNode;
  if (props.delay > 0) {
    let seconds = Math.floor(props.delay / 1000);
    let minutes = 0;
    if (seconds > 60) {
      minutes = Math.floor(seconds / 60);
      seconds = seconds - minutes * 60;
    }

    if (minutes > 0) {
      timeElm = <span>{"Conectando em " + minutes + "m e " + seconds + "s..."}</span>;
    } else {
      timeElm = <span>{"Conectando em " + seconds + "s..."}</span>;
    }
    retryElm = (
      <u style={{ cursor: "pointer" }} onClick={props.onReconnectNow}>
        Tentar agora
      </u>
    );
  } else {
    timeElm = <span>Connectando agora...</span>;
  }

  return (
    <div style={{ textAlign: "center" }}>
      <div className="connection-alert">
        <span>
          <b>Não conectado</b>.
        </span>{" "}
        {timeElm} {retryElm}.
      </div>
    </div>
  );
}
