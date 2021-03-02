







# 手写一个简单的RPC实现

远程接口调用

## v1

主要分为三部分：

- 公共接口sdk：定一个接口，封装成sdk---普通mavan工程
- 服务端：依赖公共接口sdk，实现接口的方法，发布服务---java工程
- 客户端：依赖公共接口sdk，通过引入接口，直接能够调用到服务端得到服务---java工程

### 1、公共接口sdk

- 定义接口

```java
public interface IOrderService {
    String queryOrderList();
    String orderById(String id);
}
```



- 定义一个公共的RpcRequest

```java
@Data
public class RpcRequest implements Serializable{

    private String className;
    private String methodName;
    private Object[] args;
    private Class[] types;
}
```



### 2、服务端

#### 1、pom中引入公共接口sdk

#### 2、实现接口

```java
public class OrderServiceImpl implements IOrderService{

    @Override
    public String queryOrderList() {
        return "EXECUTE QUERYORDERLIST METHOD";
    }

    @Override
    public String orderById(String id) {
        return "EXECUTE ORDER_BY_ID METHOD";
    }
}
```



#### 3、写一个RPC代理类

```java
public class RpcProxyServer {

    private final ExecutorService executorService= Executors.newCachedThreadPool();

    public void publisher(Object service,int port){
        ServerSocket serverSocket=null;
        try {
            serverSocket=new ServerSocket(port);
            while(true){
                Socket socket=serverSocket.accept(); //监听客户端请求，这里是连接阻塞，RIO
                //从线程池中取线程去做IO操作，避免IO阻塞
                executorService.execute(new ProcessorHandler(socket,service)); //具体的IO操作交给ProcessorHandler去做
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(serverSocket!=null){
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}

```



#### 4、IO处理类

```java
public class ProcessorHandler implements Runnable{

    private Socket socket;
    private Object service;

    public ProcessorHandler(Socket socket, Object service) {
        this.socket = socket;
        this.service = service;
    }

    @Override
    public void run() {
        ObjectInputStream inputStream=null;
        ObjectOutputStream outputStream=null;
        try {
            inputStream=new ObjectInputStream(socket.getInputStream());//获取输入流
            RpcRequest request=(RpcRequest)inputStream.readObject(); //反序列化
            //调用具体的类中的方法
            Object rs=invoke(request);
            System.out.println("服务端的执行结果："+rs);
            outputStream=new ObjectOutputStream(socket.getOutputStream());
            //将结果写回客户端
            outputStream.writeObject(rs);
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            //TODO 关闭流
            if(inputStream!=null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream!=null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Object invoke(RpcRequest request) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        //通过反射进行服务的调用
        Class clazz=Class.forName(request.getClassName());
        //找到目标方法
        Method method=clazz.getMethod(request.getMethodName(),request.getTypes());
        return method.invoke(service,request.getArgs());
    }
}

```



#### 5、主类-发布服务

```java
public class Bootstrap {
    public static void main(String[] args) {
        //SPRING BOOT
        IOrderService orderService=new OrderServiceImpl();
        RpcProxyServer rpcProxyServer=new RpcProxyServer();
        rpcProxyServer.publisher(orderService,8080);//将服务发布到8080端口
    }
}
```



### 3、客户端

#### 1、pom中引入公共接口sdk

#### 

#### 2、写RPC的代理类-java动态代理

```java
public class RpcProxyClient {

    public <T> T clientProxy(final Class<T> interfaceCls,final String host,final int port){
        return (T) Proxy.newProxyInstance(interfaceCls.getClassLoader(), new Class<?>[]{interfaceCls},new RemoteInvocationHandler(host,port));
    }
}
```



#### 3、请求封装-封装要调用的接口、方法、参数类型、参数

```java
public class RemoteInvocationHandler implements InvocationHandler{
    private String host;
    private int port;

    public RemoteInvocationHandler(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //先建立远程连接
        RpcNetTransport rpcNetTransport=new RpcNetTransport(host,port);
        //传递数据了？
        // 调用哪个接口、 哪个方法、方法的参数？
        RpcRequest request=new RpcRequest();
        request.setArgs(args);
        request.setClassName(method.getDeclaringClass().getName());
        request.setTypes(method.getParameterTypes()); //参数的类型
        request.setMethodName(method.getName());
        return rpcNetTransport.send(request);//发送消息
    }
}

```



#### 4、RPC网络传输-socket

```java
public class RpcNetTransport {

    private String host;
    private int port;

    public RpcNetTransport(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public Socket newSocket() throws IOException {
        Socket socket=new Socket(host,port);
        return socket;
    }

    public Object send(RpcRequest request){
        ObjectOutputStream outputStream=null;
        ObjectInputStream inputStream=null;
        try {
            Socket socket=newSocket();
            //IO操作
            outputStream=new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(request);
            outputStream.flush();
            inputStream=new ObjectInputStream(socket.getInputStream());
            return inputStream.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            //TODO 不写了
        }
        return null;
    }
}

```



