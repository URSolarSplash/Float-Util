package Simulation.Model;


import eu.mihosoft.jcsg.*;
import eu.mihosoft.vvecmath.Transform;
import eu.mihosoft.vvecmath.Vector3d;
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
    private CSG csgModel;
    private CSG plane;
    private double density = 1;
    private double mass = 1;

    public double getCalculatedMass(){
        return density * getVolume();
    }

    public Model(List<Triangle> triangles) {
        this.triangles = triangles;
    }

    //Model from CSG
    public Model(CSG in){
        for (Polygon poly : in.getPolygons()){
            if (poly.vertices.size() == 3) {
                Vertex v1 = poly.vertices.get(0);
                Vertex v2 = poly.vertices.get(1);
                Vertex v3 = poly.vertices.get(2);
                Vector3 v1v = new Vector3(v1.pos.x(), v1.pos.y(), v1.pos.z());
                Vector3 v2v = new Vector3(v2.pos.x(), v2.pos.y(), v2.pos.z());
                Vector3 v3v = new Vector3(v3.pos.x(), v3.pos.y(), v3.pos.z());
                triangles.add(new Triangle(v1v, v2v, v3v));
            } else if (poly.vertices.size() == 4){
                Vertex v1 = poly.vertices.get(0);
                Vertex v2 = poly.vertices.get(1);
                Vertex v3 = poly.vertices.get(2);
                Vertex v4 = poly.vertices.get(3);
                Vector3 v1v = new Vector3(v1.pos.x(), v1.pos.y(), v1.pos.z());
                Vector3 v2v = new Vector3(v2.pos.x(), v2.pos.y(), v2.pos.z());
                Vector3 v3v = new Vector3(v3.pos.x(), v3.pos.y(), v3.pos.z());
                Vector3 v4v = new Vector3(v4.pos.x(), v4.pos.y(), v4.pos.z());
                triangles.add(new Triangle(v1v, v2v, v3v));
                triangles.add(new Triangle(v1v, v3v, v4v));

            }

            //0 1 2 3

            //0 1 2
            //0 2 3

            /*
            3-------2
            |      /|
            |    /  |
            |  /    |
            |/      |
            0-------1
             */
        }
    }

    public Vector3 getCenterOfMass(){
        // For now, use the average of centers of all the triangles
        //This is COG of the shell but not the actual body :(
        //to try to make it more realistic, weight by the area of the triangle

        Vector3 out = new Vector3();
        double totalSurfaceArea = getSurfaceArea();

        for (Triangle tri : triangles){
            // Find centroid of triangle
            double x = (tri.getV1().getX() + tri.getV2().getX() + tri.getV3().getX()) / 3;
            double y = (tri.getV1().getY() + tri.getV2().getY() + tri.getV3().getY()) / 3;
            double z = (tri.getV1().getZ() + tri.getV2().getZ() + tri.getV3().getZ()) / 3;
            double area = tri.getArea();
            x *= area;
            y *= area;
            z *= area;
            out.setX(out.getX() + x);
            out.setY(out.getY() + y);
            out.setZ(out.getZ() + z);
        }


        out.setX(out.getX() / (totalSurfaceArea));
        out.setY(out.getY() / (totalSurfaceArea));
        out.setZ(out.getZ() / (totalSurfaceArea));
        return out;
    }

    public void addTriangle(Triangle triangle){
        triangles.add(triangle);
    }

    public void calculateCSG(){
        List<Polygon> polygons = new ArrayList<>();
        List<Vector3d> vertices = new ArrayList<>();
        for (Triangle tri : triangles){
            vertices.add(tri.getV1().clone());
            vertices.add(tri.getV2().clone());
            vertices.add(tri.getV3().clone());
            polygons.add(Polygon.fromPoints(vertices));
            vertices = new ArrayList<>();
        }

        Transform transform = new Transform();
        transform.translate(position);
        transform.rot(rotation);

        csgModel = CSG.fromPolygons(new PropertyStorage(),polygons).transformed(transform);
        BoundingBox bb = getCSGBoundingBox();
        plane = (new Cube(new Vector3(bb.getCenter().getX(),-1*(bb.getHeight()/2),bb.getCenter().getZ()),new Vector3(bb.getWidth() + 1,bb.getHeight(),bb.getDepth() + 1))).toCSG();
    }

    //Get slice below the zero plane
    public CSG getNegativeSlice(){
        return csgModel.intersect(plane);
    }

    //Get slice above the zero plane
    public CSG getPositiveSlice(){
        return csgModel.difference(plane);
    }


    public BoundingBox getCSGBoundingBox(){
        return new BoundingBox(new Vector3(csgModel.getBounds().getMin()),new Vector3(csgModel.getBounds().getMax()));
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
            surfaceArea += tri.getArea();
        }
        return surfaceArea;
    }
}
