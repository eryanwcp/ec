package com.eryansky.code;

/**
 * 各成员的代码贡献的各项指标<br/>
 * 当前统计：新增代码、和修改行数
 */
class LineNumber{
    // 作者
    String name;
    // 新增行数
    long add = 0l;
    // 修改行数
    long modify = 0l;
    // 总数
    long sum = 0l;

    /**
     * 构造器，必须传入该记录所属作者
     * @param name 作者姓名
     */
    public LineNumber(String name) {
        this.name = name;
    }

    /**
     * 设置新增行数，并在总数加上新增行数
     * @param add 新增行数
     */
    public void setAdd(long add) {
        this.add += add;
        this.sum += add;
    }

    /**
     * 设置修改行数，并在总数加上修改行数
     * @param modify 修改行数
     */
    public void setModify(long modify) {
        this.modify += modify;
        this.sum += modify;
    }

    // getter 和 toString
    public long getAdd() {
        return add;
    }

    public long getModify() {
        return modify;
    }

    public long getSum() {
        return sum;
    }

    @Override
    public String toString() {
        return "代码行数{" +
                "账号:'" + name + '\'' +
                ", 新增:" + add +
                ", 修改:" + modify +
                ", 总数:" + sum +
                '}';
    }
}