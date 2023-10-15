## MCU

## 使用VSCode进行开发
- C/C++ [需要下载编译工具gcc](https://jmeubank.github.io/tdm-gcc/download/)
- C51V961[Keil uVision5](https://armkeil.blob.core.windows.net/eval/C51V961.EXE)，旧版本如 C51V954 创建的工程文件，Keil Assistant 无法打开。

## 单片机写入
- 断开电源
- 点击下载
- 重新上电

## keil报错syntax error near 'int', expected '__asm'解决方案
c不支持中途定义，把变量定义放到函数中的最前面
```diff
+unsigned char num2;
+unsigned char num1;
 void main()
 {
     UART_Init();
     UART_SendByte(0x28);
     while (1) {
-        unsigned char num2 = Key();
+        num2 = Key();
         if (num2) {
             UART_SendByte(num2);
         }
 
-        unsigned char num1 = MatrixKey();
+        num1 = MatrixKey();
         if (num1) {
             UART_SendByte(num1);
         }
     }
 }
```