## git

## 导出 commit1 和 commit2 之间的差异文件（包含目录结构）
git diff --name-only commit1 commit2 | tar -czf changes.tar.gz -T -

## 凭证
``` bash
# 保存凭证到本地
git config --global credential.helper store
# 设置凭证超时
git config --global credential.helper 'cache --timeout=600'
# 查看凭证配置
git config --global --get-all credential.helper
```

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
git add --all

# 提交被修改(modified)和被删除(deleted)文件，不包括新文件(new)
git add -u
git add --update

# 提交新文件(new)和被修改(modified)文件，不包括被删除(deleted)文件
git add .
git commit -m "msg"
git push

# 还原所有文件
git checkout -- *

# 检出指定分支及目录
git clone -b <branch> <remote> <directory>

# 检出项目以及递归子模块
git clone --recursive <remote>

# 更新子模块
git submodule update

# 分步完整拉取子模块
git clone <remote>
git submodule update --init --recursive

# 使用最新的子模块内容
git submodule update --init --recursive --remote

# 删除远程分支
git push <remote> --delete <branch>

# 删除本地分支
git branch -d <branch>

# 推送到所有远端
for /F "tokens=* delims=! " %A in (' git remote ') do git push "%A"

# 创建全新分支
git checkout --orphan <branch>

# 根据当前分支创建新分支
git checkout -b <branch>

# 撤销(还原)最近一次提交
git reset HEAD~1

# 添加一个远端
git remote add <remote> <remote_url>
git remote add 205 http://192.168.1.205:3000/yzp/node_http_server.git
```
## PowerShell 写法
``` ps1
# 推送到所有远端
foreach($line in Invoke-Expression "git remote") {
    Write-Output "pushing to remote $line"
    Invoke-Expression "git push $line"
}
# 推送到所有远端(单行版本)
Invoke-Expression "git remote" | ForEach-Object -Process { Write-Output "pushing to remote $_";Invoke-Expression "git push $_" }
# 推送到所有远端(可视版本)
Invoke-Expression "git remote" | ForEach-Object -Process {
  Write-Output "pushing to remote $_";Invoke-Expression "git push $_"
}
```
## git 中文文件名乱码
``` bash
git 默认中文文件名是 \xxx\xxx 等八进制形式，是因为 对0x80以上的字符进行quote。

只需要设置core.quotepath设为false，就不会对0x80以上的字符进行quote。中文显示正常

git config --global core.quotepath false
```

## git stash 贮藏
```sh
# 贮藏
git stash save "顶栏消耗品可配置"
# -u | --include-untracked ，包含未添加的
git stash -u -m "存档"

# 显示贮藏列表
git stash list

# 显示贮藏概要 (0 代表 索引 0，即第一条)
git stash show 0

# 显示贮藏详情 (1 代表 索引 1，即第二条)。浏览过程中，按 "Q" 键退出
git stash show -p 1

# 应用贮藏（不会删除贮藏）
git stash apply 0
# 删除贮藏
git stash drop 0

# 弹出并应用贮藏（会删除贮藏）
git stash pop 0
```

## git 分支重命名
```sh
# 重命名当前分支
git branch -m [NEW_NAME]
# 重命名指定的 OLD_NAME 分支
git branch -m [OLD_NAME] [NEW_NAME]
```

## Github 不再支持用账号密码clone/push
``` mermaid
graph LR;
  Settings-->Develop_settings-->Personal_access_tokens-->Tokens_classic
```
> 推送地址: https://[token]@github.com/yzpTsubasa/[repository].git


## 相关错误
- 没有推送权限
> error: RPC failed; HTTP 403 curl 22 The requested URL returned error: 403
> send-pack: unexpteced disconnect while reading sideband packet
   

## 本地创建分支并推送到远程
```sh
git init
git checkout -b [branch_name]
git add .
git commit -m "init"
git remote add origin [url]
git push -u origin [branch_name]
```

## 查看操作记录
```sh
# reference log
git reflog
```

## 合并推送请求
```sh
# 切换到临时分支 xxxxxx-master
git checkout -b xxxxxx-master master
# 拉取要合并的内容，并解决冲突，提交
git pull https://github.com/xxxxxx/public_resource.git master
# 切回换回 master 分支
git checkout master
# 合并分支
git merge --no-ff xxxxxx-master
```

## 仅克隆一个分支
```sh
git clone --single-branch --branch [branch_name] [url] [directory]
```

## 配置core.editor属性
```sh
git config --global core.editor "code --wait"
# 每次都打开一个新窗口，那么就需要加上--new-window。
git config --global core.editor "code --wait --new-window"
# 恢复Vim，使用下面命令即可
git config --global --unset core.editor
```

## 不使用 gitignore 来忽略文件
### 使用 --skip-worktree 选项(推荐)
```sh
# 启用
git update-index --skip-worktree [file]
# 取消
git update-index --no-skip-worktree [file]
# 查看
git ls-files -v | findstr "^S"

```
### 使用 --assume-unchaged
```sh
# 启用
git update-index --assume-unchanged [file]
# 取消
git update-index --no-assume-unchanged [file]
# 查看
git ls-files -v | findstr "^h"
```