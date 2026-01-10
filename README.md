# 项目说明

本项目在mybatis的基础上可通过实体模型的配置，实现自动建表、CRUD 等ORM能力 以及 快捷的关联查询的能力；它是一个动态建模框架，支持在运行时修改模型结构。还包括列权限和行权限管理、数据插入和更新时的自动填充管理等特性。

在上述核心能力基础上，实现了ORM框架，除了继承上述功能点外；集成 springboot 框架，可以自动注册实体DAO对象和Service Bean 方便开发。
提供实体关系图功能，通过UI显示模型关系。

## ORM最简使用示例

在你的SpringBoot项目中加入本项目依赖：

```xml

<dependency>
    <groupId>io.github.myacelw</groupId>
    <artifactId>mybatis-dynamic-spring</artifactId>
    <version>[最新版本号]</version>
</dependency>

        <!-- mybatis-spring-boot-starter 也可以改为使用 mybatisplus -->
<dependency>
<groupId>org.mybatis.spring.boot</groupId>
<artifactId>mybatis-spring-boot-starter</artifactId>
</dependency>

```

`application.yml` 配置数据库，并启用自动更新数据库结构:

```yaml

spring:
  # 数据源连接池配置
  datasource:
    driver-class-name: org.h2.Driver
    url: jdbc:h2:./test;MODE=MySQL;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DATABASE_TO_UPPER=FALSE;
    username: sa
    password:

mybatis-dynamic:
  # 是否调用根据模型更新数据库方法，默认为true
  update-model: true
```

编写实体类，如：

```java
/** 数据字典 */
@Data
@FieldNameConstants
@Model(comment = "数据字典")
public class Dic {

    @IdField
    Integer id;

    @BasicField(ddlComment = "字典名", ddlCharacterMaximumLength = 20, ddlNotNull = true, ddlIndex = true, ddlIndexType = IndexType.UNIQUE)
    String name;

    @BasicField(ddlComment = "状态", ddlNotNull = true)
    Status status;

    /** 字典项目列表，一对多关系 */
    @ToMany
    List<DicItem> dicItemList;

}

/** 数据字典项 */
@Data
@FieldNameConstants
@Model(comment = "数据字典项")
public class DicItem {

    @IdField
    Integer id;

    @BasicField(ddlComment = "字典项键", ddlCharacterMaximumLength = 20, ddlNotNull = true, ddlIndexType = IndexType.UNIQUE)
    String key;

    @BasicField(ddlComment = "字典项值", ddlCharacterMaximumLength = 20, ddlNotNull = true)
    String value;

    @BasicField(ddlComment = "所属字典ID", ddlNotNull = true)
    Integer dicId;

    /** 所属字典，多对一关系 */
    @ToOne
    Dic dic;
}

```

* 解释： @Model、@IdField、@BasicField、@ToOne、@ToMany 等注解用于定义模型实体和模型关系。

实现DicService：

```java

@Service
public class DicServiceImpl extends BaseServiceImpl<Integer, Dic> {

    final BaseDao<Integer, DicItem> dicItemDao;

    public DicServiceImpl(BaseDao<Integer, Dic> dao, BaseDao<Integer, DicItem> dicItemDao) {
        super(dao);
        this.dicItemDao = dicItemDao;
    }

    /** 根据ID查询字典，同时关联查询字典项目列表 */
    @Override
    public Dic getById(@NonNull Integer id) {
        // 指定返回全部字段 和 查询字典项目列表，这会生成Join查询语句。
        return dao.getById(id, Arrays.asList("*", Dic.Fields.dicItemList));
    }

    @Transactional
    @Override
    public Integer insert(@NonNull Dic data) {
        Integer id = dao.insert(data);
        // 插入字典项目列表
        data.getDicItemList().forEach(t -> t.setDicId(id));
        dicItemDao.batchInsert(data.getDicItemList());
        return id;
    }

    /**
     * 更新字典，同时更新字典项目列表
     */
    @Transactional
    @Override
    public void update(@NonNull Dic data) {
        dao.update(data);
        dicItemDao.batchUpdate(data.getDicItemList());
    }

    @Transactional
    @Override
    public void delete(@PathVariable @NonNull Integer id) {
        dao.delete(id);
        // 按照字典ID条件删除字典项
        dicItemDao.deleteByCondition(Condition.builder().eq(DicItem::getDicId, id).build());
    }
}

```

