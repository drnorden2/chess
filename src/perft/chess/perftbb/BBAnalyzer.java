package perft.chess.perftbb;
import static perft.chess.Definitions.*;

import perft.chess.core.baseliner.BLIndexedList;
import perft.chess.core.datastruct.ArrayStack;



public class BBAnalyzer {
	private BBPosition position;
	private static boolean isAlarm = false;
	public BBAnalyzer (BBPosition position) {
		this.position = position;
	}
	public boolean checkAlarm() {
		return isAlarm;
	}
	
	private String[] getCallBackOfPosToString(int pos) {
		char[] snapshot = new char[64];
		for (int j = 0; j < 64; j++) {
			long cb = position.tCallBacks[j];
			if(((cb >>> pos) & 1L)==1L) {
				snapshot[j]=(char)('1');
			}
		}
		return this.snapshotToString(snapshot,"CallBacks","Pos:"+pos);
	}

	private String[] getMovesOfPosToString(int pos) {
		char[] snapshot = new char[64];
		long moves = 0L;///position.allMoves[pos];

		for (int j = 0; j < 64; j++) {
			if(((moves>>> j) & 1L)==1L) {
				snapshot[j]=(char)('1');
			}
		}
		return this.snapshotToString(snapshot,"CallBacks","Pos:"+pos);
	}

	
	private String[] getAttackToString(int color) {
		long own = position.allOfOneColor[color];
		char[] snapshot = new char[64];
		for (int j = 0; j < 64; j++) {
			int attack = (int)(Long.bitCount(position.tCallBacks[j]&own));
			if(attack<0) {
				System.out.println("WTF at pos ("+j+")!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			
			}
			if(attack!=0) {
				snapshot[j]=(char)('0'+attack);
			}
		}
		
		return this.snapshotToString(snapshot,"Attacks","Of Col:"+(color==COLOR_WHITE?"W":"B"));
	}

	private String[] getRawAttackToString(int color) {
		long own = position.allOfOneColor[color];
		char[] snapshot = new char[64];
		for (int j = 0; j < 64; j++) {
			int attack = (int)(Long.bitCount(position.tCallBacks[j]&own));
			if(attack!=0) {
				snapshot[j]=(char)('0'+attack);
			}
		}
		
		return this.snapshotToString(snapshot,"RawAttac","Of Col:"+(color==COLOR_WHITE?"W":"B"));
	}
	
	private String[] getEnPassanteToString() {
		char[] snapshot = new char[64];
		long enpMask = position.enPassanteMask;
		int enpassante =-1;
		if(enpMask!=0) {
			enpassante = Long.numberOfTrailingZeros(enpMask);
			if(enpassante!=-1) {
				for (int j = 0; j < 64; j++) {
					if(enpassante==j) {
						snapshot[j]=(char)('X');
					}
				}
			}
		}
		return this.snapshotToString(snapshot,"EnPassante","Pos:"+enpassante);
	}

	
	private String[] getMovesToString() {
		char[] snapshot = new char[64];
		int count=position.getMoveCount();
		
		for(int i=0;i<count;i++){
			Move move = position.getMove(i);
			snapshot[move.getOldPos()]++;			
		}
		for (int j = 0; j < 64; j++) {
			if(snapshot[j]>0) {
				snapshot[j]=(char)('0'+(snapshot[j]%10));
			}

		}
		return this.snapshotToString(snapshot,"Moves:"+(position.getColorAtTurn()==COLOR_WHITE?"W":"B"),"Size:"+count);
	}
	
	private String[] getPseudoMovesToString(int color) {
		char[] snapshot = new char[64];
		
  		long bits = position.allOfOneColor[color];
  		int retVal = Long.bitCount(bits);
  		int total = 0;
  		for(int i=0;i<retVal;i++) {
  			int pos = Long.numberOfTrailingZeros(bits);
			bits &= bits - 1;
			long moveMask = position.moveMasks[pos];
			int retVal2 = Long.bitCount(moveMask);
	  		total +=retVal2 ;
	  		snapshot[pos]=(char)retVal2;
	  		
		}

		for (int j = 0; j < 64; j++) {
			if(snapshot[j]>0) {
				snapshot[j]=(char)('0'+(snapshot[j]%10));
			}
		}
		return this.snapshotToString(snapshot,"PMovs:"+(position.getColorAtTurn()==COLOR_WHITE?"W":"B"),"Size:"+total);
	}
	

	private String[] getUntouchedToString() {
		char[] snapshot = new char[64];
		long untouched = position.untouched;
		
		char[] charBoard = new char[64];
		getCharBoard(charBoard );
		for (int j = 0; j < 64; j++) {
			if(((untouched >>> j) & 1L)==1L) {
				snapshot[j]=charBoard[j];
			}
		}
		return this.snapshotToString(snapshot,"Untouched","");

	}
	private boolean compareSnapshots(int[][] snapshot1,int[][] snapshot2) {
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 64; j++) {
				if(snapshot1[i][j]!=snapshot2[i][j]){
					return false;
				}
			}
		}
		return true;
 
	}
	@Override
	public String toString() {
		System.out.println("EXPENSIVE");
		Long.numberOfTrailingZeros(0L);
		String[] board = this.getBoardToString();
	
		String[] attackW = this.getAttackToString(COLOR_WHITE);
		String[] attackB = this.getAttackToString(COLOR_BLACK);
		
		String[] posA = this.getCallBackOfPosToString(4);//position.getKingPos(Piece.COLOR_WHITE));
		String[] posB = this.getMovesOfPosToString(4);
		String[] posC = this.getMovesToString();//this.getColorAtTurn());
		String[] posD = this.getPseudoMovesToString(COLOR_WHITE);
		String[] posE = this.getPseudoMovesToString(COLOR_BLACK);
		
		String[] posF = this.getEnPassanteToString();
		String[] posG = this.getUntouchedToString();
		
		String str="\n";
		for(int i=board.length-1;i>=0;i--) {
			str +=board[i]
					+" | " +
				attackW[i]+" | " +
				attackB[i]+" | " +
				posA[i]+" | " +
				posB[i]+" | " +
				posC[i]+" | " +
				posD[i]+" | " +
				posE[i]+" | " +
				posF[i]+" | " +
				posG[i] +"\n";

		}
		
		return str+"\n";
	}
	public String toStringDebug() {
		System.out.println("EXPENSIVE");
		Long.numberOfTrailingZeros(0L);
		String[] board = this.getBoardToString();
	
		
		String str="\n";
		for(int i=0;i<board.length;i++) {
			str +=board[i]+"\n";
		}
		
		return str+"\n";
	}

	
	private String[] snapshotToString(char[] snapshot,String headLine,String statusLine) {
		char[][] charBoard = new char[8][8];
		
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if((i+j)%2==0) {
					charBoard[i][j] = ' ';
				}else {
					charBoard[i][j] = 'W';	
				}
			}
		}
		for(int file =0;file<8;file++) {
			for (int rank =0;rank<8;rank++) {
				char cur = snapshot[getPosForFileRank(file,rank)];
				if(cur!=0) {
					charBoard[rank][file] = cur;
				}
			}
		}
		String[] rows =new String[10];
		rows[0]="   "+eight(headLine);
		for (int i = 0; i < 8; i++) {
			rows[i+1] = this.two(""+i*8)  +"|";
			
			for (int j = 0; j < 8; j++) {
				rows[i+1]+= charBoard[i][j];
			}
		}
		rows[9]="   "+eight(statusLine);
		return rows;
	}

	private String[] getBoardToString() {
		char[] charBoard = new char[64];
				
		String offBoardList  = getCharBoard(charBoard );
/*
		String isCheck = ("W:" 
						+ (position.isCheck(COLOR_WHITE)?"+":" ")
						+"  B:" 
						+ (position.isCheck(COLOR_BLACK)?"+":" "));
*/
		String isCheck = "tbd";
		return this.snapshotToString(charBoard,isCheck, offBoardList);
	}
	

	
	private String getCharBoard(char[] charBoard ) {
		int[] indices = new int[64];
		
		for(int color=0;color<2;color++) {
			int count1 = updateIndices(indices, position.allOfOneColor[color]);
			if(count1==0) {
				this.isAlarm=true;
			}
			for (int i = 0; i < count1; i++) {
				int pos = indices[i];
				int typeColor = position.fields[pos];
				String typeStr = "";
				switch (typeColor) {
				case PIECE_TYPE_WHITE_PAWN:
					typeStr = "P";
					break;
				case PIECE_TYPE_WHITE_KNIGHT:
					typeStr = "N";
					break;
				case PIECE_TYPE_WHITE_BISHOP:
					typeStr = "B";
					break;
				case PIECE_TYPE_WHITE_ROOK:
					typeStr = "R";
					break;
				case PIECE_TYPE_WHITE_QUEEN:
					typeStr = "Q";
					break;
				case PIECE_TYPE_WHITE_KING:
					typeStr = "K";
					break;
				case PIECE_TYPE_BLACK_PAWN:
					typeStr = "p";
					break;
				case PIECE_TYPE_BLACK_KNIGHT:
					typeStr = "n";
					break;
				case PIECE_TYPE_BLACK_BISHOP:
					typeStr = "b";
					break;
				case PIECE_TYPE_BLACK_ROOK:
					typeStr = "r";
					break;
				case PIECE_TYPE_BLACK_QUEEN:
					typeStr = "q";
					break;
				case PIECE_TYPE_BLACK_KING:
					typeStr = "k";
					break;
				default:
					typeStr = "X";
					System.out.println("Alarm, illegal typeColor: "+typeColor);
					System.out.println("position.allOfOneColor["+color+"]="+position.allOfOneColor[color]); 
					out(position.allOfOneColor[color]);
					isAlarm =true;
					break;
				}
				charBoard[pos] = typeStr.charAt(0);
			}
		}
		return "";
	}
	
	private String eight(String str) {
		return (str+"          ").substring(0,8);
	}
	private String two(String str) {
		return (str+"          ").substring(0,2);
	}
	
	public static String diffPositions(String a, String b) {
		String out = "";
		for(int i=0;i<a.length();i++) {
			if (a.charAt(i)==b.charAt(i)) {
				out+=a.charAt(i);
				
			}else {
				out+="█";
			}
		}
		return out;
	}
}
