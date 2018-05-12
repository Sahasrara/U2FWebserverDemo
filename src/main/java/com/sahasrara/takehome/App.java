package com.sahasrara.takehome;

import com.sahasrara.takehome.cache.InMemoryRequestCache;
import com.sahasrara.takehome.db.InMemoryLoginDatabase;
import com.yubico.u2f.U2F;
import com.yubico.u2f.attestation.MetadataService;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;

/**
 * Dropwizard Application Class.
 */
public class App extends Application<Config> {
    @Override
    public void initialize(Bootstrap<Config> bootstrap) {
        bootstrap.addBundle(new ViewBundle());
        bootstrap.addBundle(new AssetsBundle());
    }

    @Override
    public void run(Config config, Environment environment) throws Exception {
        environment.jersey().register(new Resource(
                new InMemoryRequestCache(),
                new InMemoryLoginDatabase(),
                new U2F(),
                new MetadataService()));
    }

    public static void main(String... args) throws Exception {
        new App().run(args);
    }
}
