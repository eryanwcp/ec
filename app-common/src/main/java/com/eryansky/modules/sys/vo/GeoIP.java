package com.eryansky.modules.sys.vo;

import com.eryansky.common.utils.StringUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.math.BigDecimal;

public class GeoIP implements Serializable {
    /**
     * Public IP address, or IP address specified as parameter.
     */
    private String ip;
    /**
     * Autonomous System Number (ASN) + Internet Service Provider (ISP) name.
     */
    private String organization;
    /**
     * Two-letter continent code.
     */
    @JsonProperty("continent_code")
    private String continentCode;
    private String country;
    /**
     * Two-letter ISO 3166-1 alpha-2 country code.
     */
    @JsonProperty("country_code")
    private String countryCode;
    /**
     * Three-letter ISO 3166-1 alpha-3 country code.
     */
    @JsonProperty("country_code3")
    private String country_code3;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String timezone;
    /**
     * Universal Coordinated Time (UTC) time offset.
     */
    private Integer offset;
    private Integer asn;
    private String region;
    /**
     * Postal/zip code.
     */
    @JsonProperty("postal_code")
    private String postalCode;
    private String city;


    // 格式化获取可读地理位置：如 "United States-Missouri-Wright City" 或 "中国-上海市"
    public String toFormatLocation() {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(country)) sb.append(country);
        if (StringUtils.isNotBlank(region)) sb.append("-").append(region);
        if (StringUtils.isNotBlank(city)) sb.append("-").append(city);
        if(StringUtils.isBlank(sb.toString())){
            return "局域网";
        }
        return sb.toString();
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getContinentCode() {
        return continentCode;
    }

    public void setContinentCode(String continentCode) {
        this.continentCode = continentCode;
    }

    public String getCountry_code3() {
        return country_code3;
    }

    public void setCountry_code3(String country_code3) {
        this.country_code3 = country_code3;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Integer getAsn() {
        return asn;
    }

    public void setAsn(Integer asn) {
        this.asn = asn;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
}