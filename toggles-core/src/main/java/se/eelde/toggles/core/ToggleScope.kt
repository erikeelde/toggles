package se.eelde.toggles.core

import java.util.Date

public class ToggleScope private constructor(
    public val id: Long = 0,
    public val name: String,
    public val timeStamp: Date,
)