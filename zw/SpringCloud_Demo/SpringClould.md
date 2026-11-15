# SpringClould

### 1、微服务

​         常规的Spring Boot 项目为单体项目，因为所有服务部署在同一服务器上，当访问量较大时容易出现延迟卡顿，微服务通过讲不同服务划分为不同模块并独立部署，来增加吞吐量。

​        划分方式：1、纵向拆分：按照业务模块来划分

​			   2、 横线拆分：抽取公共服务，提高复用性

### 什么是 Spring Clould

​        Spring Clould 是一个基于 Spring Clould 的微服务框架，用于快速构建分布式系统中的各种基础设施服务。

​        Spring Cloud 核心组件

| 功能               | 组件                                            | 说明                 |
| ------------------ | ----------------------------------------------- | -------------------- |
| **服务注册与发现** | **Eureka / Nacos / Consul / Zookeeper**         | 微服务之间相互感知   |
| **负载均衡**       | **Ribbon / Spring Cloud LoadBalancer**          | 客户端负载均衡调用   |
| **服务调用**       | **OpenFeign**                                   | 声明式 HTTP 客户端   |
| **配置中心**       | **Spring Cloud Config / Nacos Config**          | 动态配置管理         |
| **网关**           | **Spring Cloud Gateway**                        | API 网关、路由、鉴权 |
| **熔断限流**       | **Hystrix / Sentinel / Resilience4j**           | 服务保护机制         |
| **消息总线**       | **Spring Cloud Bus / Stream / RocketMQ Binder** | 分布式消息通信       |
| **链路追踪**       | **Sleuth + Zipkin / SkyWalking**                | 分布式请求跟踪       |
| **安全认证**       | **Spring Cloud Security / OAuth2**              | 微服务间安全管理     |

#### 1、服务注册与发现

​	微服务架构中，不同服务单独为一个项目，服务之间可能需要相互请求访问，为此需要相互通信

​		1）restTemplate（HttpClient 封装类）

通过注册并调用 restTemplate 来进行通信

```txt
restTemplate.exchange(
	url,
	mythodType,
	requestEntity
	responseEntity
)
```

缺点：1、静态 url 访问，修改麻烦，端口须在运行后知晓

​	    2、 不适用于负载均衡的场景

为此，Spring Cloud 提供了一套完整的「服务治理体系」，其中有三个角色：服务提供者、服务消费者和注册中心

<img src="D:\Study\java pro\Demo\images\image-20251027202921733.png" alt="image-20251027202921733" style="zoom: 50%;float:left" />

##### Nacos 服务注册与提供中间件

​	1、注册中心：Nacos 服务端下载并启动

```sh
docker run -d \
--name nacos \
--env-file ./nacos/custom.env \
-p 8848:8848 \
-p 9848:9848 \
-p 8080:8080 \
--restart=always \
--network local
nacos/nacos-server:latest
```

​	2、服务注册：服务内引入坐标依赖，配置服务端口，完成注册

```pom
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
```

yaml 文件配置：

```	yaml
# 1、服务名称
spring.application.name: example-service
# 2、 nacos 服务地址
spring.cloud.nacos.discovery.server-addr: your-address:8848
```

​	3、服务获取：Spring Cloud 提供了对应的 服务获取类和方法，通过调用即可完成

```txt
private final DiscoveryClient discoveryClient;

List<ServiceInstance> instances = discoveryClient.getIntence("服务名")；
ServiceInstance instance = instances.get(RandomUtil.randomInt(instance.size())); // 随机算法为例
URI url = instance.getUri();

// 通过 restTemplate 发起 http 请求
```

##### openfeign 服务调用中间件

通过对服务注册于发现的中间件的引入，实现了服务之间的通信，但是代码十分繁琐，为此，引入 openfeign 来简化服务之间的调用

openFeign 会帮我们进行 服务发现，并进行负载均衡。

1、坐标

```<!-- Spring Cloud OpenFeign -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

2、启动（配置类上）

```
@EnableFeignClients
```

3、服务接口调用

    @FeignClient(name = "product-service")  // 注册中心里的服务名
    public interface ProductClient {
    	@GetMapping("/product/{id}")
    	Product getProductById(@PathVariable("id") Long id);
    }
注：openfeign 的默认调用为 HttpUrlConnection，默认不支持连接池，性能开销大，通过配置可以修改为 Apache HttpClient 或 okkClient

1、坐标引入

```txt
<!-- Apache Http -->
<dependency>
    <groupId>io.github.openfeign</groupId>
    <artifactId>feign-httpclient</artifactId>
</dependency>
<!-- okkHttp -->
<dependency>
    <groupId>io.github.openfeign</groupId>
    <artifactId>feign-okhttp</artifactId>
</dependency>
```

2、启用配置

```txt
feign:
  httpclient:
    enabled: true   # 启用 Apache HttpClient
  okhttp:
    enabled: true   # 启用 OkHttp
```

3、最佳实现方式

​	1）将微服务分为三个子模块：1、Client 相关接口；2、Client 相关实体类；3、服务相关接口

​	抽取相对麻烦，工程结构相对更复杂，但服务之间耦合度降低

​	2）将 Client 相关 令其一个模块包括 Client、返回实体类

​	抽取更加简单，工程结构也比较清晰，但缺点是整个项目耦合度偏高

4、日志输出

​	openFeign 日志输出级别：

-  NONE：不记录日志信息（默认）
-  BASIC：仅记录请求方法，URL以及响应状态吗和执行时间
- HEADERS：在 BASIC 的基础上，额外记录了请求和响应的头信息
- FULL：记录所有请求和响应明细，包括头信息、请求体、元数据

记录方式：

​	1）局部记录：通过在 Client 类中的 @FeignClient 中添加 configuration = DefaultFeignConfig.class 记录该 Client 的日志（DefaultFeignConfig.class 为配置文件）

```
package config;

import feign.Logger;
import org.springframework.context.annotation.Bean;

// 配置 openfeign 的日志输出级别
public class DefaultFeignConfig {

    @Bean
    public Logger.Level feignLoggerLevel(){
        return Logger.Level.NONE;
    }

}
```

​	2）全局记录：通过在启动项中的 @EnableFeignClients 中添加 defaultConfiguration = DefaultFeignConfig.class 来定义全局日志级别

​	3）yml 配置

```yaml
logging:
  level:
    com.example.client.UserClient: DEBUG
```

5、异常处理

OpenFeign 异常处理主要分为 **4 类场景**：

1. **HTTP 状态码异常（4xx/5xx） → ErrorDecoder**
2. **业务异常（HTTP 200，但是 code != 0） → Decoder/responseInterceptor**
3. **网络异常（超时、连接失败） → Fallback**
4. **统一异常返回 → 全局异常处理**

```
Feign Client调用
        |
        v
    RequestInterceptor
        |
        v
    发起HTTP请求
        |
        v
    接收到Response
        |
        +----------------------------+
        |                            |
     2xx成功                       非2xx
        |                            |
        v                            v
    ResponseInterceptor        ErrorDecoder
        |                            |
        v                            v
     Decoder                      抛出异常
        |                
        v                
     返回对象       
