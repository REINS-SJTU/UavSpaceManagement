package com.zhd.geometry.algorithm;

import com.zhd.entity.tmp.DivisionPlan2;
import com.zhd.geometry.structure.Octree;
import com.zhd.geometry.structure.OctreeGrid;
import com.zhd.geometry.structure.OctreeNode;
import com.zhd.geometry.structure.Point3D;
import javafx.util.Pair;
import lombok.Data;


import java.util.*;


public class CollisionDecider {

    private static Map<String,Integer> id2Priority;
    private static Map<String,DivisionPlan2> mp;
    private static int M=10;
//    private static List<OctreeGrid> nodes;


    public static Map<String, DivisionPlan2> decideCollisionBasedOnPriority(Map<String,DivisionPlan2> mp_, Map<String,Integer> id2Priority_, Octree octree){
        M=octree.getM();
        id2Priority=id2Priority_;
        mp=mp_;

//        nodes=new ArrayList<>();
//        OctreeNode p = octree.getRoot();
//        OctreeGrid testGrid = new OctreeGrid(340, 300, 84, 8);
//        for(int i=1;i<=M;i++){
//            OctreeGrid newGrid = new OctreeGrid(testGrid.getX() >> i << i, testGrid.getY() >> i << i, testGrid.getZ() >> i << i, M - i);
//            nodes.add(newGrid);
//        }
//        for(int i=0;i<8;i++) {
//            OctreeGrid subOctreeGrid = testGrid.getSubOctreeGrid(i, M);
//            if(subOctreeGrid!=null)nodes.add(subOctreeGrid);
//        }

        dfs(octree.getRoot(), new OctreeGrid(0, 0, 0, 0),null);
        return mp;
    }

    private static List<Pair<String,OctreeGrid>> dfs(OctreeNode parent, OctreeGrid grid,String smallOcpId){
//        boolean t=false;
//        if(nodes.contains(grid)){
//            if(parent!=null)
//                System.out.println("its parent "+grid+":"+parent.getProperties()+","+smallOcpId);
//            t=true;
//        }

        List<Pair<String,OctreeGrid>> higherPriorityIds=new ArrayList<>();
        if(parent==null) return higherPriorityIds;
        if(smallOcpId!=null){
//            if(t) System.out.println("===exclude children====="+grid);
            excludeAllConflictIdExceptSmallId(smallOcpId,parent,grid);
            for(int i=0;i<8;i++) {
                OctreeNode child = parent.getKthChild(i);
                OctreeGrid subGrid = grid.getSubOctreeGrid(i, M);
                dfs(child,subGrid,smallOcpId);
            }
            return higherPriorityIds;
        }
        String maxId = getMaxPriorityOfOctreeNode(parent, grid);
        if("SMALL".equals(maxId)){
//            if(t)System.out.println("====catch small==="+grid);
            String smallOcpId2 = (String) parent.getProperties().get("small");
            for(int i=0;i<8;i++) {
                OctreeNode child = parent.getKthChild(i);
                OctreeGrid subGrid = grid.getSubOctreeGrid(i, M);
                dfs(child,subGrid,smallOcpId2);
            }
            higherPriorityIds.add(new Pair<>("[SMALL]"+smallOcpId2,grid));
            return higherPriorityIds;
        }
        if(parent.isLeaf()){


//            if(maxId!=null)higherPriorityIds.add(new Pair<>(maxId,grid));
//            return higherPriorityIds;

            List<Pair<String, OctreeGrid>> result = comparePriority(getConflictIdSet(parent, grid), grid, new ArrayList<>());
//            if(t) System.out.println(grid+" is a leaf."+getConflictIdSet(parent, grid));
//            if(t) for(int i=0;i<result.size();i++) System.out.println(result.get(i).getKey()+","+result.get(i).getValue());
            return result ;

//            Set<String> conflictIdSet = getConflictIdSet(parent, grid);
//            if(conflictIdSet==null||conflictIdSet.isEmpty()||maxId==null) return higherPriorityIds;
//            for(String idC: conflictIdSet){
//                if(id2Priority.get(idC)>id2Priority.get(maxId)){
//                    excludeGrid(maxId,grid);
//                    maxId=idC;
//                }else{
//                    excludeGrid(idC,grid);
//                }
//            }
//            higherPriorityIds.add(new Pair<>(maxId,grid));
//            return higherPriorityIds;
        }

        Set<String> currentNodeIdSet = getConflictIdSet(parent, grid);
        List<Pair<String,OctreeGrid>> L = new ArrayList<>();
        for(int i=0;i<8;i++){
            OctreeNode child = parent.getKthChild(i);
            OctreeGrid subGrid = grid.getSubOctreeGrid(i, M);
            List<Pair<String,OctreeGrid>> ids2 = dfs(child, subGrid,null);
            L.addAll(ids2);
//            if(maxId==null){
//                higherPriorityIds.addAll(ids2);
//            }else {
//                for (Pair<String, OctreeGrid> pair : ids2) {
//                    // 子优先级比父高，父挖块
//                    if ("SMALL".equals(pair.getKey()) || id2Priority.get(pair.getKey()) > id2Priority.get(maxId)) {
//                        if ("vehicle/10008".equals(pair.getKey()) && grid.equals(new OctreeGrid(664, 280, 48, 9)))
//                            System.out.println("Exclude (1)" + maxId + "," + pair.getValue());
//                        excludeGrid(maxId, pair.getValue());
//                        higherPriorityIds.add(pair);
//                    } else {
//                        // 父优先级高，子挖块
//                        if ("vehicle/10008".equals(pair.getKey()) && grid.equals(new OctreeGrid(664, 280, 48, 9)))
//                            System.out.println("Exclude (2)" + pair.getKey() + "," + pair.getValue());
//                        excludeGrid(pair.getKey(), pair.getValue());
//                    }
//                }
//            }
        }

//        if(t&&grid.equals(new OctreeGrid(340, 300, 84, 8))) {
//            List<Pair<String, OctreeGrid>> pairs = comparePriority(currentNodeIdSet, grid, L);
//            for(Pair p:pairs) System.out.println("Compare Priority Result:"+p.getKey()+","+p.getValue());
//        }
        return comparePriority(currentNodeIdSet,grid,L);
    }

