package perft.chess.perftbb;


import static perft.chess.Definitions.*;

import java.util.Arrays;

public class ContextLevel {
	
	private boolean lastLevel;
	private Move lastMove =null;;

	private final int[] fields = new int[64];
	private final long[] callBacks = new long[64];
	private final long[] moveMasks = new long[64];
	private final long[] tCallBacks = new long[64];
	
	private final long[] allOfOneColor = new long[2];//@todo WTF -stack
	private final long[] kings = new long[2];
	private final long[] knights = new long[2];
	private final long[] pawns = new long[2];
	private final int moveCount [] = new int[2];
	private long untouched=EMPTY_MASK;
	private long enPassanteMask=EMPTY_MASK;//@todo WTF -stack
	private int zobristHash=0;
	private long fcmTouched=EMPTY_MASK;

	public long[] _mLeft=new long[2];
	public long[] _mRight=new long[2];
	public long[] _mOneUp=new long[2];
	public long[] _mTwoUp=new long[2];
	
	public int castleMovesKQCount;
	public int castleMoveskqCount;
	public long castleMovesKQ;
	public long castleMoveskq;
	private int level;
	private final BBPosition position;
	
	
	

//Post move 	
	
	//Move management
	private Move[][] allMoves = new Move[64][64];
	private int[] fieldList = new int[64];  
	public int limit;

	
	private long[] idsMask = new long[64];
	private int[] idsTypeColor= new int[64];
	private int lastIndex=-1;
	private int fieldCounter=0;
	private int cursorFieldList=0;
	private int cursorMoves=0;
	private boolean rochades=true;
	public ContextLevel(BBPosition position,int level, boolean lastLevel) {
		this.position = position;
		this.level = level;
		this.lastLevel = lastLevel;
		for(int i=0;i<64;i++) {
			fieldList[i]=-1;
		}
		
	}

	
	public void init () {
//		System.out.println("Init for level:"+level);
//		System.out.println(Arrays.toString(Thread.currentThread().getStackTrace()));
		this.lastIndex =-1;
		this.cursorFieldList=0;
		this.cursorMoves=0;
		this.limit =0;
		this.fieldCounter=0;
		
	}
	public void snapshot() {
		for(int i=0;i<2;i++) {
			this.allOfOneColor[i]=position.allOfOneColor[i];
			this.kings[i]=position.kings[i];
			this.knights[i]=position.knights[i];
			
			this.pawns[i]=position.pawns[i];
			this.moveCount[i]=position.moveCount[i];
		}		
		System.arraycopy(position.tCallBacks, 0, this.tCallBacks, 0, 64);
		
		this.untouched=position.untouched;
		this.enPassanteMask=position.enPassanteMask;
		this.zobristHash=position.zobristHash;
		this.rochades = position.rochades;
		
		/*
		System.arraycopy(position.fields, 0, this.fields, 0, 64);
		System.arraycopy(position.callBacks, 0, this.callBacks, 0, 64);
		System.arraycopy(position.moveMasks, 0, this.moveMasks, 0, 64);
		*/
	}
	
	public void revertToSnapshot() {
//		System.out.println("+++++++++++++++++++++++++++++++++Reverting to Snapshot on level "+level);
		
		System.arraycopy(this.tCallBacks, 0, position.tCallBacks, 0, 64);
		
		
		int count2 = Long.bitCount(fcmTouched);
		for(int i=0;i<count2;i++) { 
			int pos = Long.numberOfTrailingZeros(fcmTouched);
			fcmTouched&=fcmTouched-1;
			position.fields[pos]=this.fields[pos];
			position.callBacks[pos]=this.callBacks[pos];
			position.moveMasks[pos]=this.moveMasks[pos];		
		}
		
		for(int i=0;i<2;i++) {
			position.allOfOneColor[i]=this.allOfOneColor[i];
			position.kings[i]=this.kings[i];
			position.knights[i]=this.knights[i];
			
			position.pawns[i]=this.pawns[i];
			position.moveCount[i]=this.moveCount[i];
		}
		position.untouched=this.untouched;
		position.enPassanteMask=this.enPassanteMask;//@todo WTF -stack		
		position.zobristHash = this.zobristHash;
		position.rochades = this.rochades;
		/*
		System.arraycopy(this.fields, 0, this.fields, 0, 64);
		System.arraycopy(this.callBacks, 0, this.callBacks, 0, 64);
		System.arraycopy(this.moveMasks, 0, position.moveMasks, 0, 64);
		*/

	}
	
