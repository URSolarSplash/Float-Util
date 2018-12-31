# Float-Util
## Summary
Float-Util is a program used for the calculation of boat buoyancy, stability, and waterline. The tool is used by the Rochester Solar Splash team to evaluate boat hull designs. Float-Util iteratively floats a model on a water volume, finding waterline and stability characteristics of a boat configuration.

Features include:
- Loading of STL models
- 3d preview of model, test configuration
- Visualization of results data
- Calculation of total water displacement.
- Waterline position and hull stability curves based on iterative floatation simulation.
- Configuration of boat model, COG and weight, fluid properties.
- Results output to PDF, CSV

## Screenshots
*Loading a model, Configuring simulation settings*

*Simulation running in background*

*Viewing simulation results: Buoyancy Information*

*Viewing simulation results: Stability Curve*

*Viewing simulation results: Waterline Information*

## Tutorial


## Implementation Details
### Hull Waterline Partitioning
Float-Util uses CSG (Constructive Solid Geometry) to split the hull mesh into above-water and below-water segments. To do this, a water plane is constructed, and the intersection of the hull mesh and the water plane is calculated. This is performed at each simulation iteration.

### Mesh Volume, Center of Mass Calculations
The simulation requires calculations of the volume and center of mass for the split hull meshes. We assume that all meshes are enclosed and made entirely of triangle polygons for simplicity.

The volume and center of mass calculations use a similar technique: For each triangle, construct a tetrahedron from the polygon to the origin. The volume and/or center of mass of the tetrahedron can be calculated. Then, based on the direction of the triangle normal (facing towards/away from the origin), the values are added or subtracted to a global accumulator.


If the triangle faces away from the origin, this represents the beginning, or "outside", of the solid body, and we add the volume to the accumulator. If the triangle faces towards the origin, this represents the end, or "inside" of the solid body, and we subtract the body spanning from this triangle to the origin.


After all the triangles have been iterated through, we are left with values representing the center of mass and volume of the entire body.

### Iterative Simulation
At each step of the simulation, two operations are performed, which control the position and rotation of the floated body:
- The volume calculation is used to obtain the mass of fluid displaced by the underwater hull segment. The hull weight is subtracted by this value to obtain a delta, which is applied to the boat's Y coordinate. This "floats" the boat vertically, finding an equilibrium for displacement.
- The center of mass calculation is used to obtain the centers for the full body and the underwater body. The center of mass of the underwater buoyancy, also known as the "center of buoyancy", is calculated. A moment is calculated and applied, rotating the hull to find a stable angle.

This simulation is iterated until both the position and rotation deltas are significantly small.
