package com.zhd.geometry.algorithm;

import com.zhd.entity.tmp.DivisionPlan2;
import com.zhd.geometry.structure.Octree;
import com.zhd.geometry.structure.OctreeGrid;
import com.zhd.geometry.structure.OctreeNode;
import com.zhd.geometry.structure.Point3D;
import javafx.util.Pair;


import java.util.*;


public class CollisionDecider {

    private static Map<String,Integer> id2Priority;
    private static Map<String,DivisionPlan2> mp;
    private static int M=10;

    public static Map<String, DivisionPlan2> decideCollisionBasedOnPriority(Map<String,DivisionPlan2> mp_, Map<String,Integer> id2Priority_, Octree octree){
        M=octree.getM();
        id2Priority=id2Priority_;
        mp=mp_;
        dfs(octree.getRoot(), new OctreeGrid(0, 0, 0, 0));
        return mp;
    }

    private static List<Pair<String,OctreeGrid>> dfs(OctreeNode parent, OctreeGrid grid){
        List<Pair<String,OctreeGrid>> higherPriorityIds= new ArrayList<>();
        if(parent==null) return higherPriorityIds;
        String maxId = getMaxPriorityOfOctreeNode(parent, grid);
        if(parent.isLeaf()){
            if(maxId!=null)higherPriorityIds.add(new Pair<>(maxId,grid));
            return higherPriorityIds;
        }

        for(int i=0;i<8;i++){
            OctreeNode child = parent.getKthChild(i);
            OctreeGrid subGrid = grid.getSubOctreeGrid(i, M);
            List<Pair<String,OctreeGrid>> ids2 = dfs(child, subGrid);
            if(maxId==null){
                higherPriorityIds.addAll(ids2);
            }else
                for(Pair<String,OctreeGrid> pair:ids2){
                    // 子优先级比父高，父挖块
                    if(id2Priority.get(pair.getKey())>id2Priority.get(maxId)){
                        excludeGrid(maxId,pair.getValue());
                        higherPriorityIds.add(pair);
                    }else{
                        // 父优先级高，子挖块
                        excludeGrid(pair.getKey(), pair.getValue());
                    }
                }
        }
        if(maxId!=null) higherPriorityIds.add(new Pair<>(maxId,grid));

        return higherPriorityIds;
    }

    private static void excludeGrid(String id,OctreeGrid grid){
        DivisionPlan2 divisionPlan2 = mp.get(id);
        List<OctreeGrid> exclude = divisionPlan2.getExclude();
        if(exclude==null)exclude=new ArrayList<>();
        exclude.add(grid);
        divisionPlan2.setExclude(exclude);
        mp.put(id,divisionPlan2);
    }

    // 做同结点的id之间的优先级筛选，选出优先级最高的，去掉其他
    private static String getMaxPriorityOfOctreeNode(OctreeNode node,OctreeGrid grid){
        Map<String, Object> props = node.getProperties();
        if (props==null|| !props.containsKey("conflictIdSet")) return null;
        Set<String> s = (Set<String>) props.get("conflictIdSet");
        int maxPriority=-1;
        String maxId=null;
        for(String id:s){
            int priority = id2Priority.get(id);
            if(maxPriority<priority){
                maxPriority=priority;
                if(maxId!=null) {
                    excludeGrid(maxId,grid);
                }
                maxId=id;
            }else {
                if(!Objects.equals(id, maxId)) excludeGrid(id,grid);
            }
        }
        props.put("maxPriority",maxPriority);
        props.put("maxPriorityId",maxId);
        node.setProperties(props);
        return maxId;
    }


    // block [pc,pMin,pMax]  block之间是否有冲突区域，输出冲突范围 [pMin,pMax]
    public static Point3D[] getBlocksConflict(Point3D[] block1, Point3D[] block2){
        Point3D pMin1 =block1[1];
        Point3D pMin2 =block2[1];
        Point3D pMax1 =block1[2];
        Point3D pMax2 =block2[2];
        if(pMin1.getX()> pMax2.getX()|| pMin2.getX()>pMax1.getX()) return null;
        if(pMin1.getY()> pMax2.getY()|| pMin2.getY()>pMax1.getY()) return null;
        if(pMin1.getZ()> pMax2.getZ()|| pMin2.getZ()>pMax1.getZ()) return null;
        double[] xx = middleTwoOfFour(new double[]{
                pMin1.getX(), pMax1.getX(), pMin2.getX(), pMax2.getX()
        });
        double[] yy = middleTwoOfFour(new double[]{
                pMin1.getY(), pMax1.getY(), pMin2.getY(), pMax2.getY()
        });
        double[] zz = middleTwoOfFour(new double[]{
                pMin1.getZ(), pMax1.getZ(), pMin2.getZ(), pMax2.getZ()
        });
        return new Point3D[]{
                new Point3D((xx[0]+xx[1])/2,(yy[0]+yy[1])/2,(zz[0]+zz[1])/2),
                new Point3D(xx[0],yy[0],zz[0]),
                new Point3D(xx[1],yy[1],zz[1])
        };
    }

    // 给出4个数，从小到达排序后返回中间两个数
    private static double[] middleTwoOfFour(double[] numbers){
        Arrays.sort(numbers);
        return new double []{numbers[1],numbers[2]};
    }
}
