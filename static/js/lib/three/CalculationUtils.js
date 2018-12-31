function volumeOfT(p1, p2, p3){
    var v321 = p3.x*p2.y*p1.z;
    var v231 = p2.x*p3.y*p1.z;
    var v312 = p3.x*p1.y*p2.z;
    var v132 = p1.x*p3.y*p2.z;
    var v213 = p2.x*p1.y*p3.z;
    var v123 = p1.x*p2.y*p3.z;
    return (-v321 + v231 + v312 - v132 - v213 + v123)/6.0;
}

function calculateVolume(geometry){
    var volumes = 0.0;

    for(var i = 0; i < geometry.faces.length; i++){
        var Pi = geometry.faces[i].a;
        var Qi = geometry.faces[i].b;
        var Ri = geometry.faces[i].c;

        var P = new THREE.Vector3(geometry.vertices[Pi].x, geometry.vertices[Pi].y, geometry.vertices[Pi].z);
        var Q = new THREE.Vector3(geometry.vertices[Qi].x, geometry.vertices[Qi].y, geometry.vertices[Qi].z);
        var R = new THREE.Vector3(geometry.vertices[Ri].x, geometry.vertices[Ri].y, geometry.vertices[Ri].z);
        volumes += volumeOfT(P, Q, R);
    }

    loadedObjectVolume = Math.abs(volumes);

    return loadedObjectVolume;
}

function round(inputNumber, decimals){
    if (isNaN(inputNumber) | inputNumber == null){
        return inputNumber;
    } else {
        return Number(Math.round(inputNumber+'e'+decimals)+'e-'+decimals);
    }
}
