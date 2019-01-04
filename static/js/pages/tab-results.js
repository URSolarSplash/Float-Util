function updateResults(){
    if (Application.hasResults){
        $("#results").html(`
            <p>Model File: `+Application.modelPath+`</p>
            <p>Model Units: `+Application.modelUnits+`</p>
            <p>Model Scale Factor: `+Application.modelScaleFactor+`</p>
            <p>Model Size X (ft): `+Application.sizeX+`</p>
            <p>Model Size Y (ft): `+Application.sizeY+`</p>
            <p>Model Size Z (ft): `+Application.sizeZ+`</p>
            <p>Model Max Size (ft): `+Application.maxSize+`</p>
            <p>Model Rotation X: `+Application.modelRotationX+`</p>
            <p>Model Rotation Y: `+Application.modelRotationY+`</p>
            <p>Model Rotation Z: `+Application.modelRotationZ+`</p>
            <p>Model COG X: `+Application.modelCogX+`</p>
            <p>Model COG Y: `+Application.modelCogY+`</p>
            <p>Model COG Z: `+Application.modelCogZ+`</p>
            <p>Model Weight: `+Application.modelWeight+`</p>
            <p>Fluid Density: `+Application.fluidDensity+`</p>
            <p>Stability Axis: `+Application.stabilityAxis+`</p>
            <p>Waterline Axis: `+Application.waterlineAxis+`</p>
        `);
    } else {
        $("#results").html("<p>Please configure and run a valid simulation to see results.</p>");
    }
}

$(function(){
    updateResults();
})


// Shows a div indicating the results are out of date.
function invalidateResults(){

}

function validateResults(){
    
}
