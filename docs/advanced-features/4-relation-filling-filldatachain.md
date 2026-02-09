# 4. Relation Filling (`FillDataChain`)

Sometimes you have a list of objects obtained from an external source (like Redis, or a manual MyBatis XML query) and you want to populate their `@ToOne` or `@ToMany` relationships without writing a loop. `FillDataChain` solves this efficiently.

**Entry Point**: `dataManager.fillChain()`

```java
// Assume we got this list from Redis, so 'department' field is null
List<User> users = cacheService.getUsers(); 

userService.fillChain()
    .data(users) // The targets to populate
    
    // 1. Simple fill: Populates the 'department' field
    .fillField("department")
    
    // 2. Complex fill: Populates 'roles', but only specific columns
    .fillField("roles", fieldBuilder -> {
        fieldBuilder.selectField("roleName") // Only fetch name
                    .selectField("roleCode");
    })
    
    .exec();

// Now 'users' have their department and roles populated.
```

---