```



### 2、网关

单体项目被分为微服务后，服务请求的鉴权、用户信息的获取更为繁琐冗余（每个项目都需要登录校验来鉴权和获取用户信息）网关包揽了路由的转发和身份校验，向外只需网关 ip ，通过网关进行请求。

#### 1、网关的主要职责

| 功能类别                                         | 详细说明                                                     |
| ------------------------------------------------ | ------------------------------------------------------------ |
| 🛣 **请求路由（Routing）**                        | 根据路径或规则，将请求转发到对应的微服务。例如 `/user/**` → user-service |
| 🧩 **负载均衡（Load Balancing）**                 | 当一个服务有多个实例时，网关会自动把请求分发给不同实例，避免单点压力。 |
| 🔐 **认证与鉴权（Auth）**                         | 校验 JWT Token、Session、API Key 等，统一处理权限逻辑。      |
| ⚙️ **限流与熔断（Rate Limit / Circuit Breaker）** | 控制访问频率，防止流量突增压垮服务。可结合 Sentinel、Resilience4j。 |
| 🚦 **统一日志与监控（Logging & Metrics）**        | 所有请求都经过网关，可以统一采集访问日志、响应时间、调用链追踪（如 Zipkin、Sleuth）。 |
| 🌐 **跨域处理（CORS）**                           | 前后端分离架构中解决跨域问题。                               |
| 🧱 **协议适配与聚合（Protocol / Aggregation）**   | 将多个微服务结果聚合为一个响应，或在 HTTP、WebSocket、gRPC 等协议间做适配。 |

#### 配置

1、配置网关微服务项目，添加相关依赖

```pom
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<aritfatId>spring-cloud-starter-gateway</artifactId>
</dependency>
<!-- 另需 nacos ，loadbalancer 依赖 -->
```

2、添加相关配置

```yaml
spring:
	cloud:
		gateway:
			routes:
				- id: item  # 路由唯一标识
				   uri : lb://item-service # 路由目标微服务名称，lb 代表负载均衡
				   predicates: # 路由断言，判断请求是否符合规则并匹配到对应微服务
					- path = /item/**  # 多个匹配路径可写一行逗号分隔，也可另起一行 - path = ...
				- id ...
```

#### `predicates`（断言）

定义**请求匹配规则**。只有满足这些规则的请求，才会被此路由处理。

常见类型如下👇：

| 断言类型   | 写法                                                         | 说明                   |
| ---------- | ------------------------------------------------------------ | ---------------------- |
| Path       | `- Path=/api/**`                                             | 路径匹配               |
| Method     | `- Method=GET,POST`                                          | HTTP 方法              |
| Host       | `- Host=**.example.com`                                      | 请求域名匹配           |
| Header     | `- Header=X-Request-Id, \d+`                                 | 请求头匹配（正则可用） |
| Query      | `- Query=token, .+`                                          | URL 参数匹配           |
| After      | `- After=2025-11-03T00:00:00+08:00[Asia/Shanghai]`           | 请求时间在之后才匹配   |
| Before     | `- Before=2025-12-01T00:00:00+08:00[Asia/Shanghai]`          | 请求时间在之前才匹配   |
| Between    | `- Between=2025-11-01T00:00:00+08:00[Asia/Shanghai],2025-11-30T00:00:00+08:00[Asia/Shanghai]` | 时间区间匹配           |
| RemoteAddr | `- RemoteAddr=192.168.1.1/24`                                | 来源 IP 匹配           |

#### `filters`（过滤器）

过滤器可以对请求或响应做修改，比如添加请求头、去掉前缀、限流、鉴权等。

常见内置过滤器如下👇：

| 过滤器名                | 示例                                           | 作用             |
| ----------------------- | ---------------------------------------------- | ---------------- |
| **StripPrefix**         | `- StripPrefix=1`                              | 去掉路径前缀     |
| **AddRequestHeader**    | `- AddRequestHeader=X-Color, red`              | 添加请求头       |
| **AddResponseHeader**   | `- AddResponseHeader=X-Result, ok`             | 添加响应头       |
| **RemoveRequestHeader** | `- RemoveRequestHeader=Cookie`                 | 删除请求头       |
| **RequestRateLimiter**  | 限流（需配置 Redis）                           |                  |
| **Retry**               | `- name=Retry, args.retries=3`                 | 请求失败重试     |
| **RewritePath**         | `- RewritePath=/old/(?<path>.*), /new/${path}` | 重写路径         |
| **PrefixPath**          | `- PrefixPath=/api`                            | 给请求路径加前缀 |

注：通过设置 gateway.default-filters 可以实现全局默认配置

#### 2、网关请求处理流程

![image-20251103203954852](D:\Study\java pro\Demo\images\image-20251103203954852.png)

#### 3、自定义路由

##### 自定义过滤器的两种方式

在 Gateway 中，可以自定义两类过滤器：

| 类型                       | 作用范围             | 实现方式                                  |
| -------------------------- | -------------------- | ----------------------------------------- |
| **Route 层 GatewayFilter** | 只作用于指定路由     | 继承 `AbstractGatewayFilterFactory`       |
| Route 层 PredicateFactory  | 只作用于指定路由     | 继承 `AbstractPredicateFactory`           |
| **全局 GlobalFilter**      | 所有路由请求都会生效 | 实现 `GlobalFilter` 接口 + `Ordered` 接口 |

##### 自定义 **Route 层 GatewayFilter**

###### 🔹 1、不携带参数

```
package com.crispysue.mall.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class MyGateWayGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    @Override
    public GatewayFilter apply(Object config){
        return new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                System.out.println("Factory2");
                return chain.filter(exchange);
            }
        };
    }
```

然后在路由配置中使用：

```
spring:
  cloud:
    gateway:
      routes:
        - id: item
          uri: lb://item-service
          filters:
            - MyGateWay # Gateway 的命名必须以GatewayFactory 结尾，已前缀为过滤器名称
```

------

###### 🔹 2、携带参数

适用于带 **可配置参数** 的自定义过滤器：

```
package com.crispysue.mall.gateway.filter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;


public class MyGateWayGatewayFilterFactory extends AbstractGatewayFilterFactory<MyGateWayGatewayFilterFactory.Config> {

    public MyGateWayGatewayFilterFactory() {
        super(Config.class);
    }

    @Data
    static class Config{
        String name;
       Integer age;
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return List.of("name", "list");
    }

    @Override
    public GatewayFilter apply(Config config) {
        return new OrderedGatewayFilter(
                new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                System.out.println("Factory1-name:" + config.getName() + "\nage:" + config.getList());
                return chain.filter(exchange);
            }
        },0);
    }
}

```

YAML 配置示例：

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: item
          uri: lb://item-service
          filters:
            - MyGateWay=Crispysue,22
```

当传参为复杂类（User）时，需写完整表达式

```yaml
filters:
  - name: MyGateWay
     args:
       name: Crispysue
     
```

注：1、完整写法时，在过滤器中就不需要实现属性名映射的方法 shortcutFieldOrder()

​		 2、过滤链会在请求中形成回路，请求自上而下，响应自下而上，可以通过 在 返回值后面添加 .then(Mono.fromRunnable(() -> {...})) 做后置操作

------

##### 自定义 PredicateFactory

```
// 自定义路由断言工厂
// 命名格式为： xxxRoutePredicateFactory  xxx为自定义名称
public class CustomRoutePredicateFactory extends AbstractRoutePredicateFactory<CustomRoutePredicateFactory.Config> {


    public CustomRoutePredicateFactory() {
        super(Config.class);
    }

    @Data
    static class Config{
        // 无参时为空即可
        // 有参数时需配置 shortcutFieldOrder() 方法
        String arg1;
        String arg2;
    }

    // 属性映射
    @Override
    public List<String> shortcutFieldOrder() {
        return List.of("arg1", "arg2");
    }
    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        // 根据需求对请求进行判断，返回true表示满足条件，返回false表示不满足条件
        return exchange -> true;
    }
}
```

yml 配置

```
predicates:
  - name: Custom
     args:
      arg1: test
      arg2: test
```

------

#####  自定义 **GlobalFilter**

### 🔹 实现方式

```
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class MyGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println("GlobalFilter: Pre logic");

        return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    System.out.println("GlobalFilter: Post logic");
                }));
    }

    @Override
    public int getOrder() {
        return -1; // 数值越小，越早执行
    }
}
```

> **注意**：GlobalFilter 的执行顺序由 `getOrder()` 控制，Pre 阶段越小越先，Post 阶段是倒序；同时 GlobalFilter 无需加载到配置文件中

### 3、nacos 配置文件共享

通过配置 nacos 的配置共享，可以让多个微服务或不同环境共享相同的配置内容（日志配置，数据库配置），无需单独配置并独立维护

​        Spring cloud 会在 Spring Boot 的 上下文（ApplicationContext）生成前创建一个临时环境，并通过 bootstrap.yml 文件加载配置，环境配置在主环境创建后复制过去，从而实现 nacos 的 共享文件配置

1、引入坐标依赖

```yaml
<!-- nacos 共享配置文件 -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
</dependency>
<!--读取bootstrap文件-->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-bootstrap</artifactId>
</dependency>
```

2、配置 bootstrap 文件

```
spring:
  cloud:
    nacos:
      discovery:
        server-addr: 192.168.152.128:8848 
      config:
        server-addr: ${spring.cloud.nacos.discovery.server-addr} # 配置文件地址
        shared-configs:
          - data-id: Mysql.yml # 对应 nacos 中存储的配置文件名（注：必须有文件后缀，载入到本地会以 id 为名称）
          - data-id: Knif4j.yml
          - data-id: Log.yml
        file-extension: yaml # 文件格式
```

注springBoot2.4以上不需要配置bootStrap文件即可生效，只需配置：

````
spring:
  config:
  	import:
  	  - nacos:xxx.yml
````

#### 配置热更新：

​	要实现 配置的热更新，nacos 中需加载对应配置文件，且其名称有相应要求： [spring.application.name]-[spring.profile.active\](可选\)，无需加载到 bootstrap 文件中，配置会在 主容器创建时加载对应属性获取对应的配置文件，对应的配置属性中加上 @RefreshScope 和 @Value(${...}}) 即可。

```txt
@Data
@RefreshScope
public class TestProperties{

	@Value("${test.value}")

}
```

#### 路由热更新

​	nacos 提供了 NacosConfigManager.getConfigService().getConfigAndSignListener 来获取配置 配置服务并获取和监听配置更新的方法，同时网关也有 RouteDefinitionWriter.save() 来更新路由，通过配置 Bean 在 容器初始化时装入到 Spring 容器中，实现配置的实时更新

```
@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicRouteLoader {

    private final NacosConfigManager nacosConfigManager;
    private final RouteDefinitionWriter writer;
    private final ApplicationEventPublisher publisher;

    private final String dataId = "gateway-routes.json";
    private final String group = "DEFAULT_GROUP";

    private final Set<String> routeIds = new HashSet<>();

    @PostConstruct
    public void initRouteConfigListener() throws NacosException {
        // 1.项目启动时，先拉取一次配置，并且添加配置监听器
        String configInfo = nacosConfigManager.getConfigService()
                .getConfigAndSignListener(dataId, group, 5000, new Listener() {
                    @Override
                    public Executor getExecutor() {
                        return null;
                    }

                    @Override
                    public void receiveConfigInfo(String configInfo) {
                        // 2.监听到配置变更，需要去更新路由表
                        updateConfigInfo(configInfo);
                    }
                });
        // 3.第一次读取到配置，也需要更新到路由表
        updateConfigInfo(configInfo);
    }

    public void updateConfigInfo(String configInfo){
        log.debug("监听到路由配置信息：{}", configInfo);
        // 1.解析配置信息，转为RouteDefinition
        List<RouteDefinition> routeDefinitions = JSONUtil.toList(configInfo, RouteDefinition.class);

        // 2.删除旧的路由表
        for (String routeId : routeIds) {
            writer.delete(Mono.just(routeId)).subscribe();
        }
        routeIds.clear();

        // 3.更新路由表
        for (RouteDefinition routeDefinition : routeDefinitions) {
            // 3.1.更新路由表
            writer.save(Mono.just(routeDefinition)).subscribe();
            // 3.2.记录路由id，便于下一次更新时删除
            routeIds.add(routeDefinition.getId());
            // 通过 ApplicationPublish 通知网关进行刷新
            publisher.publishEvent(new RefreshRoutesEvent(this));
        }
    }
}
```

### 4、服务保护

微服务之间相互调用可能会引发雪崩问题，即子服务响应超时影响整条服务链上的响应，，通过提高代码的健壮性可以降低雪崩问题的发生；同时微服务Tomcat线程占满，会影响其他业务（级联失败），这里通过以下方法来进行保护

​	1、 请求限流（限制同一时刻接口的访问量）

​	2、 线程隔离（为微服务的每个服务设置响应的最大线程数）

​	3、服务熔断（当达到阈值[慢响应/异常比例]后阻塞对该服务的访问）

在 spring cloud 中有相应的依赖可以直接进行服务保护 - Sentinel，sentinel 通过阻塞微服务的请求来进行服务保护，下面是不同方法的实现过程：

​	1.1、启动 sentinel

```
docker run -d \
--name sentinel \
--network cloud-net \
-p 8858:8858 \
bladex/sentinel-dashboard
```

​	2,1、引入坐标依赖

```txt
<dependency>
	<groupId>com.alibaba.cloud</groupId>
	<artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
</dependency>
```

​	2.2、配置 yaml 

```txt
spring:
  cloud:
    sentinel:
      transport:
        dashboard: ${mall.sentinel.addr}:${mall.sentinel.port} # sentinel 端口
      http-method-specify: true # 将请求方式作为命名前缀
```

​	3.1、请求限流/线程隔离

通过在 sentinel 命令台中 选择对应的接口并点击控流，选择 QPS / 并发线程数 并输入数量即可实现请求限流/线程隔离，超出的请求将返回427

​	3.2 服务熔断

微服务之间的调用通过 openfeign 来实现，首先要配置 openfeign 对 sentinel 的支持

```yaml
feign.sentinel.enabled: true  # 开启 openfiegn 对 sentinel 支持
```

同时设置 openfeign 无响应的回调

```
package com.crispysue.mall.api.fallback;

import com.crispysue.mall.api.clients.ItemClient;
import com.crispysue.mall.api.dto.ItemDTO;
import com.crispysue.mall.api.dto.OrderDetailDTO;
import com.crispysue.mall.common.util.CollUtils;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

public class ItemClientFallBack implements FallbackFactory<ItemClient> {
    @Override
    public ItemClient create(Throwable cause) {
        return new ItemClient() {
            @Override
            public List<ItemDTO> qureyItemByIds(List<Long> ids) {
                return CollUtils.emptyList();
            }

            @Override
            public void deductStock(List<OrderDetailDTO> items) {

            }

            @Override
            public String test() {
                System.out.println("FAIL!!!");
                return "client not get";
            }
        };
    }
}
```

在对应的 client 类上 通过 FallbackFactory.class（注册回调类 Bean 的配置类）

最后在命令台上点击熔断配置相应参数即可

### 5、分布式事务

在微服务中，如果出现调用多个服并进行数据库但是出现异常，已经执行的服务调用因为与调用服务隔离不会受到回滚，违反了事务管理的 ACID 在此引入分布式事务框架 - SEATA

SEATA 中有三个重要的角色：

- **TC** **(Transaction Coordinator)** - **事务协调者：**维护全局和分支事务的状态，协调全局事务提交或回滚。 

- **TM (Transaction Manager) -** **事务管理器：**定义全局事务的范围、开始全局事务、提交或回滚全局事务。 

- **RM (Resource Manager) -** **资源管理器：**管理分支事务，与TC交谈以注册分支事务和报告分支事务的状态，并驱动分支事务提交或回滚。 

  ![屏幕截图 2026-07-23 233510](D:\Study\java pro\Demo\images\屏幕截图 2026-07-23 233510.png)

SEATA 有两种管理方式：

​	1、XA

​	在数据库操作后不直接提交，继续持有数据库锁，同时向 TC 回报结果，TC 在事务结束时根据 RM 的回报来确定是否提交，只有 所有RM 的事务都成功时，TC 才会通知所有事务参与者提交事务，否则回滚

优点：简单，实现了分布式事务的 ACID

缺点：所有事务完成后才能结束数据库锁，性能较差

![屏幕截图 2026-07-23 233948](D:\Study\java pro\Demo\images\屏幕截图 2026-07-23 233948.png)

​	2、AT

​	与 XA 不同，事务操作后会直接提交，但是在执行前会保存一份数据库快照，参与者依旧需要通知事务状态，在事务完成后通过 TC 对结果进行评估，如果有失败的事务，则根据快照回复到修改前的数据。AT模式使用起来更加简单，无业务侵入，性能更好

![屏幕截图 2026-07-23 234021](D:\Study\java pro\Demo\images\屏幕截图 2026-07-23 234021.png)

部署过程

1、部署 seata 服务端（docker为例）

```Shell
docker run --name seata \
-p 8099:8099 \
-p 7099:7099 \
-e SEATA_IP=192.168.150.101 \
-v ./seata:/seata-server/resources \
--privileged=true \
--network hm-net \
-d \
apache/seata-server
```

2、项目中引入依赖

```txt
<dependency>
	<groupId>com.alibaba.cloud</groupId>
	<artifactId>spring-cloud-starter-alibaba-seata</artifactId>
</dependency>
```

3、配置 yml

```txt
seata:
  registry: # TC 服务注册中心的配置，微服务根据这些信息去注册中心获取tc服务地址
    type: nacos # 注册中心类型
    nacos:
      server-addr: ${spring.cloud.nacos.discovery.server-addr} # nacos 地址
      namespace: "" # nacos 中的命名空间，默认为空
      group: DEFAULT_GROUP # 组名
      application: seata-server # seata 服务名称
      username: nacos 
      password: nacos
  tx-service-group: mall # 默认的事务组，在调用事务管理时不命名的默认值
  service:
    vgroup-mapping: # 事务组和 tc 集群的映射关系
      mall: "default" 
```

4、开启事务管理

​	AT 模式为默认模式，只需在对应服务逻辑方法上添加 @GlobalTransaction 即可，XA 模式需配置 yml

```txt
seata:
  data-source-proxy-mode: XA
```

### 6、消息队列

服务的请求是以同步的方式进行，多个服务的请求会使整个请求过程缓慢，通过引入消息队列可以以异步的方式请求，提高服务效率，这里以 RabbitMQ 为例

消息队列包括 ：发布者，交换机，队列，消费者

![image-20251111213144918](D:\Study\java pro\Demo\images\image-20251111213144918.png)

- **`publisher`**：生产者，也就是发送消息的一方
- **`consumer`**：消费者，也就是消费消息的一方
- **`queue`**：队列，存储消息。生产者投递的消息会暂存在消息队列中，等待消费者处理
- **`exchange`**：交换机，负责消息路由。生产者发送的消息由交换机决定投递到哪个队列。
- **`virtual host`**：虚拟主机，起到数据隔离的作用。每个虚拟主机相互独立，有各自的exchange、queue

注：**Exchange（****交换机****）****只负责转发消息，不具备存储消息的能力**，因此如果没有任何队列与Exchange绑定，或者没有符合路由规则的队列，那么消息会丢失！

Queue 的默认采用平均分配的方式，即分配给多个消费者时不会考虑消费者的效率，会影响整体服务的速度，通过设置：

```YAML
spring:
  rabbitmq:
    listener:
      simple:
        prefetch: 1 # 每次只能获取一条消息，处理完成才能获取下一个消息
```

让消费者在获取消息时只缓存一条数据，从而加快业务效率。

##### 交换机类型：

- **Fanout**：广播，将消息交给所有绑定到交换机的队列。我们最早在控制台使用的正是Fanout交换机
- **Direct**：订阅，基于RoutingKey（路由key）发送给订阅了消息的队列
- **Topic**：通配符订阅，与Direct类似，只不过RoutingKey可以使用通配符（# - 0个或多个单词 * - 单个单词，多个单词间用 . 分隔）
- **Headers**：头匹配，基于MQ的消息头匹配，用的较少。

部署 RabbitMQ

````
docker run \
 -e RABBITMQ_DEFAULT_USER=itheima \
 -e RABBITMQ_DEFAULT_PASS=123321 \
 -v mq-plugins:/plugins \
 --name mq \
 --hostname mq \
 -p 15672:15672 \
 -p 5672:5672 \
 --network hm-net\
 -d \
 rabbitmq:management
````

##### 消息发布与消费

1、引入坐标依赖

```pom
<!--AMQP依赖，包含RabbitMQ-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-amqp</artifactId>
  </dependency
```

2、配置 yml 文件

```yaml
spring:
  rabbitmq:
    host: 192.168.150.101 # 你的虚拟机IP
    port: 5672 # 端口
    virtual-host: /hmall # 虚拟主机
    username: hmall # 用户名
    password: 123 # 密码
```

3、交换机 / 队列创建

​	1、创建 Bean 

​	创建 Bean 的方式有两种，一种直接 new ；一种采用工厂的方式

```txt
@Configuration
public class FanoutConfig {
    /**
     * 声明交换机
     * @return Fanout类型交换机
     */
    @Bean
    public FanoutExchange fanoutExchange(){
        return new FanoutExchange("hmall.fanout");	// 对应的 DirectExchange...
        // 或者通过 ExchangeBuilder.fanoutExchange("name").builde ，其余类型的差不多
    }

    /**
     * 第1个队列
     */
    @Bean
    public Queue fanoutQueue1(){
        return new Queue("fanout.queue1");
        // QueueBuilder.durable("name").build();
    }

    /**
     * 绑定队列和交换机
     */
    @Bean
    public Binding bindingQueue1(Queue fanoutQueue1, FanoutExchange fanoutExchange){
        return BindingBuilder.bind(fanoutQueue1).to(fanoutExchange);
    }

    /**
     * 第2个队列
     */
    @Bean
    public Queue fanoutQueue2(){
        return new Queue("fanout.queue2");
    }

    /**
     * 绑定队列和交换机
     */
    @Bean
    public Binding bindingQueue2(Queue fanoutQueue2, FanoutExchange fanoutExchange){
        return BindingBuilder.bind(fanoutQueue2).to(fanoutExchange); // 后跟 with("name") 定向 Queue
    }
}
```

​	2）消费者

```txt
@Component
public class SpringRabbitListener {
        // 利用RabbitListener来声明要监听的队列信息
    // 将来一旦监听的队列中有了消息，就会推送给当前服务，调用当前方法，处理消息。
    // 可以看到方法体中接收的就是消息体的内容
    @RabbitListener(queues = "simple.queue")
    public void listenSimpleQueueMessage(String msg) throws InterruptedException {
        System.out.println("spring 消费者接收到消息：【" + msg + "】");
    }
}
```

​	3）生产者

默认提供了一个 rabbitTemplate 的 实例用于发布消息，

```
rabbitTemplate.convertAndSend(exchangeName, "china.news", message); // 发送到指定消息队列
rabbitTemplate.convertAndSend(exchangeName, "china.news", null, message);// 发送到交换机中
rabbitTemplate.convertAndSend(exchangeName, "china.news","routingKey", message);// 发送到交换机并根据路由发送到指定消息队列中
rabbitTemplate.convertAndSend(exchangeName, "china.news","a.#", message);// 根据通配符路由到指定消息队列中
```

​	2、注解

通过注解可以在消费者中创建对应的交换机和队列以及路由 Routing Key

```txt
@RabbitListener(bindings = @QueueBinding(
    value = @Queue(name = "direct.queue2"),
    exchange = @Exchange(name = "hmall.direct", type = ExchangeTypes.DIRECT),
    key = {"red", "yellow"}
))
public void listenDirectQueue2(String msg){
    System.out.println("消费者2接收到direct.queue2的消息：【" + msg + "】");
}
```

4、消息转化器，传输到 MQ 的序列化器默认为 jdk 提供的，它会将消息序列化为字节，不仅不易查看，而且容量大小较大，通过配置 MessageVonverter 为自定义的转化器来进行优化。注：这边以 jackson 为例转化为 json 格式

​	1、依赖

```XML
<dependency>
    <groupId>com.fasterxml.jackson.dataformat</groupId>
    <artifactId>jackson-dataformat-xml</artifactId>
    <version>2.9.10</version>
