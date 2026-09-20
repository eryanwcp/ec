package com.eryansky.core.ip;

import com.eryansky.common.utils.http.HttpCompoents;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.common.utils.net.IpUtils;
import com.eryansky.core.ip.dto.GeoIP;
import com.eryansky.utils.AppConstants;
import com.eryansky.utils.CacheConstants;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.cache.annotation.Cacheable;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class IpDefaultProvider implements IpAPI {

    private static final Logger log = Logger.getLogger(IpDefaultProvider.class);

//    private static final String BASE_API_URL = "https://api.ip.sb";
    private static final String BASE_API_URL = "https://api.seeip.org";


    /**
     * 根据 IP 获取地理位置 (带缓存)
     */
    @Cacheable(value = {CacheConstants.CACHE_GEO_IP})
    @Override
    public GeoIP getLocationByIp(String ip) {
        if (StringUtils.isBlank(ip) || IpUtils.isInternalAddr(ip)) {
            GeoIP local = new GeoIP();
            local.setIp(ip);
            local.setCountry("局域网");
            return local;
        }

        if(AppConstants.isGeoIpEnable()){
            try {
                Map<String,String> headers = Maps.newHashMap();
                headers.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/153.0.0.0 Safari/537.36");
                String response = HttpCompoents.getInstance().get(BASE_API_URL + "/geoip/" + ip,headers, StandardCharsets.UTF_8.name());
                return JsonMapper.getInstance().fromJson(response, GeoIP.class);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        GeoIP unknown = new GeoIP();
        unknown.setIp(ip);
        unknown.setCountry("未知区域");
        return unknown;
    }
}