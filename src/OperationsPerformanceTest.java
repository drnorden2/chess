
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
    
    private static void speedTest() {
		long timeStamp =0;
		double time =0;
		int size = 10000;
		
		int [] array1  = new int[size];
		Integer [] array2  = new Integer[size];
		
		timeStamp = System.currentTimeMillis();
		for(int i=0;i<size;i++) {
			for(int j=0;j<size;j++) {
				array1[i]=i+j;
				array1[i]=array1[i]*array1[i];
			}
		}
		
		time = ((double)(long)((System.currentTimeMillis()-timeStamp)/10)/100);
		System.out.println("Run 1 in "  + time +"s");
		
		
		
		
		timeStamp = System.currentTimeMillis();
		for(int i=0;i<size;i++) {
			for(int j=0;j<size;j++) {
				array2[i]=i+j;
				array2[i]=array2[i]*array2[i];
			}
		}
		
		time = ((double)(long)((System.currentTimeMillis()-timeStamp)/10)/100);
		
		
		System.out.println("Run 2 in "  + time +"s");
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

