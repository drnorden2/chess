package perft.chess.perftbb;
import static perft.chess.Definitions.*;
public class CheckMask {
	public long[][] checkMasks = new long[64][64];
	long[] allRays = new long[64];
	
	
	public static void mainX(String[] args) {
		CheckMask cm = new CheckMask();
	}
	public CheckMask() {
		for(int i=0;i<64;i++) {
			allRays[i]=allRays(i);
		//	out(allRays[i]);
		}
		for(int i=0;i<64;i++) {
			for(int j=0;j<64;j++) {
				if(i==j)continue;
				long result =1L<<j;
				if((allRays[i]&result)!=0L) {
					int cur = j;
					int f1 = getFileForPos(i);
					int r1 = getRankForPos(i);
					
					//out(result|1L<<i);		
					while(cur!=i) {
						result|=1L<<cur;
						int f2 = getFileForPos(cur);
						int r2 = getRankForPos(cur);
						int	fDir =(int)Math.signum(f2-f1);
						int rDir =(int)Math.signum(r2-r1);
						cur = getPosForFileRank(f2-fDir,r2-rDir);
					}
					//out(result);
				}
				checkMasks[i][j]=result;
			}
		}
	}
	
	private long allRays(int sq) {
		long result = 0L;
		int rk = sq / 8, fl = sq % 8, r, f;
		for (r = rk + 1; r <= 7; r++) 
			result |= (1L << (fl + r * 8));
		for (r = rk - 1; r >= 0; r--)
			result |= (1L << (fl + r * 8));
		for (f = fl + 1; f <= 7; f++)
			result |= (1L << (f + rk * 8));
		for (f = fl - 1; f >= 0; f--)
			result |= (1L << (f + rk * 8));
		for (r = rk + 1, f = fl + 1; r <= 7 && f <= 7; r++, f++)
			result |= (1L << (f + r * 8));
		for (r = rk + 1, f = fl - 1; r <= 6 && f >= 0; r++, f--)
			result |= (1L << (f + r * 8));
		for (r = rk - 1, f = fl + 1; r >= 0 && f <= 7; r--, f++)
			result |= (1L << (f + r * 8));
		for (r = rk - 1, f = fl - 1; r >= 0 && f >= 0; r--, f--)
			result |= (1L << (f + r * 8));
		return result;
	}

	

}
