package perft.chess.core.o;
import java.util.HashMap;
import java.util.Stack;
final public class O {
	
	public final static int LVL_FINE=1;
	public final static int LVL_NORMAL=2;
	public final static int LVL_GAME_OVER=3;
	
	private final static Stack<String>prefixStack = new Stack<String>();
	private static String prefix="";

	
	public final static boolean N = false;
	private final static int LVL = LVL_NORMAL;

	
	
	private static HashMap<String,Integer> instanceCount = new HashMap<String,Integer>();
	
	public static void incrInstCount(String instanceKey) {
		Integer count = instanceCount.get(instanceKey);
		if(count==null)count=0;
		instanceCount.put(instanceKey, count+1);
	}
	
	public static int getInstCount(String instanceKey) {
		Integer count = instanceCount.get(instanceKey);
		if(count==null)count=0;
		return count;
	}
			

	
	public final static void VER(String data) {
		O.UT(data,LVL_GAME_OVER);
		throw new RuntimeException("Debug level GAME OVER!" +data);
	
	}
	public final static void _ut_(String data) {
		if(O.N && O.LVL==O.LVL_FINE) O.UT(data,LVL_FINE);
	}

	public final static void  UT(String data) {
		if (O.N)O.UT(data,LVL_NORMAL);
	}

	public final static void UT(String data, int lvl) {
		if(LVL<=lvl) {
			System.out.println(prep(prefix,data));
		}
	}
	
	private final static String prep(String prefix, String data) {
		String lines[] = data.split("\\r?\\n");
		String out = "";
		for(String line:lines) {
			out+=prefix+line+"\n";
		}
		return out.substring(0,out.length()-1);
	}

	

	
	public final static void _push(String prefix) {
		if(O.N) {
			prefixStack.add(prefix);
			regeneratePrefix();
		}
	}
	public final static void _pop() {
		if(O.N) {
			prefixStack.pop();
			regeneratePrefix();
		}
	}
	
	private final static void regeneratePrefix() {
		prefix ="";
		for(String p:prefixStack){
			prefix+=p;
		}
	}
	
	public final static void ENTER(String data) {
		if(O.N) {
			O.incrInstCount(data);
			O.UT("********** ENTER " +data+" (calls:"+O.getInstCount(data)+") **********");
			O._push("* ");
		}

	}
	
	public final static void EXIT(String data) {
		EXIT(data,"");
	}
	
	public final static void EXIT(String data,String retval) {
		if(O.N) {
			O._pop();
			if(!"".equals(retval)) {
				data+=" (ret:"+retval+")";
			}
			O.UT("********** EXIT " +data+ "**********");
		}
	}
		
	public static void mainD (String[] args) {
		O.UT("********* DEBUG START ********");
		O._push("* ");
		O.UT("DEBUG Printing 1");
		O._push("! ");
		O.UT("DEBUG Printing 3");
		O._pop();
		O.UT("DEBUG Printing 2");
		O._pop();
		O.UT("********** DEBUG END **********"); 
			
		System.out.println("__________________");
		System.out.println("__________________");

		if(O.N) {
			O.UT("********* DEBUG START ********");
			O._push("* ");
			O.UT("DEBUG Printing 1");
			O._push("! ");
			O.UT("DEBUG Printing 3");
			O._pop();
			O.UT("DEBUG Printing 2");
			O._pop();
			O.UT("********** DEBUG END **********"); 
		}
		System.out.println("__________________");	
	}
}
