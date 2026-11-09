package mx.uaz.sqa.dominio.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import mx.uaz.sqa.dominio.excepcion.EvidenciaInvalidaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("Evidencia de técnica SQA (RU-05, RS-08)")
class EvidenciaTest {

    @ParameterizedTest(name = "{0} → {1}")
    @CsvSource({
            "https://github.com/riagalv/repoCalidad/pull/3, true",
            "http://localhost:8080/reporte, true",
            "HTTPS://SONARCLOUD.IO, true",
            "ftp://servidor/archivo.zip, false",
            "github.com/riagalv/repoCalidad, false",
            "https://, false",
            "'https://url con espacios', false",
            "'', false"
    })
    @DisplayName("Solo se aceptan URLs http/https completas")
    void validaUrls(String url, boolean esperado) {
        assertEquals(esperado, Evidencia.esUrlValida(url));
    }

    @Test
    @DisplayName("deUrl() con texto vacío equivale a 'sin evidencia'")
    void urlVaciaEsNinguna() {
        Evidencia evidencia = Evidencia.deUrl("   ");

        assertFalse(evidencia.tieneRespaldo());
        assertEquals("Sin evidencia", evidencia.describir());
    }

    @Test
    @DisplayName("deUrl() rechaza una URL inválida señalando el campo evidenciaUrl")
    void rechazaUrlInvalida() {
        EvidenciaInvalidaException error = assertThrows(EvidenciaInvalidaException.class,
                () -> Evidencia.deUrl("no-es-una-url"));

        assertEquals("evidenciaUrl", error.getCampo());
    }

    @Test
    @DisplayName("deUrl() rechaza URLs de más de 500 caracteres")
    void rechazaUrlLarga() {
        String larga = "https://example.com/" + "a".repeat(500);

        assertThrows(EvidenciaInvalidaException.class, () -> Evidencia.deUrl(larga));
    }

    @Test
    @DisplayName("conArchivo() conserva la URL y agrega el archivo")
    void agregaArchivo() {
        Evidencia evidencia = Evidencia.deUrl("https://github.com/x").conArchivo("clave.pdf", "acta.pdf");

        assertTrue(evidencia.tieneUrl());
        assertTrue(evidencia.tieneArchivo());
        assertEquals("clave.pdf", evidencia.getArchivoClave());
        assertEquals("acta.pdf", evidencia.getArchivoNombre());
        assertEquals("URL: https://github.com/x | Archivo: acta.pdf", evidencia.describir());
    }

    @Test
    @DisplayName("conArchivo() exige una clave y usa la clave si no hay nombre original")
    void conArchivoValidaClave() {
        assertThrows(EvidenciaInvalidaException.class, () -> Evidencia.ninguna().conArchivo(" ", "x.pdf"));
        assertEquals("k.pdf", Evidencia.ninguna().conArchivo("k.pdf", null).getArchivoNombre());
        assertEquals(255, Evidencia.ninguna().conArchivo("k.pdf", "n".repeat(300)).getArchivoNombre().length());
    }

    @Test
    @DisplayName("combinarCon(): lo nuevo reemplaza, lo ausente se conserva")
    void combina() {
        Evidencia existente = Evidencia.deUrl("https://a.com").conArchivo("k1.pdf", "uno.pdf");

        Evidencia soloUrl = existente.combinarCon(Evidencia.deUrl("https://b.com"));
        assertEquals("https://b.com", soloUrl.getUrl());
        assertEquals("k1.pdf", soloUrl.getArchivoClave());

        Evidencia soloArchivo = existente.combinarCon(Evidencia.ninguna().conArchivo("k2.pdf", "dos.pdf"));
        assertEquals("https://a.com", soloArchivo.getUrl());
        assertEquals("dos.pdf", soloArchivo.getArchivoNombre());

        assertSame(existente, existente.combinarCon(null));
    }

    @Test
    @DisplayName("Solo archivo: describe únicamente el archivo")
    void describeSoloArchivo() {
        assertEquals("Archivo: a.png", Evidencia.ninguna().conArchivo("k.png", "a.png").describir());
    }
}
