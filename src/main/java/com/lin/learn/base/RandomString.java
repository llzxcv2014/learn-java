package com.lin.learn.base;

import java.util.NoSuchElementException;
import java.util.Random;

/**
 * see
 * <br></br>
 * <a href="https://www.cnblogs.com/thisiswhy/p/16139704.html">初看一脸问号，看懂直接跪下！</a>
 * <h4>参考资料</h4>
 * <br></br>
 * <a href="https://stackoverflow.com/questions/15182496/why-does-this-code-using-random-strings-print-hello-world">Why does this code using random strings print "hello world"?</a>
 * <br></br>
 * <a href="https://stackoverflow.com/questions/18092160/whats-with-181783497276652981-and-8682522807148012-in-random-java-7">What's with 181783497276652981 and 8682522807148012 in Random (Java 7)?</a>
 * <br></br>
 * <a href="https://bugs.openjdk.java.net/browse/JDK-8201634">Random seedUniquifier uses incorrect LCG</a>
 */
public class RandomString {

    public static String run(int i) {
        Random ran = new Random(i);
        StringBuilder sb = new StringBuilder();
        while (true) {
            int k = ran.nextInt(27);
            if (k == 0) {
                break;
            }

            sb.append((char)('`' + k));
        }

        return sb.toString();
    }

    public static long generateSeed(String goal, long start, long finish) {
        char[] input = goal.toCharArray();
        char[] pool = new char[input.length];
        label:
        for (long seed = start; seed < finish; seed++) {
            Random random = new Random(seed);

            for (int i = 0; i < input.length; i++) {
                pool[i] = (char) (random.nextInt(27) + '`');
            }

            if (random.nextInt(27) == 0) {
                for (int i = 0; i < input.length; i++) {
                    if (input[i] != pool[i]) {
                        continue label;
                    }
                }
                return seed;
            }
        }
        throw new NoSuchElementException("Sorry :/");
    }

    public static void main(String[] args) {
        System.out.println(run(-229985452) + " " + run(-147909649));
    }
}
