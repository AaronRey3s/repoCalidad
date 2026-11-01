package mx.uaz.sqa.dominio.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * RNF-03 (Trazabilidad y Auditoría): todo registro auditable guarda de forma inmutable
 * la marca de tiempo exacta y el identificador del usuario autor.
 *
 * <p>Las columnas se declaran {@code updatable = false} y la clase no expone setters,
 * por lo que una vez insertados estos valores no pueden modificarse.</p>
 */
@MappedSuperclass
public abstract class RegistroAuditable extends EntidadPersistente {

    @Column(name = "autor", nullable = false, updatable = false, length = 60)
    private String autor;

    @Column(name = "registrado_en", nullable = false, updatable = false)
    private LocalDateTime registradoEn;

    protected RegistroAuditable() {
        // Requerido por JPA
    }

    protected RegistroAuditable(String autor, LocalDateTime registradoEn) {
        this.autor = Validaciones.requerido(autor,
                "El autor del registro es obligatorio (RNF-03)", "autor", 60);
        this.registradoEn = Objects.requireNonNull(registradoEn,
                "La marca de tiempo del registro es obligatoria (RNF-03)");
    }

    public String getAutor() {
        return autor;
    }

    public LocalDateTime getRegistradoEn() {
        return registradoEn;
    }
}