    private static List<Pair<String,OctreeGrid>> comparePriority(Set<String> currentNodeIdSet,OctreeGrid grid,List<Pair<String,OctreeGrid>> L){
        List<Pair<String,OctreeGrid>> higherPriorityIds=new ArrayList<>();

        Set<String> smallIds = new HashSet<>();
        Map<String,Set<OctreeGrid>> smallGrids = new HashMap<>();

        for(Pair<String,OctreeGrid> p:L){
                String idP = p.getKey();
                OctreeGrid gridP = p.getValue();
                if(idP.startsWith("[SMALL]")){
                    String smallId = idP.substring("[SMALL]".length());
                    smallIds.add(smallId);
                    Set<OctreeGrid> G = new HashSet<>();
                    if(smallGrids.containsKey(smallId)) {
                        G=smallGrids.get(smallId);
                    }
                    G.add(gridP);
                    smallGrids.put(smallId,G);
                    if(currentNodeIdSet!=null)
                        for(String idC:currentNodeIdSet){
                            if(!Objects.equals(smallId,idC)) {
//                                if ("vehicle/10041".equals(idC) && grid.equals(new OctreeGrid(340, 300, 84, 8)))
//                                    System.out.println("Exclude (1)" + idC + "," + gridP);
                                excludeGrid(idC,gridP);
                            }
                        }
                }
        }

//        if(!smallIds.isEmpty()&&grid.equals(new OctreeGrid(340, 300, 84, 8))) System.out.println("Small Id Sets:"+smallIds);
        String maxId=null;
        Integer maxPriority=-1;
        if(currentNodeIdSet!=null)
            for(String idC:currentNodeIdSet){
                Integer priority = id2Priority.get(idC);
                if(maxId==null) {
                    maxId=idC;
                    maxPriority=priority;
                }else if(maxPriority<priority){
//                    if ("vehicle/10041".equals(maxId) && grid.equals(new OctreeGrid(340, 300, 84, 8)))
//                            System.out.println("Exclude (2)" + maxId + "," + grid);
                    if(!smallIds.contains(maxId)) {
//                        if ("vehicle/10041".equals(maxId) && grid.equals(new OctreeGrid(340, 300, 84, 8)))
//                            System.out.println("Exclude (8)" + maxId + "," + grid);
                        excludeGrid(maxId,grid);
                    }
                    else{
                        List<OctreeGrid> remainGrids = GeoUtil.excludeChildrenGrid(grid, smallGrids.get(maxId), M);
                        for(OctreeGrid g:remainGrids) {
//                            if ("vehicle/10041".equals(idC) && grid.equals(new OctreeGrid(340, 300, 84, 8)))
//                                System.out.println("Exclude (8)" + idC + "," + g);
                            excludeGrid(maxId,g);
                        }
                    }
                    maxId=idC; maxPriority=priority;
                }else{
                    if(!smallIds.contains(idC)) {
//                        if ("vehicle/10041".equals(idC) && grid.equals(new OctreeGrid(340, 300, 84, 8)))
//                            System.out.println("Exclude (3)" + idC + "," + grid);
                        excludeGrid(idC,grid);
                    }else{
                        if(smallGrids.containsKey(idC)) {
                            List<OctreeGrid> remainGrids = GeoUtil.excludeChildrenGrid(grid, smallGrids.get(idC), M);
                            for(OctreeGrid g:remainGrids) {
//                                if ("vehicle/10041".equals(idC) && grid.equals(new OctreeGrid(340, 300, 84, 8)))
//                                    System.out.println("Exclude (4)" + idC + "," + g);
                                excludeGrid(idC,g);
                            }
                        }

                    }
                }
            }

        for(Pair<String,OctreeGrid> p: L){
            String idP = p.getKey();
            OctreeGrid gridP = p.getValue();
            if(idP.startsWith("[SMALL]")) {
                higherPriorityIds.add(p);
                continue;
            }
            if(id2Priority.get(idP)>maxPriority){
                if(maxId!=null) {
//                    if("vehicle/10041".equals(maxId) && grid.equals(new OctreeGrid(340, 300, 84, 8)))
//                        System.out.println("Exclude (5):"+maxId+","+gridP);
                    excludeGrid(maxId,gridP);
                }
                higherPriorityIds.add(new Pair<>(idP,gridP));
            }else{
//                if("vehicle/10041".equals(idP) && grid.equals(new OctreeGrid(340, 300, 84, 8)))
//                    System.out.println("Exclude (6):"+idP+","+gridP);
                excludeGrid(idP,gridP);
            }
        }
        if(maxId!=null) higherPriorityIds.add(new Pair<>(maxId,grid));
//        for (Pair<String, OctreeGrid> pair : ids2) {
//                    // 子优先级比父高，父挖块
//                    if ("SMALL".equals(pair.getKey()) || id2Priority.get(pair.getKey()) > id2Priority.get(maxId)) {
//                        if ("vehicle/10008".equals(pair.getKey()) && grid.equals(new OctreeGrid(664, 280, 48, 9)))
//                            System.out.println("Exclude (1)" + maxId + "," + pair.getValue());
//                        excludeGrid(maxId, pair.getValue());
//                        higherPriorityIds.add(pair);
//                    } else {
//                        // 父优先级高，子挖块
//                        if ("vehicle/10008".equals(pair.getKey()) && grid.equals(new OctreeGrid(664, 280, 48, 9)))
//                            System.out.println("Exclude (2)" + pair.getKey() + "," + pair.getValue());
//                        excludeGrid(pair.getKey(), pair.getValue());
//                    }
//                }
        return higherPriorityIds;
    }

