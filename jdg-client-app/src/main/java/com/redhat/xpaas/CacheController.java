package com.redhat.xpaas;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

@RestController
public class CacheController {

	private static final Logger LOG = LoggerFactory.getLogger(CacheController.class);

	@Autowired
	private RemoteCacheManager cacheManager;

	@RequestMapping("/single/write/{id}")
	public void writeSingle(final @PathVariable String id, final @RequestParam String value, final @RequestParam(defaultValue = "${infinispan.cache}") String cache) {
		cacheManager.getCache(cache).put(id, value);
	}

	@RequestMapping("/single/read/{id}")
	public String readSingle(final @PathVariable String id, final @RequestParam(defaultValue = "${infinispan.cache}") String cache) {
		return (String) cacheManager.getCache(cache).get(id);
	}

	@RequestMapping("/bulk/write")
	public void writeBulk(
			final @RequestParam int from,
			final @RequestParam int count,
			final @RequestParam(defaultValue = "${bulk.pagesize}") int pageSize,
			final @RequestParam(defaultValue = "${bulk.prefix}") String prefix,
			final @RequestParam(defaultValue = "${infinispan.cache}") String cache) {
		CompletableFuture.runAsync(() -> {
			RemoteCache<String, String> cacheMap = cacheManager.getCache(cache);
			IntStream.range(from, from + count).forEach(key -> {
				cacheMap.put(Integer.toString(key), prefix + " " + key);
				if (key % pageSize == 0) {
					LOG.info("Written record with key '{}'", key);
				}
			});
			LOG.info("Bulk write completed");
		});
	}

	@RequestMapping("/bulk/read")
	public void readBulk(
			final @RequestParam int from,
			final @RequestParam int count,
			final @RequestParam(defaultValue = "${bulk.pagesize}") int pageSize,
			final @RequestParam(defaultValue = "${bulk.prefix}") String prefix,
			final @RequestParam(defaultValue = "${infinispan.cache}") String cache) {
		CompletableFuture.runAsync(() -> {
			RemoteCache<String, String> cacheMap = cacheManager.getCache(cache);
			IntStream.range(from, from + count).forEach(key -> {
				final String valueExpected = prefix + " " + key;
				final String value = cacheMap.get(Integer.toString(key));
				if (value == null || !valueExpected.equals(value)) {
					LOG.error("Missing or invalid value for key '{}', expected '{}', but was '{}'", key, valueExpected, value);
				}
				if (key % pageSize == 0) {
					LOG.info("Read record with key: '{}'", key);
				}
			});
			LOG.info("Bulk read completed");
		});
	}
}
