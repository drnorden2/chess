package perft.chess.perftbb.gen;

import static perft.chess.Definitions.*;
import perft.chess.core.datastruct.BitBoard;
import java.util.HashMap;

public class MagicNumberFinder {
	private final long[] ROOK_MAGICS = { 0x8080008022514000L, 0x0840004010002000L, 0x0880200008100080L,
			0x0100041000090020L, 0x1200020008042010L, 0x2100010004004882L, 0x1400009002080104L, 0x4080004420800D00L,
			0x2060800080344001L, 0x8014802000400090L, 0x04060022C2008390L, 0x7002002200100840L, 0x0105000800049100L,
			0x0642000200041009L, 0x8012004801440200L, 0x2202000104004082L, 0x0240008000944A20L, 0x1010004000502000L,
			0x0020010019002042L, 0x0000090010002100L, 0x1018110005080100L, 0x0264008080020004L, 0x0212140010520801L,
			0x084802000040A104L, 0x00004002800081A0L, 0x0020004040100020L, 0x0210100080200082L, 0x0112002200400811L,
			0x110C008180080004L, 0x4200020080040080L, 0x0004080400100102L, 0xA100484A00008401L, 0x0400804000800020L,
			0x0020201000C00040L, 0x2092008022001440L, 0x000100100100200CL, 0x0028020040400400L, 0x1002000402000810L,
			0x0080020001010004L, 0xA088146082000104L, 0x0040008040208000L, 0x0000400020008080L, 0x0002200141070014L,
			0x1901001004090020L, 0x00A0110008010004L, 0x1042002010040400L, 0x0001000200010004L, 0x00041A450A820014L,
			0x0801083480004100L, 0x01A0804000200080L, 0x0021004010200100L, 0x0008080010008080L, 0x0008050010080100L,
			0x0000020004008080L, 0x0821000200040100L, 0x0A00040080610200L, 0x418841008000F661L, 0x8302850020400019L,
			0x02C028401103A001L, 0x1002040900201001L, 0x0412000904102002L, 0x0202001408502302L, 0xD602021008410084L,
			0x0000050920840042L };
	private final long[] BISHOP_MAGICS = { 0x38E0602200810010L, 0x0104010812008002L, 0x85881084208A0000L,
			0x0009240104000100L, 0x2041104000820100L, 0x024A0924A0928400L, 0x0008880402200000L, 0x0001004810880880L,
			0x12B012B002080040L, 0x1002040104110208L, 0x0000080214421011L, 0x54C0080483020880L, 0x4048020210100218L,
			0x0292051042102420L, 0x0017042084042020L, 0x24C0228054026008L, 0x0E20000A82140801L, 0x0020610801040080L,
			0x0004018808001014L, 0x0048048402420800L, 0x0000811400A02001L, 0x00804012080A4004L, 0x10020800A0900808L,
			0x0001000024022201L, 0x1002200430E45008L, 0x4010846962080208L, 0x0080220410008208L, 0x0402002238008020L,
			0x2801001091004008L, 0x00010C090A008400L, 0x4900840000820810L, 0x04820A0300844100L, 0x0104210880041040L,
			0x0008226800120800L, 0x0002010112100040L, 0x1014042008040100L, 0x2248100820040020L, 0x0082408200110050L,
			0x0010090209414610L, 0x0028204042008222L, 0x080A50020800A000L, 0x1442060621080248L, 0x4108404020801000L,
			0x4400004200804800L, 0x2800401009090080L, 0x0B04154408080900L, 0x40021808110000A0L, 0x2101010102040110L,
			0x088088C808400A20L, 0x001100680A284000L, 0x00808A1100880804L, 0x8000000020A81030L, 0x01040414050C0409L,
			0x10C0218202020043L, 0x280B200C008A0500L, 0x8404240404102201L, 0x8001420C4C024000L, 0x0040704200C46000L,
			0x080020830082B040L, 0x2000124000420200L, 0x0004000040882224L, 0x0100042005010200L, 0x4000480234084204L,
			0x000A208801848380L };

