import java.io.PrintWriter;

import javax.management.loading.PrivateClassLoader;
import javax.swing.JSpinner.NumberEditor;
import javax.swing.text.StyledEditorKit.ForegroundAction;

import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.PUBLIC_MEMBER;

public class Map {
	public int X=0;
	public int Y=0;
	public int mapTurn=0;
	public Boolean softRest=false;
	//public int SOFT_TURN=5;//ソフトブロックを加味した時の移動にかかる時間
	public PrintWriter pw;
	public Square map[][] = new Square[Y][X];
	public Player player[] = new Player[4];
	public int playerSize=0;
	public Magic magic[] = new Magic[100];
	public int magicSize=0;
	public Item item[] = new Item[100];
	public int itemSize=0;
	public class Square {
		public Boolean hard;
		public Boolean soft;
		public int bomb;
		public int item;
		public Boolean cross;//p1が通過できるか
		public int walk;//p1の移動範囲と距離
		public int e1dis;//e1との距離
		public int e2dis;//e2との距離
		public int edis;//e1と2を合わせた距離
		public int nearedis;//近い方のenemyとの距離
		public int distance;//汎用の距離を保存する関数
		public int[] burst=new int[10];//このタイル上を爆発するターン数を記録する
		public Square() {
			clearAll();
		}
		public void clearAll(){
			hard=soft=cross=false;
			bomb=item=-1;
			walk=e1dis=e2dis=nearedis=distance=1000;
			edis=2000;
			for(int i=0;i<burst.length;i++)burst[i]=-1;
			//bombTurn=0;
		}
		public void clearWalk(){
			walk=1000;
		}
		public void clearBurst(){
			for(int i=0;i<burst.length;i++)burst[i]=-1;
		}
		public void clearBomb(){
			for(int i=0;i<burst.length;i++)bomb=-1;
		}
		public void clearEDis(){
			walk=e1dis=e2dis=nearedis=distance=1000;;
		}
		public void clearDis(){
			distance=1000;;
		}
		public void squareClone(Square s){
			this.hard=s.hard;
			this.soft=s.soft;
			this.bomb=s.bomb;
			this.item=s.item;
			this.cross=s.cross;
			this.walk=s.walk;
			this.e1dis=s.e1dis;
			this.e2dis=s.e2dis;
			this.edis=s.edis;
			this.nearedis=s.nearedis;
			this.distance=s.distance;
			for(int i=0;i<s.burst.length;i++)this.burst[i]=s.burst[i];
		}
	}
	public Map(int MAX_W,int MAX_H,PrintWriter pw){
		map= new Square[MAX_H][MAX_W];
		for(int i=0;i<MAX_W;i++)for(int j=0;j<MAX_H;j++)map[j][i]=new Square();
		for(int i=0;i<4;i++)this.player[i]=new Player();
		for(int i=0;i<100;i++)this.magic[i]=new Magic();
		for(int i=0;i<100;i++)this.item[i]=new Item();
		this.pw=pw;
		//this.pw.close();
	}
	public void clearBurst(){
		for(int i=0;i<X;i++)for(int j=0;j<Y;j++)this.map[j][i].clearBurst();
	}
	public void clearBomb(){
		this.magicSize=0;
		for(int i=0;i<X;i++)for(int j=0;j<Y;j++)this.map[j][i].clearBomb();
	}
	public void clearWalk(){
		for(int i=0;i<X;i++)for(int j=0;j<Y;j++)this.map[j][i].clearWalk();
	}
	public void clearEDis(){
		for(int i=0;i<X;i++)for(int j=0;j<Y;j++)this.map[j][i].clearEDis();
	}
	public void clearDis(){
		for(int i=0;i<X;i++)for(int j=0;j<Y;j++)this.map[j][i].clearDis();
	}
	public void mapClone(Map m){
		this.X=m.X;
		this.Y=m.Y;
		this.mapTurn=m.mapTurn;
		this.softRest=m.softRest;
		this.pw=m.pw;
		for(int i=0;i<X;i++)for(int j=0;j<Y;j++)this.map[j][i].squareClone(m.map[j][i]);
		for(int i=0;i<m.player.length;i++)this.player[i].playerClone(m.player[i]);
		this.playerSize=m.playerSize;
		for(int i=0;i<m.magic.length;i++)this.magic[i].magicClone(m.magic[i]);
		this.magicSize=m.magicSize;
		for(int i=0;i<m.item.length;i++)this.item[i].itemClone(m.item[i]);
		this.itemSize=m.itemSize;
	}
	public void mapUpdate(int X,int Y,int my_id,char[][] field,Player player[],int playerSize,Magic magic[],int magicSize,Item item[],int itemSize,int turn){
		this.X=X;
		this.Y=Y;
		this.mapTurn=turn;
		this.player=player;
		this.playerSize=playerSize;
		this.magic=magic;
		this.magicSize=magicSize;
		this.item=item;
		this.itemSize=itemSize;
		this.softRest=false;
		char m[][]=new char[Y][X];
		for(int i=0;i<X;i++)for(int j=0;j<Y;j++)m[j][i]=field[j][i];
		for(int i=0;i<X;i++)for(int j=0;j<Y;j++)map[j][i].clearAll();
		for(int i=0;i<X;i++){
			for(int j=0;j<Y;j++){
				switch (m[j][i]) {
				case '#':{map[j][i].hard=true; break;}
				case '+':{map[j][i].soft=true; softRest=true; break;}
				//case '@':{map[j][i].soft=true; break;}
				default:{map[j][i].cross=true;break;}
				}
			}
		}
		//爆発の計算をやりやすいようにボムの並び替えを行う
		if(this.magicSize>=2)this.magicSort();
		//爆弾のある場所は進行不可にする  同時に重なっているボムを一つにまとめる
		for(int i=0;i<this.magicSize;i++){
			//そこにボムがないならこのボムのidをそのマスに入れる
			if(map[this.magic[i].y][this.magic[i].x].bomb==-1){
				map[this.magic[i].y][this.magic[i].x].bomb=i;
				map[this.magic[i].y][this.magic[i].x].cross=false;
			}
			//すでにそこにボムが置いてあるならターンを最小にし火力を最大に合わせる
			else{
				//やりやすいように変数に入れる
				int id=map[this.magic[i].y][this.magic[i].x].bomb;
				if(this.magic[id].turn>this.magic[i].turn)this.magic[id].turn=this.magic[i].turn;
				if(this.magic[id].pow<this.magic[i].pow)this.magic[id].pow=this.magic[i].pow;
				//このボムは計算に不要になるためburstをtrueにしておく
				this.magic[i].bursted=true;
				//this.pw.println("Link! "+id+" "+i);
			}
			//どのキャラがボムを置いたか判定して置いてある個数をカウントする 青サイドになっても対応できるようofsetをつかって調整する
			int offset=0;
			if(my_id==1)offset=2;
			this.player[(this.magic[i].id+offset)%4].put++;
		}
		//アイテムの場所を更新する
		for(int i=0;i<itemSize;i++){
			map[item[i].y][item[i].x].item=item[i].type;
		}
		limitBlock();
		//this.magicPrint();
		//this.mapPrint(m);
		//this.mapPrint();
	}
	//爆弾をターン数順に並び替える 　単純ソート
	public void magicSort(){
		Magic tmp=new Magic();
		for(int i=0;i<this.magicSize-1;i++){
			int min=this.magic[i].turn;
			int min_i=i;
			Boolean change=false;
			for(int j=i+1;j<this.magicSize;j++){
				if(min>this.magic[j].turn){
					min=this.magic[j].turn;
					min_i=j;
					change=true;
				}
			}
			//交換する必要があるなら交換する
			if(change){
				tmp.magicClone(this.magic[i]);
				this.magic[i].magicClone(this.magic[min_i]);
				this.magic[min_i].magicClone(tmp);
			}
		}
	}
	//決勝用にハードブロックを敷き詰めていく
	public void limitBlock(){
		int point_x[]={1,2,3,4,5,6,7,8,9,10,11,12,13,13,13,13,13,13,13,13,13,13,13,12,11,10,9 ,8 ,7 ,6 ,5 ,4 ,3 ,2 ,1 ,1 ,1,1,1,1,1,1,1,1,3,5,7,9,11,12,12,12,12,11,9 ,7 ,5 ,3 ,2 ,2 ,2 ,2 ,3,4,5,6,7,8,9,10,11,11,11,11,11,11,11,10,9,8,7,6,5,4,3,3,3,3,3,3,5,7,9,10,10,9,7,5,4,4,5};
		int point_y[]={1,1,1,1,1,1,1,1,1,1 ,1 ,1 ,1 ,2 ,3 ,4 ,5 ,6 ,7 ,8 ,9 ,10,11,11,11,11,11,11,11,11,11,11,11,11,11,10,9,8,7,6,5,4,3,2,2,2,2,2,2 ,3 ,5 ,7 ,9 ,10,10,10,10,10,9 ,7 ,5 ,3 ,3,3,3,3,3,3,3,3 ,3 ,4 ,5 ,6 ,7 ,8 ,9 ,9 ,9,9,9,9,9,9,9,8,7,6,5,4,4,4,4,5 ,7 ,8,8,8,7,5,5};
		//ブロックの降るタイミングを保存
		int fallTurn[]=new int[101];
		for(int i=0;i<101;i++)fallTurn[i]=300+i*2;
		//ブロックに追い込まれてしまう場合を考えて角近くの2パターンを同時に塞がるとかんがえるようにする
		fallTurn[43]=300+42*2;
		fallTurn[89]=300+88*2;
		//turnの入力は0から始まるので1足して計算する
		for(int i=0;i<101;i++)if(fallTurn[i]<=mapTurn){
			//map[point_y[i]][point_x[i]].hard=true;ボムの爆発に干渉してしまうためかかない
			map[point_y[i]][point_x[i]].cross=false;
		}
	}
	//決勝用にハードブロックを敷き詰めていく　もしキャラがかぶっていればfalseを返す
	public Boolean limitBlock(int x1,int y1,int x2,int y2){
		int point_x[]={1,2,3,4,5,6,7,8,9,10,11,12,13,13,13,13,13,13,13,13,13,13,13,12,11,10,9 ,8 ,7 ,6 ,5 ,4 ,3 ,2 ,1 ,1 ,1,1,1,1,1,1,1,1,3,5,7,9,11,12,12,12,12,11,9 ,7 ,5 ,3 ,2 ,2 ,2 ,2 ,3,4,5,6,7,8,9,10,11,11,11,11,11,11,11,10,9,8,7,6,5,4,3,3,3,3,3,3,5,7,9,10,10,9,7,5,4,4,5};
		int point_y[]={1,1,1,1,1,1,1,1,1,1 ,1 ,1 ,1 ,2 ,3 ,4 ,5 ,6 ,7 ,8 ,9 ,10,11,11,11,11,11,11,11,11,11,11,11,11,11,10,9,8,7,6,5,4,3,2,2,2,2,2,2 ,3 ,5 ,7 ,9 ,10,10,10,10,10,9 ,7 ,5 ,3 ,3,3,3,3,3,3,3,3 ,3 ,4 ,5 ,6 ,7 ,8 ,9 ,9 ,9,9,9,9,9,9,9,8,7,6,5,4,4,4,4,5 ,7 ,8,8,8,7,5,5};
		//ブロックの降るタイミングを保存
		int fallTurn[]=new int[101];
		for(int i=0;i<101;i++)fallTurn[i]=300+i*2;
		//ブロックに追い込まれてしまう場合を考えて角近くの2パターンを同時に塞がるとかんがえるようにする
		fallTurn[43]=300+42*2;
		fallTurn[89]=300+88*2;
		//turnの入力は0から始まるので1足して計算する
		for(int i=0;i<101;i++)if(fallTurn[i]<=mapTurn){
			//map[point_y[i]][point_x[i]].hard=true;ボムの爆発に干渉してしまうためかかない
			if(x1==point_x[i]&&y1==point_y[i])return false;
			if(x2==point_x[i]&&y2==point_y[i])return false;
			map[point_y[i]][point_x[i]].cross=false;
		}
		return true;
	}

