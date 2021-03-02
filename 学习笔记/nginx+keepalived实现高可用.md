

# 1 nginx部署

## 1.1 安装准备

### 1.1.1相关组件安装

在linux下执行yum install gcc gcc-c++安装gcc和gcc-c++环境；

具体操作参见 2.2 redis安装包编译环境准备章节；

在linux下执行yum install pcre pcre-devel;

在linux下执行yum install zlib zlib-devel;

在linux下执行yum install openssl openssl-devel；

 

find / -name nginx

### 1.1.2 本地安装

如果4.1.1描述的yum安装无法成功安装gcc、gcc-c++、pcre、zlib，则需要下载对应的包进行本地安装：

- 1）pcre本地安装相关命令如下：

cd /usr/local/src

wget ftp://ftp.csx.cam.ac.uk/pub/software/programming/pcre/pcre-8.39.tar.gz

tar -zxvf pcre-8.37.tar.gz

cd pcre-8.34

./configure

make

make install

- 2）zlib本地安装相关命令如下：

cd /usr/local/src

 

wget http://zlib.net/zlib-1.2.11.tar.gz

tar -zxvf zlib-1.2.11.tar.gz

cd zlib-1.2.11

./configure

make

make install

- 3）openssl本地安装相关指令如下：

wget https://www.openssl.org/source/openssl-1.0.2h.tar.gz

tar zxf openssl-1.0.2h.tar.gz

cd openssl-1.0.2h

./config shared zlib

because of configuration changes, you MUST do the following before
 *** building:

提醒需要在build之前做make depend

make depend

make

make install

　mv /usr/bin/openssl /usr/bin/openssl.bak

　mv /usr/include/openssl /usr/include/openssl.bak

　ln -s /usr/local/ssl/bin/openssl /usr/bin/openssl

　ln -s /usr/local/ssl/include/openssl /usr/include/openssl

　echo “/usr/local/ssl/lib” >> /etc/ld.so.conf

　ldconfig -v

检测安装是否成功：openssl version -a

 

## 1.2 Nginx下载编译

通过链接http://nginx.org/download/下载nginx的包；

解压下载的tar.gz的包：tar -zxvf nginx-1.1.10.tar.gz

进入到nginx-1.1.10中执行

./configure --prefix=/data/nginx/

make

make install

上述指定完成编译。

## 1.3 Nginx常用指令

### 1.3.1 修改nginx配置

修改nginx文件夹下的nginx.conf文件，可以修改端口等信息。

### 1.3.2 重启/关闭/启动

进入nginx的sbin目录下；

执行./nginx 启动nginx，如果提示启动失败可能是端口被占用；

修改完conf文件后，可以使用./nginx -t进行语法检测；

重启：./nginx -s reload

关闭：pkill -9 nginx



 

# nginx配置



# keepalived

## keepalived安装

- 1、关闭SELinux & iptables
  CentOS6: chkconfig iptable off,service iptables stop
  CentOS7:  systemctl stop firewalld.service
  查看状态：  sestatus
  永久关闭：  vi /etc/selinux/config    设置 SELINUX=disabled
  临时关闭：  setenforce 0
  
- 2、安装启动
  yum install gcc pcre openssl
  yum install ipvsadm
  yum install keepalived
  
- 3、设置自动启动
  CentOS6: chkconfig keepalived on
  CentOS7: systemctl enable keepalived.service
  
- 4、启动
  CentOS6: service keepalived start
  CentOS7: systemctl start keepalived.service
  
- 5、检查进程
  ps -aux |grep keepalived
  检查虚拟IP
  ip addr
  检查运行日志
  tail -f /var/log/messages
  
  

### 查看IP和光口

ip addr

​     ![image-20210202194930716](/Users/beccaxi/Library/Application Support/typora-user-images/image-20210202194930716.png)                          

如上图所示：光口就是eth1，IP是9.134.111.212

### 查看主机名称

hostname

![image-20210202194951056](/Users/beccaxi/Library/Application Support/typora-user-images/image-20210202194951056.png)