	private final long[] ROOK_MASKS = new long[] { 0x000101010101017EL, 0x000202020202027CL, 0x000404040404047AL,
			0x0008080808080876L, 0x001010101010106EL, 0x002020202020205EL, 0x004040404040403EL, 0x008080808080807EL,
			0x0001010101017E00L, 0x0002020202027C00L, 0x0004040404047A00L, 0x0008080808087600L, 0x0010101010106E00L,
			0x0020202020205E00L, 0x0040404040403E00L, 0x0080808080807E00L, 0x00010101017E0100L, 0x00020202027C0200L,
			0x00040404047A0400L, 0x0008080808760800L, 0x00101010106E1000L, 0x00202020205E2000L, 0x00404040403E4000L,
			0x00808080807E8000L, 0x000101017E010100L, 0x000202027C020200L, 0x000404047A040400L, 0x0008080876080800L,
			0x001010106E101000L, 0x002020205E202000L, 0x004040403E404000L, 0x008080807E808000L, 0x0001017E01010100L,
			0x0002027C02020200L, 0x0004047A04040400L, 0x0008087608080800L, 0x0010106E10101000L, 0x0020205E20202000L,
			0x0040403E40404000L, 0x0080807E80808000L, 0x00017E0101010100L, 0x00027C0202020200L, 0x00047A0404040400L,
			0x0008760808080800L, 0x00106E1010101000L, 0x00205E2020202000L, 0x00403E4040404000L, 0x00807E8080808000L,
			0x007E010101010100L, 0x007C020202020200L, 0x007A040404040400L, 0x0076080808080800L, 0x006E101010101000L,
			0x005E202020202000L, 0x003E404040404000L, 0x007E808080808000L, 0x7E01010101010100L, 0x7C02020202020200L,
			0x7A04040404040400L, 0x7608080808080800L, 0x6E10101010101000L, 0x5E20202020202000L, 0x3E40404040404000L,
			0x7E80808080808000L };

	private final long[] BISHOP_MASKS = new long[] { 0x0040201008040200L, 0x0000402010080400L,
			0x0000004020100A00L, 0x0000000040221400L, 0x0000000002442800L, 0x0000000204085000L, 0x0000020408102000L,
			0x0002040810204000L, 0x0020100804020000L, 0x0040201008040000L, 0x00004020100A0000L, 0x0000004022140000L,
			0x0000000244280000L, 0x0000020408500000L, 0x0002040810200000L, 0x0004081020400000L, 0x0010080402000200L,
			0x0020100804000400L, 0x004020100A000A00L, 0x0000402214001400L, 0x0000024428002800L, 0x0002040850005000L,
			0x0004081020002000L, 0x0008102040004000L, 0x0008040200020400L, 0x0010080400040800L, 0x0020100A000A1000L,
			0x0040221400142200L, 0x0002442800284400L, 0x0004085000500800L, 0x0008102000201000L, 0x0010204000402000L,
			0x0004020002040800L, 0x0008040004081000L, 0x00100A000A102000L, 0x0022140014224000L, 0x0044280028440200L,
			0x0008500050080400L, 0x0010200020100800L, 0x0020400040201000L, 0x0002000204081000L, 0x0004000408102000L,
			0x000A000A10204000L, 0x0014001422400000L, 0x0028002844020000L, 0x0050005008040200L, 0x0020002010080400L,
			0x0040004020100800L, 0x0000020408102000L, 0x0000040810204000L, 0x00000A1020400000L, 0x0000142240000000L,
			0x0000284402000000L, 0x0000500804020000L, 0x0000201008040200L, 0x0000402010080400L, 0x0002040810204000L,
			0x0004081020400000L, 0x000A102040000000L, 0x0014224000000000L, 0x0028440200000000L, 0x0050080402000000L,
			0x0020100804020000L, 0x0040201008040200L };
	
