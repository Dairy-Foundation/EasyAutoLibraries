package dev.frozenmilk.easyautolibraries

// ANSI escape codes for text colours
const val red = "\u001B[31m"
const val green = "\u001B[32m"
const val brightBlue = "\u001B[94m"
// resets back to default
const val default = "\u001B[m"

/**
 * wraps a string to make it red
 */
fun red(b: String) = "$red$b$default"

/**
 * wraps a string to make it green
 */
fun green(s: String) = "$green$s$default"

/**
 * wraps a string to make it bright blue
 */
fun brightBlue(s: String) = "$brightBlue$s$default"

/**
 * wraps a string to display it formatted as a string
 */
fun string(s: String) = green("\"$s\"")

/**
 * wraps a string to display it formatted as a symbol
 */
fun sym(s: String) = brightBlue(s)
