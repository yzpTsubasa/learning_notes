## python
## pip 生成和安装requirements.txt
``` bash
# 方案一
pip freeze > requirements.txt

# 方案二（推荐）
# 出现网络错误时，需要启用vpn
pip install pipreqs
pipreqs ./ --encoding=utf8

# 从requirements.txt安装依赖库
pip install -r requirements.txt

```
##  打包成可执行文件
> 先安装 pytinstaller
``` bash
pip install pyinstaller
```
> 使用参数 -F 生成独立的可执行文件
``` bash
pyinstaller -F main.py
```
##  Python site-packages
> C:\Users\Administrator\AppData\Local\Programs\Python\Python37\Lib\site-packages
##  使用国内的pypi源
``` bash
 -i https://pypi.doubanio.com/simple
pip install numpy pandas matplotlib sklearn tensorflow tensorflow-gpu notebook jupyter -i https://pypi.doubanio.com/simple
pip install tensorflow_datasets tensorflow_hub
# 运行 notebook
ipython notebook
jupyter notebook
# 单独安装WebEngine
pip install PyQtWebEngine
# 直接安装本地包 wheel xxxxx.whl
pip install xxxxx.whl
```
##  python 调换字典的键和值
``` py
tmp = dict([(value, key) for (key, value) in tmp.items()])
```
##  PILLOW_VERSION 在 7.0.0 中被移除
``` bash
pip install Pillow==6.2.2 -i https://pypi.doubanio.com/simple
```
## Python2 报错 LookupError: unknown encoding: cp65001
在 bat 文件中其实可以形成这样的模式来支持中文路径：
``` bat
set PYTHONIOENCODING=UTF-8
```

## 打包单个模块到某个路径(下载离线包给其他机器用)
``` bash
pip download pyimg4 -d . > ./download.log
```