</dependency>
```

​	2、自定义 MessageConverter Bean 注入到容器中

```Java
@Bean
public MessageConverter messageConverter(){
    // 1.定义消息转换器
    Jackson2JsonMessageConverter jackson2JsonMessageConverter = new Jackson2JsonMessageConverter();
    // 2.配置自动创建消息id，用于识别不同消息，也可以在业务中基于ID判断是否是重复消息
    jackson2JsonMessageConverter.setCreateMessageIds(true);
    return jackson2JsonMessageConverter;
}
```

##### MQ消息的可靠性：

​	当 MQ 的消息得不到保证时，会出现业务上的不一致（支付服务中支付流水显示支付成功，而通过消息队列的交易服务中的订单状态却显示未支付）为此通过以下几种方法来确保 MQ 消息的可靠性

###### 1、发送者的可靠性

​	1、重试机制，也就是与 MQ 的连接问题，可以通过配置 yml 文件设置等待响应时间、重连次数、重连等待

```yaml
spring:
	rabbitmq:
		connection-timeout: 1s # 设置连接超时时间
		template:
			retry:
				enable: true # 开启超时重连机制
				initial-interval: 1000ms # 失败后的初始等待时间
				mutiplier: 1 # 失败后的下次等待时长倍数  下次等待时长 = 本次等待时长 * mutiplier
				max-attempts: 3 # 最大重连次数
