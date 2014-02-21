//playerのクラス
public class Player {
	public int pid=0;
	public int id=0;
	public int x=0;
	public int y=0;
	public int pow=0;//火力
	public int hav=0;//個数
	public int put=0;//置いてる個数
	public void playerClone(Player p){
		this.pid=p.pid;
		this.id=p.id;
		this.x=p.x;
		this.y=p.y;
		this.pow=p.pow;
		this.hav=p.hav;
		this.put=p.put;
	}
	public Boolean isRest(int rest){
		return hav<put+rest ? false:true;
	}
}