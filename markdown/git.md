## git
## ssh 拉取 git代码
- 创建 ~/.ssh/config 文件，如
``` s
# Read more about SSH config files: https://linux.die.net/man/5/ssh_config
Host 172.17.172.123
  HostName 172.17.172.123
  User tsubasa

Host github.com
  HostName github.com
  IdentityFile ~/.ssh/id_seirei # 私钥文件
  IdentitiesOnly yes

Host github.com
  HostName github.com
  IdentityFile ~/.ssh/id_rsa # 私钥文件
  IdentitiesOnly yes

```
## 在 Tortoise Git 中使用 ssh
`TortoiseGit` -> `Settings` –> `Network`
将`SSH client`(`C:\Program Files\TortoiseGit\bin\TortoisePlink.exe`)改为 `C:\Program Files\Git\usr\bin\ssh.exe`

## windows 10 下启用 git-server
- 需要启用 OpenSSH SSH Server
- 默认情况下可能没有安装这个功能，需要在【应用】中添加可选功能
- 然后重启，启动【服务】
##  常用命令
``` bash
# 提交所有变化
git add -A  
# 提交被修改(modified)和被删除(deleted)文件，不包括新文件(new)
git add -u  
# 提交新文件(new)和被修改(modified)文件，不包括被删除(deleted)文件
git add . 
git commit -m "msg"
git push
# 还原所有文件
git checkout -- * 
# 检出指定分支及目录
git clone -b <branch> <remote> <directory>
# 删除远程分支
git push <remote> --delete <branch>
# 删除本地分支
git branch -d <branch>
# 推送到所有远端
for /F "tokens=* delims=! " %A in (' git remote ') do git push "%A"
# 创建全新分支
git checkout --orphan <branch>
```
## PowerShell 写法
``` ps1
# 推送到所有远端
foreach($line in Invoke-Expression "git remote") {
    Write-Output $line
    Invoke-Expression "git push $line"
}
```
## git 中文文件名乱码
``` bash
git 默认中文文件名是 \xxx\xxx 等八进制形式，是因为 对0x80以上的字符进行quote。

只需要设置core.quotepath设为false，就不会对0x80以上的字符进行quote。中文显示正常

git config --global core.quotepath false
```