```

​	2、消息确认机制：确认发布者的消息成功传输到交换机上，以及交换机能成功投递给消息队列。针对上述两种情况有两种确认机制：publisher confirm 和 publish return。MQ 会根据处理的情况返回不同回执

| 机制                | 回调时机                                     | 作用                                           |
| ------------------- | -------------------------------------------- | ---------------------------------------------- |
| **ConfirmCallback** | 消息是否到达 **交换机（Exchange）**          | 确认消息有没有成功投递到 Exchange              |
| **ReturnCallback**  | 消息是否从 Exchange 路由到 **队列（Queue）** | 处理路由失败的情况（如 routingKey 无匹配队列） |

总结如下：

- 当消息投递到MQ，但是路由失败时，通过**Publisher Return**返回异常信息，同时返回ack的确认信息，代表投递成功
- 临时消息投递到了MQ，并且入队成功，返回ACK，告知投递成功
- 持久消息投递到了MQ，并且入队完成持久化，返回ACK ，告知投递成功
- 其它情况都会返回NACK，告知投递失败

​	开启方式：

​	1）配置 yml 文件

```txt
spring:
	rabbitmq:
		publisher-confirm-type: corrated # 开启 publisher confirm 机制，corralted 表示异步执行
		publiser-returns: true # 开启 publisher return 机制
