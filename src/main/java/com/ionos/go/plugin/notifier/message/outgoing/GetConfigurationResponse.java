package com.ionos.go.plugin.notifier.message.outgoing;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.HashMap;

/** Representation of get configuration.
 * @see <a href="https://plugin-api.gocd.org/current/notifications/#the-plugin-settings-configuration-object">here</a>
 * */
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class GetConfigurationResponse extends HashMap<String, GetConfigurationProperty> {
}
