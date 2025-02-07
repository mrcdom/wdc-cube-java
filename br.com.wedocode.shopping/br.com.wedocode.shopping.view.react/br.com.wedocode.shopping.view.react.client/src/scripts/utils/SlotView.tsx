import React, { ReactNode } from "react";
import app, { type ViewProps } from "../App";

export type SlotViewState = {
  slot?: string;
};

export default function SlotView({ vsid }: ViewProps) {
  const { state } = app.bindView<SlotViewState>(vsid);

  let contentView: ReactNode;
  if (state.slot) {
    contentView = app.createView(state.slot);
  } else {
    contentView = (<div></div>);
  }

  return contentView;
}

SlotView.register = () => app.registerView("798574115fcd", (vsid, props) => <SlotView vsid={vsid} {...props} />);
