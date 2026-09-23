// Boton de mostrar/ocultar contraseña, reutilizable en cualquier campo type="password"
// que este envuelto en un .input-group junto a un boton .toggle-clave.
document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('.toggle-clave').forEach(function (boton) {
        boton.addEventListener('click', function () {
            var grupo = boton.closest('.input-group');
            var campo = grupo.querySelector('input');
            var oculto = campo.getAttribute('type') === 'password';
            campo.setAttribute('type', oculto ? 'text' : 'password');
            boton.querySelector('.icono-ojo-abierto').hidden = !oculto;
            boton.querySelector('.icono-ojo-cerrado').hidden = oculto;
            boton.setAttribute('aria-label', oculto ? 'Ocultar contraseña' : 'Mostrar contraseña');
        });
    });
});

// =====================================================================
// Reunión con la directora (22/9/2026): el portal se veía "plano".
// =====================================================================
document.addEventListener('DOMContentLoaded', function () {
    var reducirMovimiento = window.matchMedia &&
        window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    // --- Aparecer al hacer scroll. Lo que ya está en pantalla al cargar se deja visible
    //     de una vez (sin parpadeo); lo de más abajo aparece suavemente al llegar a ello.
    var aparecer = document.querySelectorAll('.aparecer');
    if (!reducirMovimiento && 'IntersectionObserver' in window) {
        var alto = window.innerHeight || document.documentElement.clientHeight;
        aparecer.forEach(function (el) {
            if (el.getBoundingClientRect().top < alto) {
                el.classList.add('visible');
            }
        });
        document.documentElement.classList.add('js');
        var observador = new IntersectionObserver(function (entradas) {
            entradas.forEach(function (entrada) {
                if (entrada.isIntersecting) {
                    entrada.target.classList.add('visible');
                    observador.unobserve(entrada.target);
                }
            });
        }, {rootMargin: '0px 0px -40px 0px'});
        aparecer.forEach(function (el) {
            if (!el.classList.contains('visible')) {
                observador.observe(el);
            }
        });
    }

    // --- Botones "Copiar" del contacto directo (el dato va en data-copiar).
    function copiarTexto(texto) {
        if (navigator.clipboard && window.isSecureContext) {
            return navigator.clipboard.writeText(texto);
        }
        return new Promise(function (resolver, rechazar) {
            var area = document.createElement('textarea');
            area.value = texto;
            area.setAttribute('readonly', '');
            area.style.position = 'fixed';
            area.style.opacity = '0';
            document.body.appendChild(area);
            area.select();
            try {
                document.execCommand('copy') ? resolver() : rechazar();
            } catch (e) {
                rechazar(e);
            } finally {
                document.body.removeChild(area);
            }
        });
    }

    var aviso = document.createElement('div');
    aviso.className = 'visually-hidden';
    aviso.setAttribute('aria-live', 'polite');
    document.body.appendChild(aviso);

    document.querySelectorAll('[data-copiar]').forEach(function (boton) {
        var etiqueta = boton.querySelector('.boton-copiar-texto');
        var textoOriginal = etiqueta ? etiqueta.textContent : '';
        boton.addEventListener('click', function () {
            copiarTexto(boton.getAttribute('data-copiar')).then(function () {
                boton.classList.add('copiado');
                if (etiqueta) {
                    etiqueta.textContent = '¡Copiado!';
                }
                aviso.textContent = 'Copiado: ' + boton.getAttribute('data-copiar');
                setTimeout(function () {
                    boton.classList.remove('copiado');
                    if (etiqueta) {
                        etiqueta.textContent = textoOriginal;
                    }
                }, 2200);
            }, function () {
                aviso.textContent = 'No se pudo copiar. Selecciona el texto y cópialo manualmente.';
            });
        });
    });

    // --- Panel: aviso inmediato si la imagen pesa demasiado, y vista previa antes de subir.
    document.querySelectorAll('input[type="file"][data-max-bytes]').forEach(function (campo) {
        campo.addEventListener('change', function () {
            var archivo = campo.files && campo.files[0];
            var maximo = parseInt(campo.getAttribute('data-max-bytes'), 10);
            var vista = document.getElementById(campo.getAttribute('data-vista-previa'));
            campo.setCustomValidity('');
            if (vista) {
                vista.hidden = true;
            }
            if (!archivo) {
                return;
            }
            if (archivo.size > maximo) {
                campo.setCustomValidity('La imagen pesa ' + (archivo.size / 1048576).toFixed(1) +
                    ' MB y el máximo es 5 MB. Redúcela e inténtalo otra vez.');
                campo.reportValidity();
                return;
            }
            if (vista && /^image\//.test(archivo.type)) {
                vista.src = URL.createObjectURL(archivo);
                vista.hidden = false;
            }
        });
    });

    // --- Formularios con imagen: un solo envío (un doble clic en "Guardar" subía la imagen dos veces).
    document.querySelectorAll('form[enctype="multipart/form-data"]').forEach(function (form) {
        form.addEventListener('submit', function () {
            form.querySelectorAll('button[type="submit"]').forEach(function (boton) {
                boton.disabled = true;
                boton.textContent = 'Guardando…';
            });
        });
    });

    // --- Carrusel de destacados: avanza solo cada 6 s, salvo que la persona haya pedido
    //     menos movimiento en su sistema (entonces solo se mueve con los botones).
    if (window.bootstrap && bootstrap.Carousel) {
        document.querySelectorAll('[data-carrusel-portal]').forEach(function (el) {
            bootstrap.Carousel.getOrCreateInstance(el, {
                interval: reducirMovimiento ? false : 6000,
                ride: reducirMovimiento ? false : 'carousel',
                pause: 'hover'
            });
        });
    }
});