* 解释：继承的 BaseServiceImpl 实现了基础的CRUD操作，比如query查询接口等。

然后实现 DicController 类，用于处理HTTP请求：

```java

@RestController
@RequestMapping("/dic")
public class DicController {

    @Autowired
    private BaseService<Integer, Dic> service;

    @GetMapping("/{id}")
    public Dic getById(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public Integer insert(@RequestBody Dic dic) {
        return service.insert(dic);
    }

    @PutMapping
    public void update(@RequestBody Dic dic) {
        service.update(dic);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }

    @GetMapping("/list")
    public List<Dic> list() {
        return service.list();
    }

}
```

最后创建个启动入口：

```java

@SpringBootApplication
@EnableModelScan
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

* 说明： @EnableModelScan 注解用于开启模型扫描，会自动扫描项目中所有的模型类，并注册每个模型的BaseDao 和 BaseService
  Bean（如果不存在的话）。
  在这里示例中包括 DicDao、 DicItemDao 和 DicItemService ，几个spring
  Bean。由于我们自己注册了DicServiceImpl，因此不会注册默认的BaseService<Integer、Dic> Bean。

这样一个简单的CRUD示例就完成了。启动程序会首先检测数据库表、列、索引是否存在，不存在则会自动创建。

* 下面是一个典型Dao查询api的使用示例：

```java
List<DicItem> dicItemList = dicItemDao.query()
        .select(DicItem.Fields.key, DicItem.Fields.value, DicItem.Fields.dic + "." + Dic.Fields.name)
        .where(b -> b.eq(DicItem.Fields.dic + "." + Dic.Fields.name, "证件类型"))
        .asc(DicItem.Fields.key)
        .page(1, 10)
        .exec();
```

说明: 查询DicItem表左关联Dic表（默认是左关联），查询条件 dic的name为"证件类型"，并按key升序排序，分页查询第1页，每页10条记录，返回
DicItem的 key、value字段和Dic 的 name字段。

## 动态模型最简使用示例

在代码中引入`ModelService`Bean：

```java

@Autowired
private ModelService modelService;
```

调用模型创建和读写数据方法：

```java
// 创建模型
Model model = new Model();
model.setName("user");
model.addField(Field.string("name",100))
        .addField(Field.integer("age"))
        .addIntegerIdFieldIfNotExist();

// 创建或更新模型对应数据库表
modelService.update(model);

// 得到模型的数据管理器, sqlSession 是 spring 中的 SqlSessionTemplate bean，或者 手动通过 sqlSessionFactory 创建的 SqlSession 实例
DataManager<Integer> dataManager = modelService.getDataManager(model, sqlSession);

// 插入数据
Map<String, Object> data = new HashMap<>();
data.put("name","lisi");
data.put("age", 20);

Integer id = dataManager.insert(data);

// 查询数据
List<Map<String, Object>> result = dataManager.query(SimpleCondition.eq("name", "lisi"));

