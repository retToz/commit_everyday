># 第四天小结

--- 

> # 知识点1：接口新特性变化
>   - jdk7及以前，接口只有abstract方法
>   - jdk8以后，为了功能升级方便，接口中可以写 default方法 or static方法
>   - jdk9以后，由于jdk8的default方法抽取特性会被外部访问，因此新增private修饰 来让抽取的方法仅在内部调用

---

> # 知识点2：内部类
> - 成员内部类：定义在类中的内部类，new Outer().new Inter(),可以访问外部类的成员Outer.this.
> - 静态内部类: 成员内部类的静态版本，不能访问外部类的非静态成员
> - 局部内部类：定义在方法中的内部类，只能访问方法的局部变量，局部变量必须是final的
> - 匿名内部类：没有名字的内部类，通常用于实现接口或继承类，只能使用一次new 类/接口{};

---

> # 知识点3：抽象类
> - 抽象类：不能被实例化的类，可以包含抽象方法，也可以包含非抽象方法
> - 抽象方法：只有方法声明，没有方法实现，必须被子类重写
> - 抽象方法必须把类声明为abstract

---
> # 知识点4：常用API
> - java API 文档 .CHM 文件，可以查看常用API的详细用法
> - String类：字符串常量，不可变，直接赋值时在StringTable串池中存储/匹配，使用new时在堆中开辟新地址
>   - length()  // 返回字符串长度
>   - charAt(int index) // 返回指定索引处的字符
>   - substring(int beginIndex, int endIndex) // 返回指定范围的子字符串
>   - contains(CharSequence s) // 判断是否包含指定字符序列
>   - concat(String str)  //拼接
>   - replace(char oldChar, char newChar) // 替换字符
>   - toUpperCase() // 转大写
>   - toLowerCase() // 转小写
>   - trim() // 去除首尾空格
>   - equals(Object anObject) // 判断是否相等
>   - equalsIgnoreCase(String anotherString) // 判断是否相等，忽略大小写
>   - split(String regex) // 按照正则表达式分割字符串
>   - join(CharSequence delimiter, CharSequence... elements) // 按照分隔符连接字符串
>   - toCharArray() // 转字符数组
> - StringBuilder类：可变字符串，常用方法同String，但StringBuilder是可变的，因此效率更高
>   - append(String str) // 追加字符串
>   - insert(int offset, String str) // 在指定位置插入字符串
>   - delete(int start, int end) // 删除指定范围的字符
>   - replace(int start, int end, String str) // 替换指定范围的字符
>   - reverse() // 反转字符串
>   - toString() // 转字符串
---

> IDEA快捷键：
> - ctrl + alt + ↑/↓ // 上移/下移代码行
> - alt + insert // 生成代码
> - ctrl + alt + v // 生成变量
> - ctrl + x // 删除行
> - shift + F6 // 同时修改同名变量
> - ctrl + F12 // 查看类的结构
> - ctrl + shift + F12 // 查看所有方法和属性


---
> git 删除文件..
> git rm <文件名>
> git commit -m "删除文件"
> git push
