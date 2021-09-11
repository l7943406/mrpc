package cn.muchen7.loadbalance;

import java.util.List;
import java.util.Random;

/**
 * @author muchen
 */
public class RandomLoadBalance extends AbstractLoadBalance {
    @Override
    protected String doSelect(List<String> serviceAddresses) {
        int random = new Random().nextInt(serviceAddresses.size());
        return serviceAddresses.get(random);
    }
}