assertEquals(1,result.size());
assertEquals(20, result.get(0).get("age"));
assertEquals(LocalDateTime.class, result.get(0).get("createTime").getClass());
```

## Core 功能介绍：

### 模型定义和映射为数据库表

#### 模型

模型是简单Bean对象，包括模型名称和字段列表属性:

1. 模型名称会通过 驼峰转下划线方式处理为数据库表名和列名，也支持中文和关键字作为模型名，不超过50个字符；
2. 模型包含多个字段，也会自动增加id字段（作为主键）和审计字段，审计字段包括：创建时间、更新时间、创建人、更新人、和逻辑删除标记；
3. 数据库表名配置统一的前缀，在 `application.yml` 中 配置 `mybatis-dynamic.tablePrefix` ， 具体参见
   `DynamicModelAutoConfiguration` 类中配置；
4. 模型可以自定义表名（`Model.tableName`属性），支持指定表所在schema （`Model.schema`属性）。

#### 字段

字段包含名称、类型、字段类型特定的配置 属性：

1. 字段名称：会通过 驼峰转下划线方式处理为数据库表名和列名，也支持中文和关键字作为模型名，不超过50个字符；

2. 字段类型：分为基本类型`Basic` 和 高级类型：

* 基本类型: 
  - 支持 `String`、`Integer`、`Float`、`Double`、`Boolean`、`LocalDate`、`LocalDateTime`
    等基本类型，基本类型会自动映射为数据库表的列并选择合适的数据库数据类型。比如`String`类型根据设置的最大长度，如果小于2000则映射为
    `VARCHAR`类型，映射为`TEXT`或`CLOB`类型；
  - 支持 `Map`和`List`类型，在数据库中它们会转换为json，以`Text`或`CLOB`类型存储；
  - 支持指定字段映射到的数据库列类型，可通过设置`BasicField.columnDefine`的 `characterMaximumLength`、`numericPrecision`、`numericScale`取值实现；如果不设置将根据规则进行自动映射。

* 字段组类型 `GroupField`：
  - 支持定义字段组，将多个数据库列字段组合为一个字段，实际是多列的分组；

* 关联类型有: 对一类型字段`ToOneField`、对多类型字段`ToManyField`
  - 对一类型字段`ToOneField`：用于定义一个模型的关联到另一个模型的字段，也就是“多对一”或"一对一"关系，需要同时指定关联的模型名称，外键字段；
  - 对多类型字段`ToManyField`：用于定义模型间的“一对多”或“多对多”关系，需要同时指定关联的模型名称 和 关联的外键字段名称；

3. 字段支持创建索引，通过设置`columnDefine.index` 属性为`true`，可以为字段自动创建索引；

4. 字段支持设置是否必填，通过设置`columnDefine.notNull` 属性，可以为列设置是否非空；

5. 基本类型字段支持设置默认值，通过设置`columnDefine.defaultValue` 属性，可以为字段设置默认值，在插入数据时，如果未设置取值则初始化为默认值。

#### 模型映射到数据库表

`ModelService`为模型的管理服务，通过`update(Model model)` 方法，可以为模型创建或更新为对应的数据库表；

1. 支持按照数据库方言（Mysql、Oracle、Postgres）进行SQL语句转换，比如 大文本数据类型，MySQL使用 LONGTEXT类型，Postgres使用
   TEXT类型，而Oracle使用类型CLOB类型；注意达梦数据库等也是支持的，达梦数据库使用Oracle方言，其他国产数据库可以选择Mysql或Postgres方言；
2. 数据库表更新，是根据当前模型的字段配置 和 当前数据库表存在列 对比变化（增加列、变更列数据类型），进行更新的；

#### 高级功能

1. 支持更新模型时指定字段对应数据库列更新策略，`columnDefine.alterOrDropStrategy`设置为`ColumnAlterStrategy`枚举取值，可选值如下：

* `DEFAULT`: 默认处理逻辑，例如：字符串长度收缩时不处理、不符合内置的列转换规则则抛出异常
* `FORCE_ALTER`: 如果不符合列类型转换规则，仍尝试更新，如果更新失败则抛出异常；可用于字符串长度的收缩处理，或者不符合内置的列转换规则但数据库确实支持的转换；
* `IGNORE_ALTER`: 如果已经存在列则不更新（如果不存在列仍然会创建列），可用于不希望修改列的情况；
* `DROP_AND_RECREATE`: 删除并重建列，可用于列类型无法转换，但可清除列数据重建列的情况；

2. 数据库表或列改名： 
 - 数据库表改名：通过设置`Model.tableDefine.oldTableNames`属性，可以为模型指定旧表名，在更新模型时如果检测到旧表名存在，则会将旧表名迁移到新表名；
 - 数据库列改名：通过设置`BasicField.columnDefine.oldColumnNames`属性，可以为列指定旧列名，在更新模型时如果检测到旧列名存在，则会将旧列名迁移到新列名；

3. 支持更新模型时删除数据库列: 默认情况下模型变更时只增加数据库列不会删除列，如果希望删除列，可以通过`Model.tableDefine.dropColumnNames`需要删除的列名List。

4. 字段值自动填充，字段设置`fillerName`属性，可以为字段设置填充器，实现插入、修改、逻辑删除时自动填充；例如创建时间、修改时间、创建人、修改人等字段自动填充。填充器需要实现
   `Filler`接口并注册为spring bean，填充器会根据字段类型自动选择合适的填充器实现。框架提供了以下实现：

* [CreateTimeFiller.java]
* [UpdateTimeFiller.java]
* [AbstractCreatorFiller.java]
* [AbstractModifierFiller.java]

### 数据管理

`DataManager`接口提供模型数据的管理功能。

首先通过 `ModelService.createDataManager(Model model, Permission permission, SqlSession sqlSession)` 获取对应模型的数据管理器，然后调用
`DataManager`的相应接口进行增删改查操作。
注意：调用`update`方法更新数据时，传入`data`中如果不包含某个字段，则表示不更新该字段的值。

#### 高级功能

##### 支持行权限和列权限管理，参见 `Permission` 类；

1. 行权限：实际上是查询条件，对该模型查询是都会附加上行权限过滤条件，支持为空；
2. 列权限：它限制了可以返回的模型字段；支持为空，此时返回全部字段；

##### 提供模型的高级数据查询接口，参见:
`DataManager.queryChain().where(Condition condition).orderItems(List<OrderItem> orderItems).page(Page page).joins(List<Join> joins).exec()`

#### Fluent Condition API (链式查询条件)

`ConditionBuilder` 现在支持更加自然和强大的链式调用 API，可以方便地构造复杂的 `AND`、`OR`、`NOT` 组合条件，并自动处理逻辑优先级（AND 优先级高于 OR）。

示例：
*   **SQL:** `a = 1 AND (b > 2 OR c < 3) AND d = 4`
*   **代码:**
    ```java
    cb.eq("a", 1)
      .and(b -> b.gt("b", 2).or(b2 -> b2.lt("c", 3)))
      .eq("d", 4)
    ```
    或者使用更显式的链式风格：
    ```java
    cb.eq("a", 1)
      .and().bracket(b -> b.gt("b", 2).or().lt("c", 3))
      .and().eq("d", 4)
    ```

**主要特性：**
*   **逻辑连接词:** 提供 `and()`、`or()`、`not()` 方法进行逻辑切换。
*   **括号支持:** `bracket(Consumer<ConditionBuilder>)` 用于创建显式的括号分组。
*   **优先级处理:** 自动处理 `A AND B OR C` (视为 `(A AND B) OR C`) 和 `A OR B AND C` (视为 `A OR (B AND C)`)。
*   **代码复用:** 所有的连接词返回的 Connector 都具备完整的条件构造能力（eq, gt, in, etc.），减少代码重复。

1. 通过设置`joins`属性，查询接口支持扩展查询主表的同时查询出字段为 关联类型`ToOne`、`ToMany`的具体关联表的数据。
2. Join查询设置，说明：
   下面给一个典型模型场景并给出几个配置的例子：
   模型配置：

* 模型 User，字段 departments: ToMany(Department) ;
* 模型 Department ，字段 company: ToOne(Company);
* 模型 Company

查询示例1，查询User时关联 Department 和 Company，dataManager.query()接口中 joins 参数 配置如下：
`Arrays.asList(Join.of("departments"), Join.of("departments.company"))`

##### 自关联模型，支持递归查询

例如查询ID为100的部门及其所有下级部门返回树状结构的示例：

```java
Map<String, Object> root = dataManager.getRecursiveTreeById("100");

