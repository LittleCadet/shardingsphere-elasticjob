/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.reg.zookeeper;

import com.dangdang.ddframe.reg.exception.RegException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public final class ZookeeperRegistryCenterForLocalPropertiesTest {
    
    private static ZookeeperRegistryCenter zkRegCenter;
    
    @BeforeClass
    public static void init() {
        NestedTestingServer.start();
    }
    
    @Before
    public void setUp() {
        zkRegCenter = createZookeeperRegistryCenter();
    }
    
    @After
    public void tearDown() {
        zkRegCenter.close();
    }
    
    @Test
    public void assertInitWithoutLocalProperties() {
        zkRegCenter.init();
        assertFalse(zkRegCenter.isExisted("/notExisted"));
    }
    
    @Test(expected = RegException.class)
    public void assertInitWhenLocalPropertiesCannotFind() {
        zkRegCenter.getZkConfig().setLocalPropertiesPath("conf/reg/notExisted.properties");
        try {
            zkRegCenter.init();
        } catch (final RegException ex) {
            throw (RegException) ex.getCause();
        }
    }
    
    @Test
    public void assertInitForOverwriteDisabled() {
        createInitData();
        zkRegCenter.getZkConfig().setLocalPropertiesPath("conf/reg/local_overwrite.properties");
        zkRegCenter.init();
        assertThat(zkRegCenter.get("/test"), is("test"));
        assertThat(zkRegCenter.get("/test/deep/nested"), is("deepNested"));
        assertThat(zkRegCenter.get("/new"), is("new"));
    }
    
    @Test
    public void assertInitForOverwriteEnabled() {
        createInitData();
        zkRegCenter.getZkConfig().setLocalPropertiesPath("conf/reg/local_overwrite.properties");
        zkRegCenter.getZkConfig().setOverwrite(true);
        zkRegCenter.init();
        assertThat(zkRegCenter.get("/test"), is("test_overwrite"));
        assertThat(zkRegCenter.get("/test/deep/nested"), is("deepNested_overwrite"));
        assertThat(zkRegCenter.get("/new"), is("new"));
    }
    
    private ZookeeperRegistryCenter createZookeeperRegistryCenter() {
        ZookeeperConfiguration zkConfig = new ZookeeperConfiguration(NestedTestingServer.getConnectionString(), getCurrentRunningMethodName());
        zkConfig.setConnectionTimeoutMilliseconds(30000);
        return new ZookeeperRegistryCenter(zkConfig);
    }
    
    private void createInitData() {
        ZookeeperConfiguration zkConfig = new ZookeeperConfiguration(NestedTestingServer.getConnectionString(), getCurrentRunningMethodName());
        zkConfig.setLocalPropertiesPath("conf/reg/local.properties");
        zkConfig.setConnectionTimeoutMilliseconds(30000);
        ZookeeperRegistryCenter zkRegCenter = new ZookeeperRegistryCenter(zkConfig);
        zkRegCenter.init();
        zkRegCenter.close();
    }
    
    private String getCurrentRunningMethodName() {
        return Thread.currentThread().getStackTrace()[1].getMethodName();
    }
}
