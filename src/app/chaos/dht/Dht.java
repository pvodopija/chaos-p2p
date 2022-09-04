package app.chaos.dht;

import app.ServentInfo;

public interface Dht<K, V>{

    void put( K key, V value) throws DhtRoutingException;;
    V get( ServentInfo originalSender, K key ) throws DhtRoutingException;

}
