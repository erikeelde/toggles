package se.eelde.toggles.routes

import kotlinx.serialization.Serializable

@Serializable
object Applications

@Serializable
object Oss

@Serializable
object Help

@Serializable
data class Configurations(val applicationId: Long)

@Serializable
object BooleanConfiguration

@Serializable
object IntegerConfiguration

@Serializable
object EnumConfiguration

@Serializable
object StringConfiguration

@Serializable
object Scope

