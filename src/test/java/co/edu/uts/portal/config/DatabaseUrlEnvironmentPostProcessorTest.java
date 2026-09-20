package co.edu.uts.portal.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * HU de despliegue en Render: traduce DATABASE_URL (postgres://usuario:clave@host:puerto/bd,
 * tal como la entrega el Postgres administrado de Render) al formato que espera el driver JDBC.
 */
class DatabaseUrlEnvironmentPostProcessorTest {

    private final DatabaseUrlEnvironmentPostProcessor processor = new DatabaseUrlEnvironmentPostProcessor();

    @Test
    void traduceDatabaseUrlAPropiedadesJdbc() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("DATABASE_URL", "postgres://user_abc:s3cret@dpg-example-a.oregon-postgres.render.com:5432/portal_uts_prod");

        processor.postProcessEnvironment(env, null);

        assertThat(env.getProperty("spring.datasource.url"))
                .isEqualTo("jdbc:postgresql://dpg-example-a.oregon-postgres.render.com:5432/portal_uts_prod");
        assertThat(env.getProperty("spring.datasource.username")).isEqualTo("user_abc");
        assertThat(env.getProperty("spring.datasource.password")).isEqualTo("s3cret");
    }

    @Test
    void decodificaCaracteresEspecialesEnLaClave() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("DATABASE_URL", "postgres://user:cl%40ve%23rara@host:5432/bd");

        processor.postProcessEnvironment(env, null);

        assertThat(env.getProperty("spring.datasource.password")).isEqualTo("cl@ve#rara");
    }

    @Test
    void usaPuerto5432PorDefectoSiNoVieneEnLaUrl() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("DATABASE_URL", "postgres://user:pass@host/bd");

        processor.postProcessEnvironment(env, null);

        assertThat(env.getProperty("spring.datasource.url")).isEqualTo("jdbc:postgresql://host:5432/bd");
    }

    @Test
    void noHaceNadaSiNoHayDatabaseUrl() {
        MockEnvironment env = new MockEnvironment();

        processor.postProcessEnvironment(env, null);

        assertThat(env.getProperty("spring.datasource.url")).isNull();
    }

    @Test
    void rechazaUnaDatabaseUrlMalformada() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("DATABASE_URL", "esto no es una url::://");

        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class,
                () -> processor.postProcessEnvironment(env, null));
    }
}
