package dev.frozenmilk.easyautolibraries

import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty

/**
 * a helper class for creating dsl scope groups
 */
@Suppress("unused")
abstract class EasyAutoScope<SELF : EasyAutoScope<SELF>>(
    /**
     * configurations helper
     */
    protected val configurations: EasyAutoConfigurations,
) {
    constructor(root: EasyAutoScopeRoot<*>) : this(root.configurations)

    /**
     * run in scope
     */
    @Suppress("UNCHECKED_CAST")
    operator fun invoke(f: SELF.() -> Unit) {
        f(this as SELF)
    }

    /**
     * create an [EasyAutoDependency] depending on the name of the property
     */
    protected fun dependency(f: (name: String) -> EasyAutoDependency) =
        PropertyDelegateProvider { _: Any?, property ->
            val res = f(property.name)
            ReadOnlyProperty { _: Any?, _ -> res }
        }

    /**
     * the implementation gradle configuration
     */
    val implementation by configurations
    /**
     * the api gradle configuration
     */
    val api by configurations
    /**
     * the runtimeOnly gradle configuration
     */
    val runtimeOnly by configurations
    /**
     * the compileOnly gradle configuration
     */
    val compileOnly by configurations

    /**
     * the testImplementation gradle configuration
     */
    val testImplementation by configurations
    /**
     * the testRuntimeOnly gradle configuration
     */
    val testRuntimeOnly by configurations
    /**
     * the testCompileOnly gradle configuration
     */
    val testCompileOnly by configurations
}