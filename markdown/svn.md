## svn

## svn报错: E200014: Checksum mismatch
- 解决方案一
先把上传失败的文件 xxx.file 进行备份，然后执行
``` bash
svn del xxx.file
svn ci -m "删除冲突文件"
```
然后把xxx.file 重新添加到版本库里
``` bash
svn add xxx.file
svn ci -m "重新添加冲突文化到版本库" xxx.file
```
解决问题。注意删除掉冲突文件后，一定要同步到版本控制中。后面在重新把冲突文件加入到版本控制中
- 解决方案二
先在错误文件所在目录执行：
``` bash
svn update --set-depth empty
```
注意：此方法会删除此目录中的所有文件，避免万一，请主动备份

再执行:
``` bash
svn update --set-depth infinity
```
解决问题

## 常用命令
``` bash
# 还原所有修改
svn revert -R .

# 遍历当前目录下的所有未添加的文件，添加到版本控制中
svn add . --no-ignore --force

# 仅添加当前目录下的所有未添加的文件
svn add * --no-ignore  .

# 删除所有丢失的文件
for /F "tokens=* delims=! " %A in (' "svn status | findstr /R "^!"" ') do (svn delete "%A")
```