var stlLoader = new THREE.STLLoader();
const { ipcRenderer } = require('electron');

logger.log('[Background Thread] Background thread initialized.');


var isProcessing = false;
var lastInput = null;
var isCancelled = false;

$(function(){
    ipcRenderer.on('simulation-start', (event, payload) => {
        if (!isProcessing){
            isProcessing = true;
            logger.log('[Background Thread] Starting simulation...');
            try {
                lastInput = payload;
                stlLoader.load(payload.modelPath, function (geometry) {

                    console.log(geometry);

                    // Convert BufferedGeometry to raw Geometry object suitable for simulation operations.
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
});

function simulate(input){
    var output = {};

    // Apply the scaling and rotation options to the geometry.
    // This is a one-time operation; the results are baked into geometry vertex positions.
    input.geometry.applyMatrix(generateTransformMatrix(0,0,0,
        input.modelRotationX,
        input.modelRotationY,
        input.modelRotationZ,
        input.modelScaleFactor,
        input.modelScaleFactor,
        input.modelScaleFactor))
    input.geometry.computeBoundingBox();

    input.geometry.translate(input.initialModelOffsetX,input.initialModelOffsetY,input.initialModelOffsetZ);
    input.geometry.computeBoundingBox();

    // Perform waterline calculation loop
    var initialVolumeCOG = calculateVolumeCenterOfMass(input.geometry,false,false);

    output.fullVolume = initialVolumeCOG.volume;
    console.log("Calculated volume: "+output.fullVolume);
    var currentGeometry = input.geometry.clone();

    // Check if the weight exceeds full volume mass, indicating the model will sink.
    output.willSink = ((output.fullVolume * input.simulationFluidDensity) < input.modelWeight);

    output.position = {};
    output.position.x = 0;
    output.position.y = 0;
    output.position.z = 0;
    output.rotation = {};
    output.rotation.x = 0;
    output.rotation.y = 0;
    output.rotation.z = 0;
    var logString = "";

    output.iteration = 0;

    if (output.willSink){
        sendErrorPacket("Model simulation terminated: model will not float. <br>The weight of the model exceeds the weight of the total water displaced.<br>Weight of Model: "+(input.modelWeight)+" lbs<br>Weight of Displaced Water: "+(output.fullVolume * input.simulationFluidDensity)+" lbs");
        return;
    }

    if (input.modelWeight == 0){
        sendErrorPacket("Model simulation terminated: model has no weight! <br>Please set a weight to continue.");
        return;
    }

    console.log("STARTING SIMULATION");
    var floatDelta = 1000;
    var progressMetric = 0;
    var simulationSpeed = 1;
    var debounce = false;
    var oldPosition = {x:0, y:0, z:0};
    var simulationDone = false;
    var progress = 0;
    setTransformation(0,0,0,0,0,0);
    while (!simulationDone){
        //if (checkCancel()){ return; }
        output.iteration++;
        //console.log("STARTING ITERATION "+output.iteration);
        if (output.iteration > 2){
            //break;
        }

        var iterationSummary = {};
        iterationSummary.iteration = output.iteration;

        // Get volume and COG of below-water portion of object.
        var underwaterVolumeCOG = calculateVolumeCenterOfMass(currentGeometry,true,false);
        var underwaterVolume = underwaterVolumeCOG.volume;
        var fluidDisplacementMass = underwaterVolume * input.simulationFluidDensity;
        iterationSummary.underwaterVolume = underwaterVolume;
        iterationSummary.fluidDisplacementMass = fluidDisplacementMass;

        // Add position data to output
        iterationSummary.x = output.position.x;
        iterationSummary.y = output.position.y;
        iterationSummary.z = output.position.z;
        oldPosition.x = output.position.x;
        oldPosition.y = output.position.y;
        oldPosition.z = output.position.z;

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
        //console.log(iterationSummary);

        //if (checkCancel()){ return; }

        // Check simulation state
        if (Math.abs(floatDelta) < 0.001){
            simulationSpeed = 0.1;
            debounce = false;
            // Save displaced volume for current pos/rot
            // We are stable in our current simulation. Move on!
            if (input.simulationStabilityAxis == 0){
                output.rotation.x++;
                if (output.rotation.x > 180){
                    simulationDone = true;
                }
                progress = (output.rotation.x / 180) * 100;
            } else {
                output.rotation.z++;
                if (output.rotation.z > 180){
                    simulationDone = true;
                }
                progress = (output.rotation.z / 180) * 100;
            }
            sendProgressPacket(round(progress,0),output.position,output.rotation);
        }

        // Set the transformation matrix for next iteration
        setTransformation(output.position.x,output.position.y,output.position.z,output.rotation.x,output.rotation.y,output.rotation.z);

    }

    sendSuccessPacket(output);
}

function sendProgressPacket(progress,position,rotation){
    ipcRenderer.send('simulation-update-progress', {
        progress: progress,
        position: position,
        rotation: rotation
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
