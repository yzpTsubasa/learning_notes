## rust

## 设置 cargo 代理
> 在`~/.cargo`目录下创建`config`文件，文件内容如下
``` conf
[source.crates-io]
registry = "https://github.com/rust-lang/crates.io-index"

# 指定镜像(下面几个里选一个)
replace-with = 'rustcc2'

# 清华大学
[source.tuna]
registry = "https://mirrors.tuna.tsinghua.edu.cn/git/crates.io-index.git"

# 中国科学技术大学
[source.ustc]
registry = "git://mirrors.ustc.edu.cn/crates.io-index"

# 上海交通大学
[source.sjtu]
registry = "https://mirrors.sjtug.sjtu.edu.cn/git/crates.io-index"

# rustcc社区
[source.rustcc0]
registry = "https://code.aliyun.com/rustcc/crates.io-index.git"

[source.rustcc1]
registry="git://crates.rustcc.cn/crates.io-index"

[source.rustcc2]
registry="git://crates.rustcc.com/crates.io-index"
```