//子部门列表
List<Map<String, Object>> children = (List<Map<String, Object>>) root.get("children");
//子子部门列表
List<Map<String, Object>> grandson = (List<Map<String, Object>>) children.get(0).get("children");
```

##### 支持从实体类生成模型，支持抽象类实体及其子类数据映射到一个模型一张数据库表，支持模型数据查询结果映射到实体类

可以通过实体类生成的模型，支持实体的继承关系自动映射处理，并且模型数据查询的结果也映射为实体类，参见测试用例：
`Class2ModelTransferTest.entityModelTest` 方法。
测试中实体Person 为抽象类， User 和 Guest 继承 Person类；Person类`@SubTypes`
配置后实现类继承映射到单张数据库表，对应的模型中会自动增加的type字段用于区分具体哪个子类。

## 一些场景说明

### 参考 [sample](sample) 项目

### 参考 单元测试`Class2ModelTransferTest.java` 的 `entityModelTest`方法，它给出了一种对已有实体类动态增加基本字段的思路。

### 多租户场景

1. 多租户隔离: 可以为每个租户创建 `ModelService` 自己的实例，如表前缀名可以设置为租户id，这样可以保证每个租户的模型对应数据库表不冲突。
2. 全部租户共用模型：创建一个全部租户可使用的 `ModelService` 如表前缀名可以设置为`common`, 然后通过 `Permission` 的行权限实现租户数据的隔离。
3. 可以通过 `Filler` 实现数据插入时的租户字段填充(参考 `CreateTimeFiller.java` 和 `UpdateTimeFiller.java` 实现)。

## ORM 功能介绍：

该功能对标mybatisplus对应能力。当如core功能介绍中一样，拥有强大的模型关联查询能力。

### 引入依赖

引入 mybatis-dynamic-orm、 mybatis-spring-boot-starter、 spring-boot-starter-jdbc 等依赖。

```xml

