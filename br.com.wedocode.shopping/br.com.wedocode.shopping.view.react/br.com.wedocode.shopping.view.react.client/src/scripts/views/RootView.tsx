import React from "react";
import app, { type ViewProps } from "../App";

export type RootViewState = {
  contentViewId?: string;
  errorMessage?: string;
};

export default function RootView({ vsid }: ViewProps) {
  const { state } = app.bindView<RootViewState>(vsid);

  if (state.errorMessage) {
    return <div className="error">{state.errorMessage}</div>;
  }

  if (state.contentViewId) {
    return app.createView(state.contentViewId);
  }

  return <div className="error">Falta conteúdo para a página inicial</div>;
}

RootView.register = () => app.registerView("f2d345c4a610", (vsid, props) => <RootView vsid={vsid} {...props} />);