```

这里`publisher-confirm-type`有三种模式可选：

- `none`：关闭confirm机制
- `simple`：同步阻塞等待MQ的回执
- `correlated`：MQ异步回调返回回执

​	2）定义 ReturnCallback

```Java
package com.itheima.publisher.config;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Slf4j
@AllArgsConstructor
@Configuration
public class MqConfig {
    private final RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init(){
        # 1、 publish return
        rabbitTemplate.setReturnsCallback(new RabbitTemplate.ReturnsCallback() {
            @Override
            public void returnedMessage(ReturnedMessage returned) {
                log.error("触发return callback,");
                log.debug("exchange: {}", returned.getExchange());
                log.debug("routingKey: {}", returned.getRoutingKey());
                log.debug("message: {}", returned.getMessage());
                log.debug("replyCode: {}", returned.getReplyCode());
                log.debug("replyText: {}", returned.getReplyText());
            }
        });
        #2、 publish confirm
         rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (correlationData == null) return;
            String msgId = correlationData.getId();
            if (ack) {
                log.info("✅ 消息 [{}] 投递成功", msgId);
                retryCache.remove(msgId);
            } else {
                log.warn("❌ 消息 [{}] 投递失败，原因：{}", msgId, cause);
                // 根据 id 和数据进行重发...
                });
            }
        });
    }
}
```

附-confrim 失败重写

    @Slf4j
    @Component
    public class ReliableSender {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    private final Map<String, MessageWrapper> retryCache = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void init() {
        // Confirm 回调（是否到达 Exchange）
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (correlationData == null) return;
            String msgId = correlationData.getId();
    
            if (ack) {
                log.info("✅ 消息 [{}] 投递成功", msgId);
                retryCache.remove(msgId);
            } else {
                log.warn("❌ 消息 [{}] 投递失败，原因：{}", msgId, cause);
                retryCache.computeIfPresent(msgId, (id, wrapper) -> {
                    wrapper.setLastFailReason(cause);
                    wrapper.incrementRetry();
                    return wrapper;
                });
            }
        });
    
        // Return 回调（无法路由）
        rabbitTemplate.setReturnsCallback(returned -> {
            String msgId = returned.getMessage().getMessageProperties().getMessageId();
            log.warn("⚠️ 消息 [{}] 路由失败，replyText={}", msgId, returned.getReplyText());
            retryCache.computeIfPresent(msgId, (id, wrapper) -> {
                wrapper.setLastFailReason(returned.getReplyText());
                wrapper.incrementRetry();
                return wrapper;
            });
        });
    
        // 启动定时重发任务
        startRetryTask();
    }
    
    public void send(String exchange, String routingKey, String msg) {
        String msgId = UUID.randomUUID().toString();
        CorrelationData data = new CorrelationData(msgId);
    
        MessageWrapper wrapper = new MessageWrapper(exchange, routingKey, msg, data);
        retryCache.put(msgId, wrapper);
    
        rabbitTemplate.convertAndSend(exchange, routingKey, msg, message -> {
            message.getMessageProperties().setMessageId(msgId);
            return message;
        }, data);
    
        log.info("🚀 发送消息 [{}]：{}", msgId, msg);
    }
    
    private void startRetryTask() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            retryCache.values().forEach(wrapper -> {
                if (wrapper.getRetryCount() > 3) {
                    log.error("💥 消息 [{}] 超过最大重试次数，放弃。原因：{}", wrapper.getCorrelationData().getId(), wrapper.getLastFailReason());
                    retryCache.remove(wrapper.getCorrelationData().getId());
                    return;
                }
    
                long elapsed = System.currentTimeMillis() - wrapper.getLastSendTime();
                if (elapsed > 5000) { // 每5秒检查一次
                    log.warn("🔁 重发消息 [{}]（第 {} 次）", wrapper.getCorrelationData().getId(), wrapper.getRetryCount() + 1);
                    wrapper.incrementRetry();
                    wrapper.setLastSendTime(System.currentTimeMillis());
                    rabbitTemplate.convertAndSend(wrapper.getExchange(), wrapper.getRoutingKey(),
                            wrapper.getMessage(), wrapper.getCorrelationData());
                }
            });
        }, 5, 5, TimeUnit.SECONDS);
    }
    
    @Data
    @AllArgsConstructor
    static class MessageWrapper {
        private String exchange;
        private String routingKey;
        private String message;
        private CorrelationData correlationData;
        private int retryCount = 0;
        private String lastFailReason;
        private long lastSendTime = System.currentTimeMillis();
    
        void incrementRetry() {
            this.retryCount++;
        }
    }
调用时在参数后面添加一个 CorrelationData 即可

`rabbitTemplate.convertAndSend("hmall.direct", "q", "hello", new CorrelationData());`//默认会用 UUID 产生 id

###### 2、MQ 的可靠性

消息到达MQ以后，如果MQ不能及时保存，也会导致消息丢失，所以MQ的可靠性也非常重要。

1、数据持久化

​	包括交换机、消息队列、消息的持久化。交换机 / 消息队列的创建默认为 Durable 即持久的，消息默认为持久化的

2、LazyQueue

​	RabbitMQ 在 3.12 以上默认开启 LazyQueue，即消息接收后默认先存储到磁盘中，当消息从消息队列中被消费时才会取出。可以预防消息堆积的问题，如消费者宕机或出现网络故障、消息发送量激增，超过了消费者处理速度、消费者处理业务发生阻塞

​	1）直接声明lazy queue

```Java
@Bean
public Queue lazyQueue(){
    return QueueBuilder
            .durable("lazy.queue")
            .lazy() // 开启Lazy模式
            .build();
}
```

​	2）接口式声明

```Java
@RabbitListener(queuesToDeclare = @Queue(
        name = "lazy.queue",
        durable = "true",
        arguments = @Argument(name = "x-queue-mode", value = "lazy")
))
public void listenLazyQueue(String msg){
    log.info("接收到 lazy.queue的消息：{}", msg);
}
```

###### 3、消费者的可靠性

当RabbitMQ向消费者投递消息以后，需要知道消费者的处理状态如何。因为消息投递给消费者并不代表就一定被正确消费了，可能出现的故障有很多，比如：

- 消息投递的过程中出现了网络故障
- 消费者接收到消息后突然宕机
- 消费者接收到消息后，因处理不当导致异常
- ...

一旦发生上述情况，消息也会丢失。因此，RabbitMQ必须知道消费者的处理状态，一旦消息处理失败才能重新投递消息。

​	1）消费者确认机制

​	为了确认消费者是否成功处理消息，RabbitMQ提供了消费者确认机制（**Consumer Acknowledgement**）。即：当消费者处理消息结束后，应该向RabbitMQ发送一个回执，告知RabbitMQ自己消息处理状态。回执有三种可选值：

- ack：成功处理消息，RabbitMQ从队列中删除该消息
- nack：消息处理失败，RabbitMQ需要再次投递消息
- reject：消息处理失败并拒绝该消息，RabbitMQ从队列中删除该消息

​		SpringAMQP帮我们实现了消息确认。并允许我们通过配置文件设置ACK处理方式，有三种模式：

- **`none`**：不处理。即消息投递给消费者后立刻ack，消息会立刻从MQ删除。非常不安全，不建议使用
- **`manual`**：手动模式。需要自己在业务代码中调用api，发送`ack`或`reject`，存在业务入侵，但更灵活
- **`auto`**：自动模式。SpringAMQP利用AOP对我们的消息处理逻辑做了环绕增强，当业务正常执行时则自动返回`ack`.  当业务出现异常时，根据异常判断返回不同结果，**确认机制的默认处理方式**
  - 如果是**业务异常**，会自动返回`nack`；
  - 如果是**消息处理或校验异常**，自动返回`reject`;

通过配置 yml 文件设置处理方式

```txt
spring:
	rabbit:
		listener:
			simple:
				acknowledge-mode: auto
```

​	2）失败重试机制

在返回 nack 时，消息会重新入队再发送给消费者，如果依旧抛出异常则会一直投递，通过配置 yml 可以对重试机制进行修改

```
spring:
	rabbit:
		listener:
			simple:
				retry:
					enable: true # 开启消费者失败重试
					initial-interval: 1000ms # 初始失败等待时长
					mutiplier: 1 # 失败等待时长倍数，下次等待时长 = 本次 * 倍数
					max-attempts: 3 # 最大重试次数
					stateless: true # 无状态  false 有状态，如果包含事务