    private static void excludeGrid(String id,OctreeGrid grid){
        if(!mp.containsKey(id)) return;
        DivisionPlan2 divisionPlan2 = mp.get(id);
        List<OctreeGrid> exclude = divisionPlan2.getExclude();
        if(exclude==null)exclude=new ArrayList<>();
        exclude.add(grid);
        divisionPlan2.setExclude(exclude);
        mp.put(id,divisionPlan2);
    }

    private static void excludeAllConflictIdExceptSmallId(String smallId,OctreeNode node,OctreeGrid grid){
        Map<String, Object> props = node.getProperties();
        if(props==null) return ;
        Set<String> s = (Set<String>) props.get("conflictIdSet");
        if(s==null) return ;
        for(String id:s){
            if(Objects.equals(smallId,id)) continue;
//            if ("vehicle/10041".equals(id) && grid.equals(new OctreeGrid(340, 300, 84, 8)))
//                System.out.println("Exclude (7)" + id + "," + grid);
            excludeGrid(id,grid);
        }
    }

    private static Set<String> getConflictIdSet(OctreeNode node,OctreeGrid grid){
        Map<String, Object> props = node.getProperties();
        if (props==null) return null;
        if(!props.containsKey("conflictIdSet")) return null;
        return (Set<String>) props.get("conflictIdSet");
    }

    // 做同结点的id之间的优先级筛选，选出优先级最高的
    private static String getMaxPriorityOfOctreeNode(OctreeNode node,OctreeGrid grid){
        Map<String, Object> props = node.getProperties();
        if (props==null) return null;
        if(!props.containsKey("conflictIdSet")) return props.containsKey("small")?"SMALL":null;
        Set<String> s = (Set<String>) props.get("conflictIdSet");
        if(props.containsKey("small")){
//            String smallId = (String) props.get("small");
//            for(String id:s){
//                if(!Objects.equals(id,smallId)) excludeGrid(id,grid);
//            }
            return "SMALL";
        }
        int maxPriority=-1;
        String maxId=null;
        for(String id:s){
            int priority = id2Priority.get(id);
            if(maxPriority<priority){
                maxPriority=priority;
//                if(maxId!=null) {
////                    System.out.println("Exclude (3) "+maxId);
//                    if("vehicle/10008".equals(id)&&grid.equals(new OctreeGrid(664,280,48,9))) System.out.println("Exclude (3)"+maxId+","+grid);
//                    excludeGrid(maxId,grid);
//                }
                maxId=id;
            }
//            else {
//                if(!Objects.equals(id, maxId)) {
//                    if("vehicle/10008".equals(id)&&grid.equals(new OctreeGrid(664,280,48,9))) System.out.println("Exclude (4)"+id+","+grid);
//                    // grid.equals(new OctreeGrid(664,280,48,9))  (x=930, y=554, z=96, k=10)
//                    excludeGrid(id,grid);
//                }
//            }
        }
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
