package utils;

import cn.hutool.core.util.RandomUtil;

/**
 * @Author: W.Z
 * @Date: 2022/8/29 20:14
 */
public class IpUtils {

    public static String getIp() {

        StringBuilder ip = new StringBuilder();
        ip.append(RandomUtil.randomInt(0, 255)).append(".");
        ip.append(RandomUtil.randomInt(0, 255)).append(".");
        ip.append(RandomUtil.randomInt(0, 255)).append(".");
        ip.append(RandomUtil.randomInt(0, 255));
        return ip.toString();
    }
}