## 配置说明

vi /etc/keepalived/keepalived.conf
MASTER参考配置文件如下：

```conf
！Configuration File for keepalived
global_def{
	#定义报警邮件相关，可以忽略
	notification_email{
	***@***
	}
	notification_email_from ***@***
	smtp_server 10.30.**.**
	smtp_connect_timeout 30
	router_id LVS_1    #物理节点标识符，建议使用主机名，hostname命令可查询自己的主机名
}

vrrp_script chk_nginx {
    script "/etc/keepalived/nginx_check.sh" ##执行脚本位置
    interval 2 ##检测时间间隔
    weight -20 ## 如果条件成立则权重减20（-20）

}

vrrp_instance VI_1{
	state MASTER   #这里如果是备机的话，就是BACKUP，注意是大写
	interface eth2   #LVS监控的网络接口，注意和机器上实际网络端口保持一致（使用 ip addr,查看到本机IP地址或者本机小网IP地址在哪个光口下就配置哪个光口，可能是eth、em、bond），否则没有效果
	virtual_router_id 51  #同一实例下，MASTER和BACKUP的该值配置要一样，0到254的范围随便填，但是不要和别的实例相同
	priority 100   #定义优先级，主一般配置100，备机配置50到60，数字越大优先级越高
	advert_int 1    #MASTER和BACKUP负载均衡器之间同步检查的时间间隔，单位是秒
	authentication{
		auth_type PASS
		auth_pass 376879148
		#验证类型和密码，有PASS和AH两种，一般用PASS，密码主背要一致
	}
	virtual_ipaddress{
		10.5.7.253
		#虚拟IP，可以有多个地址，每个一行，不需要掩码
	}
	track_script{
		chk_nginx
		#引用上面的vrrp_script定义的脚本名称，这里没有写
	}
}
```

BACKUP配置参考如下：

```
！Configuration File for keepalived
global_def{
	#定义报警邮件相关，可以忽略
	notification_email{
	***@***
	}
	notification_email_from ***@***
	smtp_server 10.30.**.**
	smtp_connect_timeout 30
	router_id LVS_1    #物理节点标识符，建议使用主机名
}

vrrp_script chk_nginx {
    script "/etc/keepalived/nginx_check.sh" ##执行脚本位置
    interval 2 ##检测时间间隔
    weight -20 ## 如果条件成立则权重减20（-20）

}

vrrp_instance VI_1{
	state BACKUP
	interface eth1
	virtual_router_id 51
	priority 50
	advert_int 1
	authentication{
		auth_type PASS
		auth_pass 376879148
	}
	virtual_ipaddress{
		10.5.7.253
	}
	track_script{
		chk_nginx
	}
}
```



# Nginx+keepalived实现NG集群高可用

## 1、ng检查脚本自己后台运行（杀keepalived）

注意：这种方式在ng挂了以后，keepalived被杀了，所以ng恢复后需要手动启动keepalived

### keepalived的配置

这种方式keepalived.conf不需要自己调用ng检查脚本，所以不需要配置脚本

实例：

Master：

```
! Configuration File for keepalived

global_defs {
   router_id VM-144-54-centos

}
vrrp_instance VI_1 {
    state MASTER
    interface eth1
    virtual_router_id 50 
    unicast_src_ip 9.135.144.54
    unicast_peer {
        9.134.111.212
    }
    priority 100
    advert_int 1
    authentication {
        auth_type PASS
        auth_pass 123456
    }
    virtual_ipaddress {
        9.135.143.44
    }
}
```

Backup：

```
! Configuration File for keepalived

global_defs {
   router_id bhz00jjx6bbm
}

vrrp_instance VI_1 {
    state BACKUP
    interface eth1
    virtual_router_id 50 
    unicast_src_ip 9.134.111.212
    unicast_peer {
        9.135.144.54
    }    
    priority 90 
    advert_int 1
    authentication {
        auth_type PASS
        auth_pass 123456
    }
    virtual_ipaddress {
        9.135.143.44
    }
}
```



### nginx_check.sh脚本

