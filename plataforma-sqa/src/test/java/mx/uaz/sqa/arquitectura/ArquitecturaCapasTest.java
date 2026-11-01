package mx.uaz.sqa.arquitectura;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * RNF-06 (Mantenibilidad arquitectónica): verifica automáticamente que el código respete
 * la arquitectura en capas Presentación → Servicio → Dominio / Persistencia.
 */
@DisplayName("RNF-06 – Arquitectura orientada a objetos en capas")
class ArquitecturaCapasTest {

    private static final JavaClasses CLASES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("mx.uaz.sqa");

    @Test
    @DisplayName("Cada capa solo usa las capas permitidas")
    void respetaLasCapas() {
        layeredArchitecture()
                .consideringOnlyDependenciesInLayers()
                .layer("Presentacion").definedBy("mx.uaz.sqa.presentacion..")
                .layer("Servicio").definedBy("mx.uaz.sqa.servicio..")
                .layer("Dominio").definedBy("mx.uaz.sqa.dominio..")
                .layer("Persistencia").definedBy("mx.uaz.sqa.persistencia..")
                .whereLayer("Presentacion").mayNotBeAccessedByAnyLayer()
                .whereLayer("Servicio").mayOnlyBeAccessedByLayers("Presentacion")
                .whereLayer("Persistencia").mayOnlyBeAccessedByLayers("Servicio")
                .whereLayer("Dominio").mayOnlyBeAccessedByLayers("Presentacion", "Servicio", "Persistencia")
                .check(CLASES);
    }

    @Test
    @DisplayName("El dominio no depende de Spring ni de otras capas")
    void dominioIndependiente() {
        noClasses().that().resideInAPackage("mx.uaz.sqa.dominio..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..",
                        "mx.uaz.sqa.servicio..",
                        "mx.uaz.sqa.presentacion..",
                        "mx.uaz.sqa.persistencia..")
                .check(CLASES);
    }

    @Test
    @DisplayName("Los controladores no acceden directamente a los repositorios")
    void controladoresSinRepositorios() {
        noClasses().that().resideInAPackage("mx.uaz.sqa.presentacion..")
                .should().dependOnClassesThat().resideInAPackage("mx.uaz.sqa.persistencia..")
                .check(CLASES);
    }
}
