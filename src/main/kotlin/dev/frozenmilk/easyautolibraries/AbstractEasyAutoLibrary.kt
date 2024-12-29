package dev.frozenmilk.easyautolibraries

import dev.frozenmilk.easyautolibraries.util.EasyAutoLibraryDynamicObject
import dev.frozenmilk.easyautolibraries.util.SubPropertyAccess
import org.gradle.api.Action
import org.gradle.api.NonExtensible
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.internal.DynamicObjectAware
import org.gradle.internal.extensibility.NoConventionMapping
import org.gradle.internal.metaobject.DynamicInvokeResult
import org.gradle.internal.metaobject.MethodMixIn
import org.gradle.internal.metaobject.PropertyMixIn
import java.net.URI
import kotlin.reflect.KProperty

@NonExtensible
@NoConventionMapping
@Suppress("LeakingThis", "Member")
abstract class AbstractEasyAutoLibrary<SELF: AbstractEasyAutoLibrary<SELF>> (
	final override val name: String,
	private val _project: Project? = null,
) : EasyAutoLibraryMarker<SELF>(), DynamicObjectAware, MethodMixIn, PropertyMixIn, SubPropertyAccess {
	private val dynamicAccess = EasyAutoLibraryDynamicObject(this)
	val subLibraries
		get() = dynamicAccess.values.filterIsInstance<AbstractEasyAutoLibrary<*>>()
	val dependencies
		get() = dynamicAccess.values.filterIsInstance<EasyAutoLibraryDependency>()
	val repositories: List<EasyAutoLibraryRepository>
		get() = parent?.repositories ?: dynamicAccess.values.filterIsInstance<EasyAutoLibraryRepository>()

	override fun getAsDynamicObject() = dynamicAccess
	override fun getAdditionalMethods() = dynamicAccess
	override fun getAdditionalProperties() = dynamicAccess
	private val dynamicThis = DynamicInvokeResult.found(this)
	override fun tryGetProperty(): DynamicInvokeResult = dynamicThis

	private var _configurationNames: Set<String>? = null

	/**
	 * the gradle dependencies configuration name, automatically resolves to find the most local one
	 */
	var configurationNames: Set<String>
		get() =
			// trys the configuration name of the parent
			try {
				_configurationNames ?: parent?.configurationNames
			}
			// if it crashes, we know there is a parent
			catch (_: IllegalStateException) {
				error("unable to find configuration for $name, try setting the `configurationName` property in $nameTree")
			}
			// otherwise, there isn't and we don't recommend interacting with it
				?: error("unable to find configuration for $name, set the `configurationName` property")
		set(value) {
			_configurationNames = value
		}

	val project: Project
		get() = _project ?: parent?.project
			?: error("incorrectly set up FTCLibrary tree, this either needs a parent with a concrete project, or its own")

	/**
	 * registers [lib] as a SubLibrary but does not [access] it, you can then use [action] to configure it before it is accessed via [access]
	 */
	@JvmOverloads
	fun <LIB : AbstractEasyAutoLibrary<LIB>> registerSubLibrary(
		lib: LIB,
		action: Action<LIB> = Action {}
	) = dynamicAccess.registerSubLibrary(lib).also { action.execute(it) }

	/**
	 * registers but does not [access] repository, you can then use [action] to configure it before it is accessed via [access]
	 *
	 * or you can pass it to dependencies
	 */
	@JvmOverloads
	fun getOrRegisterRepository(
		name: String,
		configureRepositoryHandler: RepositoryHandler.() -> Unit,
		action: Action<EasyAutoLibraryRepository> = Action {}
	) = dynamicAccess.getOrRegisterRepository(name, configureRepositoryHandler).also { action.execute(it) }

	/**
	 * registers but does not [access] repository, you can then use [action] to configure it before it is accessed via [access]
	 *
	 * or you can pass it to dependencies
	 */
	@JvmOverloads
	fun getOrRegisterRepository(
		name: String,
		uri: String,
		action: Action<EasyAutoLibraryRepository> = Action {}
	) = dynamicAccess.getOrRegisterRepository(name, uri).also { action.execute(it) }

	/**
	 * registers but does not [access] repository, you can then use [action] to configure it before it is accessed via [access]
	 *
	 * or you can pass it to dependencies
	 */
	@JvmOverloads
	fun getOrRegisterRepository(
		name: String,
		uri: URI,
		action: Action<EasyAutoLibraryRepository> = Action {}
	) = dynamicAccess.getOrRegisterRepository(name, uri).also { action.execute(it) }

	/**
	 * registers but does not [apply] dependency, you can then use [action] to configure it before it is applied via [apply]
	 */
	@JvmOverloads
	fun registerDependency(
		name: String,
		notation: (version: String) -> String,
		repository: () -> EasyAutoLibraryRepository,
		action: Action<EasyAutoLibraryDependency> = Action {}
	) = dynamicAccess.registerDependency(name, notation, repository).also { action.execute(it) }

	/**
	 * registers but does not [apply] dependency, you can then use [action] to configure it before it is applied via [apply]
	 */
	@JvmOverloads
	fun registerDependency(
		name: String,
		notation: (version: String) -> String,
		repositoryName: String,
		action: Action<EasyAutoLibraryDependency> = Action {}
	) = dynamicAccess.registerDependency(name, notation, repositoryName).also { action.execute(it) }

	@JvmOverloads
	fun getDependency(name: String, action: Action<EasyAutoLibraryDependency> = Action {}) =
		dynamicAccess.getDependency(name)?.also { action.execute(it); it.access() }

	@JvmOverloads
	fun getRepository(name: String, action: Action<EasyAutoLibraryRepository> = Action {}) =
		dynamicAccess.getRepository(name)?.also { action.execute(it); it.access() }

	@JvmOverloads
	fun getSubLibrary(name: String, action: Action<AbstractEasyAutoLibrary<*>> = Action {}) =
		dynamicAccess.getSubLibrary(name)?.also { action.execute(it); it.access() }

	companion object {
		// TODO: investigate 'registering' functions, which would get the name from the property
		inline operator fun <reified MARKER : EasyAutoLibraryMarker<MARKER>> MARKER.getValue(
			thisRef: AbstractEasyAutoLibrary<*>,
			property: KProperty<*>
		) = when {
			EasyAutoLibraryRepository::class.java.isAssignableFrom(MARKER::class.java) -> thisRef.getRepository(this.name)
			EasyAutoLibraryDependency::class.java.isAssignableFrom(MARKER::class.java) -> thisRef.getDependency(this.name)
			AbstractEasyAutoLibrary::class.java.isAssignableFrom(MARKER::class.java) -> thisRef.getSubLibrary(this.name)
			else -> throw UnsupportedOperationException()
		} as MARKER

		inline operator fun <reified MARKER : EasyAutoLibraryMarker<MARKER>> String.getValue(
			thisRef: AbstractEasyAutoLibrary<*>,
			property: KProperty<*>
		) = when {
			EasyAutoLibraryRepository::class.java.isAssignableFrom(MARKER::class.java) -> thisRef.getRepository(this)
			EasyAutoLibraryDependency::class.java.isAssignableFrom(MARKER::class.java) -> thisRef.getDependency(this)
			AbstractEasyAutoLibrary::class.java.isAssignableFrom(MARKER::class.java) -> thisRef.getSubLibrary(this)
			else -> throw UnsupportedOperationException()
		} as MARKER

	}
}