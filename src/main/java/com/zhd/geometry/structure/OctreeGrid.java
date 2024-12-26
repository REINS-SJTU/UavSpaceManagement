package com.zhd.geometry.structure;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OctreeGrid {
    // 正序存，共M位，M位中前k位有效
    private int x;
    private int y;
    private int z;
    private int k;

    // Point转Grid: 找到 Point所在的Grid
    public static OctreeGrid point2OctreeGrid(Point3D p,int k,int M){
        int x_= (int) Math.floor(p.getX());
        int y_= (int) Math.floor(p.getY());
        int z_= (int) Math.floor(p.getZ());
        x_ >>=(M-k); x_ <<=(M-k);
        y_ >>=(M-k); y_ <<=(M-k);
        z_ >>=(M-k); z_ <<=(M-k);
        return new OctreeGrid(
                x_,y_,z_,k
        );
    }

    // 得到在第n层中的子块序号，1<=n<=k
    public int getOctreeNumber(int n,int M){
        int x_ = (x>>(M-n))&1;
        int y_ = (y>>(M-n))&1;
        int z_ = (z>>(M-n))&1;
        return (z_<<2)+(y_<<1)+x_;
    }

    // 获得第n个子块,n=0~7
    public OctreeGrid getSubOctreeGrid(int n,int M){
        int x_ = n&1;
        int y_ = (n>>1)&1;
        int z_ = (n>>2)&1;
        return new OctreeGrid(
                x_>0? setBit(x,M-k-1) : x,
                y_>0? setBit(y,M-k-1) : y,
                z_>0? setBit(z,M-k-1) : z,
                k+1
        );
    }

    // 获取上下左右前后六个相邻同大小的块
    public OctreeGrid[] getNeighbors(int M){
        int[][] directions = new int [][]{
                {1,0,0},{-1,0,0},
                {0,1,0},{0,-1,0},
                {0,0,1},{0,0,-1}
        };
        OctreeGrid[] neighbors = new OctreeGrid[6];
        for(int i=0;i<directions.length;i++){
            int [] d=directions[i];

            int x_ = (x>>(M-k))+d[0];
            if(x_<0||x_>=(1<<k)) neighbors[i]=null;
            x_<<=(M-k);

            int y_ = (y>>(M-k))+d[1];
            if(y_<0||y_>=(1<<k)) neighbors[i]=null;
            y_<<=(M-k);

            int z_ = (z>>(M-k))+d[2];
            if(z_<0||z_>=(1<<k)) neighbors[i]=null;
            z_<<=(M-k);

            neighbors[i]=new OctreeGrid(x_,y_,z_,k);
        }
        return neighbors;
    }

    // 将n的后往前数第t位设置为1
    private static int setBit(int num,int t){
        return num|(1<<t);
    }




    // 转成倒序
    public OctreeGrid reverseBits(){
        return new OctreeGrid(
                reverse(x),
                reverse(y),
                reverse(z),
                k
        );
    }

    private int reverse(int n){
        int t=0;
        while(n>0){
            t=(t<<1)|(n&1);
            n>>=1;
        }
        return t;
    }
}
