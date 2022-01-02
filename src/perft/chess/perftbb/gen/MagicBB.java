/********
 * Re: Magic Move Generation by Tord Romstad, CCC, February 20, 2008
 * Ported to Java by André Fischer
 * 
 ***************/

package perft.chess.perftbb.gen;


import perft.chess.core.datastruct.BitBoard;

public class MagicBB {
	static int[] RBits = { 12, 11, 11, 11, 11, 11, 11, 12, 11, 10, 10, 10, 10, 10, 10, 11, 11, 10, 10, 10, 10, 10, 10,
			11, 11, 10, 10, 10, 10, 10, 10, 11, 11, 10, 10, 10, 10, 10, 10, 11, 11, 10, 10, 10, 10, 10, 10, 11, 11, 10,
			10, 10, 10, 10, 10, 11, 12, 11, 11, 11, 11, 11, 11, 12 };

	static int[] BBits = { 6, 5, 5, 5, 5, 5, 5, 6, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 7, 7, 7, 7, 5, 5, 5, 5, 7, 9, 9, 7, 5,
			5, 5, 5, 7, 9, 9, 7, 5, 5, 5, 5, 7, 7, 7, 7, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 6, 5, 5, 5, 5, 5, 5, 6 };

	int transform(long b, long magic, int bits) {
		return (int) ((b * magic) >>> (64 - bits));
	}

	long rmask(int sq) {
		long result = 0L;
		int rk = sq / 8, fl = sq % 8, r, f;
		for (r = rk + 1; r <= 6; r++)
			result |= (1L << (fl + r * 8));
		for (r = rk - 1; r >= 1; r--)
			result |= (1L << (fl + r * 8));
		for (f = fl + 1; f <= 6; f++)
			result |= (1L << (f + rk * 8));
		for (f = fl - 1; f >= 1; f--)
			result |= (1L << (f + rk * 8));
		return result;
	}

	long bmask(int sq) {
		long result = 0L;
		int rk = sq / 8, fl = sq % 8, r, f;
		for (r = rk + 1, f = fl + 1; r <= 6 && f <= 6; r++, f++)
			result |= (1L << (f + r * 8));
		for (r = rk + 1, f = fl - 1; r <= 6 && f >= 1; r++, f--)
			result |= (1L << (f + r * 8));
		for (r = rk - 1, f = fl + 1; r >= 1 && f <= 6; r--, f++)
			result |= (1L << (f + r * 8));
		for (r = rk - 1, f = fl - 1; r >= 1 && f >= 1; r--, f--)
			result |= (1L << (f + r * 8));
		return result;
	}

	long find_magic(int sq, int m, boolean isBishop) {
		long mask;
		long[] b = new long[4096];
		long[] a = new long[4096];
		long[] used = new long[4096];
		long magic;
		int i = 0;
		int j = 0;
		int k = 0;
		int n = 0;
		boolean fail = false;

		mask = isBishop ? bmask(sq) : rmask(sq);
		n = Long.bitCount(mask);

		for (i = 0; i < (1 << n); i++) {
			b[i] = index_to_long(i, mask);
			a[i] = isBishop ? batt(sq, b[i]) : ratt(sq, b[i]);
		}
		for (k = 0; k < 100000000; k++) {
			magic = random_long_fewbits();

			if (Long.bitCount((mask * magic) & 0xFF00000000000000L) < 6)
				continue;

			for (i = 0; i < 4096; i++)
				used[i] = 0L;
			for (i = 0, fail = false; !fail && i < (1 << n); i++) {
				j = transform(b[i], magic, m);
				if (used[j] == 0)
					used[j] = a[i];
				else if (used[j] != a[i])
					fail = true;
			}
			if (!fail)
				return magic;
		}
		System.out.println("***Failed***");
		return 0L;
	}

	long random_long() {
		long u1, u2, u3, u4;
		u1 = (long) (Math.random() * 0xFFFF);
		u2 = (long) (Math.random() * 0xFFFF);
		u3 = (long) (Math.random() * 0xFFFF);
		u4 = (long) (Math.random() * 0xFFFF);
		return u1 | (u2 << 16) | (u3 << 32) | (u4 << 48);
	}

	long random_long_fewbits() {
		return random_long() & random_long() & random_long();
	}


	long index_to_long(int index, long mask) {
		int[] indices = new int[64];
		BitBoard rook = new BitBoard(mask);
		BitBoard curBits = new BitBoard((long) index);
		int count = rook.updateIndices(indices);
		rook.reset(0L);
		int cursor = 0;
		for (int k = 0; k < count; k++) {
			if (curBits.get(cursor++)) {
				rook.set(indices[k]);
			}
		}
		return rook.getBits();
	}

	long ratt(int sq, long block) {
		long result = 0L;
		int rk = sq / 8, fl = sq % 8, r, f;
		for (r = rk + 1; r <= 7; r++) {
			result |= (1L << (fl + r * 8));
			if ((block & (1L << (fl + r * 8))) != 0)
				break;
		}
		for (r = rk - 1; r >= 0; r--) {
			result |= (1L << (fl + r * 8));
			if ((block & (1L << (fl + r * 8))) != 0)
				break;
		}
		for (f = fl + 1; f <= 7; f++) {
			result |= (1L << (f + rk * 8));
			if ((block & (1L << (f + rk * 8))) != 0)
				break;
		}
		for (f = fl - 1; f >= 0; f--) {
			result |= (1L << (f + rk * 8));
			if ((block & (1L << (f + rk * 8))) != 0)
				break;
		}
		return result;
	}

	long batt(int sq, long block) {
		long result = 0L;
		int rk = sq / 8, fl = sq % 8, r, f;

		for (r = rk + 1, f = fl + 1; r <= 7 && f <= 7; r++, f++) {
			result |= (1L << (f + r * 8));
			if ((block & (1L << (f + r * 8))) != 0)
				break;
		}
		for (r = rk + 1, f = fl - 1; r <= 7 && f >= 0; r++, f--) {
			result |= (1L << (f + r * 8));
			if ((block & (1L << (f + r * 8))) != 0)
				break;
		}
		for (r = rk - 1, f = fl + 1; r >= 0 && f <= 7; r--, f++) {
			result |= (1L << (f + r * 8));
			if ((block & (1L << (f + r * 8))) != 0)
				break;
		}
		for (r = rk - 1, f = fl - 1; r >= 0 && f >= 0; r--, f--) {
			result |= (1L << (f + r * 8));
			if ((block & (1L << (f + r * 8))) != 0)
				break;
		}
		return result;
	}

	public static void main(String args[]) {
		MagicBB mbb = new MagicBB();
		int square;

		System.out.println("private static final long[] ROOK_MAGICS= {");
		for (square = 0; square < 64; square++) {
			System.out.print( String.format("0x%1$016X", mbb.find_magic(square, RBits[square], false)));
			if(square!=63) {
				System.out.println("L,");
			}else {
				System.out.println("L"); 
			}
		}
		System.out.println("};");
		

		System.out.println("private static final long[] BISHOP_MAGICS= {");
		for (square = 0; square < 64; square++) {
			System.out.print(String.format("0x%1$016X", mbb.find_magic(square, BBits[square], true)));
			if(square!=63) {
				System.out.println("L,");
			}else {
				System.out.println("L"); 
			}
		}
		System.out.println("};");
		
	}

}
