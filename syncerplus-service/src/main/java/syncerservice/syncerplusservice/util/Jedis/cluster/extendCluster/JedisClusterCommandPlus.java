package syncerservice.syncerplusservice.util.Jedis.cluster.extendCluster;

import redis.clients.jedis.Jedis;

import redis.clients.jedis.exceptions.*;
import redis.clients.jedis.util.JedisClusterCRC16;

import java.util.Map;

/**
 * 扩展模块 该模块主要为命令传输模块
 */
public abstract class JedisClusterCommandPlus <T>{

    private final JedisClusterConnectionHandlerPlus connectionHandler;
    private final int maxAttempts;
    private Map<String,String> nodesMap;
    public JedisClusterCommandPlus(JedisClusterConnectionHandlerPlus connectionHandler, int maxAttempts, Map<String,String> nodesMap) {
        this.connectionHandler = connectionHandler;
        this.maxAttempts = maxAttempts;
    }

    public abstract T execute(Jedis connection);

    public T run(String key) {
        return runWithRetries(JedisClusterCRC16.getSlot(key), this.maxAttempts, false, null);
    }

    public T run(int keyCount, String... keys) {
        if (keys == null || keys.length == 0) {
            throw new JedisClusterOperationException("No way to dispatch this command to Redis Cluster. 无法将此命令分派到Redis集群。");
        }

        // For multiple keys, only execute if they all share the same connection slot.
        int slot = JedisClusterCRC16.getSlot(keys[0]);
        if (keys.length > 1) {
            for (int i = 1; i < keyCount; i++) {
                int nextSlot = JedisClusterCRC16.getSlot(keys[i]);
                if (slot != nextSlot) {
                    throw new JedisClusterOperationException("No way to dispatch this command to Redis "
                            + "Cluster because keys have different slots. 因为键有不同的槽。");
                }
            }
        }

        return runWithRetries(slot, this.maxAttempts, false, null);
    }

    public T runBinary(byte[] key) {
        return runWithRetries(JedisClusterCRC16.getSlot(key), this.maxAttempts, false, null);
    }

    public T runBinary(int keyCount, byte[]... keys) {
        if (keys == null || keys.length == 0) {
            throw new JedisClusterOperationException("No way to dispatch this command to Redis Cluster.");
        }

        // For multiple keys, only execute if they all share the same connection slot.
        int slot = JedisClusterCRC16.getSlot(keys[0]);
        if (keys.length > 1) {
            for (int i = 1; i < keyCount; i++) {
                int nextSlot = JedisClusterCRC16.getSlot(keys[i]);
                if (slot != nextSlot) {
                    throw new JedisClusterOperationException("No way to dispatch this command to Redis "
                            + "Cluster because keys have different slots.");
                }
            }
        }

        return runWithRetries(slot, this.maxAttempts, false, null);
    }

    public T runWithAnyNode() {
        Jedis connection = null;
        try {
            connection = connectionHandler.getConnection();
            return execute(connection);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            releaseConnection(connection);
        }
    }

    private T runWithRetries(final int slot, int attempts, boolean tryRandomNode, JedisRedirectionException redirect) {
        if (attempts <= 0) {

            throw new JedisClusterMaxAttemptsException("No more cluster attempts left.");
        }

        Jedis connection = null;
        try {

            if (redirect != null) {

                connection = this.connectionHandler.getConnectionFromNode(redirect.getTargetNode());
                System.out.println("type3: "+connection.isConnected());

                if (redirect instanceof JedisAskDataException) {
                    // TODO: Pipeline asking with the original command to make it faster....
                    connection.asking();
                }
            } else {
                if (tryRandomNode) {
                    connection = connectionHandler.getConnection();

                } else {

                    try {

                        connection = connectionHandler.getConnectionFromSlot(slot);

                    }catch (Exception e){

                    }

                }
            }


            return execute(connection);

        } catch (JedisNoReachableClusterNodeException jnrcne) {

            throw jnrcne;
        } catch (JedisConnectionException jce) {

            // release current connection before recursion
            releaseConnection(connection);
            connection = null;

            if (attempts <= 1) {

                //We need this because if node is not reachable anymore - we need to finally initiate slots
                //renewing, or we can stuck with cluster state without one node in opposite case.
                //But now if maxAttempts = [1 or 2] we will do it too often.
                //TODO make tracking of successful/unsuccessful operations for node - do renewing only
                //if there were no successful responses from this node last few seconds
                this.connectionHandler.renewSlotCache();
            }

            return runWithRetries(slot, attempts - 1, tryRandomNode, redirect);
        } catch (JedisRedirectionException jre) {
            // if MOVED redirection occurred,
            if (jre instanceof JedisMovedDataException) {
                // it rebuilds cluster's slot cache recommended by Redis cluster specification

                this.connectionHandler.renewSlotCache(connection,nodesMap);
            }

            // release current connection before recursion
            releaseConnection(connection);
            connection = null;

            return runWithRetries(slot, attempts - 1, false, jre);
        } finally {
            releaseConnection(connection);
        }
    }

    private void releaseConnection(Jedis connection) {
        if (connection != null) {
            connection.close();
        }
    }
}
