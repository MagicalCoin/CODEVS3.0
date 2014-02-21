//爆弾のクラス
public class Magic {
	public int id=0;
	public int x=0;
	public int y=0;
	public int turn=0;//爆発までのターン
	public int pow=0;//火力
	public Boolean bursted=false;//すでに爆発したかの判定
	public void magicClone(Magic m){
		this.id=m.id;
		this.x=m.x;
		this.y=m.y;
		this.turn=m.turn;
		this.pow=m.pow;
		this.bursted=m.bursted;
	}
}
