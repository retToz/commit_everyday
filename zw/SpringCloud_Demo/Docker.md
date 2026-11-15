# Docker

## docker 是什么

​        与虚拟机相比，都能通过镜像文件简化安装配置过程，而 docker 通过内核级别的隔离机制，实现不同容器的系统隔离（均有完整的文件系统，网络环境）

### docker 操作：

1、创建容器并运行：docker run 

​	-d : 表示让容器在后台运行

​	--name ... ：容器名，唯一

​	-p 虚拟机端口：容器端口：docker 内的容器因隔离无法直接访问，通过访问虚拟机端口来间接访问 docker 容器

​	-e Key=VALUE ：环境变量

​	（通过 --env-file file_path可以绑定通过文件批量绑定环境变量）

​	name ：运行的镜像名称（注：镜像名称分为两部分：[ 镜像名 ] : [ 版本 ] 如 mysql:5.7，不写默认为最新版）

2、查看下载的镜像文件：docker images

3、删除下载镜像： docker rmi

4、镜像创建下载操作：

![image-20251024200727943](D:\Study\java pro\Demo\images\image-20251024200727943.png)

5、容器日志查看：docker logs 容器名

​	-f / -follow ：跟踪日志输出

​	--tail n ：只显示最后 n 行日志

​	-t ：显示时间戳

6、容器操作： docker exec [OPTIONS] 容器名 <命令>

​	-i ： 保持标准输入打开

​	-t ：分配伪终端，交互式命令行

​	-d ：后台执行命令

​	-t ：已指定用户执行命令（默认 root）

7、容器查看： docker ps （注：后跟 --format "table {{.Id}}..." 可以修改显示格式 ）

