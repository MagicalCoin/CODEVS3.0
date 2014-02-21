import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.IOException;



public class Cal {
	public int turn=0;
	public int Maxturn=0;
	public int time=0;
	public File file;
	public PrintWriter pw;
	public Map map;//共通のマップ
	public Player player[] = new Player[4];
	public int playerSize=0;
	public Magic magic[] = new Magic[100];
	public int magicSize=0;
	public Item item[] = new Item[100];
	public int itemSize=0;
	public int deathTurn=10;
	public int virtualturn=1;
	public SurroundAttackData sur=new SurroundAttackData();
	public class SurroundAttackData{
		int turn=-1;
		Boolean start=false;
		//スタートの座標と最後の座標
		public int x[]=new int[4];
		public int y[]=new int[4];
		//敵のいる方向
		int target=-1;
		//目的地までの必要歩数
		int needwalk1,needwalk2;
		public Boolean update(int data[]){
			if(data[0]==-1)return false;
			//p1の始点終点
			x[0]=data[2];
			y[0]=data[3];
			x[1]=data[4];
			y[1]=data[5];
			//p2の始点終点
			x[2]=data[6];
			y[2]=data[7];
			x[3]=data[8];
			y[3]=data[9];
			target=data[10];
			needwalk1=data[11];
			needwalk2=data[12];
			return true;
		}
	}
	public Cal(int MAX_W,int MAX_H){
		for(int i=0;i<4;i++)player[i]=new Player();
		for(int i=0;i<100;i++)magic[i]=new Magic();
		for(int i=0;i<100;i++)item[i]=new Item();
		
	    /*try{
	        this.file=new File("logFinal.txt");
			if(checkBeforeWritefile(file)){
				this.pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
	
				this.pw.println("codevs3.0-finalのログ");
	        }
			else{
	        	System.out.println("ファイルに書き込めません");
	        }
	    }catch(IOException e){
	    	System.out.println(e);
    	}*/
	    this.map=new Map(MAX_W,MAX_H,pw);

	}
	private boolean checkBeforeWritefile(File file2) {
    if (file.exists()){
        if (file.isFile() && file.canWrite()){
          return true;
        }
      }
      return false;
    }

	public void playerUpdate(int i,int pid,int id,int row,int col,int pow,int hav){
		player[i].pid=pid;
		player[i].id=id;
		player[i].y=row;
		player[i].x=col;
		player[i].pow=pow;
		player[i].hav=hav;
		player[i].put=0;
	}
	public void magicUpdate(int i,int id,int row,int col,int turn,int pow){
		magic[i]=new Magic();
		magic[i].id=id;
		magic[i].y=row;
		magic[i].x=col;
		magic[i].turn=turn;
		magic[i].pow=pow;
		magic[i].bursted=false;
	}

