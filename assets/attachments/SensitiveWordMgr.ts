module hg.game {

    /**
     * 敏感词管理器
     */
    export class SensitiveWordMgr {

        private treeRoot: DataNode;

        public Init(words: string): boolean {
            if (!words) {
                return false;
            }
            this.regSensitiveWords(words.split('\n'));
            return true;
        }

        private regSensitiveWords(words) {
            //这是一个预处理步骤，生成敏感词索引树，功耗大于查找时使用的方法，但只在程序开始时调用一次。
            this.treeRoot = new DataNode();
            this.treeRoot.value = "";
            var words_len = words.length;
            for (var i = 0; i < words_len; i++) {
                var word = words[i];
                var len = word.length;
                var currentBranch = this.treeRoot;
                for (var c = 0; c < len; c++) {
                    var char = word.charAt(c);
                    var tmp = currentBranch.getNode(char);
                    if (tmp) {
                        currentBranch = tmp;
                    } else {
                        currentBranch = currentBranch.addNode(char);
                    } 
                } 
                currentBranch.isEnd = true;
            }
        }
        /**
         *替换字符串中的敏感词返回 
         * @param dirtyWords
         * @return 
         * 
         */
        public replaceSensitiveWord(dirtyWords) {
            if (!this.treeRoot) {
                return dirtyWord;
            }
            var char;
            var curTree = this.treeRoot;
            var childTree;
            var curEndWordTree: DataNode;
            var dirtyWord;
        
            var c = 0; //循环索引
            var endIndex = 0; //词尾索引
            var headIndex = -1; //敏感词词首索引
            while (c < dirtyWords.length) {
                char = dirtyWords.charAt(c);
                childTree = curTree.getNode(char);
                if (childTree) //在树中遍历
                {
                    if (childTree.isEnd) {
                        curEndWordTree = childTree;
                        endIndex = c;
                    }
                    if (headIndex == -1) {
                        headIndex = c;
                    }
                    curTree = childTree;
                    c++;
                } else //跳出树的遍历
                {
                    if (curEndWordTree) //如果之前有遍历到词尾，则替换该词尾所在的敏感词，然后设置循环索引为该词尾索引
                    {
                        dirtyWord = curEndWordTree.toString();
                        dirtyWords = dirtyWords.replace(dirtyWord, this.getReplaceWord(dirtyWord.length));
                        c = endIndex;
                    } else if (curTree != this.treeRoot) //如果之前有遍历到敏感词非词尾，匹配部分未完全匹配，则设置循环索引为敏感词词首索引
                    {
                        c = headIndex;
                        headIndex = -1;
                    }
                    curTree = this.treeRoot;
                    curEndWordTree = null;
                    c++;
                }
            }
        
            //循环结束时，如果最后一个字符满足敏感词词尾条件，此时满足条件，但未执行替换，在这里补加
            if (curEndWordTree) {
                dirtyWord = curEndWordTree.toString();
                dirtyWords = dirtyWords.replace(dirtyWord, this.getReplaceWord(dirtyWord.length));
            }
        
            return dirtyWords;
        }
        
        /**
         *判断是否包含敏感词 
         * @param dirtyWords
         * @return 
         * 
         */
        public containsBadWords(dirtyWords) {
            var char;
            var curTree = this.treeRoot;
            var childTree;
            var curEndWordTree: DataNode;
            var dirtyWord;
        
            var c = 0; //循环索引
            var endIndex = 0; //词尾索引
            var headIndex = -1; //敏感词词首索引
            while (c < dirtyWords.length) {
                char = dirtyWords.charAt(c);
                childTree = curTree.getNode(char);
                if (childTree) //在树中遍历
                {
                    if (childTree.isEnd) {
                        curEndWordTree = childTree;
                        endIndex = c;
                    }
                    if (headIndex == -1) {
                        headIndex = c;
                    }
                    curTree = childTree;
                    c++;
                } else //跳出树的遍历
                {
                    if (curEndWordTree) //如果之前有遍历到词尾，则替换该词尾所在的敏感词，然后设置循环索引为该词尾索引
                    {
                        dirtyWord = curEndWordTree.toString();
                        dirtyWords = dirtyWords.replace(dirtyWord, this.getReplaceWord(dirtyWord.length));
                        c = endIndex;
                        return true;
                    } else if (curTree != this.treeRoot) //如果之前有遍历到敏感词非词尾，匹配部分未完全匹配，则设置循环索引为敏感词词首索引
                    {
                        c = headIndex;
                        headIndex = -1;
                    }
                    curTree = this.treeRoot;
                    curEndWordTree = null;
                    c++;
                }
            }
        
            //循环结束时，如果最后一个字符满足敏感词词尾条件，此时满足条件，但未执行替换，在这里补加
            if (curEndWordTree) {
                return true;
                dirtyWord = curEndWordTree.toString();
                dirtyWords = dirtyWords.replace(dirtyWord, this.getReplaceWord(dirtyWord.length));
            }
            return false;
        }
        
        private getReplaceWord(len) {
            var replaceWord = "";
            for (var i = 0; i < len; i++) {
                replaceWord += "*";
            }
            return replaceWord;
        }
    }

    class DataNode {
        data: Object;
        _isLeaf: boolean;
        isEnd: boolean;
        parent: DataNode;
        value: any;
        constructor() {
            this.data = new Object();
            this._isLeaf = null;
            this.isEnd = false;
        
            this.parent = null;
            this.value = null;
        }

        public getNode(name): DataNode {
            return this.data[name];
        }

        public addNode(char): DataNode {
            var node = new DataNode();
            this.data[char] = node;
            node.value = char;
            node.parent = this;
            return node;
        }

        /**
         *是否是叶子节点
         */
        public isLeaf(): boolean {
            var index = 0;
            for (var key in this.data) {
                index++;
            }
            this._isLeaf = index == 0
            return this._isLeaf;
        }

        public toString (): string {
            var rt = this.value;
            var node = this.parent;
            while (node) {
                rt = node.value + rt;
                node = node.parent;
            }
            return rt;
        }
    }
}