package com.zhd.geometry.algorithm;

import com.zhd.geometry.structure.Octree;
import com.zhd.geometry.structure.OctreeGrid;
import com.zhd.geometry.structure.Point3D;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class OctreeSpaceEncoder {

    private static int M=10;
    private static List<OctreeGrid> result;
    private static Octree octree;
    private static Octree octree2;

    public static void setM(int M_){ M=M_;}

    public static Point3D getGridLowerBound(OctreeGrid grid){
        return new Point3D(
                grid.getX(),
                grid.getY(),
                grid.getZ()
        );
    }

    public static Point3D getGridUpperBound(OctreeGrid grid){
        Point3D p = getGridLowerBound(grid);
        int k=grid.getK();
        return new Point3D(
                p.getX()+(1<<(M-k)),
                p.getY()+(1<<(M-k)),
                p.getZ()+(1<<(M-k))
        );
    }

    /**
     * 根据中心点和bounding box找到一组八叉树网格
     * @return
     */
    public static List<OctreeGrid> encodeWithBoundingBox(Point3D pc,Point3D pMin,Point3D pMax){
        // 1. 找到中心所在的最小适配 [可改成二分]
        int fitK;
        for(fitK=M;fitK>=0;fitK--){
//            System.out.println("======fitK:"+fitK);
//            System.out.println(1<<(M-fitK));
//            System.out.println(Math.min(pMax.getZ()-pMin.getZ(),Math.min(pMax.getX()-pMin.getX(),pMax.getY()-pMin.getY())));
//            System.out.println(isInsideBoundingBox(OctreeGrid.point2OctreeGrid(pc,fitK,M),pMax,pMin));
            if((1<<(M-fitK))>Math.min(pMax.getZ()-pMin.getZ(),Math.min(pMax.getX()-pMin.getX(),pMax.getY()-pMin.getY()))) continue;
            if(!isInsideBoundingBox(OctreeGrid.point2OctreeGrid(pc,fitK,M),pMax,pMin)) break;
        }
        fitK++;
        OctreeGrid fitGrid = OctreeGrid.point2OctreeGrid(pc, fitK, M);
        octree = new Octree(M);  // octree作为set来判重
        octree2 = new Octree(M);  // octree作为set来判重
        result=new ArrayList<>();
//        dfsForBoundingBoxEncoder(fitGrid,pMax,pMin,0);
        bfsForBoundingBoxEncoding(fitGrid,pMax,pMin);

        return result;
    }

    public static boolean isInsideBoundingBox(OctreeGrid grid,Point3D pMax,Point3D pMin){
        Point3D pl = getGridLowerBound(grid);
        Point3D pu = getGridUpperBound(grid);
        return Point3D.smallerOrEqual(pu,pMax) && Point3D.smallerOrEqual(pMin,pl);
    }

    public static boolean isOutsideBoundingBox(OctreeGrid grid,Point3D pMax,Point3D pMin){
        Point3D pl = getGridLowerBound(grid);
        Point3D pu = getGridUpperBound(grid);
        return pu.getX()<=pMin.getX() || pl.getX()>=pMax.getX()
                || pu.getY()<=pMin.getY() || pl.getY()>=pMax.getY()
                || pu.getZ()<=pMin.getZ() || pl.getZ()>=pMax.getZ();
    }

    // grid的0-7哪块和bounding box 冲突了
    private static List<OctreeGrid> getCrossAreaBoundingBox(OctreeGrid grid,Point3D pMax,Point3D pMin){
        List<OctreeGrid> result = new ArrayList<>();
        if(isOutsideBoundingBox(grid,pMax,pMin)) return result;
        for(int i=0;i<8;i++){
            OctreeGrid subOctreeGrid = grid.getSubOctreeGrid(i, M);
            if(!isOutsideBoundingBox(subOctreeGrid,pMax,pMin)){
                result.add(subOctreeGrid);
            }
        }
        return result;
    }

    private static void bfsForBoundingBoxEncoding(OctreeGrid startGrid, Point3D pMax, Point3D pMin){
        LinkedList<OctreeGrid> Q = new LinkedList<>();
        Q.addLast(startGrid);
        octree.insert(startGrid);
        while(!Q.isEmpty()){
            OctreeGrid grid = Q.removeFirst();
            if(isInsideBoundingBox(grid,pMax,pMin)) {
                result.add(grid);
                octree2.insert(grid);
                OctreeGrid[] neighbors = grid.getNeighbors(M);
                for(OctreeGrid neighbor: neighbors){
                    if(neighbor==null||neighbor.getX()<0||neighbor.getY()<0||neighbor.getZ()<0) continue;
                    if(octree.has(neighbor)) continue;
                    Q.addLast(neighbor);
                    octree.insert(neighbor);
                }
            }else{
//                System.out.println("========== else "+grid+","+octree.has(grid));
                if(grid.getK()==M) continue;
                List<OctreeGrid> subGrids = getCrossAreaBoundingBox(grid, pMax, pMin);
                for(OctreeGrid subGrid: subGrids){
//                    System.out.println("sub "+subGrid+","+octree2.has(subGrid));
                    if(subGrid==null||octree2.has(subGrid)) continue;
                    Q.addLast(subGrid);
                    octree.insert((subGrid));
                }
            }

        }
    }

    private static void dfsForBoundingBoxEncoder(OctreeGrid startGrid, Point3D pMax, Point3D pMin,int depth) {

        if(depth>2000) return ;
        if(startGrid==null||startGrid.getX()<0||startGrid.getY()<0||startGrid.getZ()<0) return ;
//        System.out.println("DFS:"+startGrid);
        if(octree.has(startGrid)) return ; // 访问过
        if (isInsideBoundingBox(startGrid, pMax, pMin)) { // 完全被包含，则加入结果集并标记访问过
            result.add(startGrid);
//            System.out.println("=====Add result:"+startGrid);
            octree.insert(startGrid);
            OctreeGrid[] neighbors = startGrid.getNeighbors(M);
            for (OctreeGrid neighbor : neighbors)
                if (neighbor != null) dfsForBoundingBoxEncoder(neighbor, pMax, pMin,depth+1);
            return;
        }
        if (startGrid.getK() == M) return; // 已经是最小的块 且不在范围内，则不继续搜索
        List<OctreeGrid> subGrids = getCrossAreaBoundingBox(startGrid, pMax, pMin);
        for (OctreeGrid subGrid : subGrids) {
            dfsForBoundingBoxEncoder(subGrid, pMax, pMin,depth+1); // 向冲突的区域搜索
        }

    }
}
