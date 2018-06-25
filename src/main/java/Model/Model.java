package Model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Model {
    private List<Triangle> triangles = new ArrayList<Triangle>();
    private Vector3 position = new Vector3();
    private Vector3 rotation = new Vector3();

    public Model(List<Triangle> triangles) {
        this.triangles = triangles;
    }

    public void addTriangle(Triangle triangle){
        triangles.add(triangle);
    }

    public BoundingBox getBoundingBox(){
        // If no vertices, bounding box is 0.
        if (triangles.size() == 0){
            return new BoundingBox(new Vector3(),new Vector3());
        }
        //Find the axis-aligned bounding box of the object.
        //To do this, look through all the triangles / vertices and find the extent of their positions
        Vector3 minPosition = new Vector3(Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE);
        Vector3 maxPosition = new Vector3(Float.MIN_VALUE,Float.MIN_VALUE,Float.MIN_VALUE);
        for (Triangle triangle : triangles){
            for (Vector3 vertex : triangle.getVertices()){
                if (minPosition.getX() > vertex.getX()){ minPosition.setX(vertex.getX()); }
                if (minPosition.getY() > vertex.getY()){ minPosition.setY(vertex.getY()); }
                if (minPosition.getZ() > vertex.getZ()){ minPosition.setZ(vertex.getZ()); }
                if (maxPosition.getX() < vertex.getX()){ maxPosition.setX(vertex.getX()); }
                if (maxPosition.getY() < vertex.getY()){ maxPosition.setY(vertex.getY()); }
                if (maxPosition.getZ() < vertex.getZ()){ maxPosition.setZ(vertex.getZ()); }
            }
        }
        return new BoundingBox(minPosition,maxPosition);
    }

    public List<Vector3> getVertices(){
        ArrayList<Vector3> vertices = new ArrayList<>();
        for (Triangle triangle : triangles){
            vertices.add(triangle.getV1());
            vertices.add(triangle.getV2());
            vertices.add(triangle.getV3());
        }
        return vertices;
    }

    //Returns the volume of the solid, assuming it's closed
    // - If it isn't the result will be messed up.
    public double getVolume(){
        double volumeSum = 0;
        for (Triangle tri : triangles){
            double v321 = tri.getV3().getX()*tri.getV2().getY()*tri.getV1().getZ();
            double v231 = tri.getV2().getX()*tri.getV3().getY()*tri.getV1().getZ();
            double v312 = tri.getV3().getX()*tri.getV1().getY()*tri.getV2().getZ();
            double v132 = tri.getV1().getX()*tri.getV3().getY()*tri.getV2().getZ();
            double v213 = tri.getV2().getX()*tri.getV1().getY()*tri.getV3().getZ();
            double v123 = tri.getV1().getX()*tri.getV2().getY()*tri.getV3().getZ();
            volumeSum += (1.0f/6.0f)*(-v321 + v231 + v312 - v132 - v213 + v123);
        }
        return Math.abs(volumeSum);
    }

    //calculate surface area of all triangles
    public double getSurfaceArea(){
        float surfaceArea = 0;
        for (Triangle tri: triangles){
            double a = getDistance(tri.getV1(),tri.getV2());
            double b = getDistance(tri.getV2(),tri.getV3());
            double c = getDistance(tri.getV3(),tri.getV1());
            double s = (a + b + c) / 2;
            surfaceArea += Math.sqrt((s*(s-a) * (s-b)*(s-c)));
        }
        return surfaceArea;
    }

    private double getDistance(Vector3 vec1, Vector3 vec2){
        return Math.sqrt(Math.pow(vec1.getX()-vec2.getX(), 2)
                    + Math.pow(vec1.getY()-vec2.getY(), 2)
                    + Math.pow(vec1.getZ()-vec2.getZ(), 2));
    }
}