<dependency>
    <groupId>io.github.myacelw</groupId>
    <artifactId>mybatis-dynamic-orm</artifactId>
    <version>${dynamic-mode.version}</version>
    <scope>compile</scope>
</dependency>
```

### 实体定义

例如定义一个字典对象和字典目录实体类：

```java 
/**
 * 数据字典
 */
@Data
@EqualsAndHashCode(callSuper = true)
@FieldNameConstants
@Model
public class Dic extends BaseEntity {

    /**
     * 字典名
     */
    String name;
    
    String dicDirectoryId;
    
    /**
     * 所属字典目录
     */
    @ToOne
    DicDirectory dicDirectory;
}
```

```java 
/**
 * 字典目录
 */

@Data
@EqualsAndHashCode(callSuper = true)
@FieldNameConstants
@Model
public class DicDirectory extends BaseTreeEntity<DicDirectory> {

    @Size(max = 100)
    @NotBlank
    String name;

}
```

`BaseEntity` 和 `BaseTreeEntity` 为框架提供的基类，可以直接使用，也可以不继承该类，只参考包含的字段定义自己的实体类。

注意：这里的关联实体的字段`dicDirectory`，使用 `@ToOne` 注解，默认对应的关联列为`dicDirectoryId`。

#### 实体字段注解

实体字段通常不需要指定注解，直接使用自动推断即可满足大部分需求。
明确指定字段注解，可以明确字段到数据库的映射关系、定制数据库列和索引等，例如：
`@BasicField(ddlCharacterMaximumLength=1000) List<String> labels;`  可以指定属性labels 转为json存储到数据库时，数据库列最大长度为1000。
注意如果不使用自动生成ddl语句，那么`ddlCharacterMaximumLength`等 `ddl`开头的属性 不会生效，无需设置。

##### 实体字段注解自动推断

* 如果字段类型为另外一个自定义类，如果该类为实体模型类（有`@Model`注解）则映射为 `@ToOne`，否则映射为`@BasicField`。
* 如果字段类型为List类型，List参数类另外一个自定义类，如果这个自定义类为实体模型类（有`@Model`注解）则映射为默认映射为
  `@ToMany`

#### 实体的继承

实体类支持使用继承关系，例如：

```java
//Person.java
@Data
@Model(tableName = "person_custom_table")
@SubTypes(subTypes = {@SubTypes.SubType(User.class), @SubTypes.SubType(Guest.class)}, subTypeFieldName = "type")

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(value = User.class, name = "User"), @JsonSubTypes.Type(value = Guest.class, name = "Guest")})
public abstract class Person {
    String id;

