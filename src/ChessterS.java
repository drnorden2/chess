import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

import perft.Game;
import perft.chess.ChessGameFactory;

public class ChessterS {
	private static final String idName = "Chesster S. 0.2 nnue";
	private Game game; 
	private int depth=3;
	private ChessGameFactory factory  = new ChessGameFactory();

	
	public static void main(String[] args) {
		new ChessterS().run();
	}
		
	private void run() {	
		Scanner input = new Scanner(System.in);
		
       	boolean exit = false;
			while (!exit) {
		        String cmd=input.nextLine();
				exit = doUCI(cmd);
			}
		}
		

	private boolean doUCI(String cmd) {
		boolean exit = false;
		if ("uci".equals(cmd)) {
			cmdUCI();
		} else if (cmd.startsWith("setoption")) {
			cmdSetOption(cmd);
		} else if ("isready".equals(cmd)) {
			cmdIsReady();
		} else if ("ucinewgame".equals(cmd)) {
			cmdUCINewGame();
		} else if (cmd.startsWith("position")) {
			cmdPosition(cmd);
		} else if (cmd.startsWith("go")) {
			cmdGo();
		} else if ("print".equals(cmd)) {
			cmdPrint();
		}else if ("exit".equals(cmd)) {
			System.out.println("Bye!");
			exit = true;
		}	
		return exit;
	}

	public void cmdUCI() {
		System.out.println("id name " + idName);
		System.out.println("id author Alexandr Malura & Andre Fischer");
		// list supported options
		System.out.println("uciok");
	}

	public void cmdSetOption(String cmd) {
		// tbd
	}

	public void cmdIsReady() {
		System.out.println("readyok");
	}

	public void cmdUCINewGame() {
		// new game
	}

	public void cmdPosition(String cmd) {
		cmd = cmd.substring("position".length()).concat(" ");
		if (cmd.contains("startpos ")) {
			cmd = cmd.substring("startpos ".length());
			String fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
			game = new Game(factory,fen,depth);
		} else if (cmd.contains("fen")) {
			String fen = cmd.substring("fen ".length());
			game = new Game(factory,fen,depth);
		}
		if (cmd.contains("moves")) {
			cmd = cmd.substring("moves ".length()).trim();
			String [] moves = cmd.split(" ");
			for(int i=0;i<moves.length;i++) {
				System.out.println("!FOUND: "+moves[i]);
				game.getBoard().setMoveByMoveStr(moves[i],false,false);
			}
		}
	}

	public void cmdGo() {
		System.out.println("GOGOGOGO!");
		game.go(depth);
	}

	public void cmdPrint() {
		// BoardGeneration.drawArray(UserInterface.WP,UserInterface.WN,UserInterface.WB,UserInterface.WR,UserInterface.WQ,UserInterface.WK,UserInterface.BP,UserInterface.BN,UserInterface.BB,UserInterface.BR,UserInterface.BQ,UserInterface.BK);
	}

}
