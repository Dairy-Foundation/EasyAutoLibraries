package dev.frozenmilk.easyautolibraries

import dev.frozenmilk.util.collections.Cons
import dev.frozenmilk.util.collections.Ord
import dev.frozenmilk.util.collections.WeightBalancedTreeMap
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

    private var dependencies: WeightBalancedTreeMap<EasyAutoDependency, Nothing?>? = null

    /**
     * adds [dependency] to this configuration
     *
     * applies the settings configured for the [dependency],
     * then invokes [f] on the actual gradle dependency object
     */
    operator fun invoke(dependency: EasyAutoDependency, f: ModuleDependency.() -> Unit = {}) {
        if (WeightBalancedTreeMap.get(Ord.HashCode, dependencies, dependency) != null) return

        // check for mutually exclusive dependencies
        WeightBalancedTreeMap.inorderFold(
            dependency.mutuallyExclusiveDependencies,
            Unit,
        ) { _, incompatibleDependency, reason ->
            val lookup = WeightBalancedTreeMap.get(
                Ord.HashCode,
                dependencies,
                incompatibleDependency,
            )
            if (lookup != null) throw MutuallyExclusiveDependenciesException(
                dependency,
                incompatibleDependency,
                reason,
            )
        }

        WeightBalancedTreeMap.inorderFold(
            dependencies,
            Unit,
        ) { _, incompatibleDependency, _ ->
            val lookup = WeightBalancedTreeMap.get(
                Ord.HashCode,
                incompatibleDependency.mutuallyExclusiveDependencies,
                dependency,
            )
            if (lookup != null) throw MutuallyExclusiveDependenciesException(
                incompatibleDependency,
                dependency,
                lookup.value,
            )
        }

        dependencies = WeightBalancedTreeMap.add(
            Ord.HashCode,
            dependencies,
            dependency,
            null,
        )

        val notation = dependency.toString()
        project.logger.log(
            logLevel(),
            "${sym(name)}(${string(notation)})",
        )
        val gradleDependency = project.dependencies.create(notation) as ModuleDependency
        gradleDependency.isTransitive = dependency.isTransitive
        configuration.dependencies.add(gradleDependency)
        Cons.forEach(dependency.excludedGroups) { group ->
            configuration.exclude(group = group)
        }
        f(gradleDependency)
    }

    override fun toString() = name
}