```

​		3）失败处理策略

​	配置失败重试机制后，默认的策略为丢弃失败的消息，而这个策略是通过 `MessageRecovery` 来配置的，有以下三种实现方式：

- `RejectAndDontRequeueRecoverer`：重试耗尽后，直接`reject`，丢弃消息。默认就是这种方式 
-  `ImmediateRequeueMessageRecoverer`：重试耗尽后，返回`nack`，消息重新入队 
-  `RepublishMessageRecoverer`：重试耗尽后，将失败消息投递到指定的交换机 ，同时携带失败原因

这里通过自定义 MessageRecover  Bean 的方式修改失败策略

```Java
@Bean
public MessageRecoverer republishMessageRecoverer(RabbitTemplate rabbitTemplate){
    return new RepublishMessageRecoverer(rabbitTemplate, "error.direct", "error");
}
```

4、业务幂等性

**幂等**是一个数学概念，用函数表达式来描述是这样的：`f(x) = f(f(x))`，例如求绝对值函数。

在程序开发中，则是指同一个业务，执行一次或多次对业务状态的影响是一致的。例如：

- 根据id删除数据
- 查询数据
- 新增数据

但数据的更新往往不是幂等的，如果重复执行可能造成不一样的后果。比如：

- 取消订单，恢复库存的业务。如果多次恢复就会出现库存重复增加的情况
- 退款业务。重复退款对商家而言会有经济损失。

消费者可能因各种原因重复执行（未返回 ack...）或是我们在用户支付成功后会发送MQ消息到交易服务，修改订单状态为已支付，就可能出现消息重复投递的情况。如果消费者不做判断，很有可能导致消息被消费多次，出现业务故障。

举例：

1. 假如用户刚刚支付完成，并且投递消息到交易服务，交易服务更改订单为**已支付**状态。
2. 由于某种原因，例如网络故障导致生产者没有得到确认，隔了一段时间后**重新投递**给交易服务。
3. 但是，在新投递的消息被消费之前，用户选择了退款，将订单状态改为了**已退款**状态。
4. 退款完成后，新投递的消息才被消费，那么订单状态会被再次改为**已支付**。业务异常。

为了保证消息处理的幂等性。这里给出两种方案：

- 唯一消息ID
- 业务状态判断

​	1）唯一消息 ID

​	通过为每条消息生成一个唯一的 id ，与消息一起投递给消费者，消费者通过存储对应消息 id 判断是否执行过来实现，而 SpringAMQP的MessageConverter自带了MessageID的功能，我们只要开启这个功能即可。

消费者通过配置完整的消息参数 Message 获取对应的 id

```JAva
public void listenQueue(Message msg){
	System.out.println("id = " + msg.getMessageProperties().getMessageId());
}
```

​	2）业务判断

​	通过在业务执行前进行判断（如业务逻辑为修改订单状态到已支付，则判断订单状态是否为未支付来证明是否重复处理）

5、兜底方案

虽然我们利用各种机制尽可能增加了消息的可靠性，但也不好说能保证消息100%的可靠。万一真的MQ通知失败该怎么办呢？

有没有其它兜底方案，能够确保订单的支付状态一致呢？

其实思想很简单：既然MQ通知不一定发送到交易服务，那么交易服务就必须自己**主动去查询**支付状态。这样即便支付服务的MQ通知失败，我们依然能通过主动查询来保证订单状态的一致。

流程如下：

![image-20251112000200905](D:\Study\java pro\Demo\images\image-20251112000200905.png)

图中黄色线圈起来的部分就是MQ通知失败后的兜底处理方案，由交易服务自己主动去查询支付状态。

不过需要注意的是，交易服务并不知道用户会在什么时候支付，如果查询的时机不正确（比如查询的时候用户正在支付中），可能查询到的支付状态也不正确。

那么问题来了，我们到底该在什么时间主动查询支付状态呢？

这个时间是无法确定的，因此，通常我们采取的措施就是利用**定时任务**定期查询，例如每隔20秒就查询一次，并判断支付状态。如果发现订单已经支付，则立刻更新订单状态为已支付即可。

##### 延迟消息

在电商的支付业务中，对于一些库存有限的商品，为了更好的用户体验，通常都会在用户下单时立刻扣减商品库存。例如电影院购票、高铁购票，下单后就会锁定座位资源，其他人无法重复购买。

但是这样就存在一个问题，假如用户下单后一直不付款，就会一直占有库存资源，导致其他客户无法正常交易，最终导致商户利益受损！

因此，电商中通常的做法就是：**对于超过一定时间未支付的订单，应该立刻取消订单并释放占用的库存**。

例如，订单支付超时时间为30分钟，则我们应该在用户下单后的第30分钟检查订单支付状态，如果发现未支付，应该立刻取消订单，释放库存。

但问题来了：如何才能准确的实现在下单后第30分钟去检查支付状态呢？

像这种在一段时间以后才执行的任务，我们称之为**延迟任务**，而要实现延迟任务，最简单的方案就是利用MQ的延迟消息了。

在RabbitMQ中实现延迟消息也有两种方案：

- 死信交换机+TTL
- 延迟消息插件

###### 	1、死信交换机

​	当消息队列中的消息出现以下状况，便会称为死信：

- 消费者使用`basic.reject`或 `basic.nack`声明消费失败，并且消息的`requeue`参数设置为false
- 消息是一个过期消息，超时无人消费
- 要投递的队列消息满了，无法投递

称为死信后，如果队列配置了 `dead-letter-exchange` 属性指定交换机，则该交换机便是死信交换机，死信会投递到该交换机中，

死信交换机的作用：

1. 收集那些因处理失败而被拒绝的消息
2. 收集那些因队列满了而被拒绝的消息
3. 收集因TTL（有效期）到期的消息

PS：死信交换机和 RepublishMessageRecoverer 失败重试方法的区别：RepublishMessageRecoverer 是通过重发消息的方式来实现重试策略，同时携带失败原因在 header 上，死信是 mq 内部把消息转移到对应的队列上，没有则直接删除。

###### 	2、延迟消息

​	延迟消息便是基于死信交换机，通过设置超时时间，使其投递到死信交换机并做超时处理，

​	1）死信交换机+ttl

​	需要配置两个交换机，一个为死信交换机，通过消费死信交换机中的死信实现延时消息

```java
@Component
public class NormalConfiguration{
    	// 交换机
    	@Bean
    	public DirectExchange normalExchange(){
        	return new DirectExchange("normal.direct");
    }
    	//消息队列
    	@Bean
    	public Queue normalQueue(){
            	return QueueBuilder.durable("normal.queue").deadLetterExchange("dlx.direct").build();
        }
    	// 绑定
    	@Bean
    	public Binding normalExchangeBinding(Queue normalQueue,DirectExchange normalExchange){
            	return bindingBuilder.bind(normalQueue).to(normalExchange).with("test");
        }
}
```

消费者通过正常绑定交换机的方式绑定死信交换机即可处理延时消息，通过对消息的后置处理器设置有效时长

```java
rabbitTemplate.convertAndSend("normal.direct","hi","test msg",new MessagePostProcessor() {
	@Overrides
	public Message postProcessMessage(Message message) throws AmqpException{
		message.getMessageProperties().setExpiration("10000"); // 毫秒
		return message;
	}
})
```

​	2）延迟消息插件

通过配置 DelayExchange插件 即可简化上述步骤，只需配置单个交换机即可

```java
// 注解
RabbitListener(bindings = @QueueBinding(
        value = @Queue(name = "delay.queue", durable = "true"),
        exchange = @Exchange(name = "delay.direct", delayed = "true"),
        key = "delay"
))
public void listenDelayMessage(String msg){
    log.info("接收到delay.queue的延迟消息：{}", msg);
}

// Bean
@Bean
    public DirectExchange delayExchange(){
        return ExchangeBuilder
                .directExchange("delay.direct") // 指定交换机类型和名称
                .delayed() // 设置delay的属性为true
                .durable(true) // 持久化
                .build();
    }

    @Bean
    public Queue delayedQueue(){
        return new Queue("delay.queue");
    }
    
    @Bean
    public Binding delayQueueBinding(){
        return BindingBuilder.bind(delayedQueue()).to(delayExchange()).with("delay");
    }
```

发送消息时，设置 x-delay属性设定延迟时间，

```java
rabbitTemplate.convertAndSend("delay.direct", "delay", message, new MessagePostProcessor() {
        @Override
        public Message postProcessMessage(Message message) throws AmqpException {
            // 添加延迟消息属性
            message.getMessageProperties().setDelay(5000);
            return message;
        }
    });
```

PS：插件本质是识别 header 中的对应属性，并根据时间将消息发送到对应交换机中。

### 7、搜索引擎

​	在使用 Mysql 进行搜索功能时，往往是用模糊匹配的方式来实现的，其存在缺点：1、查询效率低，模糊匹配是通过顺序查询的方式，没有索引优化。2、功能单一，单词匹配必须正好包含关键词才能找到，而在搜索引擎中，用户输入出现个别错字，或者用拼音搜索、同义词搜索都能正确匹配到数据。下面以 ElasticSearch 为例介绍。

倒排索引：倒排索引时基于 MySql 的正向索引而言的，MySql 会为数据表根据 id 建立对应的索引，采用 B-Link 树，即只在叶子节点存数据， 跟节点存数据索引，因而当模糊索引时无法进行优化。

ElasticSearch 通过倒排索引来实现搜索上的优化，ES 会对对应的数据属性以及搜索关键字根据分词算法进行拆分成一个个词条，数据属性生成的词条创建对应的索引表，包括词条，文档 id，对应出现位置等信息，搜索时便能根据该索引查询到对应结果。

在 ES 中，文档对应一条数据，以 Json 的格式存储；索引对应数据库，存储不同类型的数据，并通过 映射的方式定义存储的结构。

| **MySQL** | **Elasticsearch** | **说明**                                                     |
| :-------- | :---------------- | :----------------------------------------------------------- |
| Table     | Index             | 索引(index)，就是文档的集合，类似数据库的表(table)           |
| Row       | Document          | 文档（Document），就是一条条的数据，类似数据库中的行（Row），文档都是JSON格式 |
| Column    | Field             | 字段（Field），就是JSON文档中的字段，类似数据库中的列（Column） |
| Schema    | Mapping           | Mapping（映射）是索引中文档的约束，例如字段类型约束。类似数据库的表结构（Schema） |
| SQL       | DSL               | DSL是elasticsearch提供的JSON风格的请求语句，用来操作elasticsearch，实现CRUD |

##### 部署：

```Bash
docker run -d \
  --name es \
  -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
  -e "discovery.type=single-node" \
  -v es-data:/usr/share/elasticsearch/data \
  -v es-plugins:/usr/share/elasticsearch/plugins \
  --privileged \
  --network hm-net \
  -p 9200:9200 \
  -p 9300:9300 \
  elasticsearch:7.12.1
