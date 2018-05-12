package com.sahasrara.takehome.view;

import io.dropwizard.views.View;

/**
 * Main login view.
 */
public class LoginView extends View {
    public LoginView() {
        super("login.ftl");
    }
}