此时，脚本是个死循环，不断运行，不断检查ng，发现ng挂了就会杀死keepalived

实例如下:

```shell
#!/bin/bash

time3=$(date "+%Y-%m-%d %H:%M:%S")
while true
do
if [ $(netstat -tlnp|grep nginx|wc -l) -ne 1 ]
then
  echo $time3 ":ng dead,ready to stop the keepalive!"
  if [ $(service keepalived status | grep inactive | wc -l) -ne 1 ]
  then
    service keepalived stop
    echo $time3 ":success to stop the keepalived"
  fi
fi
sleep 2
echo $time3 ":I am alived ~_~"
done
```

还可以稍微优化下，就是如果发现ng挂了就启动ng，睡两秒再检查，还是挂的再杀keepalived



### 启动顺序

- 1、启动ng

- 2、启动keepalived

- 3、启动脚本（后台运行）：

​          nohup sh /etc/keepalived/nginx_check.sh >> /etc/keepalived/nginx_check.log &

​      ps.这是我自己写的，这个脚本会打印日志，会不停的运行



## 2、keepalived调用ng检查脚本（杀keepalived）

注意：这种方式在ng挂了以后，keepalived被杀了，所以ng恢复后需要手动启动keepalived

此时的keepalived要自己调用ng检查脚本

### keepalived的配置

Master:

```
! Configuration File for keepalived

global_defs {
   router_id VM-144-54-centos

}
######################多出的配置-start#########################
vrrp_script chk_nginx {
    script "/etc/keepalived/nginx_check.sh"
    interval 2
}
######################多出的配置-end#########################
vrrp_instance VI_1 {
    state MASTER
    interface eth1
    virtual_router_id 50 
    unicast_src_ip 9.135.144.54
    unicast_peer {
        9.134.111.212
    }
    priority 100
    advert_int 1
    authentication {
        auth_type PASS
        auth_pass 123456
    }
##################多出的配置-start#############################
    track_script {
        chk_nginx
    }
##################多出的配置-end#############################
    virtual_ipaddress {
        9.135.143.44
    }
}
```

Backup:

```
! Configuration File for keepalived

global_defs {
   router_id bhz00jjx6bbm

}
######################多出的配置-start#########################
vrrp_script chk_nginx {
    script "/etc/keepalived/nginx_check.sh"
    interval 2
}
######################多出的配置-end#########################
vrrp_instance VI_1 {
    state BACKUP
    interface eth1
    virtual_router_id 50 
    unicast_src_ip 9.134.111.212
    unicast_peer {
        9.135.144.54
    }    
    priority 90 
    advert_int 1
    authentication {
        auth_type PASS
        auth_pass 123456
    }
##################多出的配置-start#############################
    track_script {
        chk_nginx
    }
##################多出的配置-end#############################
    virtual_ipaddress {
        9.135.143.44
    }
}
```



### nginx_check.sh脚本

此时不是死循环了，只是一层判断，如果ng挂了，杀keepalived

```shell
#!/bin/bash
A=`ps -C nginx --no-header |wc -l`
if [ $A -eq 0 ];then
     service keepalived stop
    # cd /data/nginx/sbin
    # ./nginx
    # sleep 2
    # if [ `ps -C nginx --no-header |wc -l` -eq 0 ];then
    #     service keepalived stop
    # fi
fi
```

这个脚本不需要特意去启动，keepalived会自己不停的调用



### 启动顺序

- 1、启动ng
- 2、启动keepalived

## 3、keepalived调用ng检查脚本（不杀keepalived）

这种是通过脚本返回值动态变化keepalived设置的priority，当主的priority减少，小于备的时候就会失去VIP

这种方式keepalived是不会挂的，如果是抢占式，ng再次启动，主会立刻把VIP抢回来

### keepalived的配置

Master:

```
! Configuration File for keepalived

global_defs {
   router_id VM-144-54-centos

}

vrrp_script chk_nginx {
    script "/etc/keepalived/nginx_check_3.sh"
    interval 2
    weight -20   # 注意多了这一行配置，这种情况，如果脚本返回的不是0会将优先级减去20，优先级低于BACKUP时就会失去VIP，此时Backup设置的priority是90，主的脚本发现ng挂了1次，减少20就成了80就失去VIP了
}

vrrp_instance VI_1 {
    state MASTER
    interface eth1
    virtual_router_id 50 
    unicast_src_ip 9.135.144.54
    unicast_peer {
        9.134.111.212
    }
    priority 100
    advert_int 1
    authentication {
        auth_type PASS
        auth_pass 123456
    }
    track_script {
        chk_nginx
    }
    virtual_ipaddress {
        9.135.143.44
    }
}
```

Backup

```
! Configuration File for keepalived

global_defs {
   router_id bhz00jjx6bbm

}

vrrp_script chk_nginx {
    script "/etc/keepalived/nginx_check.sh"
    interval 2
    weight -20
}

vrrp_instance VI_1 {
    state BACKUP
    interface eth1
    virtual_router_id 50 
    unicast_src_ip 9.134.111.212
    unicast_peer {
        9.135.144.54
    }    
    priority 90 
    advert_int 1
    authentication {
        auth_type PASS
        auth_pass 123456
    }
    track_script {
        chk_nginx
    }
    virtual_ipaddress {
        9.135.143.44
    }
}
```

### nginx_check.sh脚本

```shell
#!/bin/bash
A=`ps -C nginx --no-header |wc -l`
if [ $A -eq 0 ];then
    exit 1
fi
```

当ng挂了，shell返回码为1

### 启动顺序

- 1、启动ng
- 2、启动keepalived



## 4、非抢占式

keepalived.conf

```
! Configuration File for keepalived

global_defs {
   router_id VM-144-54-centos

}

vrrp_script chk_nginx {
    script "/etc/keepalived/nginx_check_3.sh"
    interval 2
    weight -20
}

vrrp_instance VI_1 {
    state MASTER
    interface eth1
    virtual_router_id 50 
    unicast_src_ip 9.135.144.54
    unicast_peer {
        9.134.111.212
    }
    priority 100
    Nopreempt  ##注意多了这行配置，有了这个配置，主复活后不会抢回VIP，注：主备都要加上这一行配置
    advert_int 1
    authentication {
        auth_type PASS
        auth_pass 123456
    }
    track_script {
        chk_nginx
    }
    virtual_ipaddress {
        9.135.143.44
    }
}
```



## 5、双主(互为主备)

这种方式是有两个VIP的，两台机器上的keepalived一人持有一个VIP，互为主备，如果一个ng挂了，那么两个vip会被同一台机器持有

### keepalived的配置

MASTER1

```
! Configuration File for keepalived

global_defs {
   router_id VM-144-54-centos

}

vrrp_script chk_nginx {
    script "/etc/keepalived/nginx_check.sh"
    interval 2
    weight -20
}

vrrp_instance VI_1 {
    state MASTER
    interface eth1
    virtual_router_id 50 
    unicast_src_ip 9.135.144.54
    unicast_peer {
        9.134.111.212
    }
    priority 100
    advert_int 1
    authentication {
        auth_type PASS
        auth_pass 123456
    }
    track_script {
        chk_nginx
    }
    virtual_ipaddress {
        9.135.143.44
    }
}

vrrp_instance VI_2 {
    state BACKUP
    interface eth1
    virtual_router_id 51
    unicast_src_ip 9.135.144.54
    unicast_peer {
        9.134.111.212
    }
    priority 90
    advert_int 1
    authentication {
        auth_type PASS
        auth_pass 1234567
    }
    track_script {
        chk_nginx
    }
    virtual_ipaddress {
        9.135.143.43
    }
}
```



MASTER2