#### 5、主类

```java
public class App 
{
    public static void main( String[] args ){
        RpcProxyClient rpcProxyClient=new RpcProxyClient();
        IOrderService orderService=rpcProxyClient.clientProxy(IOrderService.class,"localhost",8080);

        System.out.println(orderService.queryOrderList());
        System.out.println(orderService.orderById("Mic"));

    }
}
```



## v2

相比v1，就是改用类springBoot，并使用了注解，不需要手动的去每个发布的服务调用代理了

结构其实没什么变化，依旧分为三部分：

- 公共接口sdk：定一个接口，封装成sdk---普通mavan工程
- 服务端：依赖公共接口sdk，实现接口的方法，发布服务---springboot
- 客户端：依赖公共接口sdk，通过引入接口，直接能够调用到服务端得到服务---springboot

### 1、公共接口sdk

相比v1没有任何变化

### 2、服务端

#### 1、pom中引入公共接口sdk

#### 2、实现接口

```java
//使用了一个自定义的注解
@XjRemoteService
public class OrderServiceImpl implements IOrderService{

    @Override
    public String queryOrderList() {
        return "EXECUTE QUERYORDERLIST METHOD";
    }

    @Override
    public String orderById(String id) {
        return "EXECUTE ORDER_BY_ID METHOD";
    }
}
```



#### 3、自定义注解

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component   //这个注解很重要，交给spring容器管理
public @interface XjRemoteService {

}
```



#### 4、存储发布服务的实例--内部路由

这个其实很像springMVC中的那个内部路由，这里通过传过来的请求参数就能知道客户端需要调用的是哪个类的哪个方法，就直接调用一下

```java
//单例模式
public class Mediator {

    //用来存储发布的服务的实例(服务调用的路由)
    public static Map<String ,BeanMethod> map=new ConcurrentHashMap<>();

    private volatile static Mediator instance;

    private Mediator(){};

    public static Mediator getInstance(){
       //双重检查
        if(instance==null){
            synchronized (Mediator.class){
                if(instance==null){
                    instance=new Mediator();
                }
            }
        }
        return instance;
    }

    public Object processor(RpcRequest request){
        String key=request.getClassName()+"."+request.getMethodName(); //key
       // 从路由中获取到对应的信息
        BeanMethod beanMethod=map.get(key);
        if(beanMethod==null){
            return null;
        }
      //获取到bean
        Object bean=beanMethod.getBean();
      //获取到方法
        Method method=beanMethod.getMethod();
        try {
          //执行方法
            return method.invoke(bean,request.getArgs());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

}


//这是路由的value保存的对象
@Data
public class BeanMethod {
    private Object bean;
    private Method method;
}


```



#### 5、初始化内部路由

```java
//实现BeanPostProcessor bean加载完后执行
@Component
public class InitialMerdiator implements BeanPostProcessor{

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        //如果bean带有XjRemoteService这个注解
        if(bean.getClass().isAnnotationPresent(XjRemoteService.class)){ //加了服务发布标记的bean进行远程发布
            Method[] methods=bean.getClass().getDeclaredMethods();
            for(Method method:methods){
                String key=bean.getClass().getInterfaces()[0].getName()+"."+method.getName();
                BeanMethod beanMethod=new BeanMethod();
                beanMethod.setBean(bean);
                beanMethod.setMethod(method);
                Mediator.map.put(key,beanMethod);//保存进内部路由
            }
        }
        return bean;
    }
}
```



#### 6、启动ServerSocket监听

```java
//实现ApplicationListener，spring 容器启动完成之后，会发布一个ContextRefreshedEvent=
@Component
public class SocketServerInitial implements ApplicationListener<ContextRefreshedEvent>{
    private final ExecutorService executorService= Executors.newCachedThreadPool();

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        //启动服务
        ServerSocket serverSocket=null;
        try {
            serverSocket=new ServerSocket(8888);
            while(true){
                Socket socket=serverSocket.accept(); //监听客户端请求
                executorService.execute(new ProcessorHandler(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(serverSocket!=null){
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

```



#### 7、IO处理类

```java
public class ProcessorHandler implements Runnable{

    private Socket socket;

    public ProcessorHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        ObjectInputStream inputStream=null;
        ObjectOutputStream outputStream=null;
        try {
            inputStream=new ObjectInputStream(socket.getInputStream());//?
            RpcRequest request=(RpcRequest)inputStream.readObject(); //反序列化
            //路由
            Mediator mediator=Mediator.getInstance();
            Object rs=mediator.processor(request);
            System.out.println("服务端的执行结果："+rs);
            outputStream=new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(rs);
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            // 关闭流
            if(inputStream!=null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream!=null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

```



#### 8、主类

```java
@Configuration
@ComponentScan("com.xj.example")
public class Bootstrap {

    public static void main(String[] args) {
        ApplicationContext applicationContext=
                new AnnotationConfigApplicationContext(Bootstrap.class);
    }
}
```

