	private int[] ROOK_BITS = { 64-12, 64-11, 64-11, 64-11, 64-11, 64-11, 64-11, 64-12, 64-11, 64-10, 64-10, 64-10, 64-10, 64-10, 64-10, 64-11, 64-11, 64-10, 64-10, 64-10, 64-10, 64-10, 64-10,
			64-11, 64-11, 64-10, 64-10, 64-10, 64-10, 64-10, 64-10, 64-11, 64-11, 64-10, 64-10, 64-10, 64-10, 64-10, 64-10, 64-11, 64-11, 64-10, 64-10, 64-10, 64-10, 64-10, 64-10, 64-11, 64-11, 64-10,
			64-10, 64-10, 64-10, 64-10, 64-10, 64-11, 64-12, 64-11, 64-11, 64-11, 64-11, 64-11, 64-11, 64-12 };

	private int[] BISHOP_BITS = { 64-6, 64-5, 64-5, 64-5, 64-5, 64-5, 64-5, 64-6, 64-5, 64-5, 64-5, 64-5, 64-5, 64-5, 64-5, 64-5, 64-5, 64-5, 64-7, 64-7, 64-7, 64-7, 64-5, 64-5, 64-5, 64-5, 64-7, 64-9, 64-9, 64-7, 64-5,
			64-5, 64-5, 64-5, 64-7, 64-9, 64-9, 64-7, 64-5, 64-5, 64-5, 64-5, 64-7, 64-7, 64-7, 64-7, 64-5, 64-5, 64-5, 64-5, 64-5, 64-5, 64-5, 64-5, 64-5, 64-5, 64-6, 64-5, 64-5, 64-5, 64-5, 64-5, 64-5, 64-6 };

	private long[][] BISHOP_LOOKUP = new long[64][4096];
	private long[][] ROOK_LOOKUP = new long[64][4096];
	
	
	

	public MagicNumberFinder() {
		generateBishopAttacks();
		generateRookAttacks();
	}

	public void generateBishopAttacks() {
		int[] indices = new int[64];
		for (int i = 0; i < 64; i++) {
			long bishopMask = BISHOP_MASKS[i];
			int bitCount = Long.bitCount(bishopMask);
			int total = (int) Math.pow(2, bitCount);
			for (int j = 0; j < total; j++) {
				BitBoard bishop = new BitBoard(bishopMask);
				BitBoard curBits = new BitBoard((long) j);
				int count = bishop.updateIndices(indices);
				int cursor = 0;
				bishop.reset(0L);
				for (int k = 0; k < count; k++) {
					if (curBits.get(cursor++)) {
						bishop.set(indices[k]);
					}
				}
				long cur = bishop.getBits();
				long attacks = generateBishopAttack(cur, i);
				int key = this.lookupIndexBishop(cur, i);
				if(BISHOP_LOOKUP[i][key]!=0) {
					System.out.println("Bishop Collision at pos "+i+" for "+cur);
					System.exit(-1);
				}
				BISHOP_LOOKUP[i][key] = attacks;
				
			}
		}
		for (int i = 0; i < 64; i++) {
			long bishopMask = BISHOP_MASKS[i];
			int bitCount = Long.bitCount(bishopMask);
			int total = (int) Math.pow(2, bitCount);
			for (int j = 0; j < total; j++) {
				BitBoard bishop = new BitBoard(bishopMask);
				BitBoard curBits = new BitBoard((long) j);
				int count = bishop.updateIndices(indices);
				int cursor = 0;
				bishop.reset(0L);
				for (int k = 0; k < count; k++) {
					if (curBits.get(cursor++)) {
						bishop.set(indices[k]);
					}
				}
				long cur = bishop.getBits();
				long attacks = generateBishopAttack(cur, i);
				long test = this.getBishopAttacks(i, cur);
				if(attacks !=test) {
					System.out.println("BISHOP Confusion at pos "+i+" for "+cur);
					System.exit(-1);
				}
				
			}
		}

	}

	private long generateBishopAttack(long bits, int pos) {
		long nw = 1L << pos;
		long no = 1L << pos;
		long sw = 1L << pos;
		long so = 1L << pos;

		long bishopAttack = 0L;

		for (int j = 0; j < 8; j++) {
			nw = (nw << DIR_UP_LEFT) & MASK_NOT_1_RANK & MASK_NOT_H_FILE;
			no = (no << DIR_UP_RIGHT) & MASK_NOT_1_RANK & MASK_NOT_A_FILE;
			sw = (sw >>> DIR_UP_LEFT) & MASK_NOT_8_RANK & MASK_NOT_A_FILE;
			so = (so >>> DIR_UP_RIGHT) & MASK_NOT_8_RANK & MASK_NOT_H_FILE;

			bishopAttack |= nw | no | sw | so;
			nw &= (nw & ~bits);
			no &= (no & ~bits);
			sw &= (sw & ~bits);
			so &= (so & ~bits);
		}
		// out(bishopAttack);
		return bishopAttack;
	}

