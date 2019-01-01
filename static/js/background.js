
var stlLoader = new THREE.STLLoader();
const { ipcRenderer } = require('electron');

logger.log('[Background Thread] Background thread initialized.');

THREE.Geometry.create = function (obj) {
    var field = new THREE.Geometry();
    for (var prop in obj) {
        if (field.hasOwnProperty(prop)) {
            field[prop] = obj[prop];
        }
    }

    return field;
}

var isProcessing = false;
var lastInput = null;
var isCancelled = false;

//simulation-start
//simulation-cancel
//simulation-update-progress
//simulation-success
//simulation-failure

$(function(){
    ipcRenderer.on('simulation-start', (event, payload) => {
        if (!isProcessing){
            isProcessing = true;
            logger.log('[Background Thread] Starting simulation...');
            try {
                lastInput = payload;
                stlLoader.load(payload.modelPath, function (geometry) {

                    console.log(geometry);

                    // Convert BufferedGeometry to raw Geometry object suitable for CSG operations.
                    payload.geometry = new THREE.Geometry();
                    payload.geometry.fromBufferGeometry(geometry);
                    payload.geometry.mergeVertices();

                    simulate(payload);
                }, function(){}, function(error){
                    logger.log("[Background Thread] error caught during STL loading in simulation!");
                    logger.log(error);
                    sendErrorPacket("Error caught during simulation. View debug logs for details!");
                });
            } catch (error){
                logger.log("[Background Thread] error caught during simulation!");
                logger.log(error);
                sendErrorPacket("Error caught during simulation. View debug logs for details!");
            }
            logger.log('[Background Thread] Finished simulation.');
        }
	});

/*
    ipcRenderer.on('simulation-cancel', (event,payload) => {
        logger.log('[Background Thread] Received simulation cancel message!');
        if (isProcessing){
            isCancelled = true;
        }
    });*/
});
/*
function checkCancel(){
    if (isCancelled){
        isProcessing = false;
        isCancelled = false;
        sendErrorPacket("Simulation cancelled by user!");
        return true;
    }
    return false;
}*/

function simulate(input){
    var output = {};

    // Save geometry to output object.
    output.geometry = input.geometry;

    // Apply the scaling and rotation options to the geometry.
    // This is a one-time operation; the results are baked into geometry vertex positions.
    output.geometry.scale(input.modelScaleFactor,input.modelScaleFactor,input.modelScaleFactor);
    output.geometry.rotateX(input.modelRotationX * (Math.PI/180));
    output.geometry.rotateY(input.modelRotationY * (Math.PI/180));
    output.geometry.rotateZ(input.modelRotationZ * (Math.PI/180));
    output.geometry.computeBoundingBox();

    output.geometry.translate(input.initialModelOffsetX,input.initialModelOffsetY,input.initialModelOffsetZ);
    output.geometry.computeBoundingBox();

    // Create water volume with a size large enough to accommodate any hull movements.
    output.waterGeometry = new THREE.BoxGeometry(input.waterSizeX,input.waterSizeY,input.waterSizeZ);
    output.waterGeometry.translate(0,-input.waterSizeY/2,0);
    var waterCsg = THREE.CSG.toCSG(output.waterGeometry);

    // Perform waterline calculation loop
    output.fullVolume = calculateVolume(output.geometry);
    console.log("Calculated volume: "+output.fullVolume);
    var currentGeometry = output.geometry.clone();

    // Check if the weight exceeds full volume mass, indicating the model will sink.
    output.willSink = ((output.fullVolume * input.simulationFluidDensity) < input.modelWeight);

    output.position = {};
    output.position.x = 0;
    output.position.y = 0;
    output.position.z = 0;
    var logString = "";

    output.iteration = 0;

    if (output.willSink){
        sendErrorPacket("Model simulation terminated: model will not float. <br>The weight of the model exceeds the weight of the total water displaced.<br>Weight of Model: "+(input.modelWeight)+" lbs<br>Weight of Displaced Water: "+(output.fullVolume * input.simulationFluidDensity)+" lbs");
        return;
    }

    var floatDelta = 1000;
    var progressMetric = 0;
    var simulationSpeed = 1;
    var debounce = false;
    while (Math.abs(floatDelta) > 0.001){
        //if (checkCancel()){ return; }
        output.iteration++;

        var iterationSummary = {};
        iterationSummary.iteration = output.iteration;

        // Convert geometry to CSG model.
        var geometryCsg = THREE.CSG.toCSG(currentGeometry);

        // Get volume of below-water portion of object.
        var belowWaterCsg = geometryCsg.intersect(waterCsg);
        var underwaterVolume = calculateVolume(THREE.CSG.fromCSG(belowWaterCsg));
        var fluidDisplacementMass = underwaterVolume * input.simulationFluidDensity;
        iterationSummary.underwaterVolume = underwaterVolume;
        iterationSummary.fluidDisplacementMass = fluidDisplacementMass;

        // Move the volume based on floatation delta.
        // If the model is heavier than the water it displaces, this will be negative
        floatDelta = (fluidDisplacementMass - input.modelWeight);
        if (floatDelta > 0){
            output.position.y += simulationSpeed;
            // Decrease our simulation speed so the float delta becomes closer to accurate.
            if (debounce){
                debounce = false;
                simulationSpeed *= 0.1;
            }
        } else if (floatDelta < 0){
            output.position.y -= simulationSpeed;
            debounce = true;
        }
        iterationSummary.floatDelta = floatDelta;
        iterationSummary.simulationSpeed = simulationSpeed;
        iterationSummary.x = output.position.x;
        iterationSummary.y = output.position.y;
        iterationSummary.z = output.position.z;
        console.log(iterationSummary);

        currentGeometry = output.geometry.clone();
        currentGeometry.translate(output.position.x,output.position.y,output.position.z);

        //if (checkCancel()){ return; }
        sendProgressPacket(floatDelta,output.position);
    }

    sendSuccessPacket(output);
}

function sendProgressPacket(floatDelta,position){
    ipcRenderer.send('simulation-update-progress', {
        progress: floatDelta,
        position: position
    });
}

function sendSuccessPacket(payload){
    ipcRenderer.send('simulation-success', {
        result: payload
    });
    isProcessing = false;
}

function sendErrorPacket(failureMessage){
    ipcRenderer.send('simulation-failure', {
        message: failureMessage
    });
    isProcessing = false;
}
