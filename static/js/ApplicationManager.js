//////////////////////////////////////////////////////////
// ApplicationManager.js
// Handles main application state and functionality.
//////////////////////////////////////////////////////////

var stlLoader = new THREE.STLLoader();
const { ipcRenderer } = require('electron');
var lastInput = null;

class ApplicationManager {
    constructor(){
        // Display Options
        this.displayWireframe = false;
        this.displayDebug = false;
        this.displayWater = true;

        // Simulation Options
        this.modelFile = null;
        this.modelPath = null;
        this.modelUnits = 0;
        this.modelScaleFactor = 0;
        this.modelRotationX = 0;
        this.modelRotationY = 0;
        this.modelRotationZ = 0;
        this.modelCogX = 0;
        this.modelCogY = 0;
        this.modelCogZ = 0;
        this.modelWeight = 0;
        this.simulationStabilityAxis = 0;
        this.simulationWaterlineAxis = 0;
        this.simulationFluidDensity = 0;

        // State variables
        this.hasResults = false;
        this.isRunning = false;
        this.results = null;
    }

    // Pulls settings state from the UI
    pullSettings(){
        this.modelFile = $("#model-filename").prop("files")[0];
        if (this.modelFile){
            this.modelPath = this.modelFile.path;
        } else {
            this.modelPath = null;
        }
        this.modelUnits = $("#model-units option:selected").text();
        this.modelScaleFactor = $("#model-units").val();
        this.modelRotationX = $("#model-rot-x").val();
        this.modelRotationY = $("#model-rot-y").val();
        this.modelRotationZ = $("#model-rot-z").val();
        this.modelCogX = $("#model-cog-x").val();
        this.modelCogY = $("#model-cog-y").val();
        this.modelCogZ = $("#model-cog-z").val();
        this.modelWeight = $("#model-weight").val();
        if ($("#model-stability-axis").val() == "x"){
            // X
            this.simulationStabilityAxis = 0;
            this.simulationWaterlineAxis = 1;
        } else {
            // Z
            this.simulationStabilityAxis = 1;
            this.simulationWaterlineAxis = 0;
        }
        this.simulationFluidDensity = $("#fluid-density").val();

        if (this.modelFile == null){
            $("#status-message-file").text("Status: No File Loaded")
        } else {
            $("#status-message-file").text("Status: File = "+this.modelFile.path);
        }
    }

    // Pushes settings state to the UI
    pushSettings(){

    }

    handleUpdateSuccess(payload){
        this.results = payload.result;
        console.log(payload);
        model.position.set(this.results.position.x,this.results.position.y,this.results.position.z);
        model.rotation.set(this.results.rotation.x * (Math.PI/180),this.results.rotation.y * (Math.PI/180),this.results.rotation.z * (Math.PI/180));
        setUpdateButtonReadyState();
        this.hasResults = true;
        updateResults();
        this.isRunning = false;
    }

    handleUpdateError(payload){
        showModal("Simulation Error",payload.message);
        setUpdateButtonReadyState();
        this.isRunning = false;
    }

    handleUpdateProgress(payload){
        //logger.log(payload);
        setUpdateButtonProgressState(payload.progress);
        model.position.set(payload.position.x,payload.position.y,payload.position.z);
        model.rotation.set(payload.rotation.x * (Math.PI/180),payload.rotation.y * (Math.PI/180),payload.rotation.z * (Math.PI/180));
    }

