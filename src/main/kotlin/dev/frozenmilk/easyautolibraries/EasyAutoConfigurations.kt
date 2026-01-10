package dev.frozenmilk.easyautolibraries

import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * a helper for keeping track of and creating [EasyAutoConfiguration]s
 */
class EasyAutoConfigurations internal constructor(
    private val project: Project, internal var logLevel: LogLevel
) {
    private val configurations = mutableMapOf<String, EasyAutoConfiguration>()

    /**
     * cached looked up of [EasyAutoConfiguration] by [name]
     */
    operator fun invoke(name: String) = configurations.getOrPut(name) {
        EasyAutoConfiguration(project, ::logLevel, name)
    }

    /**
     * allows you to do
     * ```kt
     * val implementation by configurations
     * ```
     * which looks up the configuration by the name of the field
     */
    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>) = run {
        val res = this(property.name)
        ReadOnlyProperty { _: Any?, _ -> res }
    }
}