package dev.frozenmilk.easyautolibraries

import dev.frozenmilk.util.collections.Ord
import dev.frozenmilk.util.collections.WeightBalancedTreeMap
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * a helper for keeping track of and creating [EasyAutoConfiguration]s
 */
class EasyAutoConfigurations internal constructor(
    private val project: Project,
    internal var logLevel: LogLevel
) {
    private var configurations: WeightBalancedTreeMap<String, EasyAutoConfiguration>? = null

    /**
     * cached looked up of [EasyAutoConfiguration] by [name]
     */
    operator fun invoke(name: String) = WeightBalancedTreeMap.get(
        Ord.HashCode,
        configurations,
        name,
    )?.value ?: run {
        val res = EasyAutoConfiguration(project, ::logLevel, name)
        configurations = WeightBalancedTreeMap.add(
            Ord.HashCode,
            configurations,
            name,
            res,
        )
        res
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