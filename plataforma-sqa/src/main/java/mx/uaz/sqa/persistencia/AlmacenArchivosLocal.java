package mx.uaz.sqa.persistencia;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import mx.uaz.sqa.dominio.excepcion.EvidenciaInvalidaException;
import mx.uaz.sqa.dominio.excepcion.RecursoNoEncontradoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Guarda las evidencias en una carpeta local (por defecto {@code ./data/evidencias}).
 * Cada archivo se renombra con un UUID para evitar colisiones y ataques de ruta.
 */
@Component
public class AlmacenArchivosLocal implements AlmacenArchivos {

    static final Set<String> EXTENSIONES_PERMITIDAS = Set.of(
            "pdf", "png", "jpg", "jpeg", "gif", "txt", "md", "csv", "html", "htm", "xml", "json",
            "zip", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "log");

    private static final Pattern CLAVE_VALIDA = Pattern.compile("^[0-9a-f\\-]{36}\\.[a-z0-9]{1,5}$");

    private final Path directorio;

    public AlmacenArchivosLocal(@Value("${sqa.evidencias.directorio:./data/evidencias}") String directorio) {
        this.directorio = Paths.get(directorio).toAbsolutePath().normalize();
    }

    @Override
    public String guardar(String nombreOriginal, byte[] contenido) {
        if (contenido == null || contenido.length == 0) {
            throw new EvidenciaInvalidaException("El archivo de evidencia está vacío", "archivo");
        }
        String extension = extensionDe(nombreOriginal);
        if (!EXTENSIONES_PERMITIDAS.contains(extension)) {
            throw new EvidenciaInvalidaException("Tipo de archivo no permitido (." + extension
                    + "). Usa: " + String.join(", ", EXTENSIONES_PERMITIDAS.stream().sorted().toList()), "archivo");
        }
        String clave = UUID.randomUUID() + "." + extension;
        try {
            Files.createDirectories(directorio);
            Files.write(directorio.resolve(clave), contenido);
        } catch (IOException e) {
            throw new UncheckedIOException("No fue posible guardar el archivo de evidencia", e);
        }
        return clave;
    }

    @Override
    public byte[] leer(String clave) {
        Path ruta = resolver(clave);
        if (!Files.exists(ruta)) {
            throw new RecursoNoEncontradoException("El archivo de evidencia ya no existe en el servidor");
        }
        try {
            return Files.readAllBytes(ruta);
        } catch (IOException e) {
            throw new UncheckedIOException("No fue posible leer el archivo de evidencia", e);
        }
    }

    @Override
    public void eliminar(String clave) {
        try {
            Files.deleteIfExists(resolver(clave));
        } catch (IOException e) {
            throw new UncheckedIOException("No fue posible eliminar el archivo de evidencia", e);
        }
    }

    private Path resolver(String clave) {
        if (clave == null || !CLAVE_VALIDA.matcher(clave).matches()) {
            throw new RecursoNoEncontradoException("Clave de archivo inválida");
        }
        return directorio.resolve(clave);
    }

    static String extensionDe(String nombre) {
        if (nombre == null) {
            return "";
        }
        int punto = nombre.lastIndexOf('.');
        if (punto < 0 || punto == nombre.length() - 1) {
            return "";
        }
        return nombre.substring(punto + 1).toLowerCase(Locale.ROOT);
    }
}
