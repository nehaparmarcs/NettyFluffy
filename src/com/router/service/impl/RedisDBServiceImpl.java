package com.router.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.router.service.IDBService;

import redis.clients.jedis.Jedis; 
import redis.clients.jedis.JedisPool; 
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;


public class RedisDBServiceImpl implements IDBService {
	
	Jedis redis;
	static JedisPool pool;
	
	public synchronized Jedis getJedisConnection() throws JedisConnectionException {
		if (pool == null) {
			JedisPoolConfig config = new JedisPoolConfig(); 
		 	config.setMaxTotal(12); 
		 	config.setMaxIdle(4);
		 	config.setMinIdle(1);
		 	pool = new JedisPool(config, "localhost", 6379); 
		}
		return pool.getResource();
	}


	public RedisDBServiceImpl() {
		redis = getJedisConnection();
	}


	public byte[] serialize(Object obj) throws IOException {
		try (ByteArrayOutputStream b = new ByteArrayOutputStream()) {
			try (ObjectOutputStream o = new ObjectOutputStream(b)) {
				o.writeObject(obj);
			}
			return b.toByteArray();
		}
	}

	public Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
		try (ByteArrayInputStream b = new ByteArrayInputStream(bytes)) {
			try (ObjectInputStream o = new ObjectInputStream(b)) {
				return o.readObject();
			}
		}
	}

	
	@Override
	public boolean containsKey(String key) {
		// TODO Auto-generated method stub
		
		try {
			return redis.exists(serialize(key))? true:false;
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return false;
	}



	/**
	 * Retrieves data stored at the key
	 * 
	 * @param key String value given for a string
	 * @return map HashMap stored corresponding to the key with chunkIds and values
	 */
	@Override
	public Map<Integer, byte[]> get(String key) {
		// TODO Auto-generated method stub
		Map<Integer, byte[]> chunkDataMap = new HashMap<Integer, byte[]>();
		try {
			if (containsKey(key)) {
				byte[] parsedKey = serialize(key);
				Set<byte[]> byteChunkIds = redis.hkeys(parsedKey);
				for (byte[] bci : byteChunkIds) {
					chunkDataMap.put((Integer) deserialize(bci), redis.hget(parsedKey, bci));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return chunkDataMap;
	}


	/**
	 * Get all the chunkIds corresponding to a Key
	 * @param key String key for which data is needed
	 * @return List of all chunkIds
	 */
	@Override
	public List<Integer> getChunkIDs(String key) {
		// TODO Auto-generated method stub
		List<Integer> chunkIDs = new ArrayList<Integer>();
		try {
			if (containsKey(key)) {
				byte[] parsedKey = serialize(key);
				Set<byte[]> byteChunkIds = redis.hkeys(parsedKey);
				for (byte[] bci : byteChunkIds) {
					chunkIDs.add((Integer) deserialize(bci));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return chunkIDs;
	}

	/** 
	 * 
	 * 
	 */
	@Override
	public boolean update(String key, int chunkID, byte[] value) {
		// TODO Auto-generated method stub
		boolean done = false;
		try {
			if (containsKey(key)) {
				byte[] parsedKey = serialize(key);
				byte[] parseChunkId = serialize(chunkID);
				if (redis.hexists(parsedKey, parseChunkId)) {
					redis.hset(parsedKey, parseChunkId, value);
					done = true;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return done;
	}


	@Override
	protected void finalize() throws Throwable {

		if (redis != null) {
			redis.close();
		}
		super.finalize();
	}

	
	

	@Override
	public String put(String key, int chunkID, byte[] value) {
		// TODO Auto-generated method stub
		if (key == null || value == null) {
			return null;
		}
		try {
			redis.hset(serialize(key), serialize(chunkID), value);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return key;
	}

	@Override
	public String store(byte[] value) {
		// TODO Auto-generated method stub
		if (value == null) {
			return null;
		}
		String uuid = UUID.randomUUID().toString();
		String key = put(uuid, 1, value);
		return key;
	}


	@Override
	public Map<Integer, byte[]> delete(String key) {
		// TODO Auto-generated method stub
		Map<Integer, byte[]> removedMap = new HashMap<Integer, byte[]>();
		try {
			if (containsKey(key)) {
				byte[] serializedKey = serialize(key);
				Set<byte[]> byteSequencIds = redis.hkeys(serializedKey);
				for (byte[] bs : byteSequencIds) {
					removedMap.put((Integer) deserialize(bs), redis.hget(serializedKey, bs));
					redis.hdel(serializedKey, bs);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return removedMap;
	}

	

	@Override
	public String getDatabaseType() {
		// TODO Auto-generated method stub
		return "Redis Database";
	}

}
