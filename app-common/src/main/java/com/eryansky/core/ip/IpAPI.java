package com.eryansky.core.ip;

import com.eryansky.client.common.dto.GeoIP;

public interface IpAPI {
    GeoIP getLocationByIp(String ip);
}
