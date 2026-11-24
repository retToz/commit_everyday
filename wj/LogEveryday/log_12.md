> # 小结

<h2>JavaSE:</h2>
- 简单了解了Stream/File/IO(暂时不深刻)
- 较全面理解了异常处理机制
  - try/catch/finally(无论如何都执行)
  - throw/throws
  - 继承结构：Eception-(Runtime,Compile,Err)
- 多线程/部分锁
  - 线程创建：继承Thread,实现Runnable(无返回)/Callable(有返回)
  - 并发：同一个处理器处理多线程
  - 并行：多个处理器处理多线程
  - 进程状态：等待wait，运行中，被抢占，休眠sleep，唤醒notify,阻塞
  - Lock可以结合finally使用，保证锁关闭
  - synchonized ()需要传入一个唯一性的参数，例如字节码xx.class,唯一对象new Object
- 网络编程
  - IPv4/IPv6 基本概念
  - UDP/TCP
  - 三次握手，四次挥手
--- 

<h2>JavaWeb:</h2>
- springboot快速入门
  - 请求（响应）数据结构：请求行、请求头、请求体
  - 状态码1xx(响应中),2xx(成功,如200成功),3xx(重定向),4xx(web端问题,如404 not found),5xx(服务端问题,如1/0)
  - Tomcat是web服务器，可以监听端口，将请求/响应数据打包成json格式传输,服务端使用httpServletRequest等即可get数据内容
  - 注解@RestController底层含@Controller和@ResponseBody，ResponseBody可以将对象/集合转json
  - 获取资源文件可使用类加载器:this.getClass().getClassLoader().getResouseAsStream("filename")作为输入流
  - 可使用 Hutool提供的IoUtil.readline 按行读写数据，需要导入Hutool-all

---
> 明天规划： 快速看看JavaSE，简单过一下list类型，然后细看一下Stream/File/IO  
> JavaWeb看完IO再看，尽量多看一些