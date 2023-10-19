#! /usr/bin/env python
# -*- coding: utf-8 -*-
"""
SVN提交前检查钩子
"""

import sys
import os
import re
import subprocess
from subprocess import PIPE
import json

# 默许日志
whitelist4message = [
    "out",
]
# 默许用户
whitelist4username = [
    "fangjie",
]
# 默许操作
whiltelist4changeaction = [
    "D",  # 删除操作
]


def main(argv):

    (repos, txn, svnlook) = argv

    (message, error) = exec_cmd([svnlook, 'log', '-t', txn, repos])
    message = message.strip()
    if message in whitelist4message:
        sys.exit(0)
    (username, error) = exec_cmd([svnlook, 'author', '-t', txn, repos])
    username = username.strip()
    if username in whitelist4username:
        sys.exit(0)

    # blacklist = (".*config\.php$", ".*/php/cache", ".*test", "config\.js$", "^.*\.db$")
    blacklist = [
        {"pattern": ('default\.res\.(\w+\.)?json'), "tip": "资源索引配置错误",
         "valid_handler": lambda filename, content: is_valid_res_json(content)},
        {"pattern": '.*\.exml', "tip": "皮肤文件名与类名不匹配", "valid_handler": lambda filename,
            content: re.search('class="[\w]*\.?{}"'.format(os.path.splitext(os.path.basename(filename))[0]), content)},
        {"pattern": ('.*\.mine', '.*\.theirs'), "tip": "请解决冲突后再提交"},
        {"pattern": ('.*\.json'), "tip": "文件存在语法错误",
         "valid_handler": lambda filename, content: is_valid_json(content)},
    ]
    min_chars = 1
    # message = message.encode("gb2312")
    num_chars = len(message)
    if num_chars < min_chars:
        sys.stderr.write("日志信息最少 {} 个字; 当前为 {} 个".format(min_chars, num_chars))
        sys.exit(1)
    # sys.stderr.write(message)
    if not re.search('[\[【]+[\d\.]+[\]】]+', message):
        sys.stderr.write("请添加研发版本号信息; 例如: [90] 新增 ***文件")
        sys.exit(1)
    (changelist, error) = exec_cmd([svnlook, 'changed', repos, '-t', txn])
    changelist = changelist.splitlines()
    for line in changelist:
        action = line[0]
        if action in whiltelist4changeaction:
            continue
        filename = line[1:].strip()
        for badcfg in blacklist:
            patterns = badcfg.get('pattern')
            valid_handler = badcfg.get("valid_handler")
            if isinstance(patterns, str):
                patterns = [patterns]
            for pattern in patterns:
                # sys.stderr.write("{}".format(pattern))
                if re.search(pattern, filename):
                    # sys.stderr.write("...searched")
                    if not valid_handler:
                        sys.stderr.write("{}: {}".format(
                            filename, badcfg.get("tip")))
                        sys.exit(1)
                    else:
                        # 文件内容
                        (content, error) = exec_cmd(
                            [svnlook, 'cat', '-t', txn, repos, filename])
                        # sys.stderr.write(content)
                        if not valid_handler(filename, content):
                            sys.stderr.write("{}: {}".format(
                                filename, badcfg.get("tip")))
                            sys.exit(1)
    sys.exit(0)


def exec_cmd(cmd, cwd=None, inputs=[], willprint=False, shell=False, **kwargs):
    subp = None
    stdout = None
    error = None
    try:
        subp = subprocess.Popen(
            cmd, stdin=PIPE, stdout=PIPE if not willprint else None, cwd=cwd, shell=shell)
    except BaseException as identifier:
        return
    (stdout, error) = subp.communicate(input="\n".join(inputs).encode())
    if not stdout is None:
        try:
            stdout = stdout.decode()
        except BaseException as identifier:
            try:
                stdout = stdout.decode(encoding='gb2312')
            except BaseException as identifier:
                pass
    return (stdout, error)


def is_valid_json(content):
    try:
        json.loads(content)
        return True
    except BaseException as identifier:
        sys.stderr.write(str(identifier) + "\n")
    return False


# 资源表的类型映射
res_json_type_map = {
    "exml": "exml",
    "jpg": "image",
    "dbbin": "bin",
    "json": "json",
    "mp3": "sound",
    "fnt": "font",
}


def is_valid_res_json(content):
    # sys.stderr.write('is_valid_res_json')
    has_error = None

    for name in res_json_type_map:
        right_type = res_json_type_map[name]
        pattern = '"name":"(\w+_{})",\s+"type":"(\w+)"'.format(name)
        # sys.stderr.write(pattern)
        founds = re.findall(pattern, content)
        if founds:
            for found in founds:
                if found[1] != right_type:
                    sys.stderr.write("资源名: {}, 类型: {}(应为{})\n".format(
                        found[0], found[1], right_type))
                    has_error = True
    return not has_error


if __name__ == "__main__":
    main(sys.argv[1:])