    @Comment("名称")
    String name;
}

//User.java
@EqualsAndHashCode(callSuper = true)
@Data
public class User extends Person {
    int age;

    String password;
    
    Status status;
}

//Guest.java
@EqualsAndHashCode(callSuper = true)
@Data
public class Guest extends Person {

    String token;

}
```

上面示例中的 `@JsonTypeInfo` 和  `@JsonSubTypes` 为 `com.fasterxml.jackson.annotation`
包下注解，用于指明ObjectMapper如何将Person串行化为json字符串。
按照类似的思路`@SubTypes`注解说明了，将`Person`的子类`User`和`Guest`转换为一张数据库表时，使用`type`列存储类名信息。
注意子类不应指定`@Model`注解，全部子类的字段对应数据库列会自动合并到主表中。

当查询`Person`时返回，`List<Person>` 会根据实际类型 转换为具体的 `User`或`Guest`类。

### 启用模型扫描，自动注入实体的Dao 和 Service类

使用 `@EnableModelScan` 注解启动实体模型扫描， 默认扫描包为当前包及其子包，可以通过 `basePackages` 属性指定扫描包。
会自动将发现的实体模型注册对应 `BaseDao<ID,T>` 和 `BaseService<ID,T>` spring bean。

默认 Dao 和 Service bean已经包含实体常用的crud方法 和 常见的树形递归查询方法（实体继承 BaseTreeEntity时可用） ，可参考 接口
`BaseDao` 和 `BaseService` 定义。

### 自定义Dao类 和 Service类

如果默认 Dao 和Service 不满足需求，可以编写自己的Bean实现例如：

```java

@Service
public class DicDirectoryServiceImpl extends BaseServiceImpl<DicDirectory> {

    private final BaseDao<String,Dic> dicDao;

    public DicDirectoryServiceImpl(BaseDao<String,DicDirectory> dao, BaseDao<String,Dic> dicDao) {
        super(dao);
        this.dicDao = dicDao;
    }

    @Override
    public void delete(@PathVariable @NonNull String id) {
        long n = dao.count(b->b.eq(BaseTreeEntity.Fields.parent, id));
        if (n > 0) {
            throw new DataException("4009751", "包含下级目录，不能删除");
        }

        long n2 = dicDao.count(b->b.eq(Dic.Fields.dicDirectory, id));
        if (n2 > 0) {
            throw new DataException("4009752", "包含数据字典，不能删除");
        }
        super.delete(id);
    }
}
```

此时默认的`BaseService<String,DicDirectory>` bean 将不再自动注册。

### 编写Controller类

可按照需要直接编写Controller类，引用上面的Service bean 实现需要的功能。
也可按照下面的示例，生成一个拥有默认crud的Controller。
具体包含的方法和功能，请参考接口`AbstractController` 和 `AbstractTreeController`实现。

```java

@RestController
@RequestMapping("/dic/")
public class DicController extends AbstractController<String,Dic> {

}
```

```java

