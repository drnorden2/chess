package perft.chess.perftbb.gen;

import static perft.chess.Definitions.*;
import perft.chess.core.datastruct.BitBoard;
import java.util.HashMap;

public class MagicNumberFinder {
	
	private final HashMap<Long,Long>[] rookAttacks;
	private final HashMap<Long,Long>[] bishopAttacks;

	public MagicNumberFinder() {
		this.bishopAttacks = this.generateBishopAttacks();
		this.rookAttacks = this.generateRookAttacks();
	}
		
	
	public HashMap<Long, Long>[]  generateBishopAttacks(){	
		int[] indices = new int[64];
		HashMap<Long, Long>[] allAttacks = new HashMap[64];	
		for(int i=0;i<64;i++) {
			allAttacks[i] = new HashMap<Long, Long>();
			long bishopMask = BISHOP_MASKS[i];
			int bitCount = Long.bitCount(bishopMask);
			int total = (int)Math.pow(2, bitCount);
			for(int j=0;j<total;j++) {
				BitBoard bishop = new BitBoard(bishopMask);
				BitBoard curBits = new BitBoard((long)j);
				int count = bishop.updateIndices(indices);
				int cursor = 0;
				bishop.reset(0L);
				for(int k=0;k<count;k++) {
					if(curBits.get(cursor++)) {
						bishop.set(indices[k]);
					}
				}
				long cur = bishop.getBits();
				long attacks = generateBishopAttack(cur, i);
				allAttacks[i].put(cur,attacks);
				//out(bishopMask);
				//out(cur);
				//out(attacks);
			}	
		}
		return allAttacks;
	}

	private long generateBishopAttack(long bits, int pos) {
		long nw=1L <<pos;
		long ne=1L <<pos;
		long sw=1L <<pos;
		long se=1L <<pos;
		
		long bishopAttack=0L;
		for(int j=0;j<8;j++) {
			nw = (nw<<DIR_UP_LEFT & MASK_NOT_1_RANK & MASK_NOT_A_FILE);
			ne = (ne<<DIR_UP_RIGHT & MASK_NOT_1_RANK & MASK_NOT_H_FILE);
			sw = (sw>>DIR_UP_LEFT & MASK_NOT_8_RANK & MASK_NOT_A_FILE);
			se = (se>>DIR_UP_RIGHT & MASK_NOT_8_RANK & MASK_NOT_H_FILE);
			
			bishopAttack|=nw|ne|sw|se;
			nw &=(nw&~bits);
			ne &=(ne&~bits);
			sw &=(sw&~bits);
			se &=(se&~bits);
		}
		//out(bishopAttack);
		return bishopAttack;
	}		


	
	public HashMap<Long, Long>[]  generateRookAttacks(){	
		int[] indices = new int[64];
		HashMap<Long, Long>[] allAttacks = new HashMap[64];	
		for(int i=0;i<64;i++) {
			allAttacks[i] = new HashMap<Long, Long>();
			long rookMask = ROOK_MASKS[i];
			int bitCount = Long.bitCount(rookMask);
			int total = (int)Math.pow(2, bitCount);
			for(int j=0;j<total;j++) {
				BitBoard rook = new BitBoard(rookMask);
				BitBoard curBits = new BitBoard((long)j);
				int count = rook.updateIndices(indices);
				rook.reset(0L);
				int cursor = 0;
				for(int k=0;k<count;k++) {
					if(curBits.get(cursor++)) {
						rook.set(indices[k]);
					}
				}
				long cur = rook.getBits();
				long attacks = generateRookAttack(cur, i);
				allAttacks[i].put(cur,attacks);
				//out(cur);
				//out(attacks);
			}	
		}
		return allAttacks;
	}

	private long generateRookAttack(long bits, int pos) {
		long n=1L <<pos;
		long w=1L <<pos;
		long s=1L <<pos;
		long e=1L <<pos;
		long rookAttack=0L;
		for(int j=1;j<=8;j++) {
			n = (n<<DIR_UP & MASK_NOT_1_RANK);
			w = (w>>DIR_LEFT& MASK_NOT_H_FILE);
			s = (s>>DIR_UP& MASK_NOT_8_RANK);
			e = (e<<DIR_LEFT& MASK_NOT_A_FILE);
			rookAttack|=n|w|s|e;
			n &=(n&~bits);
			w &=(w&~bits);
			s &=(s&~bits);
			e &=(e&~bits);
			
		}
		//out(rookAttack);
		return rookAttack;
	}		

	
	
	public long getBishopAttacks(int pos,long occ) {
		long mask = BISHOP_MASKS[pos];
		//out(mask);
		long key = mask&occ;
		//out(key);
		return bishopAttacks[pos].get(key);
	}
	
	public long getRookAttacks(int pos,long occ) {
		long mask = ROOK_MASKS[pos];
		//out(mask);
		long key = mask&occ;
		//out(key);
		return rookAttacks[pos].get(key);
	}
			

	
	public static final long[] ROOK_MASKS = new long[] { 0x000101010101017EL, 0x000202020202027CL, 0x000404040404047AL,
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
	public static final long[] BISHOP_MASKS= new long[]{
			0x0040201008040200L,
			0x0000402010080400L,
			0x0000004020100A00L,
			0x0000000040221400L,
			0x0000000002442800L,
			0x0000000204085000L,
			0x0000020408102000L,
			0x0002040810204000L,
			0x0020100804020000L,
			0x0040201008040000L,
			0x00004020100A0000L,
			0x0000004022140000L,
			0x0000000244280000L,
			0x0000020408500000L,
			0x0002040810200000L,
			0x0004081020400000L,
			0x0010080402000200L,
			0x0020100804000400L,
			0x004020100A000A00L,
			0x0000402214001400L,
			0x0000024428002800L,
			0x0002040850005000L,
			0x0004081020002000L,
			0x0008102040004000L,
			0x0008040200020400L,
			0x0010080400040800L,
			0x0020100A000A1000L,
			0x0040221400142200L,
			0x0002442800284400L,
			0x0004085000500800L,
			0x0008102000201000L,
			0x0010204000402000L,
			0x0004020002040800L,
			0x0008040004081000L,
			0x00100A000A102000L,
			0x0022140014224000L,
			0x0044280028440200L,
			0x0008500050080400L,
			0x0010200020100800L,
			0x0020400040201000L,
			0x0002000204081000L,
			0x0004000408102000L,
			0x000A000A10204000L,
			0x0014001422400000L,
			0x0028002844020000L,
			0x0050005008040200L,
			0x0020002010080400L,
			0x0040004020100800L,
			0x0000020408102000L,
			0x0000040810204000L,
			0x00000A1020400000L,
			0x0000142240000000L,
			0x0000284402000000L,
			0x0000500804020000L,
			0x0000201008040200L,
			0x0000402010080400L,
			0x0002040810204000L,
			0x0004081020400000L,
			0x000A102040000000L,
			0x0014224000000000L,
			0x0028440200000000L,
			0x0050080402000000L,
			0x0020100804020000L,
			0x0040201008040200L};

	
}
