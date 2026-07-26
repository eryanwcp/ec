package com.eryansky.fastweixin.company.message;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author Eryan
 * @date 2016-03-15
 */
public class QYVideoMsg extends QYBaseMsg {

    @JsonProperty("video")
    private Video video;

    public Video getVideo() {
        return video;
    }

    public QYVideoMsg setVideo(Video video) {
        this.video = video;
        return this;
    }

    public static class Video{
        @JsonProperty("media_id")
        private String mediaId;
        @JsonProperty("title")
        private String title;
        @JsonProperty("description")
        private String description;

        public String getMediaId() {
            return mediaId;
        }

        public Video setMediaId(String mediaId) {
            this.mediaId = mediaId;
            return this;
        }

        public String getTitle() {
            return title;
        }

        public Video setTitle(String title) {
            this.title = title;
            return this;
        }

        public String getDescription() {
            return description;
        }

        public Video setDescription(String description) {
            this.description = description;
            return this;
        }
    }
}
