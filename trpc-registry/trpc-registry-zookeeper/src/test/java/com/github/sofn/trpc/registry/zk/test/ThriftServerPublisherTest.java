package com.github.sofn.trpc.registry.zk.test;

import com.github.sofn.trpc.core.config.ServiceArgs;
import com.github.sofn.trpc.core.utils.ClassNameUtils;
import com.github.sofn.trpc.demo.Hello;
import com.github.sofn.trpc.direct.DemoClient;
import com.github.sofn.trpc.direct.HelloServer;
import com.github.sofn.trpc.registry.zk.ZkRegistry;
import com.github.sofn.trpc.server.ThriftServerPublisher;
import com.github.sofn.trpc.server.config.ServerArgs;
import com.github.sofn.trpc.utils.NumUtil;
import org.junit.Test;

import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

/**
 * Authors: sofn
 * Version: 1.0  Created at 2016-09-18 23:14.
 */
public class ThriftServerPublisherTest {

    @Test
    public void test() throws UnknownHostException, InterruptedException {
        ZkRegistry registry = new ZkRegistry();
        registry.setConnectString("localhost:2181");
        registry.setSessionTimeout(100);
        registry.setConnectionTimeout(1000);

        registry.initConnect();

        ServerArgs arg = ServerArgs.builder()
                .appkey("test")
                .host("127.0.0.1")
                .port(NumUtil.nextPort())
                .service(new ServiceArgs(new Hello.Processor<>(new HelloServer()), ClassNameUtils.getClassName(Hello.class), 80, 100))
                .registry(registry)
                .build();
        arg.afterPropertiesSet();

        ThriftServerPublisher publisher = new ThriftServerPublisher(arg);
        Thread thread = new Thread(publisher::init);
        thread.setDaemon(true);
        thread.start();

        TimeUnit.MILLISECONDS.sleep(20);
        DemoClient demoClient = new DemoClient();
        demoClient.setPort(arg.getPort());
        demoClient.bioCall();
        demoClient.nioCall();
        publisher.stop();
    }

}