```

```Bash
docker run -d \
--name kibana \
-e ELASTICSEARCH_HOSTS=http://es:9200 \
--network hm-net \
-p 5601:5601  \
kibana:7.12.1
```

#### 索引：

##### 	1、创建索引

- 请求方式：`PUT`

- 请求路径：`/索引库名`，可以自定义

- 请求参数：`mapping`映射、

  常见的Mapping属性包括：

  - `type`：字段数据类型，常见的简单类型有： 
    - 字符串：`text`（可分词的文本）、`keyword`（精确值，例如：品牌、国家、ip地址）
    - 数值：`long`、`integer`、`short`、`byte`、`double`、`float`、
    - 布尔：`boolean`
    - 日期：`date`
    - 对象：`object`
  - `index`：是否创建索引，默认为`true`
  - `analyzer`：使用哪种分词器
  - `properties`：该字段的子字段

```txt
PUT /索引库名称
{
	"mappings":{
		"properties":{
			"字段名":{
				"type": "text",
				"analyzer": "ik_smart"	
			}
			"字段名":{
				"type": "keyword"
				"index": "false"	
			}
			"字段名":{
				"properties":{
            				"字段名":{
            					"type": "keyword"
            				}
				}
			}
		}
	}
}
```

##### 	2、查询索引库

- 请求方式：GET 
-  请求路径：/索引库名 
-  请求参数：无 

```Plain
GET /索引库名
```

##### 	3、修改索引库

索引库建立后是无法进行修改的，处理添加新的字段到其中

```txt
PUT /索引库名/_mapping
{
	"properties": "Integer"
		}
	}
}
```

​	4、删除索引库

- 请求方式：DELETE 
-  请求路径：/索引库名 
-  请求参数：无 

````
DELETE /索引库名
````

#### 2、文档操作：

##### 	1、新增文档

```txt
POST /索引库名/_doc/文档 id
{
	"字段名": "值",
	"字段名": "值",
	"字段名":{
		"子字段名":"值",
		"子字段名":"值"
	}
}
```

##### 	2、查询文档

```txt
GET /索引库名/_doc/文档 id
```

##### 	3、删除文档

```txt
DELETE /索引库名/_doc/文档 id
```

##### 	4、修改文档

修改文档的方式包括两种：

​	1、全量修改：删除原有的重新建立一份

​	2、局部修改，修改文档中的部分字段

​	1、全量修改

全量修改跟1新建差不多，除了访问方式变了

```txt
PUT /索引库名/_doc/文档 id
{
	"字段1": "值1",
	"字段2": "值2",
	// ... 略
}
```

​	2、局部修改

局部修改是只修改指定id匹配的文档中的部分字段。

```txt
POST /索引库名/_update/文档 id
{
	"doc":{
		"修改字段名": "值"
	}
}
```

#### 3、批处理

批操作的请求方式如下：

```txt
POST _bulk
{"index": {"_index": "test", "_id": "1"}}
{"field1": "value1"}
{"delete": {"_index": "test", "_id": "2"}}
{"create: {"_index": "test", "_id":  "3"}"}
{"field1": "value2"}
{"update": {"_index": "test", "_id": "1"}}
{"doc": {"field2" : "value3"}}
```

其中：

- `index`代表新增操作
  - `_index`：指定索引库名
  - `_id`指定要操作的文档id
  - `{ "field1" : "value1" }`：则是要新增的文档内容

- `delete`代表删除操作
  - `_index`：指定索引库名
  - `_id`指定要操作的文档id

- `update`代表更新操作
  - `_index`：指定索引库名
  - `_id`指定要操作的文档id
  - `{ "doc" : {"field2" : "value2"} }`：要更新的文档字段

示例，批量新增：

```
POST /_bulk
{"index": {"_index":"heima", "_id": "3"}}
{"info": "黑马程序员C++讲师", "email": "ww@itcast.cn", "name":{"firstName": "五", "lastName":"王"}}
{"index": {"_index":"heima", "_id": "4"}}
{"info": "黑马程序员前端讲师", "email": "zhangsan@itcast.cn", "name":{"firstName": "三", "lastName":"张"}}
```

#### 4、RestAPI

在 java 中也有对应的依赖包，方便我们发起http请求

1、引入坐标：

```XML
<dependency>
    <groupId>org.elasticsearch.client</groupId>
    <artifactId>elasticsearch-rest-high-level-client</artifactId>
</dependency>
```

2、注入 Bean 对象

```txt
RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(HttpHost.create("http://192.168.152.128:9200")))
```

3、操作

###### 	1、创建索引库

```
// 1、创建 Request 对象
CreateIndexRequest request = new CreateIndexRequest("goods");	//goods 为索引库名
// 2、 准备请求参数
request.source(MAPPING_TEMPLATE, XContentType.JSON);	//MAPPING_TEMPLATE 为 JSON 参数对应文本
// 3、 发送请求
client.indices().create(request, RequestOptions.DEFAULT);
```

###### 	2、索引库是否存在

```
// 1、 创建 Request 请求
GetIndexRequest request = new GetIndexRequest("goods");
// 2、 发送请求
boolean flag = client.indices().exists(request, RequestOptions.DEFAULT);
System.out.println(flag);
```

###### 	3、删除索引库

```
        // 1、 创建 Request 请求
        DeleteIndexRequest request = new DeleteIndexRequest("goods");
        // 2、 发送请求
        client.indices().delete(request, RequestOptions.DEFAULT);
```

###### 	4、新增文档

```txt
// 1.根据id查询商品数据
    Item item = itemService.getById(100002644680L);
    // 2.转换为文档类型
    ItemDoc itemDoc = BeanUtil.copyProperties(item, ItemDoc.class);
    // 3.将ItemDTO转json
    String doc = JSONUtil.toJsonStr(itemDoc);

    // 1.准备Request对象
    IndexRequest request = new IndexRequest("items").id(itemDoc.getId());
    // 2.准备Json文档
    request.source(doc, XContentType.JSON);
    // 3.发送请求
    client.index(request, RequestOptions.DEFAULT);
```

###### 	5、查询文档

```
// 1.准备Request对象
    GetRequest request = new GetRequest("items").id("100002644680");
    // 2.发送请求
    GetResponse response = client.get(request, RequestOptions.DEFAULT);
    // 3.获取响应结果中的source
    String json = response.getSourceAsString();
    
    ItemDoc itemDoc = JSONUtil.toBean(json, ItemDoc.class);
    System.out.println("itemDoc= " + ItemDoc);
```

###### 	6、删除文档

```txt
// 1.准备Request，两个参数，第一个是索引库名，第二个是文档id
    DeleteRequest request = new DeleteRequest("item", "100002644680");
    // 2.发送请求
    client.delete(request, RequestOptions.DEFAULT);
```

​	7、修改文档

​	修改文档包括两种：1、全量修改，与新建文档的方式一致；2、局部修改，如下所示：

```
// 1.准备Request
    UpdateRequest request = new UpdateRequest("items", "100002644680");
    // 2.准备请求参数
    request.doc(
            "price", 58800,
            "commentCount", 1
    );
    // 3.发送请求
    client.update(request, RequestOptions.DEFAULT);
```

​	8、批量导入

能添加的请求有：

- `IndexRequest`，也就是新增
- `UpdateRequest`，也就是修改
- `DeleteRequest`，也就是删除

```txt
    // 1.创建Request
    BulkRequest request = new BulkRequest();
    // 2.准备请求参数
    request.add(new IndexRequest("items").id("1").source("json doc1", XContentType.JSON));
    request.add(new IndexRequest("items").id("2").source("json doc2", XContentType.JSON));
    // 3.发送请求
    client.bulk(request, RequestOptions.DEFAULT);
