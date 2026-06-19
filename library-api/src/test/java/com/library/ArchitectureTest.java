package com.library;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.Architectures;

@AnalyzeClasses(packages = "com.library", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule LAYERS = Architectures.layeredArchitecture()
            .consideringAllDependencies()
            .layer("Interfaces").definedBy("com.library.interfaces..")
            .layer("Application").definedBy("com.library.application..")
            .layer("Domain").definedBy("com.library.domain..")
            .layer("Infrastructure").definedBy("com.library.infrastructure..")
            .whereLayer("Interfaces").mayNotBeAccessedByAnyLayer()
            .whereLayer("Application").mayOnlyBeAccessedByLayers("Interfaces")
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure", "Interfaces")
            .whereLayer("Infrastructure").mayOnlyBeAccessedByLayers("Interfaces", "Application")
            .ignoreDependency(
                    com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage("com.library"),
                    com.tngtech.archunit.core.domain.JavaClass.Predicates.equivalentTo(LibraryApplication.class))
            .ignoreDependency(
                    com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage("com.library.config.."),
                    com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage("com.library.interfaces.."))
            .ignoreDependency(
                    com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage("com.library.config.."),
                    com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage("com.library.domain.."))
            .ignoreDependency(
                    com.tngtech.archunit.core.domain.JavaClass.Predicates.equivalentTo(com.library.infrastructure.security.SecurityConfig.class),
                    com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage("com.library.interfaces.."));

    @ArchTest
    static final ArchRule NO_SPRING_IN_DOMAIN = com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses()
            .that().resideInAPackage("com.library.domain..")
            .should().dependOnClassesThat().resideInAPackage("org.springframework..");

    @ArchTest
    static final ArchRule NO_JAKARTA_IN_DOMAIN = com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses()
            .that().resideInAPackage("com.library.domain..")
            .should().dependOnClassesThat().resideInAPackage("jakarta.persistence..");
}
