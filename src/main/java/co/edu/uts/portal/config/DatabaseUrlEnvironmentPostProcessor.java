package co.edu.uts.portal.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Traduce la variable DATABASE_URL (formato "postgres://usuario:clave@host:puerto/basededatos",
 * como la que entrega Render para su Postgres administrado) a las tres propiedades que
 * necesita el driver JDBC: spring.datasource.url/username/password.
 *
 * Solo actua si DATABASE_URL esta presente; si no, el datasource sigue viniendo de
 * application.yml (DB_HOST/DB_PORT/DB_NAME/DB_USERNAME/DB_PASSWORD, o de
 * application-dev.yml en desarrollo local). Se registra via META-INF/spring.factories
 * porque debe ejecutarse antes de que Spring arme el DataSource.
 */
public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String databaseUrl = environment.getProperty("DATABASE_URL");
        if (databaseUrl == null || databaseUrl.isBlank()) {
            return;
        }

        try {
            URI uri = new URI(databaseUrl);
            String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + puerto(uri) + uri.getPath();

            String usuario = null;
            String clave = null;
            String userInfo = uri.getUserInfo();
            if (userInfo != null && userInfo.contains(":")) {
                int i = userInfo.indexOf(':');
                usuario = URLDecoder.decode(userInfo.substring(0, i), StandardCharsets.UTF_8);
                clave = URLDecoder.decode(userInfo.substring(i + 1), StandardCharsets.UTF_8);
            }

            Map<String, Object> propiedades = new LinkedHashMap<>();
            propiedades.put("spring.datasource.url", jdbcUrl);
            if (usuario != null) {
                propiedades.put("spring.datasource.username", usuario);
            }
            if (clave != null) {
                propiedades.put("spring.datasource.password", clave);
            }

            environment.getPropertySources()
                    .addFirst(new MapPropertySource("databaseUrl", propiedades));
        } catch (URISyntaxException e) {
            throw new IllegalStateException("DATABASE_URL no es una URL valida: " + e.getMessage(), e);
        }
    }

    private int puerto(URI uri) {
        return uri.getPort() != -1 ? uri.getPort() : 5432;
    }
}
