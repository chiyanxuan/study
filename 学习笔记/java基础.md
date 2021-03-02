## 基础

### 1、sleep和wait

```
sleep是线程类（Thread）的方法，wait是Object类的方法；
sleep不释放对象锁，wait放弃对象锁
sleep暂停线程、但监控状态仍然保持，结束后会自动恢复
wait后进入等待锁定池，只有针对此对象发出notify方法后获得对象锁进入就绪状态，准备获得对象锁进入运行状态

Java中的多线程是一种抢占式的机制，而不是分时机制。抢占式的机制是有多个线程处于可运行状态，但是只有一个线程在运行。 
共同点 ： 
1. 他们都是在多线程的环境下，都可以在程序的调用处阻塞指定的毫秒数，并返回。 
2. wait()和sleep()都可以通过interrupt()方法 打断线程的暂停状态 ，从而使线程立刻抛出InterruptedException。 
如果线程A希望立即结束线程B，则可以对线程B对应的Thread实例调用interrupt方法。如果此刻线程B正在wait/sleep/join，则线程B会立刻抛出InterruptedException，在catch() {} 中直接return即可安全地结束线程。 
需要注意的是，InterruptedException是线程自己从内部抛出的，并不是interrupt()方法抛出的。对某一线程调用 interrupt()时，如果该线程正在执行普通的代码，那么该线程根本就不会抛出InterruptedException。但是，一旦该线程进入到 wait()/sleep()/join()后，就会立刻抛出InterruptedException 。 
不同点 ： 
1.每个对象都有一个锁来控制同步访问。Synchronized关键字可以和对象的锁交互，来实现线程的同步。 
sleep方法没有释放锁，而wait方法释放了锁，使得其他线程可以使用同步控制块或者方法。 
2.wait，notify和notifyAll只能在同步控制方法或者同步控制块里面使用，而sleep可以在任何地方使用 
3.sleep必须捕获异常，而wait，notify和notifyAll不需要捕获异常 
4.sleep是线程类（Thread）的方法，导致此线程暂停执行指定时间，给执行机会给其他线程，但是监控状态依然保持，到时后会自动恢复。调用sleep不会释放对象锁。
5.wait是Object类的方法，对此对象调用wait方法导致本线程放弃对象锁，进入等待此对象的等待锁定池，只有针对此对象发出notify方法（或notifyAll）后本线程才进入对象锁定池准备获得对象锁进入运行状态。
```

### 2、整型数据的特殊性

![image-20210128170910679](/Users/beccaxi/Library/Application Support/typora-user-images/image-20210128170910679.png)

JVM中一个字节以下的整型数据会在JVM启动的时候加载进内存，除非用new Integer()显式的创建对象，否则都是同一个对象

所有只有i04是一个新对象，其他都是同一个对象。所有A，B选项为true

C选项i03和i04是两个不同的对象，返回false

D选项i02是基本数据类型，比较的时候比较的是数值，返回true

### 3、Exception

![image-20210128171155252](/Users/beccaxi/Library/Application Support/typora-user-images/image-20210128171155252.png)



### 4、GCyang区划分

![image-20210128172049356](/Users/beccaxi/Library/Application Support/typora-user-images/image-20210128172049356.png)

-Xmx：最大堆大小

-Xms：初始堆大小

-Xmn:年轻代大小

-XXSurvivorRatio：年轻代中Eden区与一个Survivor区的大小比值，题目中是3，那么 eden：s1:s2=3:1:1

年轻代5120m， Eden：Survivor=3，Survivor区大小=1024m（Survivor区有两个，即将年轻代分为5份，每个Survivor区占一份），总大小为（5120*五分之二）2048m。

-Xms初始堆大小即最小内存值为10240m