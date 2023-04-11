## mysql

### 使用 ip 访问
``` bash
mysql -h<IP> -P<PORT> -u<USER> -p<PASSWORD> -e "source xxx.sql"
```
> 此处`USER`避免使用`root`，否则可能会出现以下错误
``` log
ERROR 1045 (28000): Access denied for user 'root'@'DESKTOP-QDDVAQ3' (using password: YES)
```

### ERROR 1130: Host ’XXX′ is not allowed to connect to this MySQL server 没有权限连接指定IP的主机
> 授权
``` bash
GRANT ALL PRIVILEGES ON *.* TO '<username>'@'%' IDENTIFIED BY 'password' WITH GRANT OPTION;
# 如：
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY 'root' WITH GRANT OPTION;
```
### MySQL 版本
5.7

> 在Macbook Pro 2016 的 Bootcamp 下无法运行mysql 5.x 版本
### 语句
``` sql
-- 创建库
CREATE DATABASE `mybatis`;
-- 使用表
USE `mybatis`;
-- 创建表
CREATE TABLE `user` (
	`id` INT(20) NOT NULL PRIMARY KEY,
	`name` VARCHAR(30) NOT NULL,
	`pwd` VARCHAR(30) NOT NULL
)ENGINE=INNODB DEFAULT CHARSET=utf8;
-- 插入数据
INSERT INTO `user` (`id`, `name`, `pwd`) VALUES
(1, 'Tsubasa', 'xxx'),
(2, 'Misaki', 'yyy'),
(3, 'Hyuga', 'zzz')
```
### 备份
``` bash
# 备份库 test库
mysqldump -uroot -proot -B test > test.sql
# 备份表 test库中的user表 
mysqldump -uroot -proot test user > test.user.sql
```

### MySQL Community Server下载与安装配置(解压版本)

1. [下载 MySQL Community Server](https://dev.mysql.com/downloads/mysql/)
2. 解压到自定义目录 `x:\mysql-xxx`
3. 配置环境变量
   ```
   MYSQL_HOME: x:\mysql-xxx
   Path：%MYSQL_HOME%\bin
   ```
4. 在`%MYSQL_HOME%\bin`目录下创建 `my.ini`
	``` ini
	[client]
	port=3306
	default-character-set=utf8
	[mysqld]
	port=3306
	character_set_server=utf8
	basedir=%MYSQL_HOME%
	datadir=%MYSQL_HOME%\data
	[WinMySQLAdmin]
	%MYSQL_HOME%\bin\mysqld.exe
	```
5. 以管理员身份安装mysql服务 
   ``` bat
   mysqld.exe –install
   ```
   提示 `Service successfully installed` 安装完成
6. 初始化 mysql
   ``` bat
   mysqld --initialize-insecure
   ```
7. 启动 mysql
   ``` bat
   net start mysql
   ```
8. 设置初始密码
   ``` bat
   mysqladmin -u root -p password
   ```
   > 根据提示输入密码
9. 关闭 mysql
   ``` bat
   net stop mysql
   ```
### node.js连接mysql出现错误： ER_NOT_SUPPORTED_AUTH_MODE
1. 连接 mysql
   ``` bat
   mysql -u root -p
   ```
2. 执行指令
   ``` sql
   ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '<密码>';
   FLUSH PRIVILEGES;
   ```
