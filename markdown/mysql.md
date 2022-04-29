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