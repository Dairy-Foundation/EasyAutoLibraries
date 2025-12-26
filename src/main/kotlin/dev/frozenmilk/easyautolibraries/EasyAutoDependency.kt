package dev.frozenmilk.easyautolibraries

import dev.frozenmilk.util.collections.Cons
import dev.frozenmilk.util.collections.Ord
import dev.frozenmilk.util.collections.WeightBalancedTreeMap

/**
 * represents a gradle dependency
 *
 * the `toString()` implementation of this class provides the dependency notation:
 *
 * `group:artifact:version`
 *
 * if [version] is an empty string, the version specifier will be removed entirely:
 *
 * `group:artifact`
 *
 * if [force] is true, `!!` will be appended to the version,
 * which tells gradle to enforce the version strictly
 */
class EasyAutoDependency(
    private val group: String,
    private val artifact: String,
    /**
     * a dynamic fallback version
     */
    private val defaultVersion: () -> String,
    /**
     * optional configuration function
     */
    f: Scope.() -> Unit = {},
) {
    constructor(
        group: String,
        artifact: String,
        version: String,
        f: Scope.() -> Unit = {},
    ) : this(
        group,
        artifact,
        { version },
        f,
    )

    @Suppress("unused")
    inner class Scope internal constructor() {
        /**
         * excludes a module's group from this dependencies transitive dependencies
         *
         * @see org.gradle.api.artifacts.ModuleDependency.exclude
         */
        fun excludeGroup(group: String) {
            excludedGroups = Cons.cons(group, excludedGroups)
        }

        /**
         * if any transitive dependencies should be gotten
         *
         * true by default
         */
        var isTransitive by this@EasyAutoDependency::isTransitive

        /**
         * if the version should enforced
         */
        var force by this@EasyAutoDependency::force

        /**
         * the version of the dependency
         */
        var version by this@EasyAutoDependency::version

        /**
         * specifies that this is incompatible with another [dependency], and why
         */
        fun incompatibleWith(dependency: EasyAutoDependency, reason: String? = null) {
            mutuallyExclusiveDependencies = WeightBalancedTreeMap.add(
                Ord.HashCode,
                mutuallyExclusiveDependencies,
                dependency,
                reason,
            )
        }
    }

    private var _version: String? = null

    /**
     * the version of the dependency
     */
    var version: String
        get() = _version ?: defaultVersion()
        set(value) {
            _version = value
        }

    private val scope = Scope()

    /**
     * specify the [version] and optionally configure this with [f]
     */
    operator fun invoke(version: String, f: Scope.() -> Unit = {}) = apply {
        this.version = version
        f(scope)
    }

    /**
     * configure this with [f]
     */
    operator fun invoke(f: Scope.() -> Unit) = apply { f(scope) }

    internal var excludedGroups: Cons<String>? = null
    internal var isTransitive: Boolean = true
    internal var force: Boolean = true
    internal var mutuallyExclusiveDependencies: WeightBalancedTreeMap<EasyAutoDependency, String?>? =
        null

    init {
        f(scope)
    }

    override fun toString() = "$group:$artifact${
        if (version.isEmpty()) ""
        else ":$version${if (force) "!!" else ""}"
    }"
}