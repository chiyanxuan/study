package com.gupaoedu.example.rcp;

import com.gupaoedu.example.IOrderService;
import com.gupaoedu.example.ITestService;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ){
        RpcProxyClient rpcProxyClient=new RpcProxyClient();
        ITestService testService=rpcProxyClient.clientProxy(ITestService.class,"localhost",8080);

        System.out.println(testService.sayHello());

    }
}
