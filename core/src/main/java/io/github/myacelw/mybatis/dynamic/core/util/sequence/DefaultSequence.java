package io.github.myacelw.mybatis.dynamic.core.util.sequence;

import com.github.yitter.contract.IdGeneratorOptions;
import com.github.yitter.idgen.YitIdHelper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * 默认的雪花算法序列号生成实现类。
 * 使用yitter库，参考： https://github.com/yitter/idgenerator/tree/master/Java
 *
 * @author liuwei
 */
@Slf4j
public class DefaultSequence implements Sequence {

    public DefaultSequence() {
        short workerId = getWorkerId();

        IdGeneratorOptions options = new IdGeneratorOptions(workerId);
        // 如果ID生成需求不超过5W个/s，不用修改任何配置参数
        // 如果超过5W个/s，低于50W个/s，推荐修改：SeqBitLength=10
        // options.SeqBitLength = 10;
        // 如果超过50W个/s，接近500W个/s，推荐修改：SeqBitLength=12
        // options.SeqBitLength = 12;

        YitIdHelper.setIdGenerator(options);
    }

    private short getWorkerId() {
        // 从系统属性中获取 worker_id
        String id = System.getProperty("worker_id", System.getenv("WORKER_ID"));
        if (id != null) {
            return Short.parseShort(id);
        }

        int workerIdBitLength = 6;
        int maxWorkerId = (1 << workerIdBitLength) - 1; // 63

        String ipAddress = getIpAddress();

        if (ipAddress == null) {
            log.warn("Failed to get IP address");
            return 0;
        }

        return (short) ((ipAddress.hashCode() & 0x7fffffff) % (maxWorkerId + 1));
    }


    @SneakyThrows
    private String getIpAddress() {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                if (!addr.isLoopbackAddress() && addr instanceof Inet4Address) {
                    return addr.getHostAddress();
                }
            }
        }
        return null;
    }

    /**
     * 获取下一个 ID
     *
     * @return 下一个 ID
     */
    public long nextId() {
        return YitIdHelper.nextId();
    }

}