	//再起でキャラの移動範囲を求める
	public void walkErea(int x,int y,int n){
		//更新されてない場所でなく最短経路でもない場所の場合終了
		if(map[y][x].walk!=1000&&n>=map[y][x].walk)return;
		//このマス上を爆発する可能性があるなら
		else if(map[y][x].burst[0]!=-1){
			//念のため爆発するターン全てを見る
			for(int k=0;k<10;k++){
				//同じタイミングの爆発が無いなら処理終了 -1+1=0のため先にこの判定を行っておく
				if(map[y][x].burst[k]==-1)break;
				//爆発するときはタイミングが同じなら通れない判定にする
				else if(n==(map[y][x].burst[k]+1)){
					//this.pw.println("answ!"+map[y][x].burst[k]+1);
					return;
				}
			}		
		}
		//上記の判定に当てはまらなければ更新
		map[y][x].walk=n;
		//周囲を探索
		if(map[y][x-1].cross)walkErea(x-1, y, n+1);
		if(map[y][x+1].cross)walkErea(x+1, y, n+1);
		if(map[y-1][x].cross)walkErea(x, y-1, n+1);
		if(map[y+1][x].cross)walkErea(x, y+1, n+1);
	}
	//敵からの距離を求める
	public void enemyDistance1(int x,int y,int n){
		//ソフトブロックでなら+5　もし爆発する予定が10以内であるなら0
		if(map[y][x].soft&&!(map[y][x].burst[0]>=10))n+=5;
		//敵の真下に有るボムでないならボムの爆発までのターンを足して，もしこの爆弾が他の爆弾の爆発で消えないなら
		if(n!=0&&map[y][x].bomb!=-1&&map[y][x].burst[0]==-1)n+=magic[map[y][x].bomb].turn;
		//else if(map[y][x].bomb!=-1)n+=magic[map[y][x].bomb].turn;
		//更新されてない場所か最短経路を見つけた場合更新　最短でない場合は終了
		if(map[y][x].e1dis==1000||map[y][x].e1dis>n)map[y][x].e1dis=n;
		else return;
		//周囲を探索
		if(!map[y][x-1].hard)enemyDistance1(x-1, y, n+1);
		if(!map[y][x+1].hard)enemyDistance1(x+1, y, n+1);
		if(!map[y-1][x].hard)enemyDistance1(x, y-1, n+1);
		if(!map[y+1][x].hard)enemyDistance1(x, y+1, n+1);
	}
	//敵からの距離を求める
	public void enemyDistance2(int x,int y,int n){
		//ソフトブロックでなら+5　もし爆発する予定があり10以内であるなら0
		if(map[y][x].soft&&!(map[y][x].burst[0]>=10))n+=5;
		//敵の真下に有るボムでないならボムの爆発までのターンを足して，もしこの爆弾が他の爆弾の爆発で消えないなら
		if(n!=0&&map[y][x].bomb!=-1&&map[y][x].burst[0]==-1)n+=magic[map[y][x].bomb].turn;
		//else if(map[y][x].bomb!=-1)n+=magic[map[y][x].bomb].turn;
		//更新されてない場所か最短経路を見つけた場合更新　最短でない場合は終了
		if(map[y][x].e2dis==1000||map[y][x].e2dis>n)map[y][x].e2dis=n;
		else return;
		//周囲を探索
		if(!map[y][x-1].hard)enemyDistance2(x-1, y, n+1);
		if(!map[y][x+1].hard)enemyDistance2(x+1, y, n+1);
		if(!map[y-1][x].hard)enemyDistance2(x, y-1, n+1);
		if(!map[y+1][x].hard)enemyDistance2(x, y+1, n+1);
	}
	//中央からの距離を求める
	public void centerDistance(int x,int y,int n){
		//ソフトブロックでなら+5　もし爆発する予定があり10以内であるなら0
		if(map[y][x].soft&&!(map[y][x].burst[0]>=10))n+=5;
		//敵の真下に有るボムでないならボムの爆発までのターンを足して，もしこの爆弾が他の爆弾の爆発で消えないなら
		if(n!=0&&map[y][x].bomb!=-1&&map[y][x].burst[0]==-1)n+=magic[map[y][x].bomb].turn;
		//else if(map[y][x].bomb!=-1)n+=magic[map[y][x].bomb].turn;
		//更新されてない場所か最短経路を見つけた場合更新　最短でない場合は終了
		if(map[y][x].distance==1000||map[y][x].distance>n)map[y][x].distance=n;
		else return;
		//周囲を探索
		if(!map[y][x-1].hard)centerDistance(x-1, y, n+1);
		if(!map[y][x+1].hard)centerDistance(x+1, y, n+1);
		if(!map[y-1][x].hard)centerDistance(x, y-1, n+1);
		if(!map[y+1][x].hard)centerDistance(x, y+1, n+1);
	}
	//プレイヤーを敵に近づける
	public int approachEnemy(int x,int y,Boolean p1){
		int min=1000;
		int max_walk=-1;
		//同じ評価値の場所を配列に複数保存しランダムで選び移動することでステージ4での無限ループを防ぐ 最大16通りあると考えて配列は8用意する
		int point[]=new int[32];
		int count=0;
		//移動できる範囲の中で敵に最も近いところに移動する(walkでボムに当たらないことは考慮済み)
		for(int j=0;j<Y;j++){
			for(int i=0;i<X;i++){
				//移動範囲外であるなら次へ
				if(map[j][i].walk==1000)continue;
				else{
					//最短の場所であり自分のところから動ける場所であれば(最短が見つかればできるだけ自分のところから最長にする)
					//もし距離が前より近いなら配列を新しくする
					if(min>map[j][i].nearedis+map[j][i].walk){
						count=0;
					}
					//もし距離が前と同じなら
					else if(min==map[j][i].nearedis+map[j][i].walk){
						//歩くまでの距離が前より遠いなら配列を新しくする
						if(max_walk<map[j][i].walk)count=0;
						//歩くまでの距離が前より近いならこれは処理しない
						else if(max_walk>map[j][i].walk)continue;
						//全く前と同じなら既存の配列に新たに値を入れる
					}
					//それ以外では処理を終了
					else continue;
					min=map[j][i].nearedis+map[j][i].walk;
					//更新する際はx,yの順番で保存
					point[count++]=i;
					point[count++]=j;
					max_walk=map[j][i].walk;
				}
			}
		}
		//もしpointの中に何も座標が入ってないときは別の解を探す
		if(count==0){
			int dis=map[y][x].nearedis;
			//最短経路にあたる隣がソフトブロックの場合はその場に爆弾を置く
			if(dis>map[y][x-1].nearedis&&map[y][x-1].soft)return 5;
			else if(dis>map[y-1][x].nearedis&&map[y-1][x].soft)return 5;
			else if(dis>map[y][x+1].nearedis&&map[y][x+1].soft)return 5;
			else if(dis>map[y+1][x].nearedis&&map[y+1][x].soft)return 5;
			//それ以外は何もしない
			else return 0;
		}
		//もし逃げる場所が見つかっていたならそこに移動する
		else{
			//配列の長さの分だけ候補があるのでその中からランダムで選び出す
			int rand=(int)(Math.random()*count)/2;
			rand=0;
			//ランダムで選ばれた値を用いて移動を行う
			return move(point[rand*2], point[rand*2+1], 0, max_walk, max_walk, true,p1);
		}
		//this.pw.println("move to "+min_x+" "+min_y);
		//それ以外の場合なら指定された場所まで移動
		//return move(min_x, min_y, 0, max_walk, max_walk, true,p1);
	}
	//プレイヤーを敵に近づける
	public int approachCenter(int x,int y,Boolean p1){
		int min=1000;
		int min_dis=1000;
		int min_x=x;
		int min_y=y;
		int max_walk=0;
		//移動できる範囲の中で敵に最も近いところに移動する(walkでボムに当たらないことは考慮済み)
		for(int j=0;j<Y;j++){
			for(int i=0;i<X;i++){
				//移動範囲外であるなら次へ
				if(map[j][i].walk==1000)continue;
				else{
					if(min>=map[j][i].distance+map[j][i].walk){
						//もし距離が前と同じならdistanceがもっとも近いところに移動する
						if(min==map[j][i].distance+map[j][i].walk&&min_dis<=map[j][i].distance)continue;
						min=map[j][i].distance+map[j][i].walk;
						min_x=i;
						min_y=j;
						min_dis=map[j][i].distance;
						max_walk=map[j][i].walk;
					}
					else continue;
				}
			}
		}
		//もし座標が同じ時は何もしない
		if(min_x==x&&min_y==y){
			//何もしない
			return 0;
		}
		//this.pw.println("move to "+min_x+" "+min_y);
		//それ以外の場合なら指定された場所まで移動
		return move(min_x, min_y, 0, max_walk, max_walk, false,p1);
	}
	//近いところのソフトブロックを壊してアイテムを回収する
	public int[] correctItem(int x,int y,Boolean p1,int needHav,int needPow){
		//答えを返すための変数　次への移動方法　目的地のx座標　目的地のy座標　そこまでの歩数　アイテムを取るかどうか
		int data[]=new int[5];
		//動かないことを考えて座標を元の位置にしておく
		int min_x=x;
		int min_y=y;
		int min_walk=1000;
		int max_walk=-1;
		Boolean bomb=false;
		//playerによって爆弾を置ける数が違うのでそれの判断　ソフトブロックの時に用いる
		int p=0;
		if(!p1)p=1;
		//this.pw.println("rest is "+player[p].isRest(1));
		//近いソフトブロックを壊しそれよりアイテムが近いならそちらを回収する
		for(int j=1;j<Y;j++){
			for(int i=1;i<X;i++){
				//もし入れない場所なら次へ
				if(map[j][i].walk==1000)continue;
				//ハードブロックであるなら次へ
				if(map[j][i].hard)continue;
				//歩ける範囲内でボムを置いてたくさんブロックを壊せる場所を探す　もし爆弾をこれ以上置けない状態ならこの処理はスルー
				else if(map[j][i].walk!=1000&&player[p].isRest(1)){
					//この座標でブロックをいくつ壊せるか判定
					int soft=breakBlockEtc(i,j,player[p].pow,true,true);
					//ソフトブロックを新たに壊せて近いとこなら
					if(soft!=0&&min_walk>map[j][i].walk-soft){
						min_walk=map[j][i].walk-soft;
						min_x=i;
						min_y=j;
						max_walk=map[j][i].walk;
						bomb=true;
						data[4]=0;
					}
				}
				//ボム所持数を増やすアイテムが有るならそのアイテムを足りない分だけ優先する
				if(player[p].hav<needHav&&map[j][i].item==0){
					if(min_walk>=map[j][i].walk-(needHav-player[p].hav)){
						min_walk=map[j][i].walk-(needHav-player[p].hav);
						min_x=i;
						min_y=j;
						max_walk=map[j][i].walk;
						bomb=false;
						data[4]=1;
					}
				}
				//火力アップもアイテムを足りない分だけ優先する
				else if(player[p].pow<needPow&&map[j][i].item==1){
					if(min_walk>=map[j][i].walk-(needPow-player[p].pow)/2){
						min_walk=map[j][i].walk-(needPow-player[p].pow)/2;
						min_x=i;
						min_y=j;
						max_walk=map[j][i].walk;
						bomb=false;
						data[4]=1;
					}
				}
			}
		}
		data[1]=min_x;
		data[2]=min_y;
		data[3]=max_walk;
		//元の場所で爆弾を置かないなら敵に近づく
		if(min_x==x&&min_y==y&&!bomb){
			data[0]=this.approachCenter(x,y,p1);
			//this.pw.println("move center to "+data[0]);
			data[3]=0;
			data[4]=0;
			return data;
		}
		//this.pw.println("item correct to "+min_x+" "+min_y);
		//隣が移動目的地でそこにボムを置いたら他のブロックも壊せるなら置く(アイテムを取る瞬間で行う)
		if(max_walk==1&&player[p].isRest(1)&&!bomb&&breakBlockEtc(min_x,min_y,player[p].pow,true,true)>0)bomb=true;
		//それ以外の場合なら指定された場所まで移動
		data[0]=move(min_x, min_y, 0, max_walk, max_walk, bomb,p1);
		//this.pw.println("move to "+data[0]);
		return data;
	}
	//ソフトブロックを壊してアイテムを回収する できるだけ敵から離れたところのものを回収，破壊する 目的の数だけ集めたいアイテムは集めない
	public int[] correctItem2(int x,int y,Boolean p1,int hav,int pow){
		//答えを返すための変数　次への移動方法　目的地のx座標　目的地のy座標　そこまでの歩数　アイテムを取るかどうか
		int data[]=new int[5];
		//動かないことを考えて座標を元の位置にしておく
		int min_x=x;
		int min_y=y;
		int max_val=-100;
		int max_walk=-1;
		//playerによって爆弾を置ける数が違うのでそれの判断　ソフトブロックの時に用いる
		int p=0;
		if(!p1)p=1;
		Boolean bomb=false;
		//近いソフトブロックを壊しそれよりアイテムが近いならそちらを回収する
		for(int j=1;j<Y-1;j++){
			for(int i=1;i<X-1;i++){
				//もし入れない場所なら次へ
				if(map[j][i].walk==1000)continue;
				//ハードブロックであるなら次へ
				if(map[j][i].hard)continue;
				//ソフトブロックがありそれは壊されないブロックであれば探す隣まで歩いていけるならそのブロックを壊す もし爆弾をこれ以上置けない状態ならこの処理はスルー
				else if(map[j][i].soft&&map[j][i].burst[0]==-1&&player[p].isRest(1)){
					int soft=0;
					//この座標でブロックをいくつ壊せるか判定
					soft=breakBlockEtc(i,j,player[p].pow,true,true);
					//ソフトブロックを新たに壊せて近いとこなら
					if(soft!=0&&max_val<100+soft+map[j][i].nearedis-map[j][i].walk*2){
						max_val=100+soft+map[j][i].nearedis-map[j][i].walk*2;
						min_x=i;
						min_y=j;
						max_walk=map[j][i].walk;
						bomb=true;
						data[4]=0;
					}
				}
				//ボム所持数を増やすアイテムが有るならそのアイテムを足りない分だけ優先する
				else if(player[p].hav<hav&&map[j][i].item==0){
					if(max_val<100+(hav-player[p].hav)/2+map[j][i].nearedis-map[j][i].walk*2){
						max_val=100+(hav-player[p].hav)/2+map[j][i].nearedis-map[j][i].walk*2;
						min_x=i;
						min_y=j;
						max_walk=map[j][i].walk;
						bomb=false;
						data[4]=1;
					}
				}
				//火力アップもアイテムを足りない分だけ優先する
				else if(player[p].pow<pow&&map[j][i].item==1){
					if(max_val<100+(pow-player[p].pow)/2+map[j][i].nearedis-map[j][i].walk*2){
						max_val=100+(pow-player[p].pow)/2+map[j][i].nearedis-map[j][i].walk*2;
						min_x=i;
						min_y=j;
						max_walk=map[j][i].walk;
						bomb=false;
						data[4]=1;
					}
				}
			}
		}
		//場所が見つからないなら普通にアイテムを探す
		if(min_x==x&&min_y==y&&!bomb){
			return this.correctItem(x,y, p1, hav, pow);
		}
		//this.pw.println("item correct2 to "+min_x+" "+min_y);
		data[1]=min_x;
		data[2]=min_y;
		data[3]=max_walk;
		//隣が移動目的地でそこにボムを置いたら他のブロックも壊せるなら置く(アイテムを取る瞬間で行う)
		if(max_walk==1&&player[p].isRest(1)&&!bomb&&breakBlockEtc(min_x,min_y,player[p].pow,true,true)>0)bomb=true;
		//それ以外の場合なら指定された場所まで移動
		data[0]=move(min_x, min_y, 0, max_walk, max_walk, bomb,p1);
		return data;
	}
	/*//中心から近いところのアイテムを集める
	public int correctItem3(int x,int y,Boolean p1,int hav,int pow){
		//動かないことを考えて座標を元の位置にしておく
		int min_x=x;
		int min_y=y;
		int max_val=-200;
		int max_walk=-1;
		//playerによって爆弾を置ける数が違うのでそれの判断　ソフトブロックの時に用いる
		int p=0;
		if(!p1)p=1;
		Boolean bomb=false;
		//近いソフトブロックを壊しそれよりアイテムが近いならそちらを回収する
		for(int j=1;j<Y-1;j++){
			for(int i=1;i<X-1;i++){
				//ハードブロックであるなら次へ
				if(map[j][i].hard)continue;
				//ソフトブロックがありそれは壊されないブロックであれば探す隣まで歩いていけるならそのブロックを壊す もし爆弾をこれ以上置けない状態ならこの処理はスルー
				else if(map[j][i].soft&&map[j][i].burst[0]==-1&&player[p].isRest(1)){
					//動かないのは計算に入れない
					if(map[j][i].walk==0)continue;
					int soft=0;
					//この座標でブロックをいくつ壊せるか判定
					soft=breakBlock(i,j,player[p].pow);
					//ソフトブロックを新たに壊せて近いとこなら
					if(soft!=0&&max_val<100+soft-map[j][i].distance-map[j][i].walk){
						max_val=100+soft-map[j][i].distance-map[j][i].walk;
						min_x=i;
						min_y=j;
						max_walk=map[j][i].walk;
						bomb=true;
					}
				}
				//ボム所持数を増やすアイテムが有るならそのアイテムを足りない分だけ優先する
				else if(player[p].hav<hav&&map[j][i].item==0){
					if(max_val<100+(hav-player[p].hav)/2-map[j][i].distance-map[j][i].walk){
						max_val=100+(hav-player[p].hav)/2-map[j][i].distance-map[j][i].walk;
						min_x=i;
						min_y=j;
						max_walk=map[j][i].walk;
						bomb=false;
					}
				}
				//火力アップもアイテムを足りない分だけ優先する
				else if(player[p].pow<pow&&map[j][i].item==1){
					if(max_val<100+(pow-player[p].pow)/2-map[j][i].distance-map[j][i].walk){
						max_val=100+(pow-player[p].pow)/2-map[j][i].distance-map[j][i].walk;
						min_x=i;
						min_y=j;
						max_walk=map[j][i].walk;
						bomb=false;
					}
				}
			}
		}
		//元の場所で爆弾を置かないなら敵に近づく
		if(min_x==x&&min_y==y&&!bomb){
			return this.approachCenter(x,y,true);
		}
		this.pw.println("item correct to "+min_x+" "+min_y);
		//それ以外の場合なら指定された場所まで移動
		return move(min_x, min_y, 0, max_walk, max_walk, bomb,p1);
	}*/
	//入力座標でボムを置くと新たにブロックを破壊できる個数を返す　correctitemで使用
	public int breakBlockEtc(int x,int y,int pow,Boolean breakBlock,Boolean breakBomb){
		int soft=0;
		//特殊な配列を生成する
		int offset[]=new int[pow*5];
		//0,0,0,-1,-2,-3,0,0,0,1,2,3,0,0,0 　pow=3の時の例
		for(int s=0;s<3;s++)for(int t=0;t<pow;t++)offset[s*2*pow+t]=0;
		for(int t=0;t<pow;t++)offset[pow+t]=-(t+1);
		for(int t=0;t<pow;t++)offset[3*pow+t]=t+1;
		for(int k=0;k<4;k++){
			int count=0;
			while(count<pow){
				//ハードブロックであれば次の方向を見る
				if(map[y+offset[count+(k+1)*pow]][x+offset[count+k*pow]].hard)break;
				//ソフトブロックなら
				else if(map[y+offset[count+(k+1)*pow]][x+offset[count+k*pow]].soft){
					//それが爆発の予定がないならsoftに+する
					if(map[y+offset[count+(k+1)*pow]][x+offset[count+k*pow]].burst[0]==-1){
						if(breakBlock)soft++;
						break;
					}
					//爆発の予定があるのは除外する
					else break;
				}
				//ボムがあるなら
				else if(map[y+offset[count+(k+1)*pow]][x+offset[count+k*pow]].bomb!=-1){
					//とりあえず長いのでbombのidを変数に入れる
					int b=map[y+offset[count+(k+1)*pow]][x+offset[count+k*pow]].bomb;
					//bombが敵見方合わせてターンが6以上のボムは危険とみなして破壊する　surの途中で行わないので味方のボムも壊してもよい
					if(magic[b].turn>=6&&breakBomb&&map[y+offset[count+(k+1)*pow]][x+offset[count+k*pow]].burst[0]>=6)soft++;
				}
				count++;
			}
		}
		return soft;
	}
	