| docker pull    | 拉取镜像                       | [docker pull](https://docs.docker.com/engine/reference/commandline/pull/) |
| -------------- | ------------------------------ | ------------------------------------------------------------ |
| docker push    | 推送镜像到 Docker Registry     | [docker push](https://docs.docker.com/engine/reference/commandline/push/) |
| docker images  | 查看本地镜像                   | [docker images](https://docs.docker.com/engine/reference/commandline/images/) |
| docker rmi     | 删除本地镜像                   | [docker rmi](https://docs.docker.com/engine/reference/commandline/rmi/) |
| docker run     | 创建并运行容器（不能重复创建） | [docker run](https://docs.docker.com/engine/reference/commandline/run/) |
| docker stop    | 停止指定容器                   | [docker stop](https://docs.docker.com/engine/reference/commandline/stop/) |
| docker start   | 启动指定容器                   | [docker start](https://docs.docker.com/engine/reference/commandline/start/) |
| docker restart | 重新启动容器                   | [docker restart](https://docs.docker.com/engine/reference/commandline/restart/) |
| docker rm      | 删除指定容器                   | [docs.docker.com](https://docs.docker.com/engine/reference/commandline/rm/) |
| docker ps      | 查看容器                       | [docker ps](https://docs.docker.com/engine/reference/commandline/ps/) |
| docker logs    | 查看容器运行日志               | [docker logs](https://docs.docker.com/engine/reference/commandline/logs/) |
| docker exec    | 进入容器                       | [docker exec](https://docs.docker.com/engine/reference/commandline/exec/) |
| docker save    | 保存镜像到本地压缩文件         | [docker save](https://docs.docker.com/engine/reference/commandline/save/) |
| docker load    | 加载本地压缩文件到镜像         | [docker load](https://docs.docker.com/engine/reference/commandline/load/) |
| docker inspect | 查看容器详细信息               | [docker inspect](https://docs.docker.com/engine/reference/commandline/inspect/) |

### 数据卷：

​        数据卷是一个虚拟目录，是容器目录到宿主机目录之间映射的桥梁。容器只包含运行的最低要求的环境配置，因而无法方便的对容器内的配置和静态资源进行修改，通过数据卷可以直接访问到容器内部，实现数据的双向绑定。

创建数据卷 docker volume create [数据卷名]

挂载数据卷： docker run -v 数据卷名称：容器内目录（创建容器时使用）

查看数据卷：docker volume ls

数据卷详细内容： docker volume inspect 数据卷名

注：通过 docker -v 本地目录：容器内目录 来实现docker容器目录和宿主机目录的双向绑定（本地路径必须以 / 或 ./ 开头）

| **命令**              | **说明**             | **文档地址**                                                 |
| :-------------------- | :------------------- | :----------------------------------------------------------- |
| docker volume create  | 创建数据卷           | [docker volume create](https://docs.docker.com/engine/reference/commandline/volume_create/) |
| docker volume ls      | 查看所有数据卷       | [docs.docker.com](https://docs.docker.com/engine/reference/commandline/volume_ls/) |
| docker volume rm      | 删除指定数据卷       | [docs.docker.com](https://docs.docker.com/engine/reference/commandline/volume_prune/) |
| docker volume inspect | 查看某个数据卷的详情 | [docs.docker.com](https://docs.docker.com/engine/reference/commandline/volume_inspect/) |
| docker volume prune   | 清除数据卷           | [docker volume prune](https://docs.docker.com/engine/reference/commandline/volume_prune/) |

### 自定义镜像：

​        镜像的构件包括三层：1、基础镜像（系统函数库）2、层（安装依赖，自定义配置，静态资源配置）3、入口（容器运行时的指令）

通过 Dockerfile 文件来实现以上三步骤：

​	1、FROM ：指定基础镜像

​	2、 ENV :  设置环境变量

​	3、 COPY ： 拷贝本地文件到镜像的指定目录

​	4、RUN ：执行 Linux 的 Shell 指令，一般是安装过程的指令

​	5、 EXPOSE ：指定容器运行时的监听接口，给镜像使用者看的，无实际影响

​	6、 ENTRYPOINT ： 镜像中应用的启动命令，容器运行时调用

构建指令：docker build -t [镜像名]:[标签名] [Dockerfile 路径] （注：标签名不写默认为 latest）

### 自定义网络：

docker 中不同容器的 ip 通过子网分配来实现容器相互通信（docker 相当于交换机），但是分配的  ip 地址是动态的，不便于部署，通过自定义网络可以实现分配静态 ip

​	1、 创建一个网络 ： docker network create [name]

​	2、 查看所有网络 ： docker network ls

​	3、 消除未使用的网络 ： docker network prune

​	4、  删除指定网络 ： docker network rm

​	5、  加入指定网络 ： docker network connect [网络名] [容器名]

​	6、 离开指定网络 ： docker network disconnect [网络名] [容器名]

​	7、 查看网络详细信息 ： docker network inspect [网络名]

### 部署工具 Docker Compose:

​        项目部署一般涉及前、后端，数据库，以及各种中间件，默认一个个部署容易出错且无整体性。Docker Compose 通过一个单独的 docker-compose.yml 模板文件（yaml 格式）来定义一组相关联的容器，帮助我们实现多个相关联的容器的快速部署

```yaml
version: ""

services:
	containerA:
		image:
		container_name:
		ports:
			- ""
			- ""
	containerB:
		...
```

操作：docker compose [OPTIONS] [COMMAND]

​	-f ： 指定 compose 文件的路径和名称

​	-p ： 指定 project 的名称

​	up ： 创建并启动所有 Service 容器

​	down ： 停止并移除所有容器、网络

​	ps ： 列出所有启动的容器

​	logs ： 查看指定容器的日志

​	stop ： 停止容器

​	start ： 启动容器

​	restart ： 重启容器

​	top ： 查看运行的进程

​	exec ： 进入容器并操作	

| **docker run 参数** | **docker compose 指令** | **说明**   |
| :------------------ | :---------------------- | :--------- |
| --name              | container_name          | 容器名称   |
| -p                  | ports                   | 端口映射   |
| -e                  | environment             | 环境变量   |
| -v                  | volumes                 | 数据卷配置 |
| --network           | networks                | 网络       |