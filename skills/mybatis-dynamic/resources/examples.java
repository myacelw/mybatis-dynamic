// 1. ConditionBuilder Example
// WHERE (age > 18) AND (name LIKE 'A%')
c.gt(User::getAge, 18).startsWith(User::getName, "A")

// 2. QueryChain Example
userService.queryChain()
    .select(User.Fields.id, "department.name") // Implicit Join
    .join(Join.inner(User::getDepartment).on(c -> c.eq("active", true))) // Explicit Join
    .where(c -> c.gt(User::getAge, 18))
    .orderBy(User.Fields.createTime)
    .exec();

// 3. Recursive Query Example
// Retrieve full tree structure
Map<String, Object> tree = departmentService.getRecursiveTreeById("dept-id");

// 4. Spring Integration Setup
// 4a. Define Model
@Data @Model
public class User {
    @IdField Integer id;
    @BasicField String name;
    // ...
}

// 4b. Inject Service
@Autowired
private BaseService<Integer, User> userService;

// 4c. Use Service
public void demo() {
    User user = new User();
    user.setName("John");
    userService.insert(user);
}

// 5. Aggregation Query Example
List<Map<String, Object>> results = userService.getDataManager()
    .aggQuery()
    .groupBy("departmentId", "deptId")
    .avg("age", "avgAge")
    .max("age", "maxAge")
    .exec();
