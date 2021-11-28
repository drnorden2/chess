package perft.chess.core;

import perft.chess.core.o.O;

public class Analyzer {
	private Position position;
	public Analyzer (Position position) {
		this.position = position;
	}

	
	private String[] getCallBackOfPosToString(int pos) {
		char[] snapshot = new char[64];
		for (int j = 0; j < 64; j++) {
			FieldCallback cb = position.fields[j].getRegisteredCallbackForPos(pos);
			if(cb!=null) {
				snapshot[j]=(char)('0'+cb.getCallbackType());
			}
		}
		return this.snapshotToString(snapshot,"CallBacks","Pos:"+pos);
	}

	
	private String[] getAttackToString(int color) {
		char[] snapshot = new char[64];
		for (int j = 0; j < 64; j++) {
			int attack = position.attackTable[color].get(j);
			if(attack!=0) {
				snapshot[j]=(char)('0'+attack);
			}
		}
		return this.snapshotToString(snapshot,"Attacks","Of Col:"+(color==Piece.COLOR_WHITE?"W":"B"));
	}

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
	

	private String[] getMovesToString() {
		char[] snapshot = new char[64];
		int count=position.allMovesLists.get(position.getLevel()).size();
		for(int i=0;i<count;i++){
			Move move = position.allMovesLists.get(position.getLevel()).get(i);
			
			if(move==null) {
				O.UT("Seriously! Move missing at "+i+" from "+count );
				continue;
			}

			snapshot[move.getOldPos()]++;			
		}
		for (int j = 0; j < 64; j++) {
			if(snapshot[j]>0) {
				snapshot[j]=(char)('0'+(snapshot[j]%10));
			}

		}
		return this.snapshotToString(snapshot,"Moves:"+(position.color==Piece.COLOR_WHITE?"W":"B"),"Size:"+count);

	}

	private String[] getPseudoMovesToString(int color) {
		char[] snapshot = new char[64];
		int count =0;
		for(int i=0;i<position.allPieces[color].size();i++){
			Piece piece = position.allPieces[color].getElement(i);
			Field field = position.fields[piece.getPosition()];
			for(int j=0;j<field.getPseudoMoveCount();j++) {
				Move move = field.getPseudoMove(j); 
				snapshot[move.getOldPos()]++;			
				count++;
			}
		}
		for (int j = 0; j < 64; j++) {
			if(snapshot[j]>0) {
				snapshot[j]=(char)('0'+(snapshot[j]%10));
			}

		}
		return this.snapshotToString(snapshot,"PMovs:"+(position.color==Piece.COLOR_WHITE?"W":"B"),"Size:"+count);

	}


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
		String[] attackW = this.getAttackToString(Piece.COLOR_WHITE);
		String[] attackB = this.getAttackToString(Piece.COLOR_BLACK);
		String[] posA = this.getCallBackOfPosToString(position.getKingPos(Piece.COLOR_WHITE));
		String[] posB = this.getCallBackOfPosToString(position.getKingPos(Piece.COLOR_BLACK));
		String[] posC = this.getMovesToString();//this.getColorAtTurn());
		String[] posD = this.getPseudoMovesToString(Piece.COLOR_WHITE);
		String[] posE = this.getPseudoMovesToString(Piece.COLOR_BLACK);
		String[] posF = this.getEnPassanteToString();
		String[] posG = this.getUntouchedToString();
		
		String str="\n";
		for(int i=0;i<board.length;i++) {
			str +=board[i] +" | " +
				attackW[i]+" | " +
				attackB[i]+" | " +
				posA[i]+" | " +
				posB[i]+" | " +
				posC[i]+" | " +
				posD[i]+" | " +
				posE[i]+" | " +
				posF[i]+" | " +
				posG[i]+"\n";
			
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
				char cur = snapshot[Move.getPos(rank, file)];
				if(cur!=0) {
					charBoard[rank][file] = cur;
				}
			}
		}
		String[] rows =new String[10];
		rows[0]=eight(headLine);
		for (int i = 0; i < 8; i++) {
			rows[i+1] = "";
			
			for (int j = 0; j < 8; j++) {
				rows[i+1]+= charBoard[i][j];
			}
		}
		rows[9]=eight(statusLine);
		return rows;
	}

	private String[] getBoardToString() {
		char[] charBoard = new char[64];
				
		String offBoardList  = getCharBoard(charBoard );

		String isCheck = ("W:" 
						+ (position.isCheck(Piece.COLOR_WHITE)?"+":" ")
						+"  B:" 
						+ (position.isCheck(Piece.COLOR_BLACK)?"+":" "));
		return this.snapshotToString(charBoard,isCheck, offBoardList);
	}
	
	
	private String getCharBoard(char[] charBoard ) {
		String offBoardList = "";
		for (int i = 0; i < 64; i++) {
			Piece piece = position.fields[i].getPiece();
			if(piece==null)continue;
			
			int position = piece.getPosition();
			if(position==-1) {
				offBoardList +=piece+", ";
				continue;
			}
				
			int rank = position % 8;
			int file = position / 8;
			int color = piece.getColor();
			int type = piece.getType();
			String typeStr = "";
			switch (type) {
			case Piece.PIECE_TYPE_PAWN:
				typeStr = color == Piece.COLOR_BLACK ? "p" : "P";
				break;
			case Piece.PIECE_TYPE_KNIGHT:
				typeStr = color == Piece.COLOR_BLACK ? "n" : "N";
				break;
			case Piece.PIECE_TYPE_BISHOP:
				typeStr = color == Piece.COLOR_BLACK ? "b" : "B";
				break;
			case Piece.PIECE_TYPE_ROOK:
				typeStr = color == Piece.COLOR_BLACK ? "r" : "R";
				break;
			case Piece.PIECE_TYPE_QUEEN:
				typeStr = color == Piece.COLOR_BLACK ? "q" : "Q";
				break;
			case Piece.PIECE_TYPE_KING:
				typeStr = color == Piece.COLOR_BLACK ? "k" : "K";
				break;

			}
			charBoard[Move.getPos(file,rank)] = typeStr.charAt(0);
				
		}
		return offBoardList;
	}
	private String eight(String str) {
		return (str+"          ").substring(0,8);
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
