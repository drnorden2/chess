import static perft.chess.Definitions.*;


public class FourBitBoard {
	
	public static void main (String[] args) {
		long x = 3;
		out(x);
		x = (x | (x << 24)) & 0x000000ff000000ffL;
		x = (x | (x << 12)) & 0x000f000f000f000fL;
		x = (x | (x << 6)) & 0x0303030303030303L;
		x = (x | (x << 3)) & 0x1111111111111111L;
		x |= x << 1;
		x |= x << 2;
		out(x);
	}
}
