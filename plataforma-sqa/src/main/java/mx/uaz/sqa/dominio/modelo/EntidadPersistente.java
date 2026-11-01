package mx.uaz.sqa.dominio.modelo;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

/**
 * Superclase de todas las entidades persistentes: concentra la identidad.
 */
@MappedSuperclass
public abstract class EntidadPersistente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return id;
    }

    public boolean esNueva() {
        return id == null;
    }
}