	public void generateRookAttacks() {
		for (int i = 0; i < 64; i++) {
			int[] indices = new int[64];
			long rookMask = ROOK_MASKS[i];
			int bitCount = Long.bitCount(rookMask);
			int total = (int) Math.pow(2, bitCount);
			for (int j = 0; j < total; j++) {
				BitBoard rook = new BitBoard(rookMask);
				BitBoard curBits = new BitBoard((long) j);
				int count = rook.updateIndices(indices);
				rook.reset(0L);
				int cursor = 0;
				for (int k = 0; k < count; k++) {
					if (curBits.get(cursor++)) {
						rook.set(indices[k]);
					}
				}
				long cur = rook.getBits();
				long attacks = generateRookAttack(cur, i);
				int key = this.lookupIndexRook(cur, i);
				if(ROOK_LOOKUP[i][key]!=0) {
					System.out.println("ROOK Collision at pos "+i+" for "+cur);
					System.exit(-1);
				}
				
				ROOK_LOOKUP[i][key] = attacks;
				
			}
		}
		for (int i = 0; i < 64; i++) {
			int[] indices = new int[64];
			long rookMask = ROOK_MASKS[i];
			int bitCount = Long.bitCount(rookMask);
			int total = (int) Math.pow(2, bitCount);
			for (int j = 0; j < total; j++) {
				BitBoard rook = new BitBoard(rookMask);
				BitBoard curBits = new BitBoard((long) j);
				int count = rook.updateIndices(indices);
				rook.reset(0L);
				int cursor = 0;
				for (int k = 0; k < count; k++) {
					if (curBits.get(cursor++)) {
						rook.set(indices[k]);
					}
				}
				long cur = rook.getBits();
				long attacks = generateRookAttack(cur, i);
				long test = this.getRookAttacks(i, cur);
				if(attacks !=test) {
					System.out.println("ROOK Confusion at pos "+i+" for "+cur);
					System.exit(-1);
				}
				
			}
		}
	}

	private long generateRookAttack(long bits, int pos) {
		long n = 1L << pos;
		long w = 1L << pos;
		long s = 1L << pos;
		long e = 1L << pos;
		long rookAttack = 0L;
		for (int j = 1; j <= 8; j++) {
			n = (n << DIR_UP & MASK_NOT_1_RANK);
			w = (w >>> DIR_RIGHT & MASK_NOT_H_FILE);
			s = (s >>> DIR_UP & MASK_NOT_8_RANK);
			e = (e << DIR_RIGHT & MASK_NOT_A_FILE);
			rookAttack |= n | w | s | e;
			n &= (n & ~bits);
			w &= (w & ~bits);
			s &= (s & ~bits);
			e &= (e & ~bits);

		}
		// out(rookAttack);
		return rookAttack;
	}

	public long getBishopAttacks(int pos, long occ) {
		return BISHOP_LOOKUP[pos][(int) (((BISHOP_MASKS[pos]&occ) * BISHOP_MAGICS[pos]) >>> (BISHOP_BITS[pos]))];
		
	}

	public long getRookAttacks(int pos, long occ) {
		return ROOK_LOOKUP[pos][(int) (((ROOK_MASKS[pos]&occ) * ROOK_MAGICS[pos]) >>> (ROOK_BITS[pos]))];
	}

	
	
	private int lookupIndexBishop(long b, int pos) {
		return (int) ((b * BISHOP_MAGICS[pos]) >>> (BISHOP_BITS[pos]));
	}
	
	private int lookupIndexRook(long b, int pos) {
		return (int) ((b * ROOK_MAGICS[pos]) >>> (ROOK_BITS[pos]));
	}


}
