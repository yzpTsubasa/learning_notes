## syntax

## yaml 指定 schema 文件
``` yaml
# yaml-language-server: $schema=<schema_file_path>
```

## 时序图 sequenceDiagram
``` mermaid
sequenceDiagram
    participant F as 前端
    participant V as View
    participant C as Controller
    participant M as Model
    Note over F: 打开浏览器
    Note over F: 输入网址并回车
    F ->> +V: 发起HTTP请求
    V ->> +C: 路由请求到
    Note over C: HomeController
    C ->> +M: 获取数据
    Note over M: Database
    M -->> -C: 
    Note over C: 组织数据
    C -->> -V: 
    Note over V: 直出的HTML
    V -->> -F: 返回HTTP响应
    Note over F: 渲染页面
```