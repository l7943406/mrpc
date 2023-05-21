package cn.muchen7.loadbalance;

import java.security.SecureRandom;
import java.util.List;

/**
 * @author muchen
 */
public class RandomLoadBalance extends AbstractLoadBalance {
    private static final SecureRandom random = new SecureRandom();
    @Override
    protected String doSelect(List<String> serviceAddresses) {
        return serviceAddresses.get(RandomLoadBalance.random.nextInt(serviceAddresses.size()));
    }
}
