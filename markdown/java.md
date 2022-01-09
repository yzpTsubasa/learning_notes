## java

## spring-boot maven 配置
``` xml
<profile>
    <id>jdk-1.8</id>
    <activation>
    <activeByDefault>true</activeByDefault>
    <jdk>1.8</jdk>
    </activation>
    <properties>
    <maven.compiler.source>1.8</maven.compiler.source>
    <maven.compiler.target>1.8</maven.compiler.target>
    <maven.compiler.compilerVersion>1.8</maven.compiler.compilerVersion>
    </properties>
</profile>

<mirror>
    <id>nexus-aliyun</id>
    <mirrorOf>central</mirrorOf>
    <name>Nexus aliyun</name>
    <url>http://maven.aliyun.com/nexus/content/groups/public</url>
</mirror>
```
## Tomcat 日志乱码
tomcat目录下`"conf/logging.properties`配置文件的
``` properties
1catalina.org.apache.juli.AsyncFileHandler.encoding = UTF-8
2localhost.org.apache.juli.AsyncFileHandler.encoding = UTF-8
3manager.org.apache.juli.AsyncFileHandler.encoding = UTF-8
4host-manager.org.apache.juli.AsyncFileHandler.encoding = UTF-8
java.util.logging.ConsoleHandler.encoding = UTF-8
```
删除, 或者把 `UTF-8` 改成 `GBK`
