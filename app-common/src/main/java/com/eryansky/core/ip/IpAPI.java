package com.eryansky.core.ip;

import com.eryansky.core.ip.dto.GeoIP;

public interface IpAPI {
    GeoIP getLocationByIp(String ip);
}
