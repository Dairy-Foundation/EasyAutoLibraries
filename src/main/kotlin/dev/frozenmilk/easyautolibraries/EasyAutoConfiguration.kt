package dev.frozenmilk.easyautolibraries

import org.gradle.api.Project
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.logging.LogLevel
import org.gradle.kotlin.dsl.exclude

/**
 * represents a gradle configuration
 */
class EasyAutoConfiguration internal constructor(
    private val project: Project,
    private val logLevel: () -> LogLevel,
    private val name: String,
) {
    private val configuration = project.configurations.getByName(name)

    class MutuallyExclusiveDependenciesException(
        a: EasyAutoDependency,
        b: EasyAutoDependency,
        reason: String?,
    ) : RuntimeException(
        "found mutually exclusive dependencies:\n" //
                + "\"$a\" is mutually exclusive with \"$b\"" //
                + if (reason != null) "\nbecause $reason" else "" //
    )

    private val dependencies = mutableSetOf<EasyAutoDependency>()//: WeightBalancedTreeMap<EasyAutoDependency, Nothing?>? = null

    /**
     * adds [dependency] to this configuration
     *
     * applies the settings configured for the [dependency],
     * then invokes [f] on the actual gradle dependency object
     */
    operator fun invoke(dependency: EasyAutoDependency, f: ModuleDependency.() -> Unit = {}) {
        if (dependencies.contains(dependency)) return

        // check for mutually exclusive dependencies
        dependency.mutuallyExclusiveDependencies.forEach { (incompatibleDependency, reason) ->
            if (dependencies.contains(incompatibleDependency)) throw MutuallyExclusiveDependenciesException(
                dependency,
                incompatibleDependency,
                reason,
            )
        }
        dependencies.forEach { incompatibleDependency ->
            if (dependency.mutuallyExclusiveDependencies.contains(incompatibleDependency)) throw MutuallyExclusiveDependenciesException(
                dependency,
                incompatibleDependency,
                dependency.mutuallyExclusiveDependencies[incompatibleDependency],
            )
        }

        dependencies.add(dependency)

        val notation = dependency.toString()
        project.logger.log(
            logLevel(),
            "${sym(name)}(${string(notation)})",
        )
        val gradleDependency = project.dependencies.create(notation) as ModuleDependency
        gradleDependency.isTransitive = dependency.isTransitive
        configuration.dependencies.add(gradleDependency)
        dependency.exclusions.forEach { (group, module) ->
            configuration.exclude(group = group, module = module)
        }
        f(gradleDependency)
    }

    override fun toString() = name
}

