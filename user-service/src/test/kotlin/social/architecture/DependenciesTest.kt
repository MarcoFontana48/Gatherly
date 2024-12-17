package social.architecture

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.library.Architectures.layeredArchitecture
import org.junit.jupiter.api.Test

internal class DependenciesTest {
    @Test
    fun layerDependenciesAreRespected() {
        layeredArchitecture().consideringOnlyDependenciesInLayers()
            .layer("Infrastructure").definedBy("social.infrastructure..")
            .layer("Domain").definedBy("social.domain..")
            .whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer()
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("Infrastructure")
            .check(ClassFileImporter().importPackages("social"))
    }
}
