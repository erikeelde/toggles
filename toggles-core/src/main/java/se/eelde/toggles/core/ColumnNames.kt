package se.eelde.toggles.core

@Suppress("LibraryEntitiesShouldNotBePublic")
public object ColumnNames {
    public object Toggle {
        public const val COL_KEY: String = Configuration.COL_KEY
        public const val COL_ID: String = Configuration.COL_ID
        public const val COL_VALUE: String = ConfigurationValue.COL_VALUE
        public const val COL_TYPE: String = Configuration.COL_TYPE
    }

    public object ToggleValue {
        public const val COL_ID: String = ConfigurationValue.COL_ID
        public const val COL_VALUE: String = ConfigurationValue.COL_VALUE
        public const val COL_CONFIG_ID: String = ConfigurationValue.COL_CONFIG_ID
    }

    public object ToggleScope {
        public const val COL_ID: String = "id"
        public const val COL_APP_ID: String = "applicationId"
        public const val COL_NAME: String = "name"
        public const val COL_SELECTED_TIMESTAMP: String = "selectedTimestamp"

        public const val DEFAULT_SCOPE: String = "wrench_default"
    }

    public object Configuration {
        public const val COL_ID: String = "id"
        public const val COL_KEY: String = "configurationKey"
        public const val COL_TYPE: String = "configurationType"
    }

    public object ConfigurationValue {
        public const val COL_ID: String = "id"
        public const val COL_CONFIG_ID: String = "configurationId"
        public const val COL_VALUE: String = "value"
        public const val COL_SCOPE: String = "scope"
    }
}
