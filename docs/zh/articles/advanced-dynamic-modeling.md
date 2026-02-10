# MyBatis-Dynamic 进阶：无需实体类的全动态数据建模

> 作者：刘伟

---

## 前言

在上一篇文章 [《告别繁琐 XML：用 mybatis-dynamic 实现动态数据建模》](https://juejin.cn/post/7603653885601759282) 中，我们介绍了 mybatis-dynamic 如何通过 "模型即真相" (Model as Truth) 的理念，让 Java 实体类直接驱动数据库表结构。

但在 **低代码平台**、**报表系统** 或 **动态表单** 等场景下，我们往往面临更极致的需求：
- **表结构由用户在运行时定义**，无法预先写死 Java 类。
- **字段随时可能增减**，需要即时反映到数据库。
- **数据结构不确定**，只能用 JSON 或 Map 承载。

有人问："如果没有 Entity 类，还能用 mybatis-dynamic 吗？需要自己拼 SQL 吗？"

答案是：**完全可以，而且体验依然丝滑。**

mybatis-dynamic 的内核设计完全解耦了 `Model` (元数据) 和 `Class` (Java类型)。实体类只是定义 Model 的一种快捷方式，你完全可以绕过它，直接操作 Model。

本文将带你解锁这一"隐藏技能"，实现真正的**全动态建模**。

---

## 一、核心概念：从 Class 到 Model

在常规模式下，框架的启动流程是：
1. 扫描 `@Model` 注解的 Java 类。
2. 解析类结构，生成 `Model` 对象（包含表名、字段、类型等元数据）。
3. 根据 `Model` 对象生成 DDL，同步数据库。
4. 注册 `DataManager` 提供 CRUD 服务。

在**全动态模式**下，我们只需手动完成第 2 步——直接构造 `Model` 对象，剩下的交给框架。

---

## 二、实战演示

假设我们要实现一个简单的"在线表单设计器"，用户创建了一个 "请假申请表" (LeaveApplication)，包含 "申请人" (applicant) 和 "天数" (days) 两个字段。

### 1. 构造模型元数据

我们不需要写任何 Java 类，直接通过代码构造 `Model` 对象：

```java
// 1. 创建模型定义
Model model = new Model();
model.setName("LeaveApplication");  // 模型名称，用于索引
model.setTableName("t_leave_app");  // 数据库表名

// 2. 定义主键 (通常由系统自动管理，也可以自定义)
model.addLongIdFieldIfNotExist();   // 自动添加 Long 类型的 ID 字段

// 3. 动态添加字段
// 申请人字段
BasicField applicantField = Field.stringBuilder("applicant")
        .characterMaximumLength(50)
        .ddlComment("申请人姓名")
        .build();
model.addField(applicantField);

// 天数字段
BasicField daysField = Field.integerBuilder("days")
        .ddlDefaultValue("1")
        .ddlComment("请假天数")
        .build();
model.addField(daysField);

// 4. 添加标准审计字段 (创建人、创建时间等)
// 注意：这需要 Spring 容器中存在 CreatorFiller/ModifierFiller 的实现 Bean
model.addAuditFieldsIfNotExist();
```

> 💡 **提示**：在实际项目中，这段逻辑通常是将前端传来的 JSON 配置解析为 `Model` 对象。

### 2. 运行时注册与建表

拥有 `Model` 对象后，注入 `ModelService` 即可完成注册。框架会自动比对数据库，创建表结构。

```java
@Autowired
private ModelService modelService;

public void deployModel(Model model) {
    // 注册并更新表结构
    // 框架会自动生成: CREATE TABLE t_leave_app (...)
    modelService.updateAndRegister(model); 
    
    System.out.println("模型 " + model.getName() + " 已发布！");
}
```

### 3. 基于 Map 的数据操作

没有实体类，数据怎么存取？
mybatis-dynamic 的 `DataManager` 接口原生支持 `Map<String, Object>`。

```java
// 获取动态模型的数据管理器
// 注意：泛型 ID 类型要与 model.addLongIdFieldIfNotExist() 对应
DataManager<Long> dataManager = modelService.getDataManager("LeaveApplication", null);

// --- 插入数据 ---
Map<String, Object> data = new HashMap<>();
data.put("applicant", "张三");
data.put("days", 3);

// 执行插入 (自动处理主键和审计字段)
Long id = dataManager.insert(data);
System.out.println("新记录ID: " + id);

// --- 查询数据 ---
// 返回 List<Map<String, Object>>
List<Map<String, Object>> list = dataManager.query(c -> c
    .eq("applicant", "张三")
    .gt("days", 1)
);

// --- 更新数据 ---
Map<String, Object> update = new HashMap<>();
update.put("id", id);
update.put("days", 5);
dataManager.update(update);

// --- 分页查询 ---
PageResult<Map<String, Object>> page = dataManager.page(1, 10, null);
System.out.println("总条数: " + page.getTotal());
```

完全不需要 `BaseService` 或 `Mapper`，一切都在运行时完成。

---

## 三、进阶场景：模型热更新

低代码平台最常见需求是：用户发现少了个字段，想加一个 "原因" (reason) 字段。

在 mybatis-dynamic 中，这就像修改集合一样简单：

```java
// 1. 获取现有模型
Model currentModel = modelService.getModel("LeaveApplication");

// 2. 添加新字段
BasicField reasonField = Field.stringBuilder("reason")
        .characterMaximumLength(200)
        .ddlComment("请假原因")
        .build();
currentModel.addField(reasonField);

// 3. 热更新
// 框架会自动检测差异，执行: ALTER TABLE t_leave_app ADD COLUMN reason ...
modelService.updateAndRegister(currentModel);

// 4. 立即可以使用新字段
Map<String, Object> newData = new HashMap<>();
newData.put("applicant", "李四");
newData.put("reason", "世界那么大，我想去看看"); // 新字段
dataManager.insert(newData);
```

---

## 四、动态关联查询

即使没有实体类，只要正确配置了 `ToOne` / `ToMany` 字段元数据，`dataManager.query()` 依然会自动处理 JOIN。

```java
// 假设还有一个 User 模型
Model userModel = ...; 
modelService.register(userModel);

// 在 LeaveApplication 中动态添加关联
ToOneField userField = Field.toOneBuilder("user")
        .targetModel("User")     // 关联目标模型名称
        .joinField("applicant", "name") // 本表 applicant = User.name
        .build();
leaveModel.addField(userField);
modelService.updateAndRegister(leaveModel);

// 查询时自动带出 User 信息
// 结果 Map 中会包含 key "user"，其 value 也是一个 Map
List<Map<String, Object>> results = dataManager.query(c -> c.eq("days", 5));
```

---

## 五、生产环境的高级挑战与应对

虽然全动态建模看起来很美，但在真实的高并发、分布式生产环境中，必须保持清醒的头脑。如果不加限制地使用，可能会引发灾难。

### 1. 多实例部署的模型同步

mybatis-dynamic 的模型元数据默认存储在**内存**中。在微服务或多 Pod 部署场景下，如果实例 A 动态添加了一个字段并更新了数据库，实例 B 的内存中并不知道这个变化。

**后果**：实例 B 查询时可能会忽略新数据，或者在更新时因为不知道新字段而导致数据丢失。

**解决方案**：
*   **引入消息机制**：当一个节点更新模型时，通过 Redis Pub/Sub 或 MQ 广播 "模型变更事件"。
*   **监听刷新**：其他节点监听到事件后，重新从数据库或配置中心加载最新的模型定义，实现最终一致性。

### 2. DDL 变更的风险控制

`modelService.updateAndRegister()` 底层会执行 `ALTER TABLE` 语句。在开发环境这很快，但在生产环境：

*   **大表锁表**：如果表中有几千万数据，MySQL (特别是旧版本) 加字段可能会锁表数分钟甚至更久，导致业务中断。
*   **并发冲突**：如果两个管理员同时修改同一个表的结构，可能会导致 DDL 冲突或覆盖。

**建议**：
*   **权限分离**：限制动态 DDL 的触发权限，只允许特定管理员操作。
*   **异步执行**：对于大表变更，不要在 HTTP 请求线程中同步等待，建议提交任务后台执行。
*   **维护窗口**：对于核心大表的结构变更，尽量安排在业务低峰期进行。

---

## 总结

mybatis-dynamic 的全动态能力，使其成为构建 **动态数据平台** 的利器。

- **元数据驱动**：完全脱离 Java 编译期限制。
- **自动 DDL**：无需手动维护复杂的 SQL 脚本。
- **统一 API**：Map 和 Entity 共享同一套 Fluent API。

现在，你可以自信地回复产品经理：“想怎么改表都行，在线配置，立即生效。”

---
*更多示例请参考项目 `sample` 目录下的动态模型测试用例。*