@RestController
@RequestMapping("/dicDirectory/")
public class DicDirectoryController extends AbstractTreeController<String,DicDirectory> {

}
```

查询接口调用示例如下：

```sh
curl -X POST 'http://127.0.0.1:8080/dic/query' -H 'Content-Type: application/json' -d '{
  "condition": {
    "type": "group",
    "conditions":[
      {"field": "name", "value": "学", "operation":"likeRight"},
      {"field": "dicDirectory.name", "value": "通用"}
    ]
  },
  "orderItems": [
    {
      "field": "name",
      "asc": true
    }
  ],
  "page": {
    "current": 1,
    "size": 10
  }
}'
```

上述由于查询条件中包含了`dicDirectory.name`，因此自动关联`DicDirectory` ，查询结果中也同时包含DicDirectory属性。
也可明确设置`joinFieldPaths`为`["dicDirectory"]`，指定所需关联的表。

这种查询条件可能不是所需要的形式，建议自行编写Controller类的接口，按需编写查询条件DTO，这样形成的接口参数更容易理解，然后调用Service的query或page接口。

### 增删改时记录当前创建人和修改人，和控制人员权限

下面的接口`io.github.myacelw.mybatis.dynamic.orm.hook.CurrentUserHolder`注册为spring bean，可使得Dao类得到当前操作员id，和当前操作员权限。
这样在CRUD是人员将进行路径，并根据返回值控制权限。

```java
/**
 * 获取当前用户ID和当前用户权限的接口，
 * 实现该接口的类注册为Bean后可被DAO数据增删改查时使用。
 */
public interface CurrentUserHolder {

    /**
     * 得到当前用户ID，增删该时写入创建人修改人时使用。
     */
    String getCurrentUserId();

    /**
     * 得到当前用户权限，DAO增删改查数据时使用。
     */
    Permission getCurrentUserPermission(Class<?> entityClass);

}

```

### 实现部分liquibase能力，和application.yml中可配置项

例如：

```yaml
mybatis-dynamic:
  table-prefix: d_
  # 是否调用根据模型更新数据库方法，默认为false
  update-model: true
  # 初始化数据文件路径列表，例如 classpath:data-v1.json,classpath:data-v2.json
  init-data-files: classpath:data.json

```

* `update-model`: 为true时，将在每次启动服务时更新数据库表结构使得和现有模型定义一致。
* `init-data-file-patterns`: 可以设置数据初始化文件，每次启动服务时将检查初始化数据是否存在和一致，然后进行增加、删除或修改。

初始化数据格式如：

```json
[
  {
    "#": "========== 初始化公司数据 =========="
  },
  {
    "model": "Company",
    "method": "insertOrUpdate",
    "data": [
      {
        "id": "c1",
        "name": "公司A"
      },
      {
        "id": "c2",
        "name": "公司B"
      },
      {
        "id": "c3",
        "name": "公司C"
      }
    ]
  },
  {
    "#": "========== 初始化部门数据 =========="
  },
  {
    "model": "Department",
    "data": {
      "id": "d1",
      "name": "部门A",
      "company": "${Company[0].id}"
    }
  },
  {
    "model": "Department",
    "data": {
      "id": "d2",
      "name": "部门B",
      "company": "c2"
    }
  },
  {
    "model": "Department",
    "data": {
      "id": "d3",
      "name": "部门C",
      "company": "c3"
    }
  },
  {
    "model": "Department",
    "data": {
      "id": "d4",
      "name": "部门A-1",
      "company": "c1",
      "parent": "d1"
    }
  },
  {
    "model": "Department",
    "data": {
      "id": "d5",
      "name": "部门A-1-1",
      "company": "c1",
      "parent": "d4"
    }
  },
  {
    "model": "Department",
    "data": {
      "id": "d6",
      "name": "部门A-2",
      "company": "c1",
      "parent": "d1"
    }
  },
  {
    "model": "Department",
    "data": {
      "id": "d7",
      "name": "部门B-1",
      "company": "c2",
      "parent": "d2"
    }
  }
]
```

### 实现动态字段

实现动态实体需要实现`ext.core.dynamic.mybatis.io.github.myacelw.ExtBean`接口，增加字段：

```java

@IgnoreField
Map<String, Object> ext = new HashMap<>();
```

ext Map 可以存放运行时动态定义的任意字段（包括基本字段、关系类型字段等）；这些字段就像实体模型本身具备这个字段一样，支持增删改查，查询时也支持作为查询条件。


## 模型关系图绘制 功能介绍：

参见 [draw](draw)，orm基础上增加draw依赖后 `/draw/index.html`可以显示模型关系图。