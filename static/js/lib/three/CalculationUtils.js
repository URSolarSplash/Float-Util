// Calculate the volume of a tetrahedron.
function volumeOfTetrahedron(p1, p2, p3){
    //console.log("Computing UNCLIPPED tetra volume for following polygon:");
    //console.log(p1);
    //console.log(p2);
    //console.log(p3);
    var v321 = p3.x*p2.y*p1.z;
    var v231 = p2.x*p3.y*p1.z;
    var v312 = p3.x*p1.y*p2.z;
    var v132 = p1.x*p3.y*p2.z;
    var v213 = p2.x*p1.y*p3.z;
    var v123 = p1.x*p2.y*p3.z;
    return (-v321 + v231 + v312 - v132 - v213 + v123)/6.0;
}

function centerOfMassOfTetrahedron(p1, p2, p3){
    return 0;
}

// Calculate the volume of a tetrahedron with the fourth point at the origin.
// Clips the tetrahedron to return the area below or above the zero plane.
function clippedVolumeOfTetrahedron(p1, p2, p3, aboveZero){
    //console.log("Computing CLIPPED tetra volume for following polygon:");
    //console.log(p1);
    //console.log(p2);
    //console.log(p3);
    // First, compute the entire tetrahedron's volume
    var volume = 0;
    var v321 = p3.x*p2.y*p1.z;
    var v231 = p2.x*p3.y*p1.z;
    var v312 = p3.x*p1.y*p2.z;
    var v132 = p1.x*p3.y*p2.z;
    var v213 = p2.x*p1.y*p3.z;
    var v123 = p1.x*p2.y*p3.z;
    volume = (-v321 + v231 + v312 - v132 - v213 + v123)/6.0;
    //console.log("entire tetra volume: "+volume);

    // Get clipped remainder - This is tetrahedron result of intersection with origin plane.
    // Depending on polarity of this, subtract or add its value to total volume and COG.
    var clipped = clipTetrahedronRemainder(p1,p2,p3,aboveZero);
    // Get the volume of the clipped remainder
    v321 = clipped.p3.x*clipped.p2.y*clipped.p1.z;
    v231 = clipped.p2.x*clipped.p3.y*clipped.p1.z;
    v312 = clipped.p3.x*clipped.p1.y*clipped.p2.z;
    v132 = clipped.p1.x*clipped.p3.y*clipped.p2.z;
    v213 = clipped.p2.x*clipped.p1.y*clipped.p3.z;
    v123 = clipped.p1.x*clipped.p2.y*clipped.p3.z;
    var clipVolume = (-v321 + v231 + v312 - v132 - v213 + v123)/6.0;
    //console.log(clipped);
    //console.log("clipped volume: "+clipVolume);

    if (clipped.subtract){
        // The clipped region should be subtracted from the full volume and COG.
        volume -= clipVolume;
    } else {
        // The clipped region is what we want. Discard full volume.
        volume = clipVolume;
    }

    return volume;
}

// Calculate the center of mass of a tetrahedron with the fourth point at the origin.
// Clips the tetrahedron to return the center of mass below or above the zero plane.
function clippedCenterOfMassOfTetrahedron(p1, p2, p3, aboveZero){
    // Center of mass of a tetrahedron is the sum of the 4 points, divided by 4:
    // https://math.stackexchange.com/questions/883780/to-find-the-center-of-gravity-of-a-homogeneous-tetrahedron
    var clipped = clipTetrahedron(p1,p2,p3);
    var cog = {x:0, y:0, z:0};
    cog.x = (0 + clipped.p1.x + clipped.p2.x + clipped.p3.x) / 4;
    cog.y = (0 + clipped.p1.y + clipped.p2.y + clipped.p3.y) / 4;
    cog.z = (0 + clipped.p1.z + clipped.p2.z + clipped.p3.z) / 4;
    return cog;
}

