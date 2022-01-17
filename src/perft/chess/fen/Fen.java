package perft.chess.fen;
import static perft.chess.Definitions.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import perft.chess.core.o.O;
import perft.chess.*;


public class Fen {
	private static List<String> lib;
	private static String fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
	//private static String fen = "8/pppppppp/8/8/8/8/PPPPPPPP/8 w KQkq - 0 1";
	//private static String fen = "rnbqkbnr/ppp11ppp/8/3pp3/3PP3/8/PPP11PPP/RNBQKBNR w KQkq - 0 1";
	//private static String fen = "8/8/8/8/8/8/8/R3K2R w KQkq - 0 1";
	//private static String fen = "8/8/8/pP6/8/8/8/8 w KQkq - 0 1";
	//private static String fen = "8/p7/8/8/8/8/1P6/8 w KQkq - 0 1";
	//private static String fen = "8/p6P/8/8/8/8/8/8 w KQkq - 0 1";
	
	
	public void loadInitialPosition(Position position) {
		this.loadCustomPosition(position,fen);		
	}

	public Fen() {
		this(false);
	}
	
	
	public Fen(boolean loadLib) {
		if(loadLib) {
			if(lib ==null) {
				lib = loadLib("m8n2.txt");
			}		
		}
	}
	
	public List<String> loadLib(String libName) {
		String fileName = "../AllResources/Chesster/"+libName;
		System.out.println("Loading: "+fileName);
		List <String> list = new ArrayList<String>();
		URL url = null;
		try {
			
			url = getClass().getResource(fileName);
			
			URLConnection c = url.openConnection();
			BufferedReader reader = new BufferedReader(new InputStreamReader(c.getInputStream()));
			String line ="";
			while (line!=null) {
				line = reader.readLine();
				if (line!=null) {
					if(isFan(line)) {
						System.out.println(line);
						list.add(line);
					}
				}else{
					reader.close();
				}
	
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("\n\n\n");
		return list;
	}
	private boolean isFan(String line) {
		int counter =0;
		int cur =line.indexOf('/',0);
		while(cur!=-1) {
			counter ++;
			cur = line.indexOf('/',cur+1);
		}
		return counter>=6;
	}
		
	public void loadRandomPosition(Position position) {
		String fen = lib.get((int)(Math.random()*lib.size()));
		System.out.println("Random Pick:"+fen);
		loadCustomPosition(position,fen);
	}	
	
	
	public void loadCustomPosition(Position position,String fen) {
		int[][] typeCounter = new int[2][6];
		int[] typeOffset = new int[] {0,8,10,12,14,15};
		 
		
		int end = -1;
		for (int i = 0; i < 8; i++) {
			end = fen.indexOf("/");
			if (end == -1) {
				end = fen.indexOf(" ");
			}
			String s = fen.substring(0, end);
			fen = fen.substring(end + 1, fen.length()).trim();
			//System.out.println(s);
			int cursor =0;
			for(int j=0;j<s.length();j++) {
				char c = s.charAt(j);
				if((c-'0')>0 &&(c-'0')<=8) {
					cursor+=(c-'0');
					//System.out.println("Cursor"+cursor);
				}else {
					int file = cursor;
					int rank = 7-i;
					int[] piece = pieceForChar(c);
					int type = piece[0];
					int color = piece[1];
					typeCounter[color][type]++;
					int pos = getPosForFileRank(file,rank);
					//System.out.println ("color:"+color+" type:" +type+" index:"+pos);
					
					position.initialAddToBoard(color, type, pos);
					int  index = 64 * (PIECE_TYPE_ANY * 2 + color)+pos;
					//System.out.println(""+position.toString());
					cursor++;
				}
			}
		}


		// turn
		end = fen.indexOf(" ");
		String turn = fen.substring(0, end);
		fen = fen.substring(end + 1, fen.length()).trim();
		//System.out.println(turn);
		if("w".equalsIgnoreCase(turn)) {
			position.initialTurn(COLOR_WHITE);
		}else {
			position.initialTurn(COLOR_BLACK);
		}
		
		
		// Rochade
		end = fen.indexOf(" ");
		String rochade = fen.substring(0, end);
		fen = fen.substring(end, fen.length()).trim();
		
		//System.out.println(rochade);
		for(int j=0;j<rochade.length();j++) {
			char c = rochade.charAt(j);
			switch(c) {
				case 'K':
					position.initialUntouched(_1, _H);
					position.initialUntouched(_1, _E);
					break;
				case 'Q':
					position.initialUntouched(_1, _A);
					position.initialUntouched(_1, _E);
					break;
				case 'k':
					position.initialUntouched(_8, _H);
					position.initialUntouched(_8, _E);
					break;
				case 'q':
					position.initialUntouched(_8, _A);
					position.initialUntouched(_8, _E);
					break;
			}
		}

		
		
		// en Passant
		int enpassantePos=-1;
		end = fen.indexOf(" ");
		if(end==-1) {
			end = fen.length();
		}
		String enpassant = fen.substring(0, end);
		fen = fen.substring(end, fen.length()).trim();
		//System.out.println(enpassant);
		
		if(!"-".equals(enpassant) ){
			int file = enpassant.charAt(0)-'a';
			int rank = enpassant.charAt(1)-'1'; 
			enpassantePos = getPosForFileRank(file,rank) ;
			position.initialEnPassantePos(enpassantePos);
		}
		
		position.initialEval();
		
	}

	
	public int[] pieceForChar(char c) {
		int[] piece =new int[2];//type and color	
		int TYPE=0;
		int COLOR =1;
		switch(c) {
			case 'P':
				piece[TYPE]= PIECE_TYPE_PAWN;
				piece[COLOR]= COLOR_WHITE;
				break;
			case 'N':
				piece[TYPE]= PIECE_TYPE_KNIGHT;
				piece[COLOR]= COLOR_WHITE;
				break;
			case 'B':
				piece[TYPE]= PIECE_TYPE_BISHOP;
				piece[COLOR]= COLOR_WHITE;
				break;
			case 'R':
				piece[TYPE]= PIECE_TYPE_ROOK;
				piece[COLOR]= COLOR_WHITE;
			break;
			case 'Q':
				piece[TYPE]= PIECE_TYPE_QUEEN;
				piece[COLOR]= COLOR_WHITE;
			break;
			case 'K':
				piece[TYPE]= PIECE_TYPE_KING;
				piece[COLOR]= COLOR_WHITE;
			break;
			case 'p':
				piece[TYPE]= PIECE_TYPE_PAWN;
				piece[COLOR]= COLOR_BLACK;
				break;
			case 'n':
				piece[TYPE]= PIECE_TYPE_KNIGHT;
				piece[COLOR]= COLOR_BLACK;
				break;
			case 'b':
				piece[TYPE]= PIECE_TYPE_BISHOP;
				piece[COLOR]= COLOR_BLACK;
				break;
			case 'r':
				piece[TYPE]= PIECE_TYPE_ROOK;
				piece[COLOR]= COLOR_BLACK;
			break;
			case 'q':
				piece[TYPE]= PIECE_TYPE_QUEEN;
				piece[COLOR]= COLOR_BLACK;
			break;
			case 'k':
				piece[TYPE]= PIECE_TYPE_KING;
				piece[COLOR]=                                                                               COLOR_BLACK;
			break;
			
		}
		return piece;
	}


	
		
	/*
	public void printFEN(Position position, Move lastMove) {
		System.out.println(getFEN(config,lastMove));
	}	
	
	public String getFEN(Configuration config, Move lastMove) {
		StringBuffer sb = new StringBuffer();
		// rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1
		for (int i = 0; i < 8; i++) {
			int gap = 0;
			for (int j = 0; j < 8; j++) {
				Stone stone = config.getBoardMap().getStone(new Coordinate(i, j));
				if (stone == null) {
					gap++;
				} else {
					if (gap != 0) {
						sb.append(gap);
						gap = 0;
					}
					int type = stone.getType();
					String s = null;
					switch (type) {
					case Stone.TYPE_PEON:
						s = "p";
						break;
					case Stone.TYPE_BISHOP:
						s = "b";
						break;
					case Stone.TYPE_HORSE:
						s = "n";
						break;
					case Stone.TYPE_ROOK:
						s = "r";
						break;
					case Stone.TYPE_QUEEN:
						s = "q";
						break;
					case Stone.TYPE_KING:
						s = "k";
						break;
					}

					if (stone.getColor() == Stone.WHITE) {
						s = s.toUpperCase();
					}
					sb.append(s);
				}
			}
			if (gap != 0) {
				sb.append(gap);
			}
			sb.append("/");
		}

		// Move?
		if (config.getBoardMap().getCurColor() == Stone.WHITE) {
			sb.append(" w ");
		} else {
			sb.append(" b ");
		}

		boolean wK =  config.getBoardMap().isUntouched(new Coordinate(Game._1, Game._E));
		boolean bK = config.getBoardMap().isUntouched(new Coordinate(Game._8, Game._E));
		boolean wTq = config.getBoardMap().isUntouched(new Coordinate(Game._1, Game._A));
		boolean wTk = config.getBoardMap().isUntouched(new Coordinate(Game._1, Game._H));
		boolean bTq = config.getBoardMap().isUntouched(new Coordinate(Game._8, Game._A));
		boolean bTk = config.getBoardMap().isUntouched(new Coordinate(Game._8, Game._H));
		
		String rochade ="";
		if (wK) {
			if (wTk) {
				rochade+="K";
			}
			if (wTq) {
				rochade+="Q";
			}
		}

		if (bK) {
			if (bTk) {
				rochade+="k";
			}
			if (bTq) {
				rochade+="q";
			}
		}
		if("".equals(rochade)) {
			rochade ="-";
		}
		
		sb.append(" " + rochade);
		
		String enpassant = "-";
		if (lastMove != null) {
			Coordinate target = lastMove.getTarget();
			Stone stone = config.getBoardMap().getStone(target);
			if (stone.getType() == Stone.TYPE_PEON) {
				Coordinate source = lastMove.getSource();
				if (stone.getColor() == Stone.WHITE) {
					if (source.getX() == Stone._2 && target.getX() == Stone._4) {
						enpassant = "" + (char) ('a' + (char) source.getY()) + "3";
					}
				} else {
					if (source.getX() == Stone._7 && target.getX() == Stone._5) {
						enpassant = "" + (char) ('a' + (char) source.getY()) + "6";
					}
				}
			}
		}
		sb.append(" " + enpassant);
		return sb.toString();
	}
	*/
}
