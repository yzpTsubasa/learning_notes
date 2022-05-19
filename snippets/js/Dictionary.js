class Dictionary {

    constructor() {
        this.keys = [];
        this.values = [];
        this.key2index = {};
    }
    
    set(key, value) {
        let index = this.key2index[key];
        if (index == null) {
            this.key2index[key] = index = this.keys.length;
            this.keys[index] = key;
        }
        this.values[index] = value;
    }

    remove(key) {
        let index = this.key2index[key];
        if (index != null) {
            this.keys.splice(index, 1);
            this.values.splice(index, 1);
            delete this.key2index[key];
        }
    }

    get(key) {
        let index = this.key2index[key];
        if (index != null) {
            return this.values[index];
        }
    }

    size() {
        return this.keys.length;
    }

    reset() {
        this.keys.length = 0;
        this.values.length = 0;
        this.key2index = {};
    }
}
