// Mejoras progresivas de la interfaz. La aplicacion funciona igual sin JavaScript:
// todas las reglas de negocio se validan en el servidor.
(function () {
    'use strict';

    // Dashboard: cambiar de proyecto al seleccionar en la lista
    document.querySelectorAll('select[data-autoenviar]').forEach(function (select) {
        select.addEventListener('change', function () {
            if (select.form) {
                select.form.submit();
            }
        });
    });

    // CU02: vista previa de los rangos del semaforo mientras se capturan los umbrales
    var formMetrica = document.getElementById('formulario-metrica');
    if (formMetrica) {
        var sentido = formMetrica.querySelector('[name="sentido"]');
        var optimo = formMetrica.querySelector('[name="limiteOptimo"]');
        var critico = formMetrica.querySelector('[name="limiteCritico"]');
        var salida = document.getElementById('vista-previa-umbrales');
        var actualizar = function () {
            var o = parseFloat(optimo.value);
            var c = parseFloat(critico.value);
            if (isNaN(o) || isNaN(c)) {
                salida.textContent = '';
                return;
            }
            var mayor = sentido.value === 'MAYOR_ES_MEJOR';
            var coherente = mayor ? o > c : o < c;
            if (!coherente) {
                salida.textContent = 'Los umbrales se cruzan: ' + (mayor
                    ? 'el \u00f3ptimo debe ser MAYOR que el cr\u00edtico.'
                    : 'el \u00f3ptimo debe ser MENOR que el cr\u00edtico.');
                salida.style.color = '#d93025';
                return;
            }
            salida.style.color = '';
            salida.textContent = mayor
                ? 'Verde \u2265 ' + o + ' \u00b7 Amarillo \u2265 ' + c + ' y < ' + o + ' \u00b7 Rojo < ' + c
                : 'Verde \u2264 ' + o + ' \u00b7 Amarillo > ' + o + ' y \u2264 ' + c + ' \u00b7 Rojo > ' + c;
        };
        [sentido, optimo, critico].forEach(function (el) {
            if (el) {
                el.addEventListener('input', actualizar);
                el.addEventListener('change', actualizar);
            }
        });
        actualizar();
    }

    // RS-08: avisar antes de enviar si el archivo de evidencia excede el limite del servidor
    var formEvidencia = document.getElementById('formulario-evidencia');
    if (formEvidencia) {
        var archivo = formEvidencia.querySelector('input[type="file"]');
        var error = document.getElementById('error-archivo');
        formEvidencia.addEventListener('submit', function (evento) {
            var maximo = parseInt(archivo.getAttribute('data-max-bytes'), 10);
            if (archivo.files.length > 0 && archivo.files[0].size > maximo) {
                evento.preventDefault();
                error.textContent = 'El archivo pesa m\u00e1s de 10 MB. Compr\u00edmelo o comparte una URL en su lugar.';
                error.hidden = false;
            }
        });
    }
})();
