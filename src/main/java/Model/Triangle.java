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
}