    startUpdate(doSimulate){
        if (!this.isRunning){
            this.isRunning = true;


            // Assemble input parameters for the background thread.
            var simulationParameterPacket = {};

            Application.pullSettings();

            // Update display state
            updateDisplayState();

            simulationParameterPacket.modelPath = this.modelPath;
            simulationParameterPacket.modelUnits = this.modelUnits;
            simulationParameterPacket.modelScaleFactor = this.modelScaleFactor;
            simulationParameterPacket.modelRotationX = this.modelRotationX;
            simulationParameterPacket.modelRotationY = this.modelRotationY;
            simulationParameterPacket.modelRotationZ = this.modelRotationZ;
            simulationParameterPacket.modelCogX = this.modelCogX;
            simulationParameterPacket.modelCogY = this.modelCogY;
            simulationParameterPacket.modelCogZ = this.modelCogZ;
            simulationParameterPacket.modelWeight = this.modelWeight;
            simulationParameterPacket.simulationStabilityAxis = this.simulationStabilityAxis;
            simulationParameterPacket.simulationWaterlineAxis = this.simulationWaterlineAxis;
            simulationParameterPacket.simulationFluidDensity = this.simulationFluidDensity;

            if (simulationParameterPacket.modelPath != null){
                // Load the STL geometry before passing to the simulation thread.
                // This way, we can use the geometry for interim visualization.
                stlLoader.load(simulationParameterPacket.modelPath, function (geometry) {
                    // Apply the scaling and rotation options to the geometry.
                    // This is a one-time operation; the results are baked into geometry vertex positions.
                    geometry.applyMatrix(generateTransformMatrix(0,0,0,
                        simulationParameterPacket.modelRotationX,
                        simulationParameterPacket.modelRotationY,
                        simulationParameterPacket.modelRotationZ,
                        simulationParameterPacket.modelScaleFactor,
                        simulationParameterPacket.modelScaleFactor,
                        simulationParameterPacket.modelScaleFactor))
                    geometry.computeBoundingBox();

                    // Calculate model sizing data
                    // Calculate sizes of the object.
                    Application.sizeX = geometry.boundingBox.max.x - geometry.boundingBox.min.x;
                    Application.sizeY = geometry.boundingBox.max.y - geometry.boundingBox.min.y;
                    Application.sizeZ = geometry.boundingBox.max.z - geometry.boundingBox.min.z;
                    Application.maxSize = Math.max(Application.sizeX,Math.max(Application.sizeY,Application.sizeZ));

                    // Position the model so that it is centered and completely out of the water volume.
                    // This should position the lowest vertex at y = 0.
                    Application.initialModelOffsetX = -(geometry.boundingBox.max.x + geometry.boundingBox.min.x) / 2;
                    Application.initialModelOffsetY = -geometry.boundingBox.min.y;
                    Application.initialModelOffsetZ = -(geometry.boundingBox.max.z + geometry.boundingBox.min.z) / 2;
                    geometry.translate(Application.initialModelOffsetX,Application.initialModelOffsetY,Application.initialModelOffsetZ);
                    geometry.computeBoundingBox();

                    // Calculate water volume size
                    Application.waterSizeX = Application.sizeX*20;
                    Application.waterSizeY = Application.sizeY*20;
                    Application.waterSizeZ = Application.sizeZ*20;

                    simulationParameterPacket.sizeX = Application.sizeX;
                    simulationParameterPacket.sizeY = Application.sizeY;
                    simulationParameterPacket.sizeZ = Application.sizeZ;
                    simulationParameterPacket.maxSize = Application.maxSize;
                    simulationParameterPacket.initialModelOffsetX = Application.initialModelOffsetX;
                    simulationParameterPacket.initialModelOffsetY = Application.initialModelOffsetY;
                    simulationParameterPacket.initialModelOffsetZ = Application.initialModelOffsetZ;
                    simulationParameterPacket.waterSizeX = Application.waterSizeX;
                    simulationParameterPacket.waterSizeY = Application.waterSizeY;
                    simulationParameterPacket.waterSizeZ = Application.waterSizeZ;

                    setModel(geometry);
                    setModelPosition({x:0, y:0, z:0});

                    if (doSimulate){
                        // Send signal and data packet to start the simulation.
                        logger.log(simulationParameterPacket);
                        ipcRenderer.send('simulation-start', simulationParameterPacket);
                        setUpdateButtonProgressState(0);
                    }

                }, function(){}, function(error){
                    if (doSimulate){
                        Application.handleUpdateError({message:"Error when loading STL file: "+error});
                    }
                });
            } else {
                if (doSimulate){
                    Application.handleUpdateError({message:"No model file specified!"});
                }
            }

            if (!doSimulate){
                Application.isRunning = false;
            }

            lastInput = simulationParameterPacket;
        }
    }

    cancelUpdate(){
        /*
        logger.log("[Main Thread] Cancelling simulation by user...");
        ipcRenderer.send('simulation-cancel', {});
        if (this.running){
            this.isRunning = false;
            // Wait to set button to ready state.
            setTimeout(function(){
                setUpdateButtonReadyState();
            },2000);
        }
        */
    }
}

// Create an instance of the application state
var Application = new ApplicationManager();
logger.log("[Main Thread] Application state object initialized.");

$(function(){
    // Register IPC callbacks for the response commands
    ipcRenderer.on('simulation-update-progress', (event, payload) => {
        Application.handleUpdateProgress(payload);
    });
    ipcRenderer.on('simulation-success', (event, payload) => {
        Application.handleUpdateSuccess(payload);

    });
    ipcRenderer.on('simulation-failure', (event, payload) => {
        Application.handleUpdateError(payload);
    });
});


//////////////////////////////////////////////////////////
// End of File
//////////////////////////////////////////////////////////
