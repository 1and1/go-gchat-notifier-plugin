package com.ionos.go.plugin.notifier.message;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Getter;

/** Representation of a configuration property definition. */
@Builder
public class ConfigurationProperty {

    /**
     * The default value for {@code this} property.
     */
    @Expose @SerializedName("default-value")
    @Getter private String defaultValue;

    /**
     * The value for {@code this} property.
     */
    @Expose
    @Getter private String value;

    /**
     * Flag indicating whether the content of {@code this} property value should be displayed hidden.
     */
    @Expose
    @Getter private Boolean secure;

    /**
     * Flag indicating whether {@code this} property is part of the configuration identifier.
     */
    @Expose @SerializedName("part-of-identity")
    @Getter private Boolean partOfIdentity;

    /**
     * FLag indicating whether a value for {@code this} property is required
     */
    @Getter private Boolean required;

    /**
     * The display name for {@code this} property.
     */
    @Expose @SerializedName("display-name")
    @Getter private String displayName;

    /**
     * The display order for {@code this} property.
     */
    @Expose @SerializedName("display-order")
    @Getter private String displayOrder;
}
