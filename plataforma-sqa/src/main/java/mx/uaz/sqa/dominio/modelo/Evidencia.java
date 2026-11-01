package mx.uaz.sqa.dominio.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.net.URI;
import java.net.URISyntaxException;
import mx.uaz.sqa.dominio.excepcion.EvidenciaInvalidaException;

/**
 * Evidencia documental de una técnica SQA (RU-05, RS-08): una URL válida y/o un archivo adjunto.
 * Objeto de valor inmutable.
 */
@Embeddable
public class Evidencia {

    public static final int LONGITUD_MAXIMA_URL = 500;

    @Column(name = "evidencia_url", length = LONGITUD_MAXIMA_URL)
    private String url;

    @Column(name = "evidencia_archivo_clave", length = 120)
    private String archivoClave;

    @Column(name = "evidencia_archivo_nombre", length = 255)
    private String archivoNombre;

    protected Evidencia() {
        // Requerido por JPA
    }

    private Evidencia(String url, String archivoClave, String archivoNombre) {
        this.url = url;
        this.archivoClave = archivoClave;
        this.archivoNombre = archivoNombre;
    }

    public static Evidencia ninguna() {
        return new Evidencia(null, null, null);
    }

    /**
     * Crea una evidencia a partir de una URL. Si el texto viene vacío devuelve {@link #ninguna()};
     * si no está vacío pero no es una URL http/https válida, lanza {@link EvidenciaInvalidaException}.
     */
    public static Evidencia deUrl(String texto) {
        if (texto == null || texto.isBlank()) {
            return ninguna();
        }
        String limpia = texto.trim();
        if (limpia.length() > LONGITUD_MAXIMA_URL) {
            throw new EvidenciaInvalidaException(
                    "La URL de evidencia excede " + LONGITUD_MAXIMA_URL + " caracteres", "evidenciaUrl");
        }
        if (!esUrlValida(limpia)) {
            throw new EvidenciaInvalidaException(
                    "La URL de evidencia no es válida: usa una dirección completa que inicie con http:// o https://",
                    "evidenciaUrl");
        }
        return new Evidencia(limpia, null, null);
    }

    /** Devuelve una nueva evidencia que conserva la URL e incorpora el archivo almacenado. */
    public Evidencia conArchivo(String clave, String nombreOriginal) {
        if (clave == null || clave.isBlank()) {
            throw new EvidenciaInvalidaException("El archivo de evidencia no se almacenó correctamente", "archivo");
        }
        String nombre = (nombreOriginal == null || nombreOriginal.isBlank()) ? clave : nombreOriginal.trim();
        if (nombre.length() > 255) {
            nombre = nombre.substring(nombre.length() - 255);
        }
        return new Evidencia(url, clave, nombre);
    }

    /** Combina esta evidencia con otra: los datos nuevos reemplazan a los existentes. */
    public Evidencia combinarCon(Evidencia nueva) {
        if (nueva == null) {
            return this;
        }
        String urlFinal = nueva.url != null ? nueva.url : url;
        boolean archivoNuevo = nueva.tieneArchivo();
        return new Evidencia(urlFinal,
                archivoNuevo ? nueva.archivoClave : archivoClave,
                archivoNuevo ? nueva.archivoNombre : archivoNombre);
    }

    public static boolean esUrlValida(String texto) {
        if (texto == null || texto.isBlank()) {
            return false;
        }
        try {
            URI uri = new URI(texto.trim());
            String esquema = uri.getScheme();
            String host = uri.getHost();
            return esquema != null
                    && ("http".equalsIgnoreCase(esquema) || "https".equalsIgnoreCase(esquema))
                    && host != null && !host.isBlank();
        } catch (URISyntaxException e) {
            return false;
        }
    }

    public boolean tieneUrl() {
        return esUrlValida(url);
    }

    public boolean tieneArchivo() {
        return archivoClave != null && !archivoClave.isBlank();
    }

    /** RS-08: hay respaldo cuando existe un archivo adjunto o una URL válida. */
    public boolean tieneRespaldo() {
        return tieneUrl() || tieneArchivo();
    }

    public String describir() {
        if (!tieneRespaldo()) {
            return "Sin evidencia";
        }
        StringBuilder texto = new StringBuilder();
        if (tieneUrl()) {
            texto.append("URL: ").append(url);
        }
        if (tieneArchivo()) {
            if (texto.length() > 0) {
                texto.append(" | ");
            }
            texto.append("Archivo: ").append(archivoNombre);
        }
        return texto.toString();
    }

    public String getUrl() {
        return url;
    }

    public String getArchivoClave() {
        return archivoClave;
    }

    public String getArchivoNombre() {
        return archivoNombre;
    }
}
