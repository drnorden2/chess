import perft.Game;
import perft.chess.ChessGameFactory;
import perft.chess.core.o.O;
import perft.chess.perftmb.Field;


public class Main {


	public static void main(String[] args) {
		//play(4, 151107, "5R2/7k/8/8/K7/8/8/6R1 w - - 0 1");
		//lay(3, 1,"rnbqkbnr/ppp2ppp/8/3pp3/6P1/P4P2/1PPPP2P/RNBQKBNR b KQkq - 0 3");//"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
		play(3, 1,"5k2/P6R/8/6n1/5P2/8/5PPP/5RK1 w KQkq - 0 1");
	}
	private static void play(int depth, long nodes, String fen) {
		ChessGameFactory factory  = new ChessGameFactory();
		ChessGameFactory.bitBoard=true;
		System.out.println("Loading:"+fen+":");
		Game game = new Game(factory,fen,depth);
		game.play(0);
	}
}
