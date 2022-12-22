package com.lin.learn.base;

/**
 * getfield opcode优化
 * 简单来说就是将成员变量赋值给局部变量
 * JDK中有多处使用了这种优化，这种优化手段只使用于当年的带代码，对JIT编译没有任何影响
 * @see java.util.HashMap#put(Object, Object) put调用了putVal方法
 * @see String#trim() String类中trim方法
 * <br></br>
 * see:
 * <br></br>
 * <a href="https://www.cnblogs.com/thisiswhy/p/16159127.html">从源码里的一个注释，我追溯到了12年前，有点意思</a>
 * <br></br>
 * 相关资料：
 * <br></br>
 * <a href="https://stackoverflow.com/questions/37362109/why-other-classes-like-biginteger-in-jdk7-does-not-use-avoid-getfield-opcode-t?r=SearchResults">why other classes like BigInteger in jdk7 does not use "avoid getfield opcode" tech just like String.trim</a>
 * <br></br>
 * <a href="https://stackoverflow.com/questions/28975415/why-jdk-code-style-uses-a-variable-assignment-and-read-on-the-same-line-eg-i">Why jdk code style uses a variable assignment and read on the same line - eg. (i=2) < max</a>
 * <br></br>
 * <a href="https://www.oracle.com/technical-resources/articles/javase/devinsight-1.html">The Developer Insight Series, Part 1: Write Dumb Code -- Advice From Four Leading Java Developers</a>
 * <br></br>
 * <a href="https://stackoverflow.com/questions/2785964/in-arrayblockingqueue-why-copy-final-member-field-into-local-final-variable">In ArrayBlockingQueue, why copy final member field into local final variable?</a>
 * <br></br>
 * <a href="https://mail.openjdk.org/pipermail/core-libs-dev/2010-May/004165.html">Performance of locally copied members ?</a>
 * <br></br>
 * <a href="http://icyfenix.cn/tricks/2020/graalvm/graal-compiler.html">新一代即时编译器</a>
 */
public class GetFieldOpcodeOptimization {

    private final char[] chars = new char[5];

    public void run() {
        /*
         * 使用局部变量val，然后访问val可以将三次getfield指令减少到一次
         * 取而代之的是aload指令。用局部变量将成员属性“缓存”到局部变量表中，也就是搞到栈上
         */
        char[] val = chars;
        System.out.println(val[0]);
        System.out.println(val[1]);
        System.out.println(val[2]);
    }

    public static void main(String[] args) {
        GetFieldOpcodeOptimization optimization = new GetFieldOpcodeOptimization();
        optimization.run();
    }
}
