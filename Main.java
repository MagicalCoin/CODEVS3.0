import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;



public class Main {
	
	final int MAX_H = 20;
	final int MAX_W = 20;
	
	public Cal cal=new Cal(MAX_W,MAX_H);
	
	public static void main(String[] args) {

		Main codevs3 = new Main();
		
		// AIの名前を出力
		System.out.println("MagicAl");
		System.out.flush();

		while (codevs3.input()) { // 正しい入力が受け取れる間ループする。入力が受け取れない場合 false を返すのでループを抜ける。
			//codevs3.solve();
			int out[]=codevs3.cal.calculate();
			String str;
			//codevs3.cal.pw.flush();
			//数字を文字列に変換する
			str=codevs3.output(out[0]);
			System.out.println(str);
			str=codevs3.output(out[1]);
			System.out.println(str);
			
			System.out.flush();
		}
		//codevs3.cal.pw.close();
		//System.out.println("erroer");
		return;
	}
	boolean input() {
		Scanner scanner = new Scanner(System.in);

		char[] str;

		int time=scanner.nextInt();
		//int time=0;
		int turn = scanner.nextInt();
		int MAX_TURN = scanner.nextInt();
		int my_id = scanner.nextInt();
		int Y = scanner.nextInt();
		int X = scanner.nextInt();


		char[][] field = new char[MAX_H][MAX_W];
		for (int y = 0; y < Y; y++) {
			str = scanner.next().toCharArray();
			for (int x = 0; x < X; x++) {
				field[y][x] = str[x];
			}
		}

		//my_idによってplayerの保存する順番を変える
		int offset=0;
		if(my_id==1)offset=2;
		int ch_size = cal.playerSize = scanner.nextInt();
		for (int i = 0; i < ch_size; i++) cal.playerUpdate((i+offset)%4,scanner.nextInt(),scanner.nextInt(),scanner.nextInt(),scanner.nextInt(),scanner.nextInt(),scanner.nextInt());
		
		int magic_size = cal.magicSize = scanner.nextInt();
		for (int i = 0; i < magic_size; i++) cal.magicUpdate(i,scanner.nextInt(),scanner.nextInt(),scanner.nextInt(),scanner.nextInt(),scanner.nextInt());

		int item_size = cal.itemSize = scanner.nextInt();
		for (int i = 0; i < item_size; i++) cal.itemUpdate(i,scanner.next().toCharArray(),scanner.nextInt(),scanner.nextInt());
		
		cal.mapUpdate(time,turn, MAX_TURN,my_id, X, Y, field);
		
		String endStr = scanner.next(); // 文字列「END」を読む

		if (endStr.equals("END")) {
			return true;
		} else {
			return false;
		}
	}
	public String output(int a){
		String str="NONE";
		switch (a%5) {
		case 1:str="LEFT";break;
		case 2:str="UP";break;
		case 3:str="RIGHT";break;
		case 4:str="DOWN";break;
		default:break;
		}
		if(a>=50)str+=" MAGIC "+a/10;
		else if(a>=5)str+=" MAGIC 5";
		return str;
	}
}
