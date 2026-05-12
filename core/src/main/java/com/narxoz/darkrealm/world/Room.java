package com.narxoz.darkrealm.world;



public class Room {
    public int x, y, w, h;
    public Room(int x,int y,int w,int h){ this.x=x; this.y=y; this.w=w; this.h=h; }
    public int centerX(){ return x+w/2; }
    public int centerY(){ return y+h/2; }
    public boolean overlaps(Room o){
        return x<o.x+o.w+1&&x+w+1>o.x&&y<o.y+o.h+1&&y+h+1>o.y;
    }
}
