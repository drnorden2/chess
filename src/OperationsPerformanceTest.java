
import java.util.function.BinaryOperator;
/**********
 * by Kevin Anderson
 * https://stackoverflow.com/questions/45829516/java-relative-performance-of-math-operators
 * modified to Long by Andre Fischer
 *
 */
public class OperationsPerformanceTest {
    private static void test(String desc, BinaryOperator<Long> op, long a, long b, long startIter)
    {
        long maxIter = startIter;
        long elapsed;
        do {
            maxIter *= 2;
            long start = System.currentTimeMillis();
            for (long niter = 0; niter < maxIter; ++niter) {
                long res = op.apply(a, b);
            }
            elapsed = System.currentTimeMillis() - start;
        } while (elapsed <= 10_000);
        System.out.printf("%-15s/sec\t%g\n",
            desc, (maxIter * 1000.0) / elapsed);
    }

    public static void main(String[] arg)
    {
        test("Addition (double)", (Long a, Long b) -> {
            return a + b;
        }, 483902, 42347, 10_000_00);
        test("Subtraction (double)", (Long a, Long b) -> {
            return a - b;
        }, 483902, 42347, 10_000_00);
        test("Multiplication (double)", (Long a, Long b) -> {
            return a * b;
        }, 483902, 42347, 1_000_00);
        test("Division (double)", (Long a, Long b) -> {
            return a / b;
        }, 483902, 42347, 1_00_000);
        test("Log10", (Long a, Long b) -> {
            return (long)Math.log10(a);
        }, 483902, 42347, 1_00_000);
        test("LogE", (Long a, Long b) -> {
            return (long)Math.log(a);
        }, 483902, 42347, 1_00_000);
        test("Power", (Long a, Long b) -> {
            return (long)Math.pow(a, b);
        }, 483902, 12, 10_000);
    }
}

