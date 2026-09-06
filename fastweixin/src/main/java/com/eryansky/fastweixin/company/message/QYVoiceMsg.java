package com.eryansky.fastweixin.company.message;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author Eryan
 * @date 2016-03-15
 */
public class QYVoiceMsg extends QYBaseMsg {

    @JsonProperty("voice")
    private Voice voice;

    public Voice getVoice() {
        return voice;
    }

    public QYVoiceMsg setVoice(Voice voice) {
        this.voice = voice;
        return this;
    }

    public static class Voice{
        @JsonProperty("media_id")
        private String mediaId;

        public String getMediaId() {
            return mediaId;
        }

        public Voice setMediaId(String mediaId) {
            this.mediaId = mediaId;
            return this;
        }
    }
}
