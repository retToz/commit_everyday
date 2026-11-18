<h1>Kubernates-容器编排技术</h1>

​	主要解决容器在生产环境中的部署、调度、网络、存储、扩缩容和故障恢复问题。

## 基本架构

![img-2](.\img\img-2.jpg)

1>主节点 Master Node

​	1、etcd ： 键值对数据库，保存集群状态变更数据	

​	2、API Service ： K8s 统一操作入口，可以通过 kubectl 进行一些基础CRUD操作

​	3、Scheduler ： 决定 pod 调度到哪个工作节点（根据工作节点的负载状态决定）

​	4、Controller Manager ： 

2>工作节点 Worker Node ： 相当于一台虚拟机

​	1、kubelet ： 管理和维护 pod ，并将 pod 状况汇报给 API Service

​	２、Controller Manager： 监控并维护 pod 状况（负责故障修复）

​	3、container-runtime ：容器运行时，负责获取拉取镜像、创建容器及维护 pod

​		kubectl（kubernate命令行管理工具）：

​		**查看资源**

```
# 查看集群节点
kubectl get nodes

# 查看所有命名空间
kubectl get namespaces

# 查看 Pod
kubectl get pods

# 查看 Service
kubectl get svc

# 查看 Deployment
kubectl get deployments

# 查看指定命名空间
kubectl get pods -n study

# 查看 Pod 所在节点及 IP
kubectl get pods -n study -o wide
```

​		**创建资源**

```
# 创建命名空间
kubectl create namespace study

# 创建 Deployment
kubectl create deployment nginx --image=nginx -n study

# 使用 YAML 创建或更新资源
kubectl apply -f nginx.yaml
```

​		**修改资源**

```
# 编辑 Deployment
kubectl edit deployment nginx -n study

# 修改副本数量
kubectl scale deployment nginx --replicas=3 -n study

# 更新镜像
kubectl set image deployment/nginx nginx=nginx:1.27 -n study
```

​		**删除资源**

```
kubectl delete pod nginx -n study

kubectl delete deployment nginx -n study

kubectl delete -f nginx.yaml
```

​		**3. 排查问题常用命令**

| 命令                   | 作用                    |
| ---------------------- | ----------------------- |
| `kubectl describe pod` | 查看 Pod 详细信息和事件 |
| `kubectl logs`         | 查看容器日志            |
| `kubectl exec`         | 进入容器执行命令        |
| `kubectl get events`   | 查看集群事件            |
| `kubectl top nodes`    | 查看节点资源使用        |
| `kubectl top pods`     | 查看 Pod 资源使用       |

kubernates 中创建基本资源有两种方式：1、kubectl 命令行声明；2、yaml 配置文件。下面均以 yaml配置为例

## 基本属性：



​	1、pod： K8s 中最小的调度单元，类似 Docker 中的容器，为一个或多个容器的组合

```
kubectl run nginx --image=nginx -n study
```

​	删除后，ip 地址会变化，且不会自动重新创建

​	2、sidecar： pod 中因 耦合原因 / 资源共享 必须要将多个容器组合到一起（比如日志收集组件/服务监控组件）这种模式叫边车模式

​	多声明一份容器即可

```
spec:
  volumes:
    - name: nginx-logs
      emptyDir: {}

  containers:
    # 主容器
    - name: nginx
      image: nginx:latest
    # Sidecar 容器
    - name: log-sidecar
      image: busybox:1.36
```

​	3、service： 在集群内部，pod 通过 ip 连接，但是一当出现故障就会重新新建一个 pod ，对应 ip 也会发生改变，为此引入中间层 service ，通过中间层来访问下层 pod

| 类型         | 作用                      | 访问范围         |
| ------------ | ------------------------- | ---------------- |
| ClusterIP    | 集群内部访问              | 集群内部         |
| NodePort     | 通过节点 IP 和端口访问    | 集群外部也可访问 |
| LoadBalancer | 通过外部负载均衡器访问    | 集群外部         |
| ExternalName | 将 Service 映射到外部域名 | DNS 名称解析     |

```````
apiVersion: v1
kind: Service
metadata:
  name: nginx-service
  namespace: study
spec:
  type: NodePort
  selector:(匹配器)
    app: nginx
  ports:
    - name: http
      protocol: TCP
      port: 80
      targetPort: 80
      nodePort: 30080
```````



​	4、volume： pod 有故障重建的可能，通过存储卷可以实现数据持久化

