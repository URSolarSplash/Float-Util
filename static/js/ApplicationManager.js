//////////////////////////////////////////////////////////
// ApplicationManager.js
// Handles main application state and functionality.
//////////////////////////////////////////////////////////

var stlLoader = new THREE.STLLoader();

// add functionality to delete an item from an array by value
Array.prototype.remove = function() {
    var what, a = arguments, L = a.length, ax;
    while (L && this.length) {
        what = a[--L];
        while ((ax = this.indexOf(what)) !== -1) {
            this.splice(ax, 1);
        }
    }
    return this;
};

class ApplicationManager {
    constructor(){
        this.wireframe = false;
        this.debug = false;
        this.showWater = true;
        this.model = null;
        this.stabilityAxis = 0;
        this.waterlineAxis = 1;
        this.hasResults = false;
    }

    startUpdate(){
        // Import the state variables from the control panel.

    }

    cancelUpdate(){

    }

    handleUpdateSuccess(){

    }

    handleUpdateError(){

    }

    handleUpdateProgress(){

    }

    // Update the simulation, models, etc.
    update(){
        console.log("Updating...");
        // Disable the update button temporarily
        $("#update-panel").text("Updating...");
        $("#update-panel").removeClass("update-panel-ready");
        $("#update-panel").off();

        this.model = $("#model-filename").prop("files")[0];
        this.hasResults = false;
        if (this.model == null){
            $("#status-message-file").text("Status: No File Loaded")
        } else {
            $("#status-message-file").text("Status: File = "+this.model.path);
        }
        this.modelScaleFactor = $("#model-units").val();
        this.modelUnits = $("#model-units option:selected").text();
        if ($("#model-stability-axis").val() == "x"){
            // X
            this.stabilityAxis = 0;
            this.waterlineAxis = 1;
        } else {
            // Z
            this.stabilityAxis = 1;
            this.waterlineAxis = 0;
        }
        this.modelRotationX = $("#model-rot-x").val();
        this.modelRotationY = $("#model-rot-y").val();
        this.modelRotationZ = $("#model-rot-z").val();
        this.modelCogX = $("#model-cog-x").val();
        this.modelCogY = $("#model-cog-y").val();
        this.modelCogZ = $("#model-cog-z").val();
        this.modelWeight = $("#model-weight").val();
        this.fluidDensity = $("#fluid-density").val();

        if (this.model != null){
            stlLoader.load(Application.model.path, function (geometry) {
                // We now have geometry, update visuals and perform simulation.
                Application.geometry = geometry.clone();
                Application.updateSimulate(Application.geometry);
            }, function(){}, function(error){
                console.log("ERROR WHILE LOADING STL:");
                console.log(error);
            });
        } else {
            this.updateFinished();
        }
    }

    updateSimulate(geometry){
        console.log("Updating simulation...");
        this.hasResults = true;

        console.log(geometry);
        geometry.scale(Application.modelScaleFactor,Application.modelScaleFactor,Application.modelScaleFactor);
        geometry.rotateX(Application.modelRotationX * (Math.PI/180));
        geometry.rotateY(Application.modelRotationY * (Math.PI/180));
        geometry.rotateZ(Application.modelRotationZ * (Math.PI/180));
        geometry.computeBoundingBox();
        Application.sizeX = geometry.boundingBox.max.x - geometry.boundingBox.min.x;
        Application.sizeY = geometry.boundingBox.max.y - geometry.boundingBox.min.y;
        Application.sizeZ = geometry.boundingBox.max.z - geometry.boundingBox.min.z;
        Application.maxSize = Math.max(Application.sizeX,Math.max(Application.sizeY,Application.sizeZ));

        // Calculate center of model and offset the model.
        Application.modelOffsetX = -(geometry.boundingBox.max.x + geometry.boundingBox.min.x) / 2;
        //Application.modelOffsetY = -(model.geometry.boundingBox.max.y + model.geometry.boundingBox.min.y) / 2;
        Application.modelOffsetY = -geometry.boundingBox.min.y;
        Application.modelOffsetZ = -(geometry.boundingBox.max.z + geometry.boundingBox.min.z) / 2;
        geometry.translate(Application.modelOffsetX,Application.modelOffsetY,Application.modelOffsetZ);
        geometry.computeBoundingBox();

        // Create waterline model
        var waterGeometry = new THREE.BoxGeometry(Application.sizeX*20,Application.sizeY*20,Application.sizeZ*20);
        waterGeometry.translate(0,-Application.sizeY*10,0);
        var waterCsg = THREE.CSG.toCSG(waterGeometry);

        // Perform waterline calculation loop


        var rawGeometry = new THREE.Geometry();
        rawGeometry.fromBufferGeometry(geometry);
        rawGeometry.mergeVertices();
        console.log(rawGeometry);
        var fullVolume = calculateVolume(rawGeometry);
        console.log("Calculated volume: "+fullVolume);
        var currentGeometry = rawGeometry.clone();

        // Check if the weight exceeds full volume mass, indicating the model will sink.
        var willSink = ((fullVolume * Application.fluidDensity) < Application.modelWeight);
        if (willSink){
            console.log("Model weighs more than its full-underwater displacement! Model will sink!");
        }

        var x = 0;
        var y = 0;
        var z = 0;
        var logString = "";

        if (!willSink){
            var iter = 0;
            var floatDelta = 1000;
            while (Math.abs(floatDelta) > 0.001){
                iter++;

                // Convert geometry to CSG model.
                var geometryCsg = THREE.CSG.toCSG(currentGeometry);

                // Get volume of below-water portion of object.
                var belowWaterCsg = geometryCsg.intersect(waterCsg);
                var underwaterVolume = calculateVolume(THREE.CSG.fromCSG(belowWaterCsg));
                var fluidDisplacementMass = underwaterVolume * Application.fluidDensity;
                logString += "i = "
                logString += iter;
                logString += ",v = ";
                logString += underwaterVolume;
                logString += ",m = ";
                logString += fluidDisplacementMass;

                // Move the volume based on floatation delta.
                // If the model is heavier than the water it displaces, this will be negative
                floatDelta = (fluidDisplacementMass - Application.modelWeight) * 0.01;
                y += floatDelta * 0.01;

                logString += ",D = ";
                logString += floatDelta;
                logString += (",pos: x = "+x+",y = "+y+",z = "+z);
                currentGeometry = rawGeometry.clone();
                currentGeometry.translate(x,y,z);

                console.log(logString);
                logString = "";

                updateModel(rawGeometry,waterGeometry, new THREE.Vector3(x,y,z));

            }
        } else {
            // Do nothing to the visualization if the model can't be floated.
            // Show a model indicating the problem.
            showModal("Simulation Error","Model simulation terminated: model will not float. <br>The weight of the model exceeds the weight of the total water displaced.<br>Weight of Model: "+(Application.modelWeight)+" lbs<br>Weight of Displaced Water: "+(fullVolume * Application.fluidDensity+" lbs"))
        }

        //var resultGeometry = THREE.CSG.fromCSG(geometryCsg);

        console.log("Done... displaying results.");

        // Update results when done.
        updateResults();
        updateModel(rawGeometry,waterGeometry, new THREE.Vector3(x,y,z));

        this.updateFinished();
    }

    updateFinished(){
        // Readd click handler to update button and update visual style.
        $("#update-panel").text("Update Results");
        $("#update-panel").addClass("update-panel-ready");
        $("#update-panel").click(function(){
            Application.update();
        });
    }

}

//////////////////////////////////////////////////////////
// End of File
//////////////////////////////////////////////////////////
