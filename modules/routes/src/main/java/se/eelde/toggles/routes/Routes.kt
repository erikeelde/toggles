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
data class BooleanConfiguration(val configurationId: Long, val scopeId: Long)

@Serializable
data class IntegerConfiguration(val configurationId: Long, val scopeId: Long)

@Serializable
data class EnumConfiguration(val configurationId: Long, val scopeId: Long)

@Serializable
data class StringConfiguration(val configurationId: Long, val scopeId: Long)

@Serializable
data class Scope(val applicationId: Long)

