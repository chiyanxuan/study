package com.gupaoedu.example.rcp;

import com.gupaoedu.example.IOrderService;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ){
        RpcProxyClient rpcProxyClient=new RpcProxyClient();
        IOrderService orderService=rpcProxyClient.clientProxy(IOrderService.class,"localhost",8080);

        System.out.println(orderService.queryOrderList());
        System.out.println(orderService.orderById("Mic"));

    }
}
