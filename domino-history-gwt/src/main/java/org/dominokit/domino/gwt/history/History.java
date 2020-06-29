package org.dominokit.domino.gwt.history;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class History {

    @JsProperty
    public native double getLength();

    public native void back();

    public native void forward();

    public native void pushState(JsState jsState, String title, String url);

    public native void replaceState(JsState jsState, String title, String url);
}
