
public class Item {
	public int type=0;//0がボム数増で1が火力増
	public int x=0;
	public int y=0;
	public void itemClone(Item i){
		this.type=i.type;
		this.x=i.x;
		this.y=i.y;
	}
}
