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
