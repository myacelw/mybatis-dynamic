# Global Marketing Pitch & Content

## 1. Pitch Email (For Editors/Bloggers)

**Subject:** News/Article: A New "Code-First" ORM for Java (Better than JPA?)

**Body:**

Hi [Name/Editor],

I'm writing to introduce **mybatis-dynamic**, a new open-source Java ORM framework that bridges the gap between JPA's convenience and MyBatis's control.

Unlike traditional MyBatis, it offers a **code-first approach** where the database schema is automatically managed and updated based on your Java models—even at runtime. It features a rich fluent API, automatic REST endpoints, and seamless Spring Boot integration without the XML configuration hell.

I've prepared a technical article titled **"Stop Writing Boilerplate: Why MyBatis-Dynamic is the Future of Java ORM"** that dives into:
1.  The problem with current ORMs (JPA's "magic" vs. MyBatis's verbosity).
2.  How to achieve dynamic schema updates at runtime.
3.  Writing complex SQL using a type-safe fluent API.

**Project Repo:** https://github.com/myacelw/mybatis-dynamic

Would you be interested in publishing this article or a news piece about the v1.0 release?

Best regards,

[Your Name]
mybatis-dynamic Team

---

## 2. Article Draft

# Stop Writing Boilerplate: Why MyBatis-Dynamic is the Future of Java ORM

For years, Java developers have been stuck in a dilemma: choose **Hibernate/JPA** for productivity but struggle with "magic" performance issues and complex mappings, or choose **MyBatis** for control but drown in XML files and manual schema management.

Enter **mybatis-dynamic**—a modern, fluent, and dynamic ORM that gives you the best of both worlds.

### The Problem: Static Schemas in a Dynamic World

Traditional ORMs assume your database schema is relatively static. You define entities, create tables (or let Hibernate do it once), and then run your app. But what if your business logic requires adding fields on the fly? What if you want the type-safety of Java without writing a single line of SQL or XML?

### The Solution: MyBatis-Dynamic

**mybatis-dynamic** is built on top of the battle-tested MyBatis engine but completely removes the need for XML. It introduces a **Code-First** philosophy where your Java models *are* your source of truth.

#### 1. Zero-XML, Code-First Schema Management

Forget about writing `CREATE TABLE` scripts or maintaining Liquibase changelogs for every minor field addition. With `@Model`, the framework automatically syncs your database schema with your Java classes at startup.

```java
@Data
@Model(comment = "User Table")
public class User {
    @IdField
    private Integer id;

    @BasicField(ddlNotNull = true)
    private String name;

    @BasicField(ddlComment = "User Age")
    private Integer age;
}
```

#### 2. Runtime Model Modification

This is where it gets crazy. You can modify your data models programmatically *at runtime*, and the framework will update the database table structure immediately. This is a game-changer for SaaS platforms allowing custom user fields.

```java
// Add a 'phone' column to the User table at runtime!
Model userModel = modelService.getModelForClass(User.class);
userModel.getFields().add(Field.string("phone", 100));
modelService.update(userModel);
```

#### 3. A Fluent API That Reads Like English

Writing complex dynamic SQL in pure MyBatis is painful (`<if test="...">`). With mybatis-dynamic, you use a type-safe wrapper that handles logical precedence automatically.

```java
// Find users where (age > 18 AND status = 'Active') OR role = 'Admin'
userService.query()
    .where(c -> c.bracket(b -> b.gt("age", 18).eq("status", "Active"))
                 .or(b -> b.eq("role", "Admin")))
    .joins(Join.of("department")) 
    .exec();
```

#### 4. Instant REST APIs

Need to expose your data quickly? The framework can automatically generate full CRUD REST controllers for your models.

*   `GET /api/dynamic/User?age=20&page=1`
*   `POST /api/dynamic/User`

### Conclusion

If you love the control of MyBatis but hate the boilerplate, **mybatis-dynamic** is worth a look. It simplifies the 90% of CRUD tasks while staying out of your way for the complex 10%.

Check out the project on GitHub: [https://github.com/myacelw/mybatis-dynamic](https://github.com/myacelw/mybatis-dynamic)