```

#### 5、DSL 查询

ElasticSearch 提供了基于 JSON 的 DSL(Domain Specific Language) 语句来定义查询条件，查询可分为两大类：

- **叶子查询（Leaf** **query** **clauses）**：一般是在特定的字段里查询特定值，属于简单查询，很少单独使用。
- **复合查询（Compound** **query** **clauses）**：以逻辑方式组合多个叶子查询或者更改叶子查询的行为方式。

```txt
GET /索引库名/_search
{
	"query":{
		"查询类型":{
			// ..查询条件
		}
	}
}
```

##### 	1、叶子查询

查询类型包括：

- **全文检索查询（Full Text Queries）**：利用分词器对用户输入搜索条件先分词，得到词条，然后再利用倒排索引搜索词条。例如：

  - `match`：

    ```JSON
    GET /{索引库名}/_search
    {
      "query": {
        "match": {
          "字段名": "搜索条件"
        }
      }
    }
    ```

  - `multi_match`:

    ```JSON
    GET /{索引库名}/_search
    {
      "query": {
        "multi_match": {
          "query": "搜索条件",
          "fields": ["字段1", "字段2"]
        }
      }
    }
    ```

- **精确查询（****Term-level queries****）**：不对用户输入搜索条件分词，根据字段内容精确值匹配。但只能查找keyword、数值、日期、boolean类型的字段。例如：

  - `ids`

    ```txt
    "query": {
        "ids": {
        	"values": ["1","2","3"']
        }
      }
    ```

    

  - `term`

    ```txt
    "query": {
        "term": {
          "字段名": {
            "value": "搜索条件"
          }
        }
      }
    ```

  - `range`

    ```JSON
      "query": {
        "range": {
          "字段名": {
            "gte": {最小值},
            "lte": {最大值}
          }
        }
    ```

    `range`是范围查询，对于范围筛选的关键字有：

    - `gte`：大于等于
    - `gt`：大于
    - `lte`：小于等于
    - `lt`：小于

- **地理坐标查询****：**用于搜索地理位置，搜索方式很多，例如：

  - `geo_bounding_box`：按矩形搜索
  - `geo_distance`：按点和半径搜索

查询结果大致如下：

![image-20251117174327973](D:\Study\java pro\Demo\images\image-20251117174327973.png)

##### 	2、复合查询

符合查询大致分为两类：

- 第一类：基于逻辑运算组合叶子查询，实现组合条件，例如
  - bool
- 第二类：基于某种算法修改查询时的文档相关性算分，从而改变文档排名。例如：
  - function_score
  - dis_max

###### 	1、算分函数：

​	function score 查询中包含四部分内容：

- **原始查询**条件：query部分，基于这个条件搜索文档，并且基于BM25算法给文档打分，**原始算分**（query score)
- **过滤条件**：filter部分，符合该条件的文档才会重新算分
- **算分函数**：符合filter条件的文档要根据这个函数做运算，得到的**函数算分**（function score），有四种函数 
  - weight：函数结果是常量
  - field_value_factor：以文档中的某个字段值作为函数结果
  - random_score：以随机数作为函数结果
  - script_score：自定义算分函数算法
- **运算模式**：算分函数的结果、原始查询的相关性算分，两者之间的运算方式，包括： 
  - multiply：相乘
  - replace：用function score替换query score

```JSON
GET /hotel/_search
{
  "query": {
    "function_score": {
      "query": {  .... }, // 原始查询，可以是任意条件
      "functions": [ // 算分函数
        {
          "filter": { // 满足的条件，品牌必须是Iphone
            "term": {
              "brand": "Iphone"
            }
          },
          "weight": 10 // 算分权重为2
        }
      ],
      "boost_mode": "multipy" // 加权模式，求乘积
    }
  }
}
```

###### 	2、bool 查询

bool查询，即布尔查询。就是利用逻辑运算来组合一个或多个查询子句的组合。bool查询支持的逻辑运算有：

- must：必须匹配每个子查询，类似“与”
- should：选择性匹配子查询，类似“或”
- must_not：必须不匹配，**不参与算分**，类似“非”
- filter：必须匹配，**不参与算分**

bool查询的语法如下：

```JSON
GET /items/_search
{
  "query": {
    "bool": {
      "must": [
        {"match": {"name": "手机"}}
      ],
      "should": [
        {"term": {"brand": { "value": "vivo" }}},
        {"term": {"brand": { "value": "小米" }}}
      ],
      "must_not": [
        {"range": {"price": {"gte": 2500}}}
      ],
      "filter": [
        {"range": {"price": {glte": 1000}}}
      ]
    }
  }
}
```

注：出于性能考虑，与搜索关键字无关的查询尽量采用must_not或filter逻辑运算，避免参与相关性算分。

###### 	3、排序

ES 默认是根据相关度算分排序的，但也支持自定义的方式对结果进行排序，可以参与排序的字段类型有：Keyword、数值类型、地理坐标类型、日期类型等

```JSON
GET /indexName/_search
{
  "query": {
    "match_all": {}
  },
  "sort": [
    {
      "排序字段": {
        "order": "排序方式asc和desc"
      }
    }
  ]
}
```

###### 	4、分页

ES 默认情况下只返回 10 条数据，其他情况就要手动修改分页参数了

elasticsearch中通过修改`from`、`size`参数来控制要返回的分页结果：

- `from`：从第几个文档开始
- `size`：总共查询几个文档

类似于mysql中的`limit ?, ?`

```JSON
GET /items/_search
{
  "query": {
    "match_all": {}
  },
  "from": 0, // 分页开始的位置，默认为0
  "size": 10,  // 每页文档数量，默认10
  "sort": [
    {
      "price": {
        "order": "desc"
      }
    }
  ]
}
```

深度分页： ES 的数据一般会采用分片存储，即把索引数据分成 N 分，存储在集群中，这种方式有利于数据扩散，但对分页带来了一定麻烦，对于以某种条件查询前 N 来说，会查询每个分片的前 N 个后合并排序，得到其中对应的部分，当 N 不断变大，效率也随之降低，为此 ES 提供了两种解决方案：

- `search after`：分页时需要排序，原理是从上一次的排序值开始，查询下一页数据。官方推荐使用的方式。
- `scroll`：原理将排序后的文档id形成快照，保存下来，基于快照做分页。官方已经不推荐使用。

###### 	5、高亮

ES 根据 分词算法 时获取的关键词位置，可以在关键词前后添加 html 语块，实现 关键词高亮

```JSON
GET /{索引库名}/_search
{
  "query": {
    "match": {
      "搜索字段": "搜索关键字"
    }
  },
  "highlight": {
    "fields": {
      "高亮字段名称": {
        "pre_tags": "<em>",
        "post_tags": "</em>"
      }
    }
  }
}
```

##### 3、RestClient 查询

发送请求：这里关键的API有两个，一个是`request.source()`，它构建的就是DSL中的完整JSON参数。其中包含了`query`、`sort`、`from`、`size`、`highlight`等所有功能：

![image-20251117182226208](D:\Study\java pro\Demo\images\image-20251117182226208.png)

另一个是`QueryBuilders`，其中包含了我们学习过的各种**叶子查询**、**复合查询**等：

![image-20251117182449519](D:\Study\java pro\Demo\images\image-20251117182449519.png)

结果解析：

![image-20251117182927786](D:\Study\java pro\Demo\images\image-20251117182927786.png)

**代码解读**：

elasticsearch返回的结果是一个JSON字符串，结构包含：

- `hits`：命中的结果 
  - `total`：总条数，其中的value是具体的总条数值
  - `max_score`：所有结果中得分最高的文档的相关性算分
  - `hits`：搜索结果的文档数组，其中的每个文档都是一个json对象 
    - `_source`：文档中的原始数据，也是json对象

因此，我们解析响应结果，就是逐层解析JSON字符串，流程如下：

- `SearchHits`：通过`response.getHits()`获取，就是JSON中的最外层的`hits`，代表命中的结果 
  - `SearchHits``#``getTotalHits().value`：获取总条数信息
  - `SearchHits#getHits()`：获取`SearchHit`数组，也就是文档数组 
    - `SearchHit#getSourceAsString()`：获取文档结果中的`_source`，也就是原始的`json`文档数据

#### 6、数据聚合

聚合可以方便我们对数据进行统计、分析、运算。常见的聚合包括三类：

- **桶（****`Bucket`****）**聚合：用来对文档做分组 
  - `TermAggregation`：按照文档字段值分组，例如按照品牌值分组、按照国家分组
  - `Date Histogram`：按照日期阶梯分组，例如一周为一组，或者一月为一组
-  **度量（****`Metric`****）**聚合：用以计算一些值，比如：最大值、最小值、平均值等 
  - `Avg`：求平均值
  - `Max`：求最大值
  - `Min`：求最小值
  - `Stats`：同时求`max`、`min`、`avg`、`sum`等
-  **管道（****`pipeline`****）**聚合：其它聚合的结果为基础做进一步运算 

**注意：**参加聚合的字段必须是keyword、日期、数值、布尔类型

###### 	1、 Bucket 聚合

​	基本语法如下：

```JSON
GET /items/_search
{
  "size": 0, 
  "aggs": {
    "category_agg": {
      "terms": {
        "field": "category",
        "size": 20
      }
    }
  }
}
```

语法说明：

- `size`：设置`size`为0，就是每页查0条，则结果中就不包含文档，只包含聚合
- `aggs`：定义聚合
  - `category_agg`：聚合名称，自定义，但不能重复
    - `terms`：聚合的类型，按分类聚合，所以用`term`,还有 日期分桶`date_histogram`、范围分桶`range`
      - `field`：参与聚合的字段名称
      - `size`：希望返回的聚合结果的最大数量

响应结果如下：

![image-20251117185309892](D:\Study\java pro\Demo\images\image-20251117185309892.png)

###### 	2、带条件查询

在桶聚合前，可以带上 DSL 查询对结果添加限定条件

```JSON
GET /items/_search
{
  "query": {
    "bool": {
      "filter": [
        {
          "term": {
            "category": "手机"
          }
        },
        {
          "range": {
            "price": {
              "gte": 300000
            }
          }
        }
      ]
    }
  }, 
  "size": 0, 
  "aggs": {
    "brand_agg": {
      "terms": {
        "field": "brand",
        "size": 20
      }
    }
  }
}
```

###### 	3、Metric 聚合

通过 Metric 聚合，可以对桶聚合的结果进行运算，获取最大/最小值，平均值等

```JSON
GET /items/_search
{
  "size": 0, 
  "aggs": {
    "brand_agg": {
      "terms": {
        "field": "brand",
        "size": 20
      },
      "aggs": {
        "stats_meric": {
          "stats": {
            "field": "price"
          }
        }
      }
}
```

可以看到在`brand_agg`聚合的内部加了一个`aggs`参数。这个聚合就是`brand_agg`的子聚合，会对`brand_agg`形成的每个桶中的文档分别统计。

- `stats_meric`：聚合名称
  - `stats`：聚合类型，stats是`metric`聚合的一种
    - `field`：聚合字段，这里选择`price`，统计价格

另外，我们还可以让聚合按照每个品牌的价格平均值排序，对应的排序名称为 "聚合名称.avg/min/max": "asc"

###### 	4、RestClient 实现聚合

`aggs`聚合条件与`query`条件是同一级别，都属于查询JSON参数。因此依然是利用`request.source()`方法来设置。

![image-20251117192529568](D:\Study\java pro\Demo\images\image-20251117192529568.png)

对应结果获取如下

![image-20251117192551553](D:\Study\java pro\Demo\images\image-20251117192551553.png)

不同聚合类型只需修改 Terms 为 Range、Histogram

注：在聚合后加上 .subAggregation() 即可添加子聚合，添加与获取与上面类似
