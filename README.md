在post接口中解析json数据，自动注入到方法参数中。

- 支持PART（部分）字段注入

```json
{
  "name": "张三",
  "age": 30
}
```

```java

@JsonInject(JsonInjectMode.PART) // 标注该方法参数需要注入json数据，使用PART模式（默认），表示只注入部分字段
@PostMapping("/example")
public Result example(String name) {
    // ... 处理逻辑
    return Result.success("成功");
}
```

- 支持WHOLE（全部）字段注入
```json
[1, 2, 3]
```

```java

@JsonInject(JsonInjectMode.WHOLE) // 使用WHOLE模式，进行内容的整个注入
@PostMapping("/example")
public Result example(List<Integer> ids) {
    // ... 处理逻辑
    return Result.success("成功");
}
```

- 支持指定字段注入
```json
{
  "name": "张三",
  "age": 30
}
```

```java

@JsonInject(JsonInjectMode.PART) // 标注该方法参数需要注入json数据，使用PART模式（默认），表示只注入部分字段
@PostMapping("/example")
public Result example(@JsonField("name") String personName) {
    // ... 处理逻辑
    return Result.success("成功");
}
```

- 支持hibernate-validator校验