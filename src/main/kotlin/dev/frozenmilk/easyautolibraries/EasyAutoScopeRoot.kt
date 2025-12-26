package dev.frozenmilk.easyautolibraries

import org.gradle.api.Project
import org.gradle.api.logging.LogLevel

/**
 * a helper for creating a root scope object and a configurations object
 */
abstract class EasyAutoScopeRoot<SELF : EasyAutoScopeRoot<SELF>>(
    protected val project: Project,
    logLevel: LogLevel
) : EasyAutoScope<SELF>(EasyAutoConfigurations(project, logLevel)) {
    /**
     * provides a single point of external access to the log level
     */
    var logLevel by configurations::logLevel
}