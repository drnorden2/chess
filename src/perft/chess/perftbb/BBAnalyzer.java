package perft.chess.perftbb;
import static perft.chess.Definitions.*;



public class BBAnalyzer {
	private BBPosition position;

	public BBAnalyzer (BBPosition position) {
		this.position = position;
	}

	
	private String[] getCallBackOfPosToString(int pos) {
		char[] snapshot = new char[64];
		for (int j = 0; j < 64; j++) {
			long cb = position.tCallBacks[j].get();
			if(((cb >> pos) & 1L)==1L) {
				snapshot[j]=(char)('1');
			}
		}
		return this.snapshotToString(snapshot,"CallBacks","Pos:"+pos);
	}

	
	private String[] getAttackToString(int color) {
		long own = position.allOfOneColor[color].get();
		long correction = position.correctors[color].get();
		char[] snapshot = new char[64];
		for (int j = 0; j < 64; j++) {
			int attack = (int)(Long.bitCount(position.tCallBacks[j].get()&own)
					- (( correction>> j) & 1 ));
			if(attack!=0) {
				snapshot[j]=(char)('0'+attack);
			}
		}
		return this.snapshotToString(snapshot,"Attacks","Of Col:"+(color==COLOR_WHITE?"W":"B"));
	}
	/*
	private String[] getEnPassanteToString() {
		char[] snapshot = new char[64];
		int enpassante = position.enPassantePos.get();
		if(enpassante!=-1) {
			for (int j = 0; j < 64; j++) {
				if(enpassante==j) {
					snapshot[j]=(char)('X');
				}
			}
		}
		return this.snapshotToString(snapshot,"EnPassante","Pos:"+enpassante);
	}
	*/

	
	private String[] getMovesToString() {
		char[] snapshot = new char[64];
		int count=position.getMoves();
		for(int i=0;i<count;i++){
			Move move = position.getMoveObj(i);
			snapshot[move.getOldPos()]++;			
		}
		for (int j = 0; j < 64; j++) {
			if(snapshot[j]>0) {
				snapshot[j]=(char)('0'+(snapshot[j]%10));
			}

		}
		return this.snapshotToString(snapshot,"Moves:"+(position.getColorAtTurn()==COLOR_WHITE?"W":"B"),"Size:"+count);
	}
	private int wtfcount;
	
	private String[] getPseudoMovesToString(int color) {
		
		char[] snapshot = new char[64];
		int count =0;
		int[] indices1 = new int[64];
		long own = position.allOfOneColor[color].get();
		long notOwn = ~own;
		int count1 = updateIndices(indices1, own);
		
		for (int i = 0; i < count1; i++) {
			int pos = indices1[i];
			long moveMask = position.allMoves[pos].get() & notOwn;
			
			int index = pos + position.fields[pos].get();
			Move[] moves = position.lookUp.getMoveMap(index);
			int[] indices2 = new int[64];
			int count2 = updateIndices(indices2, moveMask);	
			//System.out.println(i+":"+count2);
			//out(moveMask);
			for (int j = 0; j < count2; j++) {
				Move move = moves[indices2[j]];//TBD
				int movePos = move.getOldPos();
					
				snapshot[movePos]++;			
				count++;	
			}
		}
		for (int j = 0; j < 64; j++) {
			if(snapshot[j]>0) {
				snapshot[j]=(char)('0'+(snapshot[j]%10));
			}

		}
		return this.snapshotToString(snapshot,"PMovs:"+(position.getColorAtTurn()==COLOR_WHITE?"W":"B"),"Size:"+count);

	}
	
/*	
	private String[] getUntouchedToString() {
		char[] snapshot = new char[64];
		int count=position.getMoves();
		char[] charBoard = new char[64];
		String offBoardList = getCharBoard(charBoard);
		for(int i=0;i<count;i++){
			for (int j = 0; j < 64; j++) {
				if(position.fields[j].getPiece()!=null &&!position.fields[j].getPiece().isTouched()) {
					snapshot[j]=charBoard[j];
				}
			}
		}
		return this.snapshotToString(snapshot,"Untouched","");

	}
*/
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
		
		String[] posA = this.getCallBackOfPosToString(58);//position.getKingPos(Piece.COLOR_WHITE));
		String[] posB = this.getCallBackOfPosToString(23);
		String[] posC = this.getMovesToString();//this.getColorAtTurn());
		String[] posD = this.getPseudoMovesToString(COLOR_WHITE);
		String[] posE = this.getPseudoMovesToString(COLOR_BLACK);
		/*
		String[] posF = this.getEnPassanteToString();
		String[] posG = this.getUntouchedToString();
		*/
		String str="\n";
		for(int i=0;i<board.length;i++) {
			str +=board[i]
					+" | " +
				attackW[i]+" | " +
				attackB[i]+" | " +
				posA[i]+" | " +
				posB[i]+" | " +
				posC[i]+" | " +
				posD[i]+" | " +
				posE[i]+" | " +
				/*
				posF[i]+" | " +
				posG[i]+;
			*/
			"\n";

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
					charBoard[i][j] = '▒';	
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
			int count1 = updateIndices(indices, position.allOfOneColor[color].get());
			for (int i = 0; i < count1; i++) {
				int pos = indices[i];
				int typeColor = position.fields[pos].get();
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
	
	public String diffPositions(String a, String b) {
		String out = "";
		for(int i=0;i<a.length();i++) {
			if (a.charAt(i)==b.charAt(i)) {
				if(a.charAt(i) >= '0'&&a.charAt(i) <= '9') {
					out+="·";
				}else {
					out+=a.charAt(i);
				}
			}else {
				out+="█";
			}
		}
		return out;
	}
}