	public void extractFromRawMoves(int pos, long idMask,int idTypeColor, Move[] rawMoves) {
		idsMask[pos]=idMask;
		idsTypeColor[pos]=idTypeColor;
		Move[] moves = allMoves[pos];
		fieldList[fieldCounter++]=pos;
		int retVal = Long.bitCount(idMask);
		for(int i=0;i<retVal;i++) {
			int mPos = Long.numberOfTrailingZeros(idMask);
			moves[i]=rawMoves[mPos];
			if(moves[i]==null) {
				throw new RuntimeException("There was no move at pos("+mPos+") for mask:\n"+toStr(idsMask[pos]));
			}
			idMask&= idMask- 1;			
		}
		moves[retVal]=null;//stopMove
	}
	int counter =0;
	public void addMoves(int pos, long idMask,int idTypeColor, Move[] moves) {
		//System.out.println("Counter:"+(++counter));
		idsMask[pos]=idMask;
		idsTypeColor[pos]=idTypeColor;
		/*
		// test
		int i=0;
		long bits =idMask;
		for(;moves[i]!=null;i++) {
			int cur = moves[i].getNewPos();
			if((bits&SHIFT[cur])==0) {
				System.out.println("Bug @"+pos+"for TypeColor"+idTypeColor);
				out(bits);
				out(position._occ);
				System.out.println("on i=="+cur+" there is no move!");
			//	throw new RuntimeException("there you go");
			}else {
				bits&=~SHIFT[cur];
			}
		}
		if(bits!=0) {
			System.out.println("Bug @"+pos+"for TypeColor"+idTypeColor +"("+idMask+")");
			out(bits);
			System.out.println("There are moves remaining! deleted:"+i);
			out(position.allOfOneColor[0]);
			out(position.allOfOneColor[1]);
			
			//throw new RuntimeException("there you go");
		
		}
		*/
		
		Move[]newMoves = allMoves[pos];
		int ii=0;
		for(;moves[ii]!=null;ii++) {
			newMoves[ii]=moves[ii];
		}
		newMoves[ii]=null;

		
		if(idMask!=EMPTY_MASK) {
			fieldList[fieldCounter++]=pos;
		}
	}
	
	public boolean checkForReuseMoves(int pos, long idMask,int idTypeColor) {
		if(idsMask[pos]==idMask && idsTypeColor[pos]==idTypeColor) {
			if(idMask!=EMPTY_MASK) {
				fieldList[fieldCounter++]=pos;
			}
			return true;
		}
		return false;
	}
	
	public Move[] getMoves(int pos, long idMask,int idTypeColor) {
		if(idsMask[pos]==idMask && idsTypeColor[pos]==idTypeColor) {
			return allMoves[pos];
		}
		return null;
	}
	public void resetIterator() {
		this.lastIndex =-1;
		this.cursorFieldList=0;
		this.cursorMoves=0;		
	}
	
	public void setLimit(int limit) {
		this.limit = limit;
		resetIterator();
		/*
		for(int i=0;i<=limit;i++) {
			if(allMoves[fieldList[cursorFieldList]][cursorMoves]==null) {
				cursorMoves=0;
				cursorFieldList++;
			}
			if( i==limit) {//last i is just a check and only a cursor move in case of null
				//System.out.println("Counter"+i+"/"+limit);
				break;
			}
			if(fieldList[cursorFieldList]==-1 || allMoves[fieldList[cursorFieldList]][cursorMoves]==null) {
				throw new RuntimeException("Limit Exception (fieldList["+cursorFieldList+"]=="+fieldList[cursorFieldList]+")at level:"+level+": Counter"+i+"/"+limit);
			}
			cursorMoves++;
			
		}
		resetIterator();
		*/
	}
	
	


	public Move getMove(int index) {
		//System.out.println("getting Move "+index+" in level "+level);
		if(index ==lastIndex) {
			return lastMove;
		}
		
		if(index>=limit) {
			//@todo WTF
			throw new RuntimeException("Index("+index+")Off Limit:"+limit +"in level:"+level);
		}
		lastMove = allMoves[fieldList[cursorFieldList]][cursorMoves++];
		if(index-1==lastIndex) {
			if(lastMove==null) {
				cursorMoves=0;
				cursorFieldList++;
				int fieldCursor = fieldList[cursorFieldList];
				
				if(fieldCursor==-1) {
					System.out.println("WTF FieldCursor -1 for index:"+index);
				}
				lastMove = allMoves[fieldCursor][cursorMoves++];
			}
			lastIndex = index;
		}else {
			System.out.println("Whooza Run on Level ("+level+")!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			this.resetIterator();
			for(int i=0;i<=index;i++) {
				if(allMoves[fieldList[cursorFieldList]][cursorMoves]==null) {
					cursorMoves=0;
					cursorFieldList++;
				}
				if(i==index) {//last i is just a check and only a cursor move in case of null
					break;
				}
				cursorMoves++;
			}
			lastIndex = index;
			lastMove = allMoves[fieldList[cursorFieldList]][cursorMoves++];
		}
		if(lastMove==null) {
			throw new RuntimeException("Null move for index"+index+" out of "+limit);
		}
		return lastMove;
	}
	
	public void trigger(int pos) {
		if((this.fcmTouched&SHIFT[pos])==0) {
			this.fcmTouched|=SHIFT[pos];
			this.fields[pos]= position.fields[pos];
			this.callBacks[pos]=position.callBacks[pos];
			this.moveMasks[pos]=position.moveMasks[pos];
		}
	}
}