	//プレイヤーを目的地に移動させるための最短距離を辿る
	public int move(int x,int y,int t,int n,int N,Boolean bomb,Boolean p1){
		int toword=0;
		if(map[y][x].walk==0){
			toword=t;
			//移動場所が今の場所なら
			if(N==0&&bomb&&map[y][x].bomb==-1){
				toword+=5;
				//this.pw.println("bombput");
			}
			//移動場所が隣で爆発させる予定なら
			else if(N==1&&bomb)toword+=5;
		}
		//playerで近づき方を変える
		if(p1){
			//隣でもし爆風が通る予定ならそこに爆弾はおかない
			//if(n==1&&map[y][x].burst[0]!=-1)bomb=false;
			if(map[y][x-1].walk==n-1)toword=move(x-1,y,3,n-1,N,bomb,p1);
			else if(map[y-1][x].walk==n-1)toword=move(x,y-1,4,n-1,N,bomb,p1);
			else if(map[y][x+1].walk==n-1)toword=move(x+1,y,1,n-1,N,bomb,p1);
			else if(map[y+1][x].walk==n-1)toword=move(x,y+1,2,n-1,N,bomb,p1);
		}
		else{
			//隣でもし爆風が通る予定ならそこに爆弾はおかない
			//if(n==1&&map[y][x].burst[0]!=-1)bomb=false;
			if(map[y+1][x].walk==n-1)toword=move(x,y+1,2,n-1,N,bomb,p1);
			else if(map[y][x+1].walk==n-1)toword=move(x+1,y,1,n-1,N,bomb,p1);
			else if(map[y-1][x].walk==n-1)toword=move(x,y-1,4,n-1,N,bomb,p1);
			else if(map[y][x-1].walk==n-1)toword=move(x-1,y,3,n-1,N,bomb,p1);
		}
		return toword;
	}
	//ボムの爆発を処理する　誘爆やいつどこまで爆発するか等
	public void bombErea(){
		for(int i=0;i<this.magicSize;i++){
			//まだ爆発しておらず重なっているボムでないのなら判定する
			if(!magic[i].bursted){
				magic[i].bursted=true;
				bombBurst(magic[i].x, magic[i].y, magic[i].pow, magic[i].turn);
			}
		}
	    //this.pw.println("MagicSize "+this.magicSize);
		//bombEreaPrint();
	}
	//一つのボムの爆発を処理する
	//ボムの火力内の判定をする
	public void bombBurst(int x,int y,int pow,int turn){
		map[y][x].burst[0]=turn;
		//-----------------左------------------
		for(int i=1;i<=pow;i++){
			if(x-i<0)break;
			if(map[y][x-i].hard)break;
			else if(map[y][x-i].soft){
				//もしはじめて爆発が当たるブロックならそこで終わる　同時の爆発で消える場合も同様
				if(map[y][x-i].burst[0]==-1||map[y][x-i].burst[0]==turn){
					map[y][x-i].burst[0]=turn;
					break;
				}
				//もし初めての爆発で当たるのではないなら通過する
				else{
					//その場所の爆発の履歴の最後にこの爆発を入れる
					for(int j=0;j<map[y][x-i].burst.length;j++){
						if(map[y][x-i].burst[j]==-1||map[y][x-i].burst[j]==turn){map[y][x-i].burst[j]=turn;break;}
					}
				}
			}
			else if(map[y][x-i].bomb!=-1){
				//その場所の爆発の履歴の最後にこの爆発を入れる
				for(int j=0;j<map[y][x-i].burst.length;j++){
					if(map[y][x-i].burst[j]==-1||map[y][x-i].burst[j]==turn){map[y][x-i].burst[j]=turn;break;}
				}
				//もし爆発していないボムの上を通るなら
				if(!magic[map[y][x-i].bomb].bursted){
					magic[map[y][x-i].bomb].bursted=true;
					bombBurst(x-i,y,magic[map[y][x-i].bomb].pow,turn);
				}
			}
			//その他のマスなら爆発を通過させる
			else{
				//その場所の爆発の履歴の最後にこの爆発を入れる
				for(int j=0;j<map[y][x-i].burst.length;j++){
					if(map[y][x-i].burst[j]==-1||map[y][x-i].burst[j]==turn){
						map[y][x-i].burst[j]=turn;
						break;
					}
				}			
			}
		}
		//---------------------上-------------------------------
		for(int i=1;i<=pow;i++){
			if(y-i<0)break;
			if(map[y-i][x].hard)break;
			else if(map[y-i][x].soft){
				//もしはじめて爆発が当たるブロックならそこで終わる　同時の爆発で消える場合も同様
				if(map[y-i][x].burst[0]==-1||map[y-i][x].burst[0]==turn){
					map[y-i][x].burst[0]=turn;
					break;
				}
				//もし初めての爆発で当たるのではないなら通過する
				else{
					//その場所の爆発の履歴の最後にこの爆発を入れる
					for(int j=0;j<map[y-i][x].burst.length;j++){
						if(map[y-i][x].burst[j]==-1||map[y-i][x].burst[j]==turn){map[y-i][x].burst[j]=turn;break;}
					}
				}
			}
			else if(map[y-i][x].bomb!=-1){
				//その場所の爆発の履歴の最後にこの爆発を入れる
				for(int j=0;j<map[y-i][x].burst.length;j++){
					if(map[y-i][x].burst[j]==-1||map[y-i][x].burst[j]==turn){map[y-i][x].burst[j]=turn;break;}
				}
				//もし爆発していないボムの上を通るなら
				if(!magic[map[y-i][x].bomb].bursted){
					magic[map[y-i][x].bomb].bursted=true;
					bombBurst(x,y-i,magic[map[y-i][x].bomb].pow,turn);
				}
			}
			//その他のマスなら爆発を通過させる
			else{
				//その場所の爆発の履歴の最後にこの爆発を入れる
				for(int j=0;j<map[y-i][x].burst.length;j++){
					if(map[y-i][x].burst[j]==-1||map[y-i][x].burst[j]==turn){
						map[y-i][x].burst[j]=turn;
						break;
					}
				}			
			}
		}
		//--------------------右-------------------------
		for(int i=1;i<=pow;i++){
			if(x+i>=X)break;
			if(map[y][x+i].hard)break;
			else if(map[y][x+i].soft){
				//もしはじめて爆発が当たるブロックならそこで終わる　同時の爆発で消える場合も同様
				if(map[y][x+i].burst[0]==-1||map[y][x+i].burst[0]==turn){
					map[y][x+i].burst[0]=turn;
					break;
				}
				//もし初めての爆発で当たるのではないなら通過する
				else{
					//その場所の爆発の履歴の最後にこの爆発を入れる
					for(int j=0;j<map[y][x+i].burst.length;j++){
						if(map[y][x+i].burst[j]==-1||map[y][x+i].burst[j]==turn){map[y][x+i].burst[j]=turn;break;}
					}
				}
			}
			else if(map[y][x+i].bomb!=-1){
				//その場所の爆発の履歴の最後にこの爆発を入れる
				for(int j=0;j<map[y][x+i].burst.length;j++){
					if(map[y][x+i].burst[j]==-1||map[y][x+i].burst[j]==turn){map[y][x+i].burst[j]=turn;break;}
				}
				//もし爆発していないボムの上を通るなら
				if(!magic[map[y][x+i].bomb].bursted){
					magic[map[y][x+i].bomb].bursted=true;
					bombBurst(x+i,y,magic[map[y][x+i].bomb].pow,turn);
				}
			}
			//その他のマスなら爆発を通過させる
			else{
				//その場所の爆発の履歴の最後にこの爆発を入れる
				for(int j=0;j<map[y][x+i].burst.length;j++){
					if(map[y][x+i].burst[j]==-1||map[y][x+i].burst[j]==turn){
						map[y][x+i].burst[j]=turn;
						break;
					}
				}			
			}
		}
		//-----------------------下----------------------------------
		for(int i=1;i<=pow;i++){
			if(y+i>=Y)break;
			if(map[y+i][x].hard)break;
			else if(map[y+i][x].soft){
				//もしはじめて爆発が当たるブロックならそこで終わる　同時の爆発で消える場合も同様
				if(map[y+i][x].burst[0]==-1||map[y+i][x].burst[0]==turn){
					map[y+i][x].burst[0]=turn;
					break;
				}
				//もし初めての爆発で当たるのではないなら通過する
				else{
					//その場所の爆発の履歴の最後にこの爆発を入れる
					for(int j=0;j<map[y+i][x].burst.length;j++){
						if(map[y+i][x].burst[j]==-1||map[y+i][x].burst[j]==turn){map[y+i][x].burst[j]=turn;break;}
					}
				}
			}
			else if(map[y+i][x].bomb!=-1){
				//その場所の爆発の履歴の最後にこの爆発を入れる
				for(int j=0;j<map[y+i][x].burst.length;j++){
					if(map[y+i][x].burst[j]==-1||map[y+i][x].burst[j]==turn){map[y+i][x].burst[j]=turn;break;}
				}
				//もし爆発していないボムの上を通るなら
				if(!magic[map[y+i][x].bomb].bursted){
					magic[map[y+i][x].bomb].bursted=true;
					bombBurst(x,y+i,magic[map[y+i][x].bomb].pow,turn);
				}
			}
			//その他のマスなら爆発を通過させる
			else{
				//その場所の爆発の履歴の最後にこの爆発を入れる
				for(int j=0;j<map[y+i][x].burst.length;j++){
					if(map[y+i][x].burst[j]==-1||map[y+i][x].burst[j]==turn){
						map[y+i][x].burst[j]=turn;
						break;
					}
				}			
			}
		}/**/
	}
	//ボムから逃れるための移動の答えを出す関数
	public int avoidBurst(int x,int y,Boolean p1){
		//逃げられないことを考えて座標を元の位置にしておく
		int min_x=x;
		int min_y=y;
		int min=1000;
		int min_edis=1000;
		//最短で移動できてボムの範囲に入らないようなところに移動する
		for(int j=1;j<Y-1;j++){
			for(int i=1;i<X-1;i++){
				//移動範囲外であるなら次へ
				if(map[j][i].walk==1000)continue;
				else{
					//p1とp2で逃げる場所を変えるため条件文を少し変える
					if(p1){
						//爆発せず最短の場所であるなら  
						if(map[j][i].burst[0]==-1&&map[j][i].walk<=min){
							//もし距離が同じなら敵に近い方を選ぶようにする
							if(map[j][i].walk==min&&min_edis<=map[j][i].nearedis)continue;
							min=map[j][i].walk;
							min_x=i;
							min_y=j;
							min_edis=map[j][i].nearedis;
						}
					}
					else{
						//爆発せず最短の場所であるなら  
						if(map[j][i].burst[0]==-1&&map[j][i].walk<=min){
							//もし距離が同じなら敵に近い方を選ぶようにする
							if(map[j][i].walk==min&&min_edis<map[j][i].nearedis)continue;
							min=map[j][i].walk;
							min_x=i;
							min_y=j;
							min_edis=map[j][i].nearedis;
						}
					}
				}
			}
		}

		//元の場所のままなら生き残れるマスを一つ見つけそこへ向かって移動する
		if(min_x==x&&min_y==y){
			//6ターン先まで探索する
			int death=isDeath(x, y, 0, 10, 0);
			//int death=0;
			//もし-1が帰ってきていたら動かない
			//return death>=0?death:0;
			if(death==-1){
				//this.pw.println("Death!");
				return 0;
			}
			else{
				//this.pw.println("NotDeath!" +death);
				return death;
			}
		}
		//もし逃げる場所が見つかっていたならそこに移動する
		else return move(min_x, min_y, 0, min , min, false,p1);
	}
	//敵を倒せた状態でできるだけ敵から遠くの安全なところに逃げるための移動の答えを出す関数
	public int avoidEnemy(int x,int y){
		//逃げられないことを考えて座標を元の位置にしておく
		int min_x=x;
		int min_y=y;
		int min=2000;
		int max_edis=-100;
		//最短で移動できてボムの範囲に入らないようなところに移動する
		for(int j=0;j<Y;j++){
			for(int i=0;i<X;i++){
				//移動範囲外であるなら次へ
				if(map[j][i].walk==1000)continue;
				else{
					//爆発せず最短の場所であるなら  
					if(map[j][i].burst[0]==-1&&max_edis<=map[j][i].edis-map[j][i].walk*2){
						//もし距離が同じなら敵に近い方を選ぶようにする
						if(max_edis==map[j][i].edis-map[j][i].walk&&min<=map[j][i].walk)continue;
						min=map[j][i].walk;
						min_x=i;
						min_y=j;
						max_edis=map[j][i].edis-map[j][i].walk*2;
					}
				}
			}
		}
		//元の場所のままなら生き残れるマスを一つ見つけそこへ向かって移動する
		if(min_x==x&&min_y==y){
			//this.pw.println("All seek!");
			//12ターン先まで探索する
			int death=isDeath(x, y, 0, 12, 0);
			//int death=0;
			//もし-1が帰ってきていたら動かない
			//return death>=0?death:0;
			if(death==-1){
				//this.pw.println("Death!");
				return 0;
			}
			else{
				//this.pw.println("NotDeath!" +death);
				return death;
			}
		}
		//もし逃げる場所が見つかっていたならそこに移動する
		else{
			//this.pw.println("Avoid to "+min_x+" "+min_y);
			return move(min_x, min_y, 0, min , min, false,true);
		}
	}
	//ｎ手先まで５通りの動きを探索して生き残るなら正の値を返す  towordは無くしてreturn towordはreturn 0でも代用可
	//先を読むような探索の時はすでに1手動いているのでnを1にする
	public int isDeath(int x,int y,int n,int N,int toword){
		if(map[y][x].burst[0]!=-1){
			//安全であるかのフラグ　もし今いる場所でこの先爆発が起きないなら処理を終了する
			Boolean safe=true;
			//念のため爆発するターン全てを見る
			for(int k=0;k<10;k++){
				//同じタイミングの爆発が無いなら処理終了 -1+1=0のため先にこの判定を行っておく
				if(map[y][x].burst[k]==-1)break;				
				//爆発するときはタイミングが同じなら通れない判定にする
				else if(n==(map[y][x].burst[k]+1)){
					//死ぬので返り値は-1になる
					return -1;
				}
				//爆発が今のターンであるnより先に起きるならsafeフラグをfalseにする
				else if(n<(map[y][x].burst[k]+1)){
					//この先爆発が起きるので安全かどうかわからない
					safe=false;
				}
			}
			//この先爆発が起きる可能性は無いのでここまでの過程を返す
			if(safe){
				//this.pw.println("safe!"+x+" "+y+" "+n);
				return toword;
			}
		}
		//今のマスに爆発が無いのならここまでの過程を返す
		else{
			//this.pw.println("Avoid burst!"+x+" "+y+" "+n);
			return toword;
		}
		//もし指定ターンまでで逃げ切れなければ死ぬ判定にする
		if(n==N){
			//this.pw.println("survive!"+x+" "+y+" "+n);
			return -1;
		}
		//通れる場所で帰ってくる値が正であるなら生き残れる場所なのでそこまでのルートの行き方を返していく 爆発のタイミングなどから動かないことも考慮する
		//さらに，はじめにボムが存在しターンが経ってボムが消えたところも移動できることを考慮する
		//動かない時はボムを下においてることもあるので動かない時だけボムを置いてないかここの爆発がnまで猶予が有るなら探索を継続させる
		int death=-1;
		if(map[y][x].cross||map[y][x].burst[0]>n){if(isDeath(x, y, n+1,N,0)>=0)death=0;}
		if(map[y][x-1].cross||(map[y][x-1].bomb>=0&&map[y][x-1].burst[0]<n)){if(isDeath(x-1, y, n+1,N,1)>=0)death=1;}
		if(map[y-1][x].cross||(map[y-1][x].bomb>=0&&map[y-1][x].burst[0]<n)){if(isDeath(x, y-1, n+1,N,2)>=0)death=2;}
		if(map[y][x+1].cross||(map[y][x+1].bomb>=0&&map[y][x+1].burst[0]<n)){if(isDeath(x+1, y, n+1,N,3)>=0)death=3;}
		if(map[y+1][x].cross||(map[y+1][x].bomb>=0&&map[y+1][x].burst[0]<n)){if(isDeath(x, y+1, n+1,N,4)>=0)death=4;}
		return death;
	}
/*
	private void mapPrint(char[][] m){
		this.pw.println("map");
		String string;
		for(int j=0;j<Y;j++){
			string="";
			for(int i=0;i<X;i++){
				string+=m[j][i]+" ";
			}
			this.pw.println(string);
		}
		this.pw.println("");
	}
	
	public void mapPrint(){
		String string;
		for(int j=0;j<Y;j++){
			string="";
			for(int i=0;i<X;i++){
				string+= map[j][i].hard?1:0;
			}
			string+=" ";
			for(int i=0;i<X;i++){
				string+= map[j][i].soft?1:0;
			}
			string+=" ";
			for(int i=0;i<X;i++){
				string+= map[j][i].cross?1:0;
			}
			string+=" ";
			for(int i=0;i<X;i++){
				string+= map[j][i].bomb+"\t";
			}
			string+="\t";
			for(int i=0;i<X;i++){
				string+= map[j][i].item+"\t";
			}
			string+=" ";
			this.pw.println(string);
		}
		this.pw.println("");
	}
	public void magicPrint(){
		this.pw.println("MagicPrint");
		String string;
		for(int i=0;i<this.magicSize;i++){
			string="";
			string+="id "+magic[i].id+" x "+magic[i].x+" y "+magic[i].y+" pow "+magic[i].pow+" turn "+magic[i].turn+" bursted "+magic[i].bursted;
			this.pw.println(string);
		}
	}
	public void walkPrint(String str){
		this.pw.println(str+" walk");
		String string;
		for(int j=0;j<Y;j++){
			string="";
			for(int i=0;i<X;i++){
				string+= map[j][i].walk+"\t";
			}
			this.pw.println(string);
		}
		this.pw.println("");
	}
	public void enemy1DistancePrint(){
		this.pw.println("enemy1 distance");
		String string;
		for(int j=0;j<Y;j++){
			string="";
			for(int i=0;i<X;i++){
				string+= map[j][i].e1dis+"\t";
			}
			this.pw.println(string);
		}
		this.pw.println("");
	}
	public void enemy2DistancePrint(){
		this.pw.println("enemy2 distance");
		String string;
		for(int j=0;j<Y;j++){
			string="";
			for(int i=0;i<X;i++){
				string+= map[j][i].e2dis+"\t";
			}
			this.pw.println(string);
		}
		this.pw.println("");
	}
	public void enemyDistancePrint(){
		this.pw.println("enemy distance");
		String string;
		for(int j=0;j<Y;j++){
			string="";
			for(int i=0;i<X;i++){
				string+= map[j][i].edis+"\t";
			}
			this.pw.println(string);
		}
		this.pw.println("");
	}
	public void centerDistancePrint(){
		this.pw.println("center distance");
		String string;
		for(int j=0;j<Y;j++){
			string="";
			for(int i=0;i<X;i++){
				string+= map[j][i].distance+"\t";
			}
			this.pw.println(string);
		}
		this.pw.println("");
	}
	public void bombEreaPrint(){
		String string;
		for(int j=0;j<Y;j++){
			string="";
			for(int k=0;k<5;k++){
				for(int i=0;i<X;i++){
					string+= map[j][i].burst[k]+"\t";
				}
				string+="\t";
			}
			this.pw.println(string);
		}
		this.pw.println("");
	}*/
}