// Clips a tetrahedron by the zero XZ plane.
// Returns the tetrahedron that represents either the clipped volume, or the region to subtract.
// Sets subtract = true if this is a region to subtract from the original volume.
function clipTetrahedronRemainder(p1,p2,p3,aboveZero){
    var clipped = {p1:{x:0, y:0, z:0}, p2:{x:0, y:0, z:0}, p3:{x:0, y:0, z:0}, subtract: false};
    var p1AboveZero = (p1.y >= 0);
    var p2AboveZero = (p2.y >= 0);
    var p3AboveZero = (p3.y >= 0);
    var numAboveZero = p1AboveZero + p2AboveZero + p3AboveZero;

    // Case 1: All of the tetrahedron will be clipped
    // (We are clipping to above plane and all points are below plane) OR (We are clipping below plane and all points are above plane)
    if ((aboveZero && numAboveZero == 0) || (!aboveZero && numAboveZero == 3)){
        // Return a tetrahedron of size 0 since nothing is above the zero XZ plane.
        //console.log("all of tetrahedron will be clipped");
        return clipped;
    }
    // Case 2: None of the tetrahedron will be clipped
    // (We are clipping to above plane and all points are above plane) OR (We are clipping below plane and all points are below plane)
    if ((!aboveZero && numAboveZero == 0) || (aboveZero && numAboveZero == 3)){
        // Return a tetrahedron of size zero, which will be subtracted, meaning the full tetrahedron will be used.
        clipped.subtract = true;
        //console.log("none of tetrahedron will be clipped");
        return clipped;
    }

    // Choose which points to clip, and clip them
    // Decision of which part of tetrahedron to keep is made later
    var p12intersect = mathjs.intersect([p1.x,p1.y,p1.z],[p2.x,p2.y,p2.z],[0,1,0,0])
    var p13intersect = mathjs.intersect([p1.x,p1.y,p1.z],[p3.x,p3.y,p3.z],[0,1,0,0])
    var p23intersect = mathjs.intersect([p2.x,p2.y,p2.z],[p3.x,p3.y,p3.z],[0,1,0,0])
    clipped.p1.x = p1.x; clipped.p1.y = p1.y; clipped.p1.z = p1.z;
    clipped.p2.x = p2.x; clipped.p2.y = p2.y; clipped.p2.z = p2.z;
    clipped.p3.x = p3.x; clipped.p3.y = p3.y; clipped.p3.z = p3.z;
    if ((numAboveZero == 1 && p1AboveZero) || (numAboveZero == 2 && !p1AboveZero)){
        clipped.p2.x = p12intersect[0]; clipped.p2.y = p12intersect[1]; clipped.p2.z = p12intersect[2];
        clipped.p3.x = p13intersect[0]; clipped.p3.y = p13intersect[1]; clipped.p3.z = p13intersect[2];
        //console.log("lines 1-2 and 1-3 are clipped");
    } else if ((numAboveZero == 1 && p2AboveZero) || (numAboveZero == 2 && !p2AboveZero)){
        clipped.p1.x = p12intersect[0]; clipped.p1.y = p12intersect[1]; clipped.p1.z = p12intersect[2];
        clipped.p3.x = p23intersect[0]; clipped.p3.y = p23intersect[1]; clipped.p3.z = p23intersect[2];
        //console.log("lines 1-2 and 2-3 are clipped");
    } else if ((numAboveZero == 1 && p3AboveZero) || (numAboveZero == 2 && !p3AboveZero)){
        clipped.p1.x = p13intersect[0]; clipped.p1.y = p13intersect[1]; clipped.p1.z = p13intersect[2];
        clipped.p2.x = p23intersect[0]; clipped.p2.y = p23intersect[1]; clipped.p2.z = p23intersect[2];
        //console.log("lines 1-3 and 2-3 are clipped");
    }

    if (numAboveZero == 1){
        // If there is 1 point above zero, subtract if we want volume below zero.
        clipped.subtract = !aboveZero;
    } else if (numAboveZero == 2){
        // If there are 2 points above zero, subtract if we want volume above zero.
        clipped.subtract = aboveZero;
    }
    //console.log("numAboveZero: "+numAboveZero);

    return clipped;
}

// Calculate the volume and center of mass of the input geometry, clipped below or above the zero plane if doClipping = true.
function calculateVolumeCenterOfMass(geometry, doClipping, aboveZero){
    var output = {volume: 0, cog:{x:0, y:0, z:0}};

    //console.log("Calculating volume of geometry");

    // Iterate through every face, and find the volume of a tetrahedron from this triangle
    // to the origin. This volume will be signed based on the normal of the face.
    // The sum of all of these volumes is the total volume.
    // At the same time, compute a weighted average of all of the center of masses
    for(var i = 0; i < geometry.faces.length; i++){
        var Pi = geometry.faces[i].a;
        var Qi = geometry.faces[i].b;
        var Ri = geometry.faces[i].c;

        var P = new THREE.Vector3(geometry.vertices[Pi].x, geometry.vertices[Pi].y, geometry.vertices[Pi].z);
        var Q = new THREE.Vector3(geometry.vertices[Qi].x, geometry.vertices[Qi].y, geometry.vertices[Qi].z);
        var R = new THREE.Vector3(geometry.vertices[Ri].x, geometry.vertices[Ri].y, geometry.vertices[Ri].z);

        // Find clipped / unclipped volume of the tetrahedron, add it to summed volume
        var tetrahedronVolume = 0;
        if (doClipping){
            tetrahedronVolume = clippedVolumeOfTetrahedron(P, Q, R, aboveZero);
            //console.log("Got clipped tetrahedron volume of "+tetrahedronVolume);
        } else {
            tetrahedronVolume = volumeOfTetrahedron(P, Q, R);
            //console.log("Got unclipped tetrahedron volume of "+tetrahedronVolume);
        }
        output.volume += tetrahedronVolume;

        // Add weighted center of mass to center of mass coordinates
        // Weight is the volume of the object, and is signed which indicates normal direction.
        // This assumes the model has a uniform density.
        // TODO
        /*
        var tetrahedronCenterOfMass = clippedCenterOfMassOfTetrahedron(P, Q, R, aboveZero);
        output.cog.x += tetrahedronCenterOfMass.x * tetrahedronVolume;
        output.cog.y += tetrahedronCenterOfMass.y * tetrahedronVolume;
        output.cog.z += tetrahedronCenterOfMass.z * tetrahedronVolume;
        */
    }
    // Calculate the absolute value of the volume, in case the normals are all inverted.
    output.volume = Math.abs(output.volume);

    // Divide center of mass coordinates by the total volume,
    // since each coordinate was previously multipled by its volume in a weighted average.
    // Check first that volume is not zero.
    if (output.volume != 0){
        output.cog.x /= output.volume;
        output.cog.y /= output.volume;
        output.cog.z /= output.volume;
    } else {
        // If output volume is zero, set COG to zero.
        output.cog.x = 0;
        output.cog.y = 0;
        output.cog.z = 0;
    }
    return output;
}

function round(inputNumber, decimals){
    if (isNaN(inputNumber) | inputNumber == null){
        return inputNumber;
    } else {
        return Number(Math.round(inputNumber+'e'+decimals)+'e-'+decimals);
    }
}
