/* 
 * Copyright (c) 2008-2010, Hazel Ltd. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.hazelcast.core;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.XmlConfigBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.*;

/**
 * HazelcastTest tests some specific cluster behavior.
 * Node is created for each test method.
 */
public class HazelcastClusterTest {

    @Before
    @After
    public void init() throws Exception {
        Hazelcast.shutdownAll();
    }

    @Test
    public void testMapPutAndGetUseBackupData() throws Exception {
        Config config = new XmlConfigBuilder().build();
        String mapName1 = "testMapPutAndGetUseBackupData";
        String mapName2 = "testMapPutAndGetUseBackupData2";
        MapConfig mapConfig1 = new MapConfig();
        mapConfig1.setName(mapName1);
        mapConfig1.setUseBackupData(true);
        MapConfig mapConfig2 = new MapConfig();
        mapConfig2.setName(mapName2);
        mapConfig2.setUseBackupData(false);
        config.getMapConfigs().put(mapName1, mapConfig1);
        config.getMapConfigs().put(mapName2, mapConfig2);
        HazelcastInstance h1 = Hazelcast.newHazelcastInstance(config);
        IMap<Object, Object> m1 = h1.getMap(mapName1);
        IMap<Object, Object> m2 = h1.getMap(mapName2);
        m1.put(1, 1);
        m2.put(1, 1);
        assertEquals(1, m1.get(1));
        assertEquals(1, m1.get(1));
        assertEquals(1, m1.get(1));
        assertEquals(1, m2.get(1));
        assertEquals(1, m2.get(1));
        assertEquals(1, m2.get(1));
        assertEquals(0, m1.getLocalMapStats().getHits());
        assertEquals(3, m2.getLocalMapStats().getHits());
    }

    @Test
    public void testLockKeyWithUseBackupData() {
        Config config = new XmlConfigBuilder().build();
        String mapName1 = "testLockKeyWithUseBackupData";
        MapConfig mapConfig1 = new MapConfig();
        mapConfig1.setName(mapName1);
        mapConfig1.setUseBackupData(true);
        config.getMapConfigs().put(mapName1, mapConfig1);
        HazelcastInstance h1 = Hazelcast.newHazelcastInstance(config);
        IMap<String, String> map = h1.getMap(mapName1);
        map.lock("Hello");
        try {
            assertFalse(map.containsKey("Hello"));
        } finally {
            map.unlock("Hello");
        }
        map.put("Hello", "World");
        map.lock("Hello");
        try {
            assertTrue(map.containsKey("Hello"));
        } finally {
            map.unlock("Hello");
        }
        map.remove("Hello");
        map.lock("Hello");
        try {
            assertFalse(map.containsKey("Hello"));
        } finally {
            map.unlock("Hello");
        }
    }

    @Test
    public void testIssue290() throws Exception {
        String mapName = "testIssue290";
        Config config = new XmlConfigBuilder().build();
        MapConfig mapConfig = new MapConfig();
        mapConfig.setName(mapName);
        mapConfig.setTimeToLiveSeconds(1);
        config.getMapConfigs().put(mapName, mapConfig);
        HazelcastInstance h1 = Hazelcast.newHazelcastInstance(config);
        IMap<Object, Object> m1 = h1.getMap(mapName);
        m1.put(1, 1);
        assertEquals(1, m1.get(1));
        assertEquals(1, m1.get(1));
        Thread.sleep(1050);
        assertEquals(null, m1.get(1));
        m1.put(1, 1);
        assertEquals(1, m1.get(1));
    }
}