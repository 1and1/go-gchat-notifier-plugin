package com.ionos.go.plugin.notifier.message.outgoing;

import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** Response for the {@code notifications-interested-in} request.
 * @see <a href="https://plugin-api.gocd.org/current/notifications/#notifications-interested-in">here</a>
 * */
@AllArgsConstructor
public class NotificationsInterestedInResponse {
    @Expose
    @Getter
    private String[] notifications;
}