	public void itemUpdate(int i,char type[],int row,int col){
		item[i]=new Item();
		item[i].type = (type[0] == 'N' ? 0 : 1);//int
		item[i].y=row;
		item[i].x=col;
	}
	public void mapUpdate(int time,int turn,int maxturn,int my_id,int X,int Y,char[][] field){
		this.Maxturn=maxturn;
		this.turn=turn;
		this.time=time;
		//this.pw.println("----------------turn "+turn+"-------------------");
		this.map.mapUpdate(X, Y,my_id, field,player,playerSize,magic,magicSize,item,itemSize,turn);
		//map.MapPrint(map);
	}
	//キャラの動き　p1のボムのターン数　p2のボムのターン数
	public int[] calculate(){
		//もし時間が残り50000秒を切ったら計算回数を減らす
		if(this.time<50000){
			deathTurn=6;
			virtualturn=0;
		}
		//プレイヤー別にmapを生成する
	    Map p1=new Map(map.X,map.Y,pw);
	    Map p2=new Map(map.X,map.Y,pw);
	    p1.mapClone(map);
		//----------------事前の共通の計算---------------------
		//ボムの爆発範囲を求める
		p1.bombErea();
		//p1.bombEreaPrint();
		//敵との障害物を含めた距離を求める
		p1.enemyDistance1(player[2].x, player[2].y, 0);
		p1.enemyDistance2(player[3].x, player[3].y, 0);
		//p1とp2の距離を合わせた値を算出しておく
		for(int j=0;j<p1.Y;j++)for(int i=0;i<p1.X;i++)p1.map[j][i].edis=p1.map[j][i].e1dis+p1.map[j][i].e2dis;
		//p1.enemy1DistancePrint();
		//p1.enemy2DistancePrint();
		//p1.enemyDistancePrint();
		int nearEnemy=2;
		//もし敵と重なっているときは距離を遠く感じさせその場所に留まらないようにする approach用
		//for(int i=0;i<2;i++)for(int j=2;j<4;j++)if(player[i].x==player[j].x&&player[i].y==player[j].y)p1.map[player[i].y][player[i].x].e1dis=3;
		//if(player[2].x==player[3].x&&player[2].y==player[3].y)p1.map[player[3].y][player[3].x].e1dis=3;
		//p1.enemy1DistancePrint();
		//p1.enemy2DistancePrint();
		//----------------個別での事前の計算------------------------
		p2.mapClone(p1);
		//マップ中央から見た距離を求める
		p1.centerDistance(p1.X/2, p1.Y/2-1, 0);
		p2.centerDistance(p2.X/2, p2.Y/2+1, 0);
		//p1.centerDistancePrint();
		//e1よりe2のほうが近い場合e2をnearedisに代入する
		if(p1.map[player[0].y][player[0].x].e1dis>p1.map[player[0].y][player[0].x].e2dis){
			for(int j=0;j<p1.Y;j++)for(int i=0;i<p1.X;i++)p1.map[j][i].nearedis=p1.map[j][i].e2dis;
			nearEnemy=3;
		}
		else for(int j=0;j<p1.Y;j++)for(int i=0;i<p1.X;i++)p1.map[j][i].nearedis=p1.map[j][i].e2dis;
		//e1よりe2のほうが近い場合e2をnearedisに代入する
		if(p2.map[player[0].y][player[0].x].e1dis>p2.map[player[0].y][player[0].x].e2dis)for(int j=0;j<p2.Y;j++)for(int i=0;i<p2.X;i++)p2.map[j][i].nearedis=p2.map[j][i].e2dis;
		else for(int j=0;j<p2.Y;j++)for(int i=0;i<p2.X;i++)p2.map[j][i].nearedis=p2.map[j][i].e2dis;
		//移動できる範囲を求める　ボムの爆発の計算後が望ましい
		p1.walkErea(player[0].x, player[0].y, 0);
		//p1.walkPrint("p1");
		p2.walkErea(player[1].x, player[1].y, 0);
		//p2.walkPrint("p2");
		//----------------ここから思考-------------------
		int answer1=-1;
		int answer2=-1;
		//味方と敵がすでに確定で死ぬのかどうかを見る
		int deathp1=p1.isDeath(p1.player[0].x, p1.player[0].y, 0, deathTurn, 0);
		int deathp2=p1.isDeath(p1.player[1].x, p1.player[1].y, 0, deathTurn, 0);
		int deathe1=p1.isDeath(p1.player[2].x, p1.player[2].y, 0, deathTurn, 0);
		int deathe2=p1.isDeath(p1.player[3].x, p1.player[3].y, 0, deathTurn, 0);
		//敵を倒しているかどうかを保存し回避の際に用いる
		Boolean killEnemy=false;
		//敵を倒す思考をするかどうかの判定　回避の際に用いる
		Boolean attackEnemy=false;
		//-----------------味方が死んでいると判定された場合-----------------------------------------
		if(deathp1==-1||deathp2==-1){
			//this.pw.println("player will death!");
			//勝敗をカウントする変数
			int win=0;
			if(deathp1==-1)win--;
			if(deathp2==-1)win--;
			if(deathe1==-1)win++;
			if(deathe2==-1)win++;
			//もし負けているなら敵を何とかして倒す
			if(win<0){
				//確実に敵を倒せるか探す
				int maxval=-1;
				int max_i=0;
				int max_j=0;
				Boolean flug=false;
				for(int i=9;i>=0;i--){
					for(int j=9;j>=0;j--){
						//動ける入力かどうかを判断し無理なら次へ
						if(!correctInput(map, i, j, true))continue;
						//確実に敵を倒せる解を探す
						int val=virtualAttack3(map,i,j);
						//敵を確実に2人倒せるならこちらは死んでもいいので行う
						if(val==2){
							int d=this.playerIsDeath3(map,i,j);
							//敵を二人倒せるなら問答無用で倒しに行く
							//this.pw.println("Enemy killall!");
							maxval=val;
							max_i=i;
							max_j=j;
							attackEnemy=true;
							if(d==1){
								//this.pw.println("player also death so another answer seek");
								flug=true;
								break;
							}
							else if(d==0){
								//this.pw.println("perfect victory!");
								flug=true;
								break;
							}
						}
						//敵を一人倒せても敵の動き次第で死ぬ可能性があるならこの動きをやめる
						if(val==1){
							int d=this.playerIsDeath3(map,i,j);
							//もし二人死んでしまうのであれば意味が無いのでスルー
							if(d>=2){
								//this.pw.println("player may death2");
								continue;
							}
							//一人でも倒せるならそれを選択する
							else{
								maxval=val;
								max_i=i;
								max_j=j;
								attackEnemy=true;
							}
						}
					}
					if(flug)break;
				}
				//確実に敵を相殺できるならこの答えを選ぶ
				if(maxval>=0){
					answer1=max_i;
					answer2=max_j;
					killEnemy=true;
				}
				//確実に敵を倒すことはできないでのでisdeathでは求まらなかった回避をできることを祈る
				else{
					deathTurn=12;
					virtualturn=1;
				}
			}
		}
		//-----------------------------------------------------------------------------------
		//敵が死ぬと判定されたら逃げる
		if(deathe1==-1||deathe2==-1){
			//this.pw.println("enemy will death!");
			//answer1=p1.avoidBurst(player[0].x, player[0].y,true);
			//answer2=p2.avoidBurst(player[1].x, player[1].y,false);
			answer1=p1.avoidEnemy(player[0].x, player[0].y);
			answer2=p2.avoidEnemy(player[1].x, player[1].y);
			//this.pw.println("avoid answer1 "+answer1+" answer2 "+answer2);
			killEnemy=true;
			//現時点での解での死ぬ可能性を記録しておくく
			int a=this.playerIsDeath3(map,answer1,answer2);
			//全通り探索で敵に倒される可能性のあるキャラを少ないのを選ぶ
			for(int i=4;i>=0;i--){
				for(int j=4;j>=0;j--){
					//動ける入力かどうかを判断し無理なら次へ
					if(!correctInput(map, i, j, true))continue;
					int d=this.playerIsDeath3(map,i,j);
					//全通り探索で調べた解の方が死なないなら
					if(a>d){
						//this.pw.println("better avoid found");
						answer1=i;
						answer2=j;
						a=d;
					}					
				}
			}
			//this.pw.println("avoid answer1 "+answer1+" answer2 "+answer2);
		}
		//誰も死なないなら普通どおり行動する
		else{
			//同時で数手先まで探索する場合(敵と近くにいる時に行う)
			if(p1.map[player[0].y][player[0].x].nearedis<=100||p2.map[player[1].y][player[1].x].nearedis<=100){
				//----------------p1p2の思考--------------------
				//this.pw.println("---------- p1p2 think--------------");
				//answer1=answer2=-1;
				//-------------もし確実に倒せる解がなければ敵の動きによっては倒せる可能性のある解を探す------------------------------
			    //留る動く置く動いておくの10通りを計算する その中で最高評価を見つけ出す(敵を倒せる最短ターンを導く)
				int maxval=-1;
				int max_i=0;
				int max_j=0;
				int min_d=2;
				Boolean flug=false;
				for(int i=9;i>=0;i--){
					for(int j=9;j>=0;j--){
						//動ける入力かどうかを判断し無理なら次へ
						if(!correctInput(map, i, j, true))continue;
						//確実に敵を倒せる解を探す
						int val=virtualAttack3(map,i,j);
						//敵を確実に2人倒せるならコチラは一人死ぬまでは許容
						if(val==2){
							int d=this.playerIsDeath3(map,i,j);
							//プレイヤーが死ぬ可能性があるのが一人だけなら倒しに行く
							if(d>=min_d){
								//this.pw.println("player may all death");
								continue;
							}
							else{
								//this.pw.println("Enemy killall!");
								maxval=val;
								max_i=i;
								max_j=j;
								attackEnemy=true;
								min_d=d;
								//タイブレイクようにコチラが死なない解もないか探しておく
								if(d==1){
									//this.pw.println("player also death so another answer seek");
								}
								else if(d==0){
									//this.pw.println("perfect victory!");
									flug=true;
									break;
								}
							}
						}
						//敵を一人倒せても敵の動き次第で死ぬ可能性があるならこの動きをやめる
						if(val==1){
							//this.pw.println("Answer may be found2");
							if(this.playerIsDeath2(map,virtualturn,i,j)>0){
								//this.pw.println("player may death2");
								continue;
							}
							else{
								maxval=val;
								max_i=i;
								max_j=j;
								attackEnemy=true;
							}
						}
					}
					if(flug)break;
				}/**/
				//this.pw.println("maxValue2 "+maxval);
				//もし敵を見つける解が見つかれば
				if(maxval>=0){
					//this.pw.println("AnswerFound2!");
					answer1=max_i;
					answer2=max_j;
					//this.pw.println("answer1 "+answer1);
					//this.pw.println("answer2 "+answer2);
				}
				//解がなければ
				else{
					//this.pw.println("NotFound2!");
				}
			}
			//敵を確実に倒すことができない時 毎ターン囲めるかを探しないならアイテムを集める
			if(answer1==-1){
				//もし攻撃開始ポイントのボムが片方でも消されていたら実行をやめる
				if(map.map[sur.y[0]][sur.x[0]].bomb==-1||map.map[sur.y[2]][sur.x[2]].bomb==-1){
					sur.start=false;
					//this.pw.println("Bomb disappear");
				}
				//p1p2からの距離を求めるためのmapを作成する move用
				Map mp1=new Map(map.X,map.Y,pw);
				mp1.mapClone(map);
				mp1.walkErea(mp1.player[0].x, mp1.player[0].y, 0);
				Map mp2=new Map(map.X,map.Y,pw);
				mp2.mapClone(map);
				mp2.walkErea(mp2.player[1].x, mp2.player[1].y, 0);
				//すでにボムを起き始めているなら
				if(sur.start){
					//this.pw.println("Let's surround");
					int point1[]=surroundAttack(map, true);
					int point2[]=surroundAttack(map, false);
					//-1が返ってきていたら避けるようにする
					if(point1[0]!=-1)answer1=mp1.move(point1[0], point1[1], 0, mp1.map[point1[1]][point1[0]].walk, mp1.map[point1[1]][point1[0]].walk, true, true);
					else answer1=p1.avoidEnemy(player[0].x, player[0].y);
					if(point2[0]!=-1)answer2=mp2.move(point2[0], point2[1], 0, mp2.map[point2[1]][point2[0]].walk, mp2.map[point2[1]][point2[0]].walk, true, false);
					else answer2=p2.avoidEnemy(player[1].x, player[1].y);
					//たーンを減らす
					sur.turn--;
					//もしターンが減りきったら止める
					if(sur.turn<5)sur.turn=5;
					if(answer1>=5)answer1=answer1%5+sur.turn*10;
					if(answer2>=5)answer2=answer2%5+sur.turn*10;
				}
				//------------------------------sur not start-----------------------------------------
				//まだボムを置いていないなら場所を探す
				if(!sur.start){
					//this.pw.println("seek surround point");
					//囲める場所を探し見つかれば配列を返してそれが正しいか見る
					if(sur.update(surroundAttackPoint(map))){
						answer1=mp1.move(sur.x[0], sur.y[0], 0, sur.needwalk1, sur.needwalk1, false, true);
						answer2=mp2.move(sur.x[2], sur.y[2], 0, sur.needwalk2, sur.needwalk2, false, false);
						//this.pw.println("p1 move to "+sur.x[0]+" "+sur.y[0]);
						//this.pw.println("p2 move to "+sur.x[2]+" "+sur.y[2]);
						//this.pw.println("sur move answer1 "+answer1+" answer2 "+answer2);
						//目的地に一歩の移動で行けるなら
						if(sur.needwalk1<=1&&sur.needwalk2<=1){
							//爆発までのターンをセット
							sur.turn=15;
							answer1=answer1%5+sur.turn*10;
							answer2=answer2%5+sur.turn*10;
							//もし片方でもこの動きをしても危なくないならstartする
							if(this.playerIsDeath2(map,virtualturn,answer1, answer2)>0){
								//this.pw.println("danger so dont put bomb");
								answer1=answer1%5;
								answer2=answer2%5;
							}
							else{
								//this.pw.println("Attack start");
								sur.start=true;
							}
						}
					}
					//囲めないのであればアイテムを集める
					else{
						//this.pw.println("p1p2 Item correct");
						int correct1[];
						if(p1.map[player[0].y][player[0].x].nearedis<=5)correct1=p1.correctItem2(player[0].x, player[0].y,true,8,10);
						else correct1=p1.correctItem(player[0].x, player[0].y,true,8,10);
						int correct2[]=p2.correctItem(player[1].x, player[1].y,false,8,10);
						//this.pw.println("answer1 "+correct1[0]+" answer2 "+correct2[0]);
						//二人の動きが同じ所に向かい片方の方が先着するならもう片方の行動を変える
						if(correct1[1]==correct2[1]&&correct1[2]==correct2[2]){
							//もしp1よりp2の方が到着が遅いならp2のmapをupdateして行動を変える
							if(correct1[3]<correct2[3]){
								virtualMapUpdate2(p2, correct2[1], correct2[2], map.player[0].pow);
							}
							//もしp2よりp1の方が到着が遅いならp1のmapをupdateして行動を変える
							else if(correct1[3]>correct2[3]){
								virtualMapUpdate2(p1, correct1[1], correct1[2], map.player[1].pow);
							}
							//もし到着は同時で片方が爆破させる目的で近づくのであれば別の所に移動させる
							else if(correct1[3]==correct2[3]&&correct1[4]==0){
								virtualMapUpdate2(p1, correct1[1], correct1[2], map.player[1].pow);
							}
							else if(correct1[3]==correct2[3]&&correct2[4]==0){
								virtualMapUpdate2(p2, correct2[1], correct2[2], map.player[0].pow);
							}
							//同時に到着でアイテムを集めるのであれば更新する必要がない
							else{}
							//updateしたマップで行動を再び求める
							if(p1.map[player[0].y][player[0].x].nearedis<=5)correct1=p1.correctItem2(player[0].x, player[0].y,true,8,10);
							else correct1=p1.correctItem(player[0].x, player[0].y,true,8,10);
							correct2=p2.correctItem(player[1].x, player[1].y,false,8,10);
						}
						//以上からの動きをanswerに入れる
						answer1=correct1[0];
						answer2=correct2[0];
					}
					//breakblockのなかでburstを使うためbombereaを求めるためにmapを複製する p1,p2は中身をいじっているので使わない
					Map m=new Map(map.X,map.Y,pw);
					m.mapClone(map);
					m.bombErea();
					//ここまでで求めた答えの中のボムを置かない選択肢をボムを置くように変えてターン数の長いボムを破壊できるようになれば実行する
					if(answer1<5){
						//行動が0,1,2,3,4の順番の時の移動先
						int offset[]={0,0,-1,0,0,-1,1,0,0,1};
						//もし行動後の座標でボムを置くと長いボムが一つでも壊せるなら
						if(m.breakBlockEtc(m.player[0].x+offset[answer1*2], m.player[0].y+offset[answer1*2+1], m.player[0].pow,false, true)>0){
							answer1+=5;
							//もしこの変更で(変更前から危ないとしても)危ないのであれば元に戻す
							if(this.playerIsDeath2(map,virtualturn,answer1, answer2)>0){
								//this.pw.println("danger so before answer");
								answer1=answer1%5;
							}
							else{
								//this.pw.println("Change answer safe");
							}
						}
						//else this.pw.println("Not need change answer");
					}
					if(answer2<5){
						//行動が0,1,2,3,4の順番の時の移動先
						int offset[]={0,0,-1,0,0,-1,1,0,0,1};
						//もし行動後の座標でボムを置くと長いボムが一つでも壊せるなら
						if(m.breakBlockEtc(m.player[1].x+offset[answer2*2], m.player[1].y+offset[answer2*2+1], m.player[1].pow,false, true)>0){
							answer2+=5;
							//もしこの変更で(変更前から危ないとしても)危ないのであれば元に戻す
							if(this.playerIsDeath2(map,virtualturn,answer1, answer2)>0){
								//this.pw.println("danger so before answer");
								answer2=answer2%5;
							}
							else{
								//this.pw.println("Change answer safe");
							}
						}
						//else this.pw.println("Not need change answer");
					}
				}
				//-----------------------------------------------------------------------------------------
			}
		}
		//ここまでで出た答えで死ぬ可能性があるかを見てあるなら避ける答えを導く
		//もし敵がこちらを巻き込み死のうとしても死なないところを全通り探してそこに移動する
		Map m=new Map(map.X,map.Y,pw);
		if(!attackEnemy&&!killEnemy&&this.playerIsDeath2(map,virtualturn,answer1, answer2)>0){
			//this.pw.println("------------------This answer is danger!-----------------------------");
			//this.pw.println("answer1 "+answer1+" answer2 "+answer2);
			//this.pw.println("deathcount "+this.playerIsDeath2(map,virtualturn,answer1, answer2));
			//m1を初期化して
		    m.mapClone(map);
		    Boolean flug=false;
		    //同じ所に毎回よけて無限ループになるのを防ぐためにランダムで優先度を毎回変える
		    //0を探索するのはできるだけ最後にする
		    //----------------------------------------------------
		    int min_val=1000000;
		    int min_pat1=0;
		    int min_pat2=0;
		    //4方向の動き方を配列に入れる
		    //int pat1[]={1,2,3,4,0};
		    int pat1[]=centerArray(player[0].x, player[0].y);
		    //配列の中身をシャッフルして最後に0を加える
		    //pat1=arrayShuffle(pat1, pat1.length);
			for(int i=0;i<pat1.length;i++){
			    //4方向の動き方を配列に入れる
			    //int pat2[]={1,2,3,4,0};
				int pat2[]=centerArray(player[1].x, player[1].y);
			    //配列の中身をシャッフルして最後に0を加える
			    //pat2=arrayShuffle(pat2, pat2.length);
				for(int j=0;j<pat2.length;j++){
					//動ける入力かどうかを判断し無理なら次へ
					if(!correctInput(m, pat1[i], pat2[j], true))continue;
					//敵の動き次第で死ぬ可能性を判定する
					int val=this.playerIsDeath2(map,virtualturn,pat1[i], pat2[j]);
					//この状態なら死なないなら
					if(val==0){
						//this.pw.println("avoid answer found!");
						flug=true;
						//もし片方だけ変えれば大丈夫な場合もう片方の指令は変えないようにする
						if(this.playerIsDeath2(map,virtualturn,pat1[i], answer2)==0){
							answer1=pat1[i];
						}
						else if(this.playerIsDeath2(map,virtualturn,answer1,pat2[j])==0){
							answer2=pat2[j];
						}
						//両方変えなければならない時は変える
						else{
							answer1=pat1[i];
							answer2=pat2[j];
						}
						break;
					}
					else{
						//前のより死なないなら更新
						if(min_val>val){
							min_val=val;
							min_pat1=pat1[i];
							min_pat2=pat2[j];
						}
					}
				}
				if(flug)break;
			}
			//死なない道が見つかっているならコレで行く
			if(flug);//this.pw.println("avoid answer1 "+answer1+" answer2 "+answer2);
			else{
				answer1=min_pat1;
				answer2=min_pat2;
				//this.pw.println("Avoid answer Not Found answer1 "+answer1+" answer2 "+answer2);/**/
			}
		}
		else {
			//this.pw.println("This answer is safety");
		}
		//this.pw.println("");
		//this.pw.println("last answer1 "+answer1+" answer2 "+answer2);
		if(map.mapTurn>1000){
			answer1=answer2=2;
		}
		//---------------答えの出力----------------------
		int answer[]={0,0};
		answer[0]=answer1;
		answer[1]=answer2;
		return answer;
	}
	//数手先を読むための仮想的にマップを更新するための関数 playerのidを入れることで敵味方どちらにでも対応できるようにする
	public Boolean virtualMapUpdate(Map m,int pat[]){
		//this.pw.println("Move pat1 "+pat[0]+"  pat2 "+pat[1]);
		//this.pw.println("Where p1 "+m.player[0].x+" "+m.player[0].y+"  p2 "+m.player[1].x+" "+m.player[1].y);
	    //m.mapClone(map);
	    //次のターン爆発する予定のボムを爆発させる
	    for(int i=0;i<m.magicSize;i++){
	    	if(m.magic[i].turn==0&&!m.magic[i].bursted){
	    		//ボムのある場所でターン0で爆発させる
	    		m.bombBurst(m.magic[i].x, m.magic[i].y, m.magic[i].pow, 0);
	    		//爆発したボムの上を通れるようにする
	    		m.map[m.magic[i].y][m.magic[i].x].bomb=-1;
	    		m.map[m.magic[i].y][m.magic[i].x].cross=true;
	    	}
	    }
		//this.pw.println("Where p1 "+m.player[p1].x+" "+m.player[p1].y+"  p2 "+m.player[p2].x+" "+m.player[p2].y);
	    //if(turn>290)m.bombEreaPrint();
	    //ボムの爆発上に移動した場合falseを返す
	    if(m.map[m.player[0].y][m.player[0].x].burst[0]!=-1||m.map[m.player[1].y][m.player[1].x].burst[0]!=-1){
	    	//this.pw.println("Move On bursted!!");
	    	return false;
	    }
	    //ボムがターン0で爆発する判定はもう必要ないので初期化しておく
	    m.clearBurst();
	    //新しく置くボムを配列に入れる
	    for(int i=0;i<pat.length;i++){
		    if(pat[i]>=5){
		    	//もしもとからそこにボムがないなら
		    	if(m.map[m.player[i].y][m.player[i].x].bomb==-1){
			    	m.magic[m.magicSize].id=i;
			    	m.magic[m.magicSize].x=m.player[i].x;
			    	m.magic[m.magicSize].y=m.player[i].y;
			    	//もしpatが50以上ならターンを指定する
			    	if(pat[i]>=50)m.magic[m.magicSize].turn=pat[i]/10;
			    	else m.magic[m.magicSize].turn=5;
			    	m.magic[m.magicSize].pow=m.player[i].pow;
			    	m.magic[m.magicSize].bursted=false;
			    	//マップも更新する
			    	m.map[m.player[i].y][m.player[i].x].bomb=m.magicSize;
			    	m.map[m.player[i].y][m.player[i].x].cross=false;
			    	m.magicSize++;
		    	}
		    	//もしそこにボムがあるなら
		    	else{
					//やりやすいように変数に入れる
					int id=m.map[m.player[i].y][m.player[i].x].bomb;
					if(pat[i]>=50&&m.magic[id].turn>pat[i]/10)m.magic[id].turn=pat[i]/10;
					else if(m.magic[id].turn>5)m.magic[id].turn=5;
					if(m.magic[id].pow<m.player[i].pow)m.magic[id].pow=m.player[i].pow;
		    	}
		    	//ボムを置くならカウントしておく
			    m.player[i].put++;
		    }
	    }
	    //残っているボムのカウントを1減らす
	    for(int i=0;i<m.magicSize;i++)m.magic[i].turn--;
	    //ターン数順にボムをソートし直す
	    m.magicSort();
	    //ソートでボムが入れ替わった場合mapのbombも変えないといけないため更新する
	    for(int i=0;i<m.magicSize;i++){
	    	if(!m.magic[i].bursted)m.map[m.magic[i].y][m.magic[i].x].bomb=i;
	    }
	    //ターンを1数進めておく　limitblock用
	    m.mapTurn++;
	    //this.pw.println("mapturn "+m.mapTurn);
	    //次のターンになってブロックが降ってくるかもしれない計算をする もし自キャラがやられたらfalseを返す
	    if(!m.limitBlock(m.player[0].x,m.player[0].y,m.player[1].x,m.player[1].y)){
	    	//this.pw.println("Move On block!");
	    	return false;
	    }
	    //m.magicPrint();
	    //m.mapPrint();
	    //this.pw.println("MagicSize "+m.magicSize);
	    return true;
	}
	//全探索などの前にこの動きができるかどうかを調べておく
	public Boolean correctInput(Map m,int pat1,int pat2,Boolean player){
		int offset=0;
		//もし敵側の照会ならばoffsetを2にする
		if(!player)offset=2;
		Map m2=new Map(m.X,m.Y,pw);
		m2.mapClone(m);
		//キャラ座標をupdateしておく
		if(pat1%5==1){m2.player[offset].x--;}
		else if(pat1%5==2){m2.player[offset].y--;}
		else if(pat1%5==3){m2.player[offset].x++;}
		else if(pat1%5==4){m2.player[offset].y++;}
		if(pat2%5==1){m2.player[offset+1].x--;}
		else if(pat2%5==2){m2.player[offset+1].y--;}
		else if(pat2%5==3){m2.player[offset+1].x++;}
		else if(pat2%5==4){m2.player[offset+1].y++;}
		//移動できない場所に移動しようとしたときはfalse　動かない時は構わない
		if(pat1%5!=0&&!m2.map[m2.player[offset].y][m2.player[offset].x].cross)return false;
		if(pat2%5!=0&&!m2.map[m2.player[offset+1].y][m2.player[offset+1].x].cross)return false;
		//これ以上ボムを置けない時に置こうとしたらfalse　playerのみ見る
		if(player&&pat1>=5&&!m2.player[offset].isRest(1))return false;
		if(player&&pat2>=5&&!m2.player[offset+1].isRest(1))return false;
		return true;
	}
	//playerが敵の動き方によっては死ぬかどうかを判定する 敵は自爆をしてくる前提で考える
	public int playerIsDeath2(Map m,int n,int pat1,int pat2){
		//this.pw.println("n "+n+"player move "+pat1+" "+pat2);
		//this.pw.println("player move from"+m.player[0].x+" "+m.player[0].y);
		Map m2=new Map(m.X,m.Y,pw);
		m2.mapClone(m);
		//死ぬカウントをする
		int totaldeathcount=0;
		//キャラ座標をupdateしておく
		if(pat1%5==1){m2.player[0].x--;}
		else if(pat1%5==2){m2.player[0].y--;}
		else if(pat1%5==3){m2.player[0].x++;}
		else if(pat1%5==4){m2.player[0].y++;}
		//キャラ座標をupdateしておく
		if(pat2%5==1){m2.player[1].x--;}
		else if(pat2%5==2){m2.player[1].y--;}
		else if(pat2%5==3){m2.player[1].x++;}
		else if(pat2%5==4){m2.player[1].y++;}
		////敵の操作を全通り行う　ｎが0の時だけボムを移動して置く操作のみする
		int p=0;
		if(n==0)p=6;
		for(int i=p;i<10;i++){
			for(int j=p;j<10;j++){
				Map m3=new Map(m2.X,m2.Y,pw);
				m3.mapClone(m2);
				int deathcount=0;
				//動ける入力かどうかを判断し無理なら次へ
				if(!correctInput(m3, i, j, false))continue;
				//キャラ座標をupdateしておく
				if(i%5==1){m3.player[2].x--;}
				else if(i%5==2){m3.player[2].y--;}
				else if(i%5==3){m3.player[2].x++;}
				else if(i%5==4){m3.player[2].y++;}
				//キャラ座標をupdateしておく
				if(j%5==1){m3.player[3].x--;}
				else if(j%5==2){m3.player[3].y--;}
				else if(j%5==3){m3.player[3].x++;}
				else if(j%5==4){m3.player[3].y++;}
				//intの配列に行動の仕方を入れる
				int pat[]={pat1,pat2,i,j};
				//指定された動きでmapをupdateする
				if(virtualMapUpdate(m3, pat)){
					//this.pw.println("e1 "+i+" e2 "+j);
					//死ぬかを判定するためにボムを爆発させなければならないため違うmapを用意する
					Map m4=new Map(m3.X,m3.Y,pw);					
					m4.mapClone(m3);
					//爆発範囲を調べる
					m4.bombErea();
					int death=0;
					//death判定  味方が死ぬならfalseを返す 死ぬのは6手先まで見る　すでに1手動いているのでnに1を入れる
					death=m4.isDeath(m4.player[0].x, m4.player[0].y, 0, deathTurn, 0);
					if(death==-1){
						//this.pw.println("p1 may death!");
						deathcount++;
					}
					death=m4.isDeath(m4.player[1].x, m4.player[1].y, 0, deathTurn, 0);
					if(death==-1){
						//this.pw.println("p2 may death!");
						deathcount++;
					}
				}
				//p1,p2が途中で死んだ場合もtrueを返す
				else{
					deathcount+=2;
					//this.pw.println("Move on burst!");
				}
				//もしこれまでの移動ですでに死んでいる場合は省く
				if(deathcount!=0){
					totaldeathcount+=deathcount+n*1000;
					continue;
				}
				//もしnが0であれば終了
				if(n==0)continue;
				//アイテムを取ったら能力をupdateする 移動→爆弾設置→能力更新なのでこの順番で良い
				for(int k=0;k<4;k++){
					if(m3.map[m3.player[k].y][m3.player[k].x].item==0){
						m3.player[k].hav++;
						m3.map[m3.player[k].y][m3.player[k].x].item=-1;
					}
					else if(m3.map[m3.player[k].y][m3.player[k].x].item==1){
						m3.player[k].pow++;
						m3.map[m3.player[k].y][m3.player[k].x].item=-1;
					}
				}
				//留る動く5通りを計算する　その動きの中で死なない動きがないかを計算する
				int min_death=1000;
				for(int s=0;s<5;s++){
					for(int t=0;t<5;t++){
						//動ける入力かどうかを判断し無理なら次へ
						if(!correctInput(m3, s, t, true))continue;
						int d=playerIsDeath2(m3,n-1,s,t);
						if(min_death>d)min_death=d;
						if(min_death==0){
							//this.pw.println("deathcount "+deathcount);
							//this.pw.println("safety move "+pat1+" "+pat2+" "+s+" "+t);
							//m3.magicPrint();
							break;
						}
					}
					if(min_death==0)break;
				}
				totaldeathcount+=min_death;
			}
		}
		//敵がどのようにボムをおいても死ななければtrueを返す
		return totaldeathcount;
	}
	//一手先まで見てプレイヤーが死ぬ可能性が0のプレイヤーの数を返す
	public int playerIsDeath3(Map m,int pat1,int pat2){
		//this.pw.println("n "+n+"player move "+pat1+" "+pat2);
		//this.pw.println("player move from"+m.player[0].x+" "+m.player[0].y);
		Map m2=new Map(m.X,m.Y,pw);
		m2.mapClone(m);
		//死ぬカウントをする
		int count=0;
		int deathcount1=0;
		int deathcount2=0;
		//キャラ座標をupdateしておく
		if(pat1%5==1){m2.player[0].x--;}
		else if(pat1%5==2){m2.player[0].y--;}
		else if(pat1%5==3){m2.player[0].x++;}
		else if(pat1%5==4){m2.player[0].y++;}
		//キャラ座標をupdateしておく
		if(pat2%5==1){m2.player[1].x--;}
		else if(pat2%5==2){m2.player[1].y--;}
		else if(pat2%5==3){m2.player[1].x++;}
		else if(pat2%5==4){m2.player[1].y++;}
		for(int i=5;i<10;i++){
			for(int j=5;j<10;j++){
				Map m3=new Map(m2.X,m2.Y,pw);
				m3.mapClone(m2);
				//動ける入力かどうかを判断し無理なら次へ
				if(!correctInput(m3, i, j, false))continue;
				//キャラ座標をupdateしておく
				if(i%5==1){m3.player[2].x--;}
				else if(i%5==2){m3.player[2].y--;}
				else if(i%5==3){m3.player[2].x++;}
				else if(i%5==4){m3.player[2].y++;}
				//キャラ座標をupdateしておく
				if(j%5==1){m3.player[3].x--;}
				else if(j%5==2){m3.player[3].y--;}
				else if(j%5==3){m3.player[3].x++;}
				else if(j%5==4){m3.player[3].y++;}
				//intの配列に行動の仕方を入れる
				int pat[]={pat1,pat2,i,j};
				//指定された動きでmapをupdateする
				if(virtualMapUpdate(m3, pat)){
					count++;
					//this.pw.println("e1 "+i+" e2 "+j);
					//死ぬかを判定するためにボムを爆発させなければならないため違うmapを用意する
					Map m4=new Map(m3.X,m3.Y,pw);					
					m4.mapClone(m3);
					//爆発範囲を調べる
					m4.bombErea();
					int death=0;
					//death判定  味方が死ぬならfalseを返す 死ぬのは6手先まで見る　すでに1手動いているのでnに1を入れる
					death=m4.isDeath(m4.player[0].x, m4.player[0].y, 0, deathTurn, 0);
					if(death==-1){
						//this.pw.println("p1 may death!");
						deathcount1++;
					}
					death=m4.isDeath(m4.player[1].x, m4.player[1].y, 0, deathTurn, 0);
					if(death==-1){
						//this.pw.println("p2 may death!");
						deathcount2++;
					}
				}
				//p1,p2が途中で死んだ場合もtrueを返す
				else{
					continue;
					//this.pw.println("Move on burst!");
				}
			}
		}
		int death=0;
		if(deathcount1!=0)death++;
		if(deathcount2!=0)death++;
		//もしcountされないような動きしかしていなければdeathを+する
		if(count==0)death+=2;
		//敵がどのようにボムをおいても死ななければtrueを返す
		return death;
	}
	//敵のうごきによって倒せる可能性があるかを探す　敵の動ける数の中でどれだけ多く倒せるかをカウントし可能性の多い回答を選ぶ
	public int virtualAttack2(Map m,int pat1,int pat2){
		Map m2=new Map(m.X,m.Y,pw);
		m2.mapClone(m);
		//動ける入力かどうかを判断し無理なら次へ
		if(!correctInput(m2, pat1, pat2, true))return -1;
		//キャラ座標をupdateしておく
		if(pat1%5==1){m2.player[0].x--;}
		else if(pat1%5==2){m2.player[0].y--;}
		else if(pat1%5==3){m2.player[0].x++;}
		else if(pat1%5==4){m2.player[0].y++;}
		//キャラ座標をupdateしておく
		else if(pat2%5==1){m2.player[1].x--;}
		else if(pat2%5==2){m2.player[1].y--;}
		else if(pat2%5==3){m2.player[1].x++;}
		else if(pat2%5==4){m2.player[1].y++;}
		int killcount=0;
		//敵の動ける方向を探す
		for(int i=0;i<5;i++){
			for(int j=0;j<5;j++){
				Map m3=new Map(m2.X,m2.Y,pw);
				m3.mapClone(m2);
				//動ける入力かどうかを判断し無理なら次へ
				if(!correctInput(m3, i, j, false))continue;
				//キャラ座標をupdateしておく
				if(i%5==1){m3.player[2].x--;}
				else if(i%5==2){m3.player[2].y--;}
				else if(i%5==3){m3.player[2].x++;}
				else if(i%5==4){m3.player[2].y++;}
				//キャラ座標をupdateしておく
				if(j%5==1){m3.player[3].x--;}
				else if(j%5==2){m3.player[3].y--;}
				else if(j%5==3){m3.player[3].x++;}
				else if(j%5==4){m3.player[3].y++;}
				//こちらが何もしてない状況で敵が死ぬ行動をするときはカウントしない
				Map m5=new Map(m3.X,m3.Y,pw);
				m5.mapClone(m3);
				m5.bombErea();
				//死ぬかどうかをみて死ぬならcontinue
				if(m5.isDeath(m5.player[2].x, m5.player[2].y, 1, 10, 0)==-1)continue;
				if(m5.isDeath(m5.player[3].x, m5.player[3].y, 1, 10, 0)==-1)continue;
				//intの配列に行動の仕方を入れる
				int pat[]={pat1,pat2,i,j};
				//指定された動きでmapをupdateする
				if(virtualMapUpdate(m3, pat)){
					//死ぬかを判定するためにボムを爆発させなければならないため違うmapを用意する
					Map m4=new Map(m3.X,m3.Y,pw);
					m4.mapClone(m3);
					//爆発範囲を調べる
					m4.bombErea();
					//death判定  味方が死ぬなら-1を返す 死ぬのは6手先まで見る　すでに1手動いているのでnに1を入れる
					int death=m4.isDeath(m4.player[0].x, m4.player[0].y, 0, 10, 0);
					//death判定 敵が死ぬなら-1を返す 死ぬのは6手先まで見る　すでに1手動いているのでnに1を入れる
					death=m4.isDeath(m4.player[2].x, m4.player[2].y, 0, 10, 0);
					if(death==-1){
						killcount++;
					}
					death=m4.isDeath(m4.player[3].x, m4.player[3].y, 0, 10, 0);
					if(death==-1){
						killcount++;
					}

				}
				//途中で死んだ場合も-1を返す
				else return -1;
			}
		}
		//もし敵を倒せる可能性がないなら-1を返す
		if(killcount==0)return -1;
		//敵を倒せるカウントを返す
		return killcount;
	}
	//敵のうごきによっても変わらず確実に敵を倒せる解があるかを探す
	public int virtualAttack3(Map m,int pat1,int pat2){
		//this.pw.println("");
		Map m2=new Map(m.X,m.Y,pw);
		m2.mapClone(m);
		//動ける入力かどうかを判断し無理なら次へ
		if(!correctInput(m2, pat1, pat2, true))return -1;
		//キャラ座標をupdateしておく
		if(pat1%5==1){m2.player[0].x--;}
		else if(pat1%5==2){m2.player[0].y--;}
		else if(pat1%5==3){m2.player[0].x++;}
		else if(pat1%5==4){m2.player[0].y++;}
		//キャラ座標をupdateしておく
		if(pat2%5==1){m2.player[1].x--;}
		else if(pat2%5==2){m2.player[1].y--;}
		else if(pat2%5==3){m2.player[1].x++;}
		else if(pat2%5==4){m2.player[1].y++;}
		int count=0;
		int killcount1=0;
		int killcount2=0;
		//敵の動ける方向を探す
		for(int i=0;i<5;i++){
			for(int j=0;j<5;j++){
				Map m3=new Map(m2.X,m2.Y,pw);
				m3.mapClone(m2);
				//動ける入力かどうかを判断し無理なら次へ
				if(!correctInput(m3, i, j, false))continue;
				//キャラ座標をupdateしておく
				if(i%5==1){m3.player[2].x--;}
				else if(i%5==2){m3.player[2].y--;}
				else if(i%5==3){m3.player[2].x++;}
				else if(i%5==4){m3.player[2].y++;}
				//キャラ座標をupdateしておく
				if(j%5==1){m3.player[3].x--;}
				else if(j%5==2){m3.player[3].y--;}
				else if(j%5==3){m3.player[3].x++;}
				else if(j%5==4){m3.player[3].y++;}
				//intの配列に行動の仕方を入れる
				int pat[]={pat1,pat2,i,j};
				//指定された動きでmapをupdateする
				if(virtualMapUpdate(m3, pat)){
					//判定が正しく行われたらcountをプラスする
					count++;
					//死ぬかを判定するためにボムを爆発させなければならないため違うmapを用意する
					Map m4=new Map(m3.X,m3.Y,pw);
					m4.mapClone(m3);
					//爆発範囲を調べる
					m4.bombErea();
					//m4.bombEreaPrint();
					//death判定  味方が死ぬなら-1を返す 死ぬのは6手先まで見る　すでに1手動いているのでnに1を入れる
					/*int death=m4.isDeath(m4.player[0].x, m4.player[0].y, 0, 12, 0);
					//自キャラが死ぬのは後に判定するので見ない　敵がボムをうまく置いて逃げる可能性があるので0~9まで調べている
					if(death==-1){
						//this.pw.println("p1 may death!");
						return 0;
					}
					death=m4.isDeath(m4.player[1].x, m4.player[1].y, 0, 12, 0);
					if(death==-1){
						//this.pw.println("p2 may death!");
						return 0;
					}*/
					//death判定 敵が死ぬなら-1を返す 死ぬのは6手先まで見る　すでに1手動いているのでnに1を入れる
					int death1=m4.isDeath(m4.player[2].x, m4.player[2].y, 0, deathTurn, 0);
					if(death1==-1){
						//this.pw.println("e1 may death!");
						//this.pw.println("pat1 "+i);
						killcount1++;
					}
					int death2=m4.isDeath(m4.player[3].x, m4.player[3].y, 0, deathTurn, 0);
					if(death2==-1){
						//this.pw.println("e2 may death!");
						//this.pw.println("pat2 "+j);
						//this.pw.println("e1 "+i+" e2 "+j);
						killcount2++;
					}
					//敵をどちらも倒せない解があるときはコレ以上探索しても無意味なので0を返す
					if(death1!=-1&&death2!=-1)return 0;
				}
				//途中で死んだ場合も0を返す
				else return 0;
			}
		}
		//もしcountが0になるかもしれないということを考慮して0の時は0を返す
		if(count==0)return 0;
		//敵を確実に倒せるか判断して倒せる数を返す
		int kill=0;
		if(count==killcount1){
			//this.pw.println("count "+count);
			kill++;
		}
		if(count==killcount2)kill++;
		//敵を倒せる数を返す
		return kill;
	}
	//配列の中身をシャッフルして最後に0を加える
	public int[] arrayShuffle(int p[],int size){
		int tmp=0;
		for(int i=0;i<size-1;i++){
			int num=(int)((size-i)*Math.random())+i;
			tmp=p[i];
			p[i]=p[num];
			p[num]=tmp;
		}
		//入力された配列から0を入れるために一つ長い入れるを作る
		int pat[]=new int[size+1];
		for(int i=0;i<size;i++)pat[i]=p[i];
		pat[size]=0;
		return pat;
	}
	//できるだけ中心に向かうように優先して探索する　とどまるのは最後　ボムを置いたら逃げれる可能性を考えて
	public int[] centerArray(int x,int y){
		int pat[]=new int[10];
		if(x<=map.X/2&&y<=map.Y/2){
			if(x<=y){pat[0]=3;pat[1]=4;pat[2]=2;pat[3]=1;}
			else {pat[0]=4;pat[1]=3;pat[2]=1;pat[3]=2;}
		}
		else if(x<=map.X/2&&y>map.Y/2){
			if(x<=map.Y-y-1){pat[0]=3;pat[1]=2;pat[2]=4;pat[3]=1;}
			else {pat[0]=2;pat[1]=3;pat[2]=1;pat[3]=4;}
		}
		else if(x>map.X/2&&y<=map.Y/2){
			if(map.X-x-1<=y){pat[0]=1;pat[1]=4;pat[2]=2;pat[3]=3;}
			else {pat[0]=4;pat[1]=1;pat[2]=3;pat[3]=2;}
		}
		else if(x>map.X/2&&y>map.Y/2){
			if(map.X-x-1<=map.Y-y-1){pat[0]=1;pat[1]=2;pat[2]=4;pat[3]=3;}
			else {pat[0]=2;pat[1]=1;pat[2]=3;pat[3]=4;}
		}
		pat[4]=0;
		for(int i=0;i<5;i++)pat[i+5]=pat[i]+5;
		return pat;
	}
	//敵をターンの長いボムで囲んで倒す　
	public int[] surroundAttackPoint(Map m){
		Map m2=new Map(m.X,m.Y,pw);
		m2.mapClone(m);
		//p1p2からの距離を求める それだけのためのMapを作成する
		Map mp1=new Map(m.X,m.Y,pw);
		mp1.mapClone(m);
		mp1.walkErea(mp1.player[0].x, mp1.player[0].y, 0);
		Map mp2=new Map(m.X,m.Y,pw);
		mp2.mapClone(m);
		mp2.walkErea(mp2.player[1].x, mp2.player[1].y, 0);
		//最短の距離を保存する変数
		int min_dis=1000;
		int total_dis=1000;
		//最短の場所に関する情報を保存する変数 　原点のx,y座標　p1の始点のx,y座標　p1の終点のx,y座標　p2の始点のx,y座標　p2の終点のx,y座標　targetの方向　p1必要歩数　p2必要歩数
		int best_point[]=new int[13];
		best_point[0]=-1;
		//マップの中心に近い十字路を見る
		for(int j=1;j<m2.Y-1;j+=2){
			for(int i=1;i<m2.X-1;i+=2){
				//座標をわかりやすいように他の変数に保存　敵が2マス内側にいるような場所を選ぶ
				int x[]={i,-i,m2.player[2].x+1,-(m2.player[2].x-1),m2.player[3].x+1,-(m2.player[3].x-1)};
				int y[]={j,-j,m2.player[2].y+1,-(m2.player[2].y-1),m2.player[3].y+1,-(m2.player[3].y-1)};
				//そこを原点として左上，左下，右下，右上の順で見て敵二人がその方向にいるかを判定
				int target=-1;
				for(int k=0;k<4;k++){
					//%を用いる方式では反時計回りに見ることができないので新たな変数を用いる
					int tmp=0;
					if(k==1||k==2)tmp=1;
					//+,-を用いることで同じ不等号で様々な方向を見れるようにしている
					if(x[k/2]>x[k/2+2]&&x[k/2]>x[k/2+4]&&y[tmp]>y[tmp+2]&&y[tmp]>y[tmp+4]){
						target=k;
						break;
					}
				}
				//敵がどこから見ても範囲に収まらないなら次を見る
				if(target==-1)continue;
				//敵が範囲に収まるなら原点を起点とした包囲ができるかを考える
				else{
					//this.pw.println("targetFound "+target+" point "+i+" "+j);
					//上左下右上の順で見ていく
					int ad[]={0,-1,-1,0,0,1,1,0,0,-1};
					//始点と終点を保存するための変数
					int dst[]=new int[8];
					//始点を保存しておく
					dst[0]=dst[4]=i+ad[(target)*2]*2;
					dst[1]=dst[5]=j+ad[(target)*2+1]*2;
					dst[2]=dst[6]=i+ad[(target+1)*2]*2;
					dst[3]=dst[7]=j+ad[(target+1)*2+1]*2;
					//始点の座標が遠すぎる場所なら探索を止める(立ち入れない場所も含める)
					if(mp1.map[dst[1]][dst[0]].walk>20&&mp2.map[dst[1]][dst[0]].walk>20)continue;
					if(mp1.map[dst[3]][dst[2]].walk>20&&mp2.map[dst[3]][dst[2]].walk>20)continue;
					//原点からtargetの方向を囲む順路をたどり，置ける場所までを記録する
					for(int s=0;s<2;s++){
						//原点から進んだ十字路から壁に当たるまで探索
						for(int t=2;t<15;t++){
							int u=i+ad[(target+s)*2]*t;
							int v=j+ad[(target+s)*2+1]*t;
							//端まで行くなら終わる
							if(u==0||v==0||u==m2.X-1||v==m2.Y-1)break;
							//もし壁に突き当たるなどした時はそこまでとする
							if(!m2.map[v][u].cross)break;
							//移動できない場所でないなら次に進む
							dst[4+s*2]=u;
							dst[4+s*2+1]=v;
						}
					}
					//上で通れる場所にボムをおいた場合敵を倒すことができるかを検証
					Map m3=new Map(m.X,m.Y,pw);
					m3.mapClone(m);
					//他の要素で倒せてしまうことが無いよう置くボムのみで判定する
					m3.clearBomb();
					m3.clearBurst();
					int kill=0;
					//二人の火力が違うので2回行う
					for(int k=0;k<2;k++){
						for(int s=0;s<2;s++){
							//ボムを先ほど求めた始点から終点までの十字路の数だけ置く
							for(int t=0;t<Math.abs(i+j-dst[4+s*2]-dst[4+s*2+1])/2;t++){
								//ボムを更新
						    	m3.magic[m3.magicSize].id=s;
						    	//十字路にボムを置く
						    	m3.magic[m3.magicSize].x=i+ad[(target+s)*2]*(t+1)*2;
						    	m3.magic[m3.magicSize].y=j+ad[(target+s)*2+1]*(t+1)*2;
						    	m3.magic[m3.magicSize].turn=20;
						    	m3.magic[m3.magicSize].pow=m3.player[Math.abs(s-k)].pow;
						    	m3.magic[m3.magicSize].bursted=false;
						    	//マップも更新する
						    	m3.map[m3.magic[m3.magicSize].y][m3.magic[m3.magicSize].x].bomb=m3.magicSize;
						    	m3.map[m3.magic[m3.magicSize].y][m3.magic[m3.magicSize].x].cross=false;
						    	m3.magicSize++;
							}
						}
						m3.bombErea();
						//m3.bombEreaPrint();
						int death1=m3.isDeath(player[2].x, player[2].y, 0, deathTurn, 0);
						int death2=m3.isDeath(player[3].x, player[3].y, 0, deathTurn, 0);
						if(death1==-1&&death2==-1){
							//敵を倒せる判定の結果を保存　0では不可　1か2では片方だけ　3ではどちらでも可
							kill+=k+1;
						}
					}
					//もし敵を倒せないことがわかればここで処理を終了
					if(kill==0)continue;
					//this.pw.println("kakomeru point "+i+" "+j);
					//ボムを置く始点終点全ての距離を予め配列に入れておく
					int walk[]=new int[8];
					//原点から2マスだけターゲットの方向に動いた場所にかかる歩数
					walk[0]=mp1.map[dst[1]][dst[0]].walk;
					//ボムを置く終点から置き始めるとしてそこの場所にかかる歩数
					walk[1]=mp1.map[dst[5]][dst[4]].walk;
					walk[2]=mp2.map[dst[3]][dst[2]].walk;
					walk[3]=mp2.map[dst[7]][dst[6]].walk;
					walk[4]=mp2.map[dst[1]][dst[0]].walk;
					walk[5]=mp2.map[dst[5]][dst[4]].walk;
					walk[6]=mp1.map[dst[3]][dst[2]].walk;
					walk[7]=mp1.map[dst[7]][dst[6]].walk;
					//置く担当を入れ替えても探索
					for(int k=0;k<2;k++){
						//敵を倒せないパターンのときはスルーする
						if(k==0&&kill==2)continue;
						else if(k==1&&kill==1)continue;
						//ボムを置き始めるスタート地点を始点終点の2*2の4通りで探し，最もキャラから近い場所を求める
						for(int s=0;s<2;s++){
							//中心からのみのパターンで見る
							if(s==1)continue;
							for(int t=0;t<2;t++){
								//中心からのみのパターンで見る
								if(t==1)continue;
								//2人の距離の合計とその最大歩数を取る
								int dis=walk[k*6+t]+walk[k*2+2+s];
								int walk1=walk[k*6+t];
								int walk2=walk[k*2+2+s];
								//大きい方を保存する
								if(walk1<walk2)walk1=walk2;
								//その場所に行くまでにかかる最大歩数が最も小さいものを選ぶ
								if(min_dis>=walk1){
									if(min_dis==walk1&&total_dis<=dis)continue;
									else total_dis=dis;
									//this.pw.println("Answer point "+i+" "+j);
									min_dis=dis;
									//kが1ではsとtが入れ替わるのでそれを処理する
									int tmpt=t;
									int tmps=s;
									if(k==1){tmpt=s;tmps=t;}
									//best pointに渡すための変数　原点のx,y座標　p1の始点のx,y座標　p1の終点のx,y座標　p2の始点のx,y座標　p2の終点のx,y座標　targetの方向　p1必要歩数　p2必要歩数
									int point[]={i,j,dst[tmpt*4+k*2],dst[tmpt*4+k*2+1],dst[(1-tmpt)*4+k*2],dst[(1-tmpt)*4+k*2+1],dst[s*4+(1-k)*2],dst[tmps*4+(1-k)*2+1],dst[(1-tmps)*4+(1-k)*2],dst[(1-tmps)*4+(1-k)*2+1],target,walk[k*6+tmpt],walk[k*2+2+tmps]};
									best_point=point;
								}
							}
						}
					}
				}
			}
		}
		//this.pw.println("surroundAttackPoint");
		//String string="";
		//for(int i=0;i<best_point.length;i++)string+= best_point[i]+" ";
		//this.pw.println(string);
		return best_point;
	}
	//敵を囲む座標が決まって攻撃を始めた後に囲み続ける行動を実行　目標座標を返す
	public int[] surroundAttack(Map m,Boolean p1){
		Map m2=new Map(m.X,m.Y,pw);
		m2.mapClone(m);
		//プレイヤー番号を代入
		int p=0;
		if(!p1)p=2;
		int dstpoint[]={-1,0};
		//始点と終点の差から変位を求める
		int toword=-1;
		if(sur.x[p]==sur.x[p+1]){
			if(sur.y[p]<sur.y[p+1])toword=3;
			else toword=1;
		}
		else{
			if(sur.x[p]<sur.x[p+1])toword=2;
			else toword=0;
		}
		int adx[]={-2,0,2,0};
		int ady[]={0,-2,0,2};
		//置くべき場所を網羅していく
		for(int i=0;i<7;i++){
			//もし置くべき場所にボムがないならそこを目的地とする
			if(m2.map[sur.y[p]+ady[toword]*i][sur.x[p]+adx[toword]*i].bomb==-1){
				dstpoint[0]=sur.x[p]+adx[toword]*i;
				dstpoint[1]=sur.y[p]+ady[toword]*i;
				break;
			}
			//最後までボムが置かれているなら終了
			else if(sur.x[p]+adx[toword]*i==sur.x[p+1]&&sur.y[p]+ady[toword]*i==sur.y[p+1])break;
		}
		return dstpoint;
	}
	//correctitem用のmapupdate 移動先のアイテムを消してボムを置いたと仮定して周りのブロックが破壊されるとする
	public void virtualMapUpdate2(Map p,int x,int y,int pow){
		p.map[y][x].item=-1;
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
				if(p.map[y+offset[count+(k+1)*pow]][x+offset[count+k*pow]].hard)break;
				//ソフトブロックで爆発の予定がないなら
				if(p.map[y+offset[count+(k+1)*pow]][x+offset[count+k*pow]].soft&&p.map[y+offset[count+(k+1)*pow]][x+offset[count+k*pow]].burst[0]==-1){
					//そのブロックを爆発の範囲にあるとする
					p.map[y+offset[count+(k+1)*pow]][x+offset[count+k*pow]].burst[0]=5;
					break;
				}
				count++;
			}
		}
	}
}