| 类型      | 存储位置          | Pod 删除后   |
| --------- | ----------------- | ------------ |
| emptyDir  | Node 本地临时目录 | 数据删除     |
| hostPath  | Node 宿主机目录   | 数据通常保留 |
| ConfigMap | K8s 配置对象      | 配置仍存在   |
| Secret    | K8s 敏感配置对象  | 配置仍存在   |

###### 	**1. emptyDir：临时存储**

​		Pod 创建时，K8s 自动创建一个空目录，Pod 内的多个容器可以共享它。

​		注：emptyDir 的生命周期跟随 Pod，不跟随容器。

```
volumes:
  - name: data
    emptyDir: {}
```

###### 	**2. hostPath：挂载 Node 本地目录**

​		把 Node 上的真实目录挂载到 Pod 中。

```
volumes:
  - name: mysql-data
    hostPath:
      path: /data/mysql
      type: DirectoryOrCreate
```

###### 	3、**Kubernetes ConfigMap**

​		一般而言，配置文件都会写死在应用层里（如 mysql 连接地址）， configMap 可以在外部创建配置同时应用到容器中。

​	配置：

```test
apiVersion: v1
kind: ConfigMap
metadata:
  name: mes-config（k8s 中的资源名称）
data:
  application.yml(加载文件名称,后跟内容): |
    spring:
      datasource:
        url: jdbc:mysql://mysql:3306/mes
      data:
        redis:
          host: redis
```

​	使用：

​		1> 环境变量注入

    spec:
      containers:
        - name: mes
          image: mes:latest
          env:
            - name: DB_HOST
              valueFrom:
                configMapKeyRef:
                  name: mes-config
                  key: DB_HOST
    
            - name: REDIS_HOST
              valueFrom:
                configMapKeyRef:
                  name: mes-config
                  key: REDIS_HOST

​		2> 挂载配置文件

```
spec:
  containers:
    - name: mes
      image: mes:latest
      volumeMounts:
        - name: config-volume
          mountPath: /app/config

  volumes:
    - name: config-volume
      configMap:
        name: mes-config
```

​	3> 自定义挂在路径和名称

```
spec:
  containers:
    - name: mes
      image: mes:latest
      volumeMounts:
        - name: config-volume
          mountPath: /app/config

  volumes:
    - name: config-volume
      configMap:
        name: mes-config
        items:
          - key: application.yml(key)
            path: application.yml(挂载路径)
          - key: nginx.conf
            path: nginx/default.conf
```

###### 	4、**Secret**

​		与 configMap 类似，用于专门存储敏感数据（非加密，以Base64编码存储）

```
apiVersion: v1
kind: Secret
metadata:
  name: mes-secret
type: Opaque
stringData(声明数据为原始数据，用Data则需填入Base64后的数据):
  username: root
  password: 123456
```

​		挂载方式与 configMap 相同

​	5、ingress

​	　负责件外部请求根据匹配规则转发到不同外部 Service 上，可以理解为 nginx

| 名称               | 作用                               |
| ------------------ | ---------------------------------- |
| Ingress            | 配置路由规则，相当于路由配置表     |
| Ingress Controller | 真正执行路由规则的软件             |
| Nginx              | 可以作为 Ingress Controller 的实现 |

```
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: mes-ingress
  namespace: default(命名空间)
spec:
  ingressClassName: nginx(实现类，另需配置ingressClass)
  rules:
    - host: mes.example.com(域名)
      http:
        paths:
          - path: /api/mes(路径)
            pathType: Prefix(匹配方式)
            backend:
              service:
                name: mes-service(svc 名称)
                port:
                  number: 8081(端口)

          - path: /api/erp
            pathType: Prefix
            backend:
              service:
                name: erp-service
                port:
                  number: 8069	
```

ingressClass 配置文件（以 nginx 为例）：

```
apiVersion: networking.k8s.io/v1
kind: IngressClass
metadata:
  name: nginx
spec:
  controller: k8s.io/ingress-nginx (声明对应的 ingress Controller)
```

​	6、deployment

​	用于管理 Pod 的控制器，

1. 创建和管理 Pod。
2. 保证指定数量的 Pod 始终运行。
3. Pod 异常退出时自动重新创建。
4. 支持应用版本更新。
5. 支持回滚到之前的版本。
6. 支持水平扩缩容。

    apiVersion: apps/v1
    kind: Deployment
    metadata:
      name: nginx-deployment
      namespace: study
    spec:
      replicas: 3(运行的 pod 数量)
      selector:
        matchLabels:
          app: nginx
      template:
        metadata:
          labels:
            app: nginx
    spec:
      containers:
        - name: nginx
          image: nginx:latest
          ports:
            - containerPort: 80

## 完整链路图：

![img-2](.\img\img-3.jpg)
