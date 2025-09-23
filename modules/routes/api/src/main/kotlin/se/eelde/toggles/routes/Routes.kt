package se.eelde.toggles.routes

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
object Applications : NavKey

@Serializable
object Oss : NavKey

@Serializable
object Help : NavKey

@Serializable
data class Configurations(val applicationId: Long) : NavKey

@Serializable
data class BooleanConfiguration(val configurationId: Long, val scopeId: Long) : NavKey

@Serializable
data class IntegerConfiguration(val configurationId: Long, val scopeId: Long) : NavKey

@Serializable
data class EnumConfiguration(val configurationId: Long, val scopeId: Long) : NavKey

@Serializable
data class StringConfiguration(val configurationId: Long, val scopeId: Long) : NavKey

@Serializable
data class Scope(val applicationId: Long) : NavKey
