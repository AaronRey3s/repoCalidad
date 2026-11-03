package mx.uaz.sqa.persistencia;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import mx.uaz.sqa.dominio.excepcion.EvidenciaInvalidaException;
import mx.uaz.sqa.dominio.excepcion.RecursoNoEncontradoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("Almacenamiento local de evidencias")
class AlmacenArchivosLocalTest {

    @TempDir
    Path carpeta;

    @Test
    @DisplayName("Guarda, lee y elimina un archivo permitido")
    void cicloCompleto() {
        AlmacenArchivosLocal almacen = new AlmacenArchivosLocal(carpeta.toString());
        byte[] contenido = "evidencia".getBytes(StandardCharsets.UTF_8);

        String clave = almacen.guardar("Acta Revisión.PDF", contenido);

        assertTrue(clave.endsWith(".pdf"));
        assertTrue(Files.exists(carpeta.resolve(clave)));
        assertArrayEquals(contenido, almacen.leer(clave));
        almacen.eliminar(clave);
        assertFalse(Files.exists(carpeta.resolve(clave)));
    }

    @Test
    @DisplayName("Rechaza archivos vacíos y extensiones no permitidas")
    void rechazaArchivosInvalidos() {
        AlmacenArchivosLocal almacen = new AlmacenArchivosLocal(carpeta.toString());

        assertThrows(EvidenciaInvalidaException.class, () -> almacen.guardar("a.pdf", new byte[0]));
        assertThrows(EvidenciaInvalidaException.class, () -> almacen.guardar("virus.exe", new byte[] {1}));
        assertThrows(EvidenciaInvalidaException.class, () -> almacen.guardar("sin-extension", new byte[] {1}));
    }

    @Test
    @DisplayName("Impide leer rutas arbitrarias (path traversal) o archivos inexistentes")
    void proteccionDeRutas() {
        AlmacenArchivosLocal almacen = new AlmacenArchivosLocal(carpeta.toString());

        assertThrows(RecursoNoEncontradoException.class, () -> almacen.leer("../../etc/passwd"));
        assertThrows(RecursoNoEncontradoException.class,
                () -> almacen.leer("123e4567-e89b-12d3-a456-426614174000.pdf"));
    }

    @Test
    @DisplayName("Obtiene la extensión en minúsculas")
    void extension() {
        assertEquals("pdf", AlmacenArchivosLocal.extensionDe("a.b.PDF"));
        assertEquals("", AlmacenArchivosLocal.extensionDe("archivo."));
        assertEquals("", AlmacenArchivosLocal.extensionDe(null));
    }
}
