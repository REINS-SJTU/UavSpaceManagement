package com.zhd.entity.tmp;

import com.zhd.geometry.structure.Point3D;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConflictZonePairs {
    private String id1;
    private String id2;
    private Point3D[] conflict;
    private int type; // 1-小与小;2-大与小;3-大与大
}
