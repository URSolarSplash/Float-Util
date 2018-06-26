package Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class Triangle {
    public Vector3 v1;
    public Vector3 v2;
    public Vector3 v3;
    public Vector3 normal;

    public Triangle() {
        v1 = new Vector3();
        v2 = new Vector3();
        v3 = new Vector3();
        normal = new Vector3();
    }

    public Triangle(Vector3 v1, Vector3 v2, Vector3 v3) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        //TODO: Calculate default normal
    }

    public List<Vector3> getVertices(){
        List vertices = new ArrayList<Vector3>();
        vertices.add(v1);
        vertices.add(v2);
        vertices.add(v3);
        return vertices;
    }

    public double getArea(){
        double a = getDistance(v1,v2);
        double b = getDistance(v2,v3);
        double c = getDistance(v3,v1);
        double s = (a + b + c) / 2;
        return Math.sqrt((s*(s-a) * (s-b)*(s-c)));
    }

    private double getDistance(Vector3 vec1, Vector3 vec2){
        return Math.sqrt(Math.pow(vec1.getX()-vec2.getX(), 2)
                + Math.pow(vec1.getY()-vec2.getY(), 2)
                + Math.pow(vec1.getZ()-vec2.getZ(), 2));
    }
}
