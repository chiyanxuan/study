### CentOS 7.5 离线安装 Nginx 1.18



# 整体情况介绍

操作系统：CentOS 7.5
Nginx版本：nginx-1.18.0.tar.gz

说明：
1、[官网](http://nginx.org/)推荐的[安装方式](http://nginx.org/en/linux_packages.html#RHEL-CentOS)，通过 yum 安装。快捷省事， 好评 ～ ！
2、官网的[离线安装方式](http://nginx.org/en/docs/configure.html)。工作需要，服务器不能连接外网，因此需要离线安装（本文内容，参考官网及其他博客。感谢各位大神的分享！本文很渣，请轻喷～～）。

通过官网离线安装的示例（下图为官网截图）可以看出，需要的依赖包括：gcc、g++、ssl、pcre、zlib；
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200527214735112.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2FjaGkwMTA=,size_16,color_FFFFFF,t_70,g_center)

## 准备阶段

1、查看 操作系统是否安装 gcc、gcc-c++；
2、从 CentOS 7 镜像中，提取依赖安装包：gcc、gcc-c++；
3、下载Nginx需要依赖的离线安装包：ssl、pcre、zlib；
4、下载Nginx离线安装包：nginx-1.18.0.tar.gz。

## 安装步骤

1、安装依赖：gcc、gcc-c++、ssl、pcre、zlib。注意：一定要先安装gcc，再安装gcc-c++。然后再安装其他，其他的没有先后顺序。
2、安装Nginx；
3、启动Nginx（直接用默认配置启动测试即可）。

# 详细步骤

## 准备

1、查看 操作系统是否安装 gcc、gcc-c++ ：
查看是否安装 gcc 的命令：

```shell
gcc -v
```

查看是否安装 gcc-c++ 的命令：

```shell
g++ -v
```

2、（如果已经安装 gcc ，忽略此步骤。）在 CentOS 7 的安装镜像（下载地址见文末），packages 目录，找到安装 gcc 相关的 rpm 包，并放到一个文件夹里（命名1），列表如下（注意：不同版本的操作系统，对应的rpm版本也不同）：

| 序号 | 安装包                                   |
| ---- | ---------------------------------------- |
| 1    | cpp-4.8.5-28.el7.x86_64.rpm              |
| 2    | gcc-4.8.5-28.el7.x86_64.rpm              |
| 3    | glibc-2.17-222.el7.x86_64.rpm            |
| 4    | glibc-common-2.17-222.el7.x86_64.rpm     |
| 5    | glibc-devel-2.17-222.el7.x86_64.rpm      |
| 6    | glibc-headers-2.17-222.el7.x86_64.rpm    |
| 7    | kernel-headers-3.10.0-862.el7.x86_64.rpm |
| 8    | libmpc-1.0.1-3.el7.x86_64.rpm            |
| 9    | mpfr-3.1.1-4.el7.x86_64.rpm              |

3、（如果已经安装 gcc-c++ ，忽略此步骤。）在 CentOS 7 的安装镜像，packages 目录，找到安装 gcc-c++ 相关的 rpm 包，并放到一个文件夹里（命名2），列表如下（注意：不同版本的操作系统，对应的rpm版本也不同）：

| 序号 | 安装包                                 |
| ---- | -------------------------------------- |
| 1    | gcc-c+±4.8.5-28.el7.x86_64.rpm         |
| 2    | libstdc+±devel-4.8.5-28.el7.x86_64.rpm |

4、下载Nginx需要依赖的离线安装包，放到一个文件夹里（命名3）。下载地址如下：
https://www.openssl.org/source/openssl-1.1.0e.tar.gz
https://ftp.pcre.org/pub/pcre/pcre-8.37.tar.gz
http://www.zlib.net/zlib-1.2.11.tar.gz
5、下载Nginx离线安装包，放到文件夹1、2、3的同级目录：
http://nginx.org/download/nginx-1.18.0.tar.gz

## 安装

1、安装 gcc （如果已经安装 gcc ，忽略此步骤。）：
进入到文件夹1

```shell
rpm -Uvh *.rpm --nodeps --force
```

2、安装 gcc-c++ （如果已经安装 gcc-c++ ，忽略此步骤。）：
进入到文件夹2

```shell
rpm -Uvh *.rpm --nodeps --force
```

3、解压并安装 pcre ：
进入到文件夹3

```shell
tar -zxvf pcre-8.37.tar.gz
cd pcre-8.37/
./configure
make && make install
```

4、解压并安装 zlib ：
进入到文件夹3

```shell
tar -zxvf zlib-1.2.11.tar.gz
cd zlib-1.2.11/
./configure
make && make install
```

5、解压并安装 openssl ：
进入到文件夹3

```shell
tar -zxvf openssl-1.1.0e.tar.gz
cd openssl-1.1.0e/
./config
make && make install
```

6、解压并安装 Nginx ：

```shell
tar -zxvf nginx-1.18.0.tar.gz
cd nginx-1.18.0/
./configure --prefix=/usr/local/nginx --with-http_ssl_module --with-pcre=./3/pcre-8.37 --with-zlib=./3/zlib-1.2.11 --with-openssl=./3/openssl-1.1.0e
# PS ： 上面是啥意思？ 去 官网 http://nginx.org/en/docs/configure.html 学习一下吧。我就不磨叽啦，毕竟有权威 ～
make && make install
```

7、启动Nginx（直接用默认配置启动测试即可）：

```shell
cd /usr/local/nginx/sbin
./nginx
```

8、开放端口：
默认防火墙应该都是开启的，因此需要开放端口。
查看端口：

```shell
firewall-cmd --zone=public --list-ports
```

开放端口：

```shell
firewall-cmd --zone=public --add-port=80/tcp --permanent
```

重启防火墙：

```shell
systemctl restart firewalld
```

9、测试：
浏览器打开，输入IP地址即可，默认使用80端口。
10、查看Nginx进程：

```shell
ps –ef|grep nginx
```

11、停止Nginx：

```shell
./nginx -s stop
```

截图比较少。离线环境没办法截图，请谅解！
最后，感谢您的耐心阅读。



# centos镜像下载

新版本系统镜像下载（当前最新是CentOS 7.4版本）

### CentOS官网

[官网地址](http://isoredirect.centos.org/centos/7.4.1708/isos/x86_64/) http://isoredirect.centos.org/centos/7.4.1708/isos/x86_64/

进入下载页面

![img](https://images2018.cnblogs.com/blog/1259802/201804/1259802-20180404175516909-540952801.png)

下载目录界面分为两个主要的资源区：

Actual Country，表示当前所在国家资源区；

Nearby Countries，表示附近国家资源区

每个资源下边又有本区的不同站点的资源，站点镜像信息中详细表示了镜像文件的地址、类型及版本号等信息。一般选择当前国家资源区的站点下载，获取资源速度比较快。

 

### 阿里云站点下载

http://mirrors.aliyun.com/centos/

进入国内的阿里云的，这里CentOS 7提供了三种ISO镜像文件的下载：DVD ISO、Everything ISO、Minimal ISO。

![img](https://images2018.cnblogs.com/blog/1259802/201804/1259802-20180404175751689-1831225518.png)

 以下针对各个版本的ISO镜像文件，进行一一说明：

CentOS-7-x86_64-DVD-1708.iso        标准安装版，一般下载这个就可以了（推荐）

CentOS-7-x86_64-NetInstall-1708.iso    网络安装镜像（从网络安装或者救援系统）  

CentOS-7-x86_64-Everything-1708.iso    对完整版安装盘的软件进行补充，集成所有软件。（包含centos7的一套完整的软件包，可以用来安装系统或者填充本地镜像）

CentOS-7-x86_64-LiveGNOME-1708.iso     GNOME桌面版  

CentOS-7-x86_64-LiveKDE-1708.iso      KDE桌面版  

CentOS-7-x86_64-Minimal-1708.iso      精简版，自带的软件最少