```! Configuration File for keepalived
! Configuration File for keepalived
global_defs {
   router_id bhz00jjx6bbm

}
vrrp_script chk_nginx {
    script "/etc/keepalived/nginx_check.sh"
    interval 2
    weight -20
}
vrrp_instance VI_1 {
    state BACKUP
    interface eth1
    virtual_router_id 50 
    unicast_src_ip 9.134.111.212
    unicast_peer {
        9.135.144.54
    }    
    priority 90 
    advert_int 1
    authentication {
        auth_type PASS
        auth_pass 123456
    }
    track_script {
        chk_nginx
    }
    virtual_ipaddress {
        9.135.143.44
    }
}
vrrp_instance VI_2 {
    state MASTER
    interface eth1
    virtual_router_id 51
    unicast_src_ip 9.134.111.212
    unicast_peer {
        9.135.144.54
    }
    priority 100
    advert_int 1
    authentication {
        auth_type PASS
        auth_pass 1234567
    }
    track_script {
        chk_nginx
    }
    virtual_ipaddress {
        9.135.143.43
    }
}
```



### 效果

ng都活着时，两台机器各持有一个VIP

MASTER1

![image-20200926231035039](/Users/beccaxi/Library/Application Support/typora-user-images/image-20200926231035039.png)

MASTER2

![image-20200926230941555](/Users/beccaxi/Library/Application Support/typora-user-images/image-20200926230941555.png)



如果MASTER2上面的ng挂了，MASTER1就会有两个VIP

![image-20200926231148727](/Users/beccaxi/Library/Application Support/typora-user-images/image-20200926231148727.png)





## 参考资料

参考：

- Nginx实现高可用：https://blog.csdn.net/qq_37939251/article/details/84062307

​        这个文章ng讲得很多，图文并茂

- Nginx-keepalived+Nginx实现高可用集群：https://www.cnblogs.com/yanjieli/p/10682064.html

​        这篇文章里面详细的说了单主和双主

- keepalived+Nginx高可用实现：https://www.cnblogs.com/itall/p/10913599.html

​       这篇文章里面ng检查脚本是手动启动运行的 

- keepalived+Nginx实现高可用(HA):https://blog.csdn.net/zzhongcy/article/details/88662535

  这篇文章讲了抢占式和非抢占式

  

# 问题记录

### 1、主备都绑定了vip

抓包：

sudo tcpdump -i eth1 vrrp -n

发现

主：

![image-20200924112657032](/Users/beccaxi/Library/Application Support/typora-user-images/image-20200924112657032.png)

备：

![image-20200924112724109](/Users/beccaxi/Library/Application Support/typora-user-images/image-20200924112724109.png)

结论：

1、两台机器在轮询往224.0.0.18（vrrp的组播地址）发送报文。理论上来说，主机处于活跃状态的时候，备份机收到报文之后是不会发送组播消息的，这个很明显就是备份机没收到主机的组播报文。



firewall开启组播通信的方法：

```shell
firewall-cmd --direct --permanent --add-rule ipv4 filter INPUT 0 --in-interface eth1 --destination 224.0.0.18 --protocol vrrp -j ACCEPT
#刷新防火墙
firewall-cmd --reload;
```

其中INPUT 0 --in-interface eth1这段的eth1是绑定了vip的网卡，替换成自己的网卡

如果出现下图情况，是防火墙没开，开一下

![image-20200924113208751](/Users/beccaxi/Library/Application Support/typora-user-images/image-20200924113208751.png)



2、可能是上联交换机禁用了arp的广播限制，造成keepalive无法通过广播通信，两台服务器抢占vip，出现同时都有vip的情况。

 **tcpdump -i eth0 vrrp -n**  检查发现 14和15都在对224.0.0.18发送消息。但是在正常情况下，备节点如果收到主节点的心跳消息时，优先级高于自己，就不会主动对外发送消息。

 

解决方法，将多播调整为单播然后重启服务：

[root@test-15]# vim /etc/keepalived.conf

  priority 50

  unicast_src_ip 172.19.1.15  #本机ip

  unicast_peer {       

​    172.19.1.14   #对端ip

  }

[root@test-14]# vim /etc/keepalived.conf

  priority 100

  unicast_src_ip 172.19.1.14  #本机ip

  unicast_peer {       

​    172.19.1.15   #对端ip

  }
