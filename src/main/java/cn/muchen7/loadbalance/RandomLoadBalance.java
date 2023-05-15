package cn.muchen7.loadbalance;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

/**
 * @author muchen
 */
public class RandomLoadBalance extends AbstractLoadBalance {
    @Override
    protected String doSelect(List<String> serviceAddresses) {
        SecureRandom random = new SecureRandom();
        return serviceAddresses.get(random.nextInt(serviceAddresses.size()));
    }
}
