# 3. 递归与树形结构操作 (Recursive & Tree Operations)

`mybatis-dynamic` 内置支持分层数据（邻接表模式），例如类别、部门或菜单。

### 3.1 递归列表 vs. 递归树

- **`queryRecursiveList`**: 获取层级结构但返回 **扁平列表**。
  - 适用于：搜索、计算，或者当您只需要所有后代/祖先而不需要嵌套结构时。
- **`queryRecursiveTree`**: 获取层级结构并将其组装成 **嵌套树结构**。
  - **要求**: 您的实体类必须有一个字段来保存子节点（例如 `List<Node> children`）。

### 3.2 关键概念

1.  **`initNodeCondition`**: 定义递归的“起始点”（根）。
2.  **`recursiveDown`**: 
    - `true`: 查找子节点、孙节点等（向下）。
    - `false`: 查找父节点、祖父节点等（向上）。

### 3.3 示例: 类别树

**模型**:
```java
@Model
public class Category {
    @IdField private String id;
    private String parentId;
    private String name;
    
    // 子节点容器 (非 DB 列)
    @IgnoreField
    private List<Category> children;
}
```

**场景 1: 获取完整树 (Root -> Leaves)**

```java
// 1. 定义根: 没有父级的类别 (parentId 为 null/empty)
List<Category> tree = categoryService.queryRecursiveTreeChain()
    .initNodeCondition(c -> c.isNull("parentId").or().eq("parentId", ""))
    .exec();

// 结果是 'Root' 类别的列表，每个类别递归包含其 'children'。
```

**场景 2: 获取面包屑 (Leaf -> Root)**

```java
// 1. 从特定子类别开始
// 2. recursiveDown(false) 意味着 "向上" 查找父级
List<Category> breadcrumbs = categoryService.queryRecursiveListChain()
    .initNodeCondition(c -> c.eq("id", "sub-cat-123"))
    .recursiveDown(false) // 向上查找
    .exec();

// 结果: [SubCategory, ParentCategory, RootCategory] (顺序取决于遍历)
```

---

