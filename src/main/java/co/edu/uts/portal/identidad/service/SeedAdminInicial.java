package co.edu.uts.portal.identidad.service;

import co.edu.uts.portal.identidad.domain.NombreRol;
import co.edu.uts.portal.identidad.domain.Rol;
import co.edu.uts.portal.identidad.domain.Usuario;
import co.edu.uts.portal.identidad.repository.RolRepository;
import co.edu.uts.portal.identidad.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Crea el administrador tecnico inicial SOLO si la tabla usuario esta vacia y se
 * definio la variable de entorno PORTAL_ADMIN_INICIAL_PASSWORD. Asi el sistema
 * puede arrancar por primera vez sin dejar credenciales escritas en el codigo.
 */
@Component
public class SeedAdminInicial implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedAdminInicial.class);

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminInicialProperties props;

    public SeedAdminInicial(UsuarioRepository usuarioRepository, RolRepository rolRepository,
                            PasswordEncoder passwordEncoder, AdminInicialProperties props) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.props = props;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (usuarioRepository.count() > 0) {
            return;
        }
        if (!StringUtils.hasText(props.password())) {
            log.warn("No hay usuarios y PORTAL_ADMIN_INICIAL_PASSWORD no esta definida: "
                    + "no se creo el administrador inicial. Defina la variable y reinicie.");
            return;
        }

        Rol rolTecnico = rolRepository.findByNombre(NombreRol.ADMIN_TECNICO)
                .orElseThrow(() -> new IllegalStateException(
                        "Falta el rol ADMIN_TECNICO; revise la migracion Flyway V2."));

        Usuario admin = new Usuario(props.nombre(), props.correo(),
                passwordEncoder.encode(props.password()));
        admin.setActivo(true);
        admin.agregarRol(rolTecnico);
        usuarioRepository.save(admin);

        log.info("Administrador tecnico inicial creado: {}", props.correo());
    }
}
