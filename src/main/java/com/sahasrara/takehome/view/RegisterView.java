package com.sahasrara.takehome.view;

import io.dropwizard.views.View;

/**
 * Main registration view.
 */
public class RegisterView extends View {
    public RegisterView() {
        super("register.ftl");
    }
}
