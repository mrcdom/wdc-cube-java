import React, { ReactNode } from "react";
import app, { type ViewProps } from "../App";

const { useCallback } = React;

// :: Actions

let onEnter = (vsid: string) => app.submit(vsid, 1);
let onSubmit = (e: React.FormEvent<HTMLFormElement>) => e.preventDefault();

// :: View

export type LoginViewState = {
  userName?: string;
  password?: string;
  errorMessage?: string;
};

export default function LoginView({ vsid }: ViewProps) {
  const { state, scope } = app.bindView<LoginViewState>(vsid);

  !state.userName && (state.userName = "");
  !state.password && (state.password = "");

  const handleOnEnter = useCallback(() => {
    const action = async () => {
      app.setFormField(vsid, "userName", state.userName);
      app.setFormField(vsid, "password", await app.cipher(state.password));
    }
    action().then(() => onEnter(vsid));
    return false
  }, [vsid]);

  const usrInputRef = React.useRef<HTMLInputElement>(null);
  const handleUsrChange = useCallback(() => {
    state.userName = usrInputRef.current.value;
    scope.forceUpdate();
  }, [vsid]);

  const pwdInputRef = React.useRef<HTMLInputElement>(null);
  const handlePwdChange = React.useCallback(() => {
    state.password = pwdInputRef.current.value;
    scope.forceUpdate();
  }, [vsid]);

  let divError: ReactNode;
  if (state.errorMessage) {
    divError = <div className="error"> {state.errorMessage}</div>;
  }

  return (
    <div className="centerOne">
      <div className="material-design-card formulario">
        <img className="logo" src="images/big_logo.png" alt="Shopping Stela" />
        <form className="campos" onSubmit={onSubmit}>
          <p>Usuário</p>
          <input ref={usrInputRef} type="text" name="user-name" autoComplete="username" onChange={handleUsrChange} value={state.userName} />
          <p>Senha</p>
          <input
            ref={pwdInputRef}
            type="password"
            name="user-password"
            autoComplete="current-password"
            onChange={handlePwdChange}
            value={state.password}
          />
          {divError}
          <div className="login">
            <button onClick={handleOnEnter}>
              <a className="link">&nbsp;LOGIN&nbsp;</a>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

LoginView.register = () => app.registerView("c677cda52d14", (vsid, props) => <LoginView vsid={vsid} {...props} />);
