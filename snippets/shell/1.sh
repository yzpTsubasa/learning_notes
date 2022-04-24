#/bin/bash
echo ">> 设置时区"
ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

echo ">> 定时任务 - 同步时间"
if ! crontab -l | grep ntpdate &>/dev/null; then
    (echo "* 1 * * * ntpdate time.windows.com >/dev/null 2>&1";crontab -l) | crontab
fi

echo ">> 禁用selinux"
sed -i '/^SELINUX\=\(enforcing\|permissive\)$/{s//SELINUX=disabled/}' /etc/selinux/config

echo ">> 关闭防火墙(应只清除策略)"
if egrep "7.[0-9]" /etc/redhat-release &>/dev/null; then
    systemctl stop firewalld
    systemctl disable firewalld
elif egrep "6.[0-9]" /etc/redhat-release &>/dev/null; then
    service iptables stop
    chkconfig iptables off
fi

echo ">> 历史命令显示操作时间"
if ! grep HISTTIMEFORMAT /etc/bashrc &>/dev/null; then
    echo 'export HISTTIMEFORMAT="%F %T `whoami` "' >> /etc/bashrc
fi

echo ">> SSH超时时间"
if ! grep "TMOUT=600" /etc/profile &>/dev/null; then
    echo "export TMOUT=600" >> /etc/profile
fi

# 添加账号 adduser tsubasa
# 设置密码 passwd tsubasa
# -a, --append　　只能与“-G”选项一起使用，把指定的用户加到一个现有的从用户组。
# -G g1[,g2,...[,gN]…], --groups g1[,g2,...[,gN]…]　　指定用户从属的一个或多个从用户组。多个用户组之间需加逗号分隔符，前后不能有空格。指定用户组时可以使用名字或数字ID。如果用户属于未列举的某个用户组的成员，还要从该用户组中删除指定的用户。如果同时指定了“-a”选项，仅把指定的用户加到列举的从用户组中即可。
# 设置权限 usermod -aG wheel tsubasa

echo ">> 禁止root远程登录"
sed -i 's/#PermitRootLogin yes/PermitRootLogin no/' /etc/ssh/sshd_config

echo ">> 禁止定时任务发送邮件"
sed -i 's/^MAILTO=root/MAILTO=""/' /etc/crontab

echo ">> 设置最大打开文件数"
if ! grep "* soft nofile 65535" /etc/security/limits.conf &>/dev/null; then
cat >> /etc/security/limits.conf << EOF
    * soft nofile 65535
    * hard nofile 65535
EOF
fi

echo ">> 系统内核优化"
cat >> /etc/sysctl.conf << EOF
net.ipv4.tcp_syncookies = 1
net.ipv4.tcp_max_tw_buckets = 20480
net.ipv4.tcp_max_syn_backlog = 20480
net.core.netdev_max_backlog = 262144
net.ipv4.tcp_fin_timeout = 20
EOF

echo ">> 减少SWAP使用（磁盘空间，比物理内存慢）"
echo "0" > /proc/sys/vm/swappiness

echo ">> 安装系统性能分析工具及其他"
yum install gcc make autoconf vim sysstat net-tools iostat iftop iotp lrzsz -y &>/dev/null

echo Complete!