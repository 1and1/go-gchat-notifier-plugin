package com.ionos.go.plugin.notifier.gchat;

import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Request for the Google chat request.
 * @see <a href="https://developers.google.com/chat/how-tos/webhooks?hl=de">here</a>
 * */
@AllArgsConstructor
@NoArgsConstructor
public class GoogleChatRequest {

    @Expose
    @Getter
    @Setter
    private String text;
}
