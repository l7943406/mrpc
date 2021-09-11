package cn.muchen7.loadbalance;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author muchen
 */
public class LoopLoadBalance extends AbstractLoadBalance {
    private final static Map<List<String>, Integer> COUNT_MAP = new ConcurrentHashMap<>();

    @Override
    protected String doSelect(List<String> serviceAddresses) {
        int index = COUNT_MAP.getOrDefault(serviceAddresses, 0);
        if (index >= serviceAddresses.size()) {
            index = 0;
        }
        String result = serviceAddresses.get(index);
        index++;
        COUNT_MAP.put(serviceAddresses, index);
        return result;
    }
}
