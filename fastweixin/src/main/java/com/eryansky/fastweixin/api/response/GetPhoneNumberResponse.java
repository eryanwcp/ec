package com.eryansky.fastweixin.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 小程序获取手机号响应
 *
 * @author Eryan
 * @date 2024-01-04
 */
public class GetPhoneNumberResponse extends BaseResponse {

    @JsonProperty("phone_info")
    private PhoneInfo phoneInfo;

    public PhoneInfo getPhoneInfo() {
        return phoneInfo;
    }

    public void setPhoneInfo(PhoneInfo phoneInfo) {
        this.phoneInfo = phoneInfo;
    }

    /**
     * 手机号信息
     */
    public static class PhoneInfo {
        /**
         * 用户绑定的手机号（国外手机号会有区号）
         */
        private String phoneNumber;
        /**
         * 没有区号的手机号
         */
        private String purePhoneNumber;
        /**
         * 区号
         */
        private String countryCode;
        /**
         * 水印信息
         */
        private Watermark watermark;

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getPurePhoneNumber() {
            return purePhoneNumber;
        }

        public void setPurePhoneNumber(String purePhoneNumber) {
            this.purePhoneNumber = purePhoneNumber;
        }

        public String getCountryCode() {
            return countryCode;
        }

        public void setCountryCode(String countryCode) {
            this.countryCode = countryCode;
        }

        public Watermark getWatermark() {
            return watermark;
        }

        public void setWatermark(Watermark watermark) {
            this.watermark = watermark;
        }
    }

    /**
     * 水印信息
     */
    public static class Watermark {
        private String appid;
        private Long timestamp;

        public String getAppid() {
            return appid;
        }

        public void setAppid(String appid) {
            this.appid = appid;
        }

        public Long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }
    }
}
