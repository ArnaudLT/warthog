package org.arnaudlt.warthog.model.user;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.ConstructorBinding;


@ConfigurationProperties(prefix = "warthog.default-settings")
@ConstructorBinding
@ConfigurationPropertiesScan
public record DefaultSettings(
        UserSettings user,
        OverviewSettings overview,
        SparkSettings spark) {}
