### 一个简易的数据库API调用生成库

#### 为什么开发这个东西？

我最近在写毕业设计，发现设计类的题目都涉及到数据库的`CRUD(create, read, update, delete)`。

而且开发完之后发现代码重复率极高，因此诞生了一个想法，将数据库的操作从特定表中剥离，并为注册的表生成 `CRUD API`操作。

可通过 `JSON API` 来直接进行CRUD

#### 后续打算

我的毕业设计是Web类，后期考虑集成 `js-sdk`

#### 面向的使用者

这个库旨在让同学们编写最少的代码来完成CRUD，尽量做到一键操作，因而可定制化程度很低，不支持更多的灵活选项。

#### 如何引入

Maven

```
<dependency>
  <groupId>com.chaochaogege</groupId>
  <artifactId>ujnbsapi</artifactId>
  <version>0.0.5</version>
</dependency>
```
#### 安装到本地

mvn install -Dmaven.test.skip=true

####上传到maven

mvn deploy -P release -Dmaven.test.skip=true

#### 实例

```java
public class App 
{
    public static void main(String[] args) {
        APIOptions apiOptions = new APIOptions().setDatabase("your database name")
                .setPassword("your database password");
        // 每一个表对应一个 column
        // TableColumn
        TableColumn column = new TableColumn("staff","uid",new ArrayList<>(Arrays.asList("username","email","phone","sex","uid","role")));
        ArrayList<TableColumn> columns = new ArrayList<>();
        columns.add(column);
        
        new APIGenerator(apiOptions,columns).run();
    }
}
```

