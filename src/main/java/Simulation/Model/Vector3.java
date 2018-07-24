package Simulation.Model;

import eu.mihosoft.vvecmath.Vector3d;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vector3 implements Vector3d {
    private double x;
    private double y;
    private double z;


    public Vector3(Vector3d in){
        this.x = in.getX();
        this.y = in.getY();
        this.z = in.getZ();
    }

    @Override
    public double getX(){
        return x;
    }

    @Override
    public double getY(){
        return y;
    }

    @Override
    public double getZ(){
        return z;
    }

    @Override
    public Vector3d clone() {
        return new Vector3(x,y,z);
    }
}
