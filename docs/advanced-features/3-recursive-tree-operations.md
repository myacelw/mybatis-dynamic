# 3. Recursive & Tree Operations

`mybatis-dynamic` has built-in support for hierarchical data (Adjacency List pattern), such as Categories, Departments, or Menus.

### 3.1 Recursive List vs. Recursive Tree

- **`queryRecursiveList`**: Fetches the hierarchy but returns a **flattened list**.
  - Useful for: Searching, calculations, or when you just need all descendants/ancestors without the nested structure.
- **`queryRecursiveTree`**: Fetches the hierarchy and assembles it into a **nested tree structure**.
  - **Requirement**: Your entity class must have a field to hold children (e.g., `List<Node> children`).

### 3.2 Key Concepts

1.  **`initNodeCondition`**: Defines the "Starting Points" (Roots) of your recursion.
2.  **`recursiveDown`**: 
    - `true`: Find children, grandchildren, etc. (Downwards).
    - `false`: Find parents, grandparents, etc. (Upwards).

### 3.3 Example: Category Tree

**Model**:
```java
@Model
public class Category {
    @IdField private String id;
    private String parentId;
    private String name;
    
    // Container for children (not a DB column)
    @IgnoreField
    private List<Category> children;
}
```

**Scenario 1: Fetch Full Tree (Root -> Leaves)**

```java
// 1. Define roots: Categories with no parent (parentId is null/empty)
List<Category> tree = categoryService.queryRecursiveTreeChain()
    .initNodeCondition(c -> c.isNull("parentId").or().eq("parentId", ""))
    .exec();

// Result is a List of 'Root' categories, each containing their 'children' recursively.
```

**Scenario 2: Fetch Breadcrumbs (Leaf -> Root)**

```java
// 1. Start from a specific sub-category
// 2. recursiveDown(false) implies "Go Up" to find parents
List<Category> breadcrumbs = categoryService.queryRecursiveListChain()
    .initNodeCondition(c -> c.eq("id", "sub-cat-123"))
    .recursiveDown(false) // Look upwards
    .exec();

// Result: [SubCategory, ParentCategory, RootCategory] (Order depends on traversal)
```

---

