package Model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BoundingBox {
    private Vector3 v1;
    private Vector3 v2;

    public double getWidth(){
        return Math.abs(v2.getX() - v1.getX());
    }

    public double getHeight(){
        return Math.abs(v2.getY() - v1.getY());
    }

    public double getDepth(){
        return Math.abs(v2.getZ() - v1.getZ());
    }

    public Vector3 getCenter(){
        return new Vector3((v1.getX() + v2.getX())/2,(v1.getY() + v2.getY())/2,(v1.getZ() + v2.getZ())/2);
    }
}
