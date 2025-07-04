package org.example.ai_api.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class IPCheck {
    // 预编译IP范围列表（包含CIDR和自定义范围）
    private static final List<Predicate<String>> IP_RANGE_PREDICATES = new ArrayList<>();

    //将内网ip以静态方式保存
    static {
        // 添加CIDR格式的IP范围
        addCidrRange("172.18.128.0/21");
        addCidrRange("10.106.0.0/18");
        addCidrRange("10.23.121.0/24");
        addCidrRange("10.23.123.0/25");
        addCidrRange("10.20.2.0/23");
        addCidrRange("10.20.8.0/24");
        addCidrRange("10.20.9.0/24");
        addCidrRange("10.20.10.0/24");
        addCidrRange("10.20.11.0/24");
        addCidrRange("10.21.50.0/23");
        addCidrRange("10.21.52.0/23");
        addCidrRange("10.22.8.0/22");
        addCidrRange("10.23.66.0/23");
        addCidrRange("10.23.68.0/23");
        addCidrRange("10.21.34.0/25");
        addCidrRange("10.21.34.128/25");
        addCidrRange("10.23.33.0/24");
        addCidrRange("10.23.34.0/25");
        addCidrRange("10.21.201.0/25");
        addCidrRange("10.21.201.128/25");
        addCidrRange("10.180.8.0/21");
        // 添加自定义IP段（特殊格式）
        addCustomRange("10.180.24.0", "10.180.41.255");
    }

    // 添加CIDR范围到检查列表
    private static void addCidrRange(String cidr) {
        IP_RANGE_PREDICATES.add(ip -> isIpInCidr(ip, cidr));
    }

    // 添加自定义IP段到检查列表
    private static void addCustomRange(String startIp, String endIp) {
        IP_RANGE_PREDICATES.add(ip -> isIpInRange(ip, startIp, endIp));
    }

    /**
     * 检查IP是否在预定义范围内
     * @param userIp 要检查的用户IP (e.g. "10.20.2.5")
     * @return true如果在任一范围内，否则false
     */
    public static boolean isIpInRanges(String userIp) {
        return IP_RANGE_PREDICATES.stream().anyMatch(p -> p.test(userIp));
    }

    /**
     * 检查IP是否属于CIDR范围
     */
    private static boolean isIpInCidr(String ip, String cidr) {
        try {
            String[] parts = cidr.split("/");
            int prefix = Integer.parseInt(parts[1]);
            long ipLong = ipToLong(ip);
            long cidrIpLong = ipToLong(parts[0]);
            long mask = (0xFFFFFFFFL << (32 - prefix)) & 0xFFFFFFFFL;
            return (ipLong & mask) == (cidrIpLong & mask);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查IP是否在自定义范围内
     */
    private static boolean isIpInRange(String ip, String startIp, String endIp) {
        try {
            long ipLong = ipToLong(ip);
            long start = ipToLong(startIp);
            long end = ipToLong(endIp);
            return ipLong >= start && ipLong <= end;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 将IP字符串转换为长整型
     */
    private static long ipToLong(String ip) {
        String[] octets = ip.split("\\.");
        return (Long.parseLong(octets[0]) << 24) +
                (Long.parseLong(octets[1]) << 16) +
                (Long.parseLong(octets[2]) << 8) +
                Long.parseLong(octets[3]);
    }
}
