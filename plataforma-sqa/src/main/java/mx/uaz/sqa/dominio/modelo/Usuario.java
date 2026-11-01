package mx.uaz.sqa.dominio.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import mx.uaz.sqa.dominio.excepcion.DatosInvalidosException;

/**
 * Usuario del sistema con uno de los roles de la Tabla 4 del SRS.
 * La contraseña se guarda únicamente como hash BCrypt.
 */
@Entity
@Table(name = "usuario")
public class Usuario extends EntidadPersistente {

    @Column(nullable = false, unique = true, length = 60)
    private String username;

    @Column(name = "nombre_completo", nullable = false, length = 120)
    private String nombreCompleto;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    @Column(nullable = false)
    private boolean activo;

    protected Usuario() {
        // Requerido por JPA
    }

    public Usuario(String username, String nombreCompleto, String passwordHash, Rol rol) {
        this.username = Validaciones.requerido(username, "El usuario es obligatorio", "username", 60);
        this.nombreCompleto = Validaciones.requerido(nombreCompleto, "El nombre es obligatorio", "nombreCompleto", 120);
        this.passwordHash = Validaciones.requerido(passwordHash, "La contraseña es obligatoria", "password", 100);
        if (rol == null) {
            throw new DatosInvalidosException("El rol es obligatorio", "rol");
        }
        this.rol = rol;
        this.activo = true;
    }

    public String getUsername() {
        return username;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Rol getRol() {
        return rol;
    }

    public boolean isActivo() {
        return activo;
    